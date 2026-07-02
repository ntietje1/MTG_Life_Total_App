//
//  LifeLinkedIOSApp.swift
//  LifeLinkedIOS
//
//  Created by Nicholas Tietje on 3/8/24.
//

import SwiftUI

@main
struct LifeLinkedIOSApp: App {
    init() {
        if ProcessInfo.processInfo.arguments.contains("--lifelinked-e2e") {
            LifeLinkedE2EState.seed()
        }
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}

private enum LifeLinkedE2EState {
    static func seed() {
        if let bundleIdentifier = Bundle.main.bundleIdentifier {
            UserDefaults.standard.removePersistentDomain(forName: bundleIdentifier)
        }

        let appVersion = Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? "0.0.0"
        UserDefaults.standard.set(appVersion, forKey: "lastSplashScreenShown")
        UserDefaults.standard.set(true, forKey: "tutorialSkip")
        UserDefaults.standard.set(true, forKey: "autoSkip")
        UserDefaults.standard.set(true, forKey: "gameStarted")
        UserDefaults.standard.set(true, forKey: "autoKo")
        UserDefaults.standard.set(false, forKey: "fastCoinFlip")
        UserDefaults.standard.set(false, forKey: "cameraRollDisabled")
        UserDefaults.standard.set(false, forKey: "keepScreenOn")
        UserDefaults.standard.set(false, forKey: "turnTimer")
        UserDefaults.standard.set(4, forKey: "numPlayers")
        UserDefaults.standard.set(false, forKey: "alt4PlayerLayout")
        UserDefaults.standard.set(true, forKey: "darkTheme")
        UserDefaults.standard.set(40, forKey: "startingLife")
        UserDefaults.standard.set(false, forKey: "devMode")
    }
}
