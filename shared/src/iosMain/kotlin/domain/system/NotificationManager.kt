package domain.system


actual class NotificationManager() {
    actual fun showNotification(message: String, duration: Long) {
//    val alertController = UIAlertController.alertControllerWithTitle(
//        title = "Notification",
//        message = message,
//        preferredStyle = UIAlertControllerStyle.Alert
//    )
//    alertController.addAction(
//        UIAlertAction.actionWithTitle(
//            title = "OK",
//            style = UIAlertActionStyle.Default,
//            handler = null
//        )
//    )
//    viewController.presentViewController(
//        viewControllerToPresent = alertController,
//        animated = true,
//        completion = null
//    )
//        val alertController = UIAlertController.alertControllerWithTitle(
//            title = "Notification",
//            message = message,
//            preferredStyle = UIAlertControllerStyle.Alert
//        )
//        alertController.addAction(
//            UIAlertAction.actionWithTitle(
//                title = "OK",
//                style = UIAlertActionStyle.Default,
//                handler = null
//            )
//        )
//        viewController.presentViewController(
//            viewControllerToPresent = alertController,
//            animated = true,
//            completion = null
//        )
//
//        // Dismiss the alert after the specified duration
//        val dispatchTime = dispatch_time(DISPATCH_TIME_NOW, duration * 1_000_000) // convert to nanoseconds
//        dispatch_after(dispatchTime, dispatch_get_main_queue()) {
//            alertController.dismissViewControllerAnimated(true, null)
//        }
    }
}

