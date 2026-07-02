import XCTest

final class LifeLinkedIOSUITests: XCTestCase {
    private var app: XCUIApplication!

    override func setUpWithError() throws {
        continueAfterFailure = false

        app = XCUIApplication()
        app.launchArguments = ["--lifelinked-e2e"]
        app.launch()
    }

    func testLifeCounterCanIncrementPlayerLife() {
        waitForElement(labeled: "Open life counter menu")
        waitForElement(labeled: "P1 life total 40")

        element(labeled: "P1 increase life").tap()

        waitForElement(labeled: "P1 life total 41")
    }

    func testGifSearchCanSelectPlayerBackground() {
        waitForElement(labeled: "P1 settings")
        element(labeled: "P1 settings").tap()

        waitForElement(labeled: "Customize P1")
        element(labeled: "Customize P1").tap()

        waitForElement(labeled: "Open GIF search")
        element(labeled: "Open GIF search").tap()

        waitForElement(labeled: "Search KLIPY input")
        element(labeled: "Search KLIPY input").tap()
        element(labeled: "Search KLIPY input").typeText("cat")
        element(labeled: "Search").tap()

        waitForElement(labeled: "GIF result e2e-gif")
        element(labeled: "GIF result e2e-gif").tap()

        waitForElement(labeled: "Notification Selected Gif Successfully")
        element(labeled: "Close dialog").tap()
        waitForElement(labeled: "P1 background image https://example.test/e2e-full.gif")
    }

    private func waitForElement(
        labeled value: String,
        timeout: TimeInterval = 10,
        file: StaticString = #filePath,
        line: UInt = #line
    ) {
        let element = element(labeled: value)
        XCTAssertTrue(
            element.waitForExistence(timeout: timeout),
            "Expected to find element labeled '\(value)'",
            file: file,
            line: line
        )
    }

    private func element(labeled value: String) -> XCUIElement {
        app.descendants(matching: .any)
            .matching(NSPredicate(format: "identifier == %@ OR label == %@", value, value))
            .firstMatch
    }
}
