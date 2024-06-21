![GitHub last commit (by committer)](https://img.shields.io/github/last-commit/ntietje1/MTG_Life_Total_App)
![Static Badge](https://img.shields.io/badge/License-Apache_2.0-orange)
![Static Badge](https://img.shields.io/badge/Kotlin-1.9.24-blue)
![Static Badge](https://img.shields.io/badge/Compose-1.7.0-blue)
![Static Badge](https://img.shields.io/badge/Ktor-2.3.8-blue)
![Static Badge](https://img.shields.io/badge/Koin-3.6.0-blue)
![Static Badge](https://img.shields.io/badge/MinSdk-24-yellow)
![Static Badge](https://img.shields.io/badge/TargetSdk-34-yellow)

[![Google Play](https://img.shields.io/badge/Google%20Play-414141?style=for-the-badge&logo=google-play&logoColor=white)](https://play.google.com/store/apps/details?id=com.hypeapps.lifelinked)
[![App Store](https://img.shields.io/badge/App%20Store-0D96F6?style=for-the-badge&logo=app-store&logoColor=white)](https://apps.apple.com/us/app/lifelinked-mtg-life-counter/id6503708612)


# LifeLinked: A MTG Life Tracking App

This Kotlin-based app is designed to simplify your Magic: The Gathering gaming experience by efficiently tracking life totals, commander damage, and offering a range of additional features for smoother gameplay.

## Features

- **Life Total Tracking:** Keep track of life totals for 1-6 players, including an alternate 4 player layout.
- **Commander Damage:** Easily update and monitor commander damage during gameplay.
- **Tracking:** Track over 20 types of counters per-player with an additional mana & storm counter option.
- **Personalization Settings:** Customize the app according to your preferences, with the ability to save and load per-player settings.
- **Custom Image Uploading:** Take your personalization even further by uploading an image to use as your player background.
- **Scryfall API Integration:** Browse the catalog of 20,000+ MTG cards to select a card art for your background, see oracle text, and reference rulings.
- **Planechase Support:** Create & save a planar deck with built-in features such as planeswalking and the planar die.
- **Crash protection:** App state is saved automatically to storage, allowing for recovery upon a crash or accidental close.
- **Utility Functions:** Includes coin flipping, dice rolling, first player selection, and tracking the monarchy.
  
## User Interface

The UI is crafted to be minimal yet intuitive, providing a clear picture of the gamestate from a glance without overwhelming elements.

<p align="middle">
  <img src="/./images/phone_screenshot_style.png" width="200" />
  <img src="/./images/phone_screenshot_track.png" width="200" /> 
  <img src="/./images/phone_screenshot_features.png" width="200" />
  <img src="/./images/phone_screenshot_search.png" width="200" />
</p>
<p align="middle">
  <img src="/./images/phone_screenshot_planechase.png" width="200" />
  <img src="/./images/phone_screenshot_commander.png" width="200" />
  <img src="/./images/phone_screenshot_coin.png" width="200" /> 
  <img src="/./images/phone_screenshot_storm.png" width="200" />
</p>

## Technologies Used

- **Compose Multiplatform:** Leveraging the Jetpack Compose framework for efficient and performant UI creation that can be shared across platforms.
- **Ktor:** Integration with 3rd part APIs over http such as the Scryfall API.
- **Koin:** Cross-platform compatable dependency injection
- **Coil:** Asynchronous image loading and caching
- **Kotlinx Serialization:** Efficient serialization/deserialization of Json.
- **Gradle** Tracks versions across the multi-module app structure.

## Customization Options

Experience the flexibility of various theme options and individual player customization settings that can be saved and loaded across different game sessions.

## Future Development

- **Official Public Launch:** LifeLinked is now available on both the [Google Play](https://play.google.com/store/apps/details?id=com.hypeapps.lifelinked) and [IOS App Store](https://apps.apple.com/us/app/lifelinked-mtg-life-counter/id6503708612).
- **Continuous Improvement:** Regular updates and addition of new features to enhance user experience.

## License

This project is licensed under the Apache 2.0 License - see the [LICENSE](LICENSE) file for details.
