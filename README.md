# LinkSink

Save and sync links with Discord webhooks.

LinkSink is a simple, privacy-focused Android app for saving links from any app and automatically syncing them to Discord via webhooks. Built with Kotlin and Jetpack Compose.

## Features

- **Share from anywhere** - Use Android's share menu to save links from any app
- **Topics** - Organize links into topics with custom webhook settings
- **Discord sync** - Automatically post saved links to Discord channels via webhooks
- **Fuzzy search** - Find links quickly with typo-tolerant search across URLs and titles
- **Date filtering** - Filter links by Today, This Week, or This Month
- **Link metadata** - Auto-fetch title, description, and thumbnail from URLs
- **Local-first** - Your data stays on your device with offline support
- **Background sync** - Links sync automatically when you're back online
- **Material You** - Dynamic theming that adapts to your system colors
- **Swipe to delete** - Clean gesture-based link management

### Topic Webhook Modes

Each topic can have its own webhook configuration:
- **Local Only** - Keep links private, no Discord sync
- **Use Global** - Use your default webhook URL
- **Custom** - Set a unique webhook per topic (great for different Discord channels)

## Screenshots

<p align="center">
  <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/1.png" width="250" alt="Link list">
  <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/2.png" width="250" alt="Share sheet">
  <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/3.png" width="250" alt="Settings">
</p>

## Installation

### F-Droid

Coming soon.

### GitHub Releases

Download the latest APK from the [Releases](../../releases) page.

### Obtainium

[<img src="https://raw.githubusercontent.com/ImranR98/Obtainium/main/assets/graphics/badge_obtainium.png" alt="Get it on Obtainium" height="60">](http://apps.obtainium.imranr.dev/redirect.html?r=obtainium://add/https://github.com/ghaith96/LinkSink)

### Build from Source

```bash
# Clone the repository
git clone https://github.com/your-username/linksink.git
cd linksink

# Build debug APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug
```

**Requirements:**
- JDK 17
- Android SDK 36

## Setup

1. Install LinkSink on your Android device
2. Open the app and go to Settings
3. Create a Discord webhook in your desired channel:
   - Open Discord → Server Settings → Integrations → Webhooks
   - Create a new webhook and copy the URL
4. Paste the webhook URL in LinkSink settings
5. Test the connection
6. Start sharing links from any app!

## Architecture

LinkSink follows modern Android architecture patterns:

- **Kotlin** with Coroutines and Flow
- **Jetpack Compose** for declarative UI
- **Room** for local database
- **DataStore** for preferences
- **Ktor** for HTTP client
- **WorkManager** for background sync

## Requirements

- Android 10 (API 29) or higher
- Internet connection for Discord sync

## Contributing

Contributions are welcome! Please feel free to submit issues and pull requests.

## License

[MIT License](LICENSE)

---

**Perfect for:**
- Saving articles to read later
- Collecting research links
- Sharing discoveries with Discord communities
- Building a personal link archive
- Organizing links by project or interest
