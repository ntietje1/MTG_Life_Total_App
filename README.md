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

## Technologies Used

- **Compose Multiplatform:** Leveraging the Jetpack Compose framework for efficient and performant UI creation that can be shared across platforms.
- **Ktor:** Integration with 3rd part APIs over http such as the Scryfall API.
- **Kotlinx Serialization:** Efficient serialization/deserialization of Json.
- **Decompose:** Cross-platform compatable app navigation.
- **Gradle Version Catalog:** Tracks versions across the multi-module app structure.

## Customization Options

Experience the flexibility of various theme options and individual player customization settings that can be saved and loaded across different game sessions.

## Future Development

- **Official Public Launch:** Currently in beta-testing phase on both Google Play and IOS App Store.
- **Continuous Improvement:** Regular updates and addition of new features to enhance user experience.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
