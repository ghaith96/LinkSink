# Plan: Grouped Topic Sections with Persistent State and Inline Sync Shortcut

**Created**: 2026-03-28  
**Branch**: master  
**Status**: approved

## Goal

Replace the flat link list on the main screen with collapsible sections grouped by topic (plus an "Uncategorized" section). Each section's expanded/collapsed state persists across sessions via DataStore. A tappable sync-mode badge on each section header lets the user cycle a topic's `HookMode` directly from the main screen — without navigating to Settings → Manage Topics.

---

## Acceptance Criteria

- [ ] Main list renders one collapsible section per topic that has links, plus "Uncategorized" when applicable. Topics with zero links are hidden.
- [ ] Section headers display the topic name and link count.
- [ ] Tapping a section header toggles expanded/collapsed; an animated chevron reflects the state.
- [ ] All sections default to expanded on first install.
- [ ] Collapsed/expanded state survives process death (stored in DataStore via `SettingsStore`).
- [ ] Deleting a topic removes its persisted section state key.
- [ ] When a search query is active OR a topic filter chip is active, the flat results list is shown (no section headers).
- [ ] Each named-topic section header shows a tappable sync-mode badge ("Local Only", "Global", "Custom").
- [ ] Tapping the badge opens a `DropdownMenu`; selecting `LOCAL_ONLY` or `USE_GLOBAL` saves immediately with no navigation.
- [ ] Selecting `CUSTOM` from the dropdown opens `EditTopicSheet` pre-filled for that topic.
- [ ] The "Uncategorized" section has no sync-mode badge.
- [ ] Existing swipe-to-delete, sync badge, manual sync button, topic-filter dropdown, and date filter are unaffected.

---

## Steps

### Step 1: Persist section expand/collapse state in `SettingsStore`

**Complexity**: standard  
**RED**: Write unit test for new `SettingsStore` methods — `getSectionStates()` returns empty map by default; `setSectionExpanded("1", false)` persists `{"1": false}`; `removeSectionState("1")` removes the key.  
**GREEN**: Add `TOPIC_SECTION_STATES` key (`stringPreferencesKey`) to `SettingsStore`. Serialize/deserialize `Map<String, Boolean>` with `kotlinx.serialization` JSON (already on classpath). Add `sectionStates: Flow<Map<String, Boolean>>`, `setSectionExpanded(key, expanded)`, and `removeSectionState(key)`.  
**REFACTOR**: Extract JSON serialization to a private helper; add a `companion object` constant for the `"uncategorized"` key.  
**Files**: `data/SettingsStore.kt`, `data/SettingsStoreTest.kt`  
**Commit**: `feat(data): persist topic section expand/collapse state in SettingsStore`

---

### Step 2: Add `TopicSection` model and grouping logic to `LinkListViewModel`

**Complexity**: standard  
**RED**: Write ViewModel unit test — given topics `[Work, Personal]` and links split between them plus two uncategorized, `uiState.Success.topicSections` contains three `TopicSection` entries with correct topic references and link lists; uncategorized is last; topics with zero links are excluded; when `searchQuery` is non-blank, `topicSections` is empty and flat `links` list is used.  
**GREEN**: Add `data class TopicSection(val topic: Topic?, val links: List<Link>)` to the `viewmodel` package. Extend `LinkListUiState.Success` with `topicSections: List<TopicSection>` (default empty for filter/search modes). In `LinkListViewModel`, wire `SettingsStore` (add constructor param), load `sectionStates` on init. Add grouping logic in the `collect` block: when no active search/filter, group the link list via `groupBy { it.topicId }` and map to `TopicSection`. Expose `fun toggleSection(key: String)` and `fun setSectionExpanded(key: String, expanded: Boolean)` that write through `SettingsStore`. Update `LinkSinkApp` DI to pass `settingsStore` to `LinkListViewModel`; update `MainActivity` accordingly.  
**REFACTOR**: Move grouping logic to a pure function `groupLinksByTopic(links, topics): List<TopicSection>` so it's independently testable.  
**Files**: `viewmodel/LinkListViewModel.kt`, `viewmodel/TopicSection.kt`, `LinkSinkApp.kt`, `MainActivity.kt`, `viewmodel/LinkListViewModelTest.kt`  
**Commit**: `feat(viewmodel): group links by topic into TopicSection, expose section toggle`

---

