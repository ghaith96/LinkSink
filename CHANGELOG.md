# Changelog

All notable changes to LinkSink will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.1.0] - 2026-03-22

### Added
- **Topics** - Organize links into topics with custom names
- **Per-topic webhooks** - Each topic can use LOCAL_ONLY, global webhook, or a custom webhook URL
- **Fuzzy search** - Search links by URL or title with typo tolerance (e.g., "rect" finds "React")
- **Date filtering** - Filter links by Today, This Week, or This Month
- **Link metadata** - Automatically fetches title, description, and thumbnail from Open Graph/Twitter Card tags
- **Topic management screen** - Create, edit, and delete topics from Settings
- **Topic picker** - Quick topic selection in share sheet with recent topics
- **Filter chips** - Visual display of active filters with one-tap clearing
- **Topic display** - Links show their assigned topic in the list

### Changed
- Enhanced share sheet with topic selection
- Improved link card layout with topic chip
- Database migrated to v3 with topics table

### Technical
- Room database migration v1 → v2 → v3
- In-memory fuzzy search using kt-fuzzy library (better device compatibility than FTS)
- WebhookRouter pure function for webhook resolution
- MetadataFetcher with regex-based Open Graph parsing

## [1.0.0] - 2026-03-15

### Added
- Initial release
- Save links from any app via share menu
- Sync links to Discord using webhooks
- Local-first with offline support
- Material You dynamic theming
- Background sync with WorkManager
- Swipe to delete links