### Step 3: Extract `EditTopicSheet` to a shared component

**Complexity**: trivial  
**RED**: (Compile-time only — confirm `TopicManagementScreen` still compiles after extraction and `LinkListScreen` can import the composable.)  
**GREEN**: Move `EditTopicSheet` (and its `private` helpers it references: `TopicForm`, webhook test UI) out of `TopicManagementScreen.kt` into a new file `ui/components/EditTopicSheet.kt`, changing visibility to `internal`. Update the import in `TopicManagementScreen.kt`.  
**REFACTOR**: None needed.  
**Files**: `ui/components/EditTopicSheet.kt`, `ui/TopicManagementScreen.kt`  
**Commit**: `refactor(ui): extract EditTopicSheet to shared component`

---

### Step 4: Add `TopicViewModel` to `MainActivity` and wire inline hook-mode update

**Complexity**: standard  
**RED**: Write test for `LinkListViewModel.updateTopicHookMode(topic, HookMode.LOCAL_ONLY)` — verifies `TopicRepository.updateTopic` is called with the updated topic and no exception is emitted.  
**GREEN**: Add `fun updateTopicHookMode(topic: Topic, mode: HookMode)` to `LinkListViewModel` that calls `topicRepository.updateTopic(topic.copy(hookMode = mode, customWebhookUrl = if (mode == HookMode.CUSTOM) topic.customWebhookUrl else null))`. Add a `_editTopicRequest: MutableSharedFlow<Topic>` / `SharedFlow<Topic>` for the CUSTOM path so `LinkListScreen` can open `EditTopicSheet`. Instantiate `TopicViewModel` in `MainActivity` alongside `LinkListViewModel`; pass it to `LinkListScreen` as a new parameter.  
**REFACTOR**: Keep the `_editTopicRequest` flow as a `SharedFlow` with replay=0 to avoid stale events on recompose.  
**Files**: `viewmodel/LinkListViewModel.kt`, `MainActivity.kt`, `viewmodel/LinkListViewModelTest.kt`  
**Commit**: `feat(viewmodel): add inline hook-mode update and edit-topic request event`

---

### Step 5: Build `TopicSectionHeader` composable with sync-mode badge

**Complexity**: standard  
**RED**: Write a Compose UI test (or screenshot/semantics test) — `TopicSectionHeader` with `hookMode = HookMode.USE_GLOBAL` shows text "Global"; tapping the badge shows a dropdown with "Local Only", "Use Global Webhook", "Custom Webhook"; selecting "Local Only" calls the `onHookModeChange` callback; "Uncategorized" header (topic = null) has no badge node in semantics.  
**GREEN**: Create `ui/components/TopicSectionHeader.kt`. The composable takes `topic: Topic?`, `linkCount: Int`, `expanded: Boolean`, `onToggle: () -> Unit`, `onHookModeChange: (HookMode) -> Unit`, `onEditCustom: () -> Unit`. Render: animated `Icon(Icons.Default.KeyboardArrowDown)` with `animateFloatAsState` rotation, topic name `Text`, count `Text`, and (if `topic != null`) a `SyncModeBadge` chip that opens a `DropdownMenu` on click. Badge label: `LOCAL_ONLY → "Local Only"`, `USE_GLOBAL → "Global"`, `CUSTOM → "Custom"`.  
**REFACTOR**: Extract `SyncModeBadge` as its own `private` composable within the same file for clarity.  
**Files**: `ui/components/TopicSectionHeader.kt`, `ui/components/TopicSectionHeaderTest.kt`  
**Commit**: `feat(ui): add TopicSectionHeader composable with animated chevron and sync-mode badge`

---

### Step 6: Update `LinkListScreen` to render sectioned layout

**Complexity**: complex  
**RED**: Write integration/UI test — with two topics and links, the `LinkListScreen` success state shows section headers; collapsing a section hides its cards; entering a search query shows flat results without headers; clearing search restores sections.  
**GREEN**: In `LinkListScreen`, replace the `LazyColumn` `items` block in `Success` state: when `topicSections` is non-empty (i.e., no active search/filter), iterate `topicSections` with `stickyHeader { TopicSectionHeader(...) }` + `if (expanded) items(section.links) { SwipeableLinkCard(...) }`. Read `sectionStates` from the viewmodel; derive the key (`topic.id.toString()` or `"uncategorized"`). Connect `onToggle` → `viewModel.toggleSection(key)`. Connect `onHookModeChange` → `viewModel.updateTopicHookMode(topic, mode)` for non-Custom modes. Connect `onEditCustom` → collect `editTopicRequest` SharedFlow and open `EditTopicSheet` (from the extracted component), passing the `topicViewModel`.  
**REFACTOR**: Extract the section-list rendering into a private `TopicSectionedList` composable to keep `LinkListScreen` readable.  
**Files**: `ui/LinkListScreen.kt`, `ui/LinkListScreenTest.kt`  
**Commit**: `feat(ui): replace flat list with collapsible topic sections on main screen`

---

### Step 7: Clean up persisted state on topic deletion

**Complexity**: standard  
**RED**: Test that calling `topicRepository.deleteTopic(id, ...)` followed by the section-state cleanup removes the key `id.toString()` from `SettingsStore`.  
**GREEN**: In `TopicViewModel.confirmDeleteTopic`, after `topicRepository.deleteTopic(...)` succeeds, call `settingsStore.removeSectionState(confirmation.topicId.toString())`. Inject `settingsStore` into `TopicViewModel`; update `LinkSinkApp` DI and `SettingsActivity` to pass it.  
**REFACTOR**: None needed.  
**Files**: `viewmodel/TopicViewModel.kt`, `LinkSinkApp.kt`, `SettingsActivity.kt`, `viewmodel/TopicViewModelTest.kt`  
**Commit**: `feat(viewmodel): clean up persisted section state when a topic is deleted`

---

## Complexity Classification

| Rating | Criteria | Review depth |
|--------|----------|--------------|
| `trivial` | Single-file rename, config change, typo fix, documentation-only | Skip inline review; covered by final `/code-review --changed` |
| `standard` | New function, test, module, or behavioral change within existing patterns | Spec-compliance + relevant quality agents |
| `complex` | Architectural change, security-sensitive, cross-cutting concern, new abstraction | Full agent suite including opus-tier agents |

---

## Pre-PR Quality Gate

- [ ] All unit tests pass (`./gradlew test`)
- [ ] All instrumentation/UI tests pass (`./gradlew connectedAndroidTest`)
- [ ] App compiles with no warnings promoted to errors
- [ ] Linter passes (`./gradlew lint`)
- [ ] `/code-review --changed` passes
- [ ] Manual smoke test: fresh install → all sections expanded; collapse one → kill app → reopen → still collapsed
- [ ] Manual smoke test: sync-mode badge cycles `LOCAL_ONLY` → `USE_GLOBAL` → opens `EditTopicSheet` for `CUSTOM`
- [ ] Manual smoke test: search hides section headers; clearing restores them

---

## Risks & Open Questions

| Risk / Question | Mitigation / Owner |
|---|---|
| **Sticky headers vs. scrolling headers** — sticky keeps the topic name visible during long sections but can look noisy with many small sections. | Default to sticky (`stickyHeader {}`); can be toggled off by changing to `item {}` in Step 6. Decide before Step 6 starts. |
| **Section order** — alphabetical by name or Room insertion order? | Recommend alphabetical (`sortedBy { it.name }`). Confirm before Step 2. |
| **`kotlinx.serialization` for DataStore JSON** — the lib is on the classpath for Ktor, but verify `@Serializable` annotation processing works in the `data` module scope. | Check `app/build.gradle.kts` KSP config before Step 1. If not wired up, use `org.json.JSONObject` instead (available on Android with no extra dep). |
| **`TopicViewModel` in `MainActivity`** — creating it there means it's scoped to `MainActivity`, not shared with `SettingsActivity`. That is fine because they are separate activities. | No action needed unless process death of `SettingsActivity` needs to coordinate (it doesn't). |
| **`EditTopicSheet` extraction (Step 3)** — `TopicForm` references `WebhookTestResult` from `viewmodel`. Moving the sheet to `ui/components/` crosses a layer boundary that is already present elsewhere. | Acceptable given existing pattern. Confirm before Step 3. |
| **`getFiltered` SQL bug for Uncategorized (`-1L` sentinel)** — the current DAO uses `topic_id = :topicId` when topicId is non-null, so passing `-1L` does not match `NULL` rows. This is a pre-existing bug. | Note it; do not fix in this feature branch to avoid scope creep. File a separate issue. |
