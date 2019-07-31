# Long press volume to skip tracks
This repo provides a systemless and standalone application which lets you skip tracks on your Android Oreo (8.0+) device by long pressing the volume keys while the screen is off. It doesn't require root but a permission has to be granted to the app via adb. For more information see the details below.

### Installation
Download the newst version of the apk [here](https://github.com/Cilenco/skipTrackLongPressVolume/releases) and install it as usual on your device. After that connect your device to a computer, enable developer settings and fire up following command:

    adb shell pm grant com.cilenco.skiptrack android.permission.SET_VOLUME_KEY_LONG_PRESS_LISTENER

On startup the app will check if has the permission. If you don't see any message the process worked and you are ready to go. If the app is installed as system application by flashing it through a recovery this step is not needed as the permission is granted automatically.

### Additions
The original by Cilenco worked quite well but has a few issues. I've corrected them and added a couple other fixes:

1. A vibration has been added when the long press is activated, as to provide feedback to the user that the long press was received and the user can let go of the button.
2. The service in general has been improved - it now has better compatibility with more media players.
3. A warning for Huawei EMUI users has been added to notify them to turn off battery saving mode for the service, or else it won't work properly.

There's also a small to-do list that I have going in regards to this project before I submit a PR to merge these changes:

1. Finish fully implementing the Huawei check - it only checks if the device is Huawei, and not if it's running official software or a ROM (This should be a simple addition though).
2. Look at other OEMs (Sony, LG, Samsung, etc) for power saving checks that interfere with this app and implement them alongside the Huawei check.
3. Add a startup receiver to start the service automatically when the device is turned on once fully configured.
4. Fix a bug where the service doesn't seem to start automatically when first setting it up - the user must first completely close the app and then re-open it before the service starts running.

### For developers
Please feel free to send me pull requests for improvements, stability and new feature ideas. Because the application uses the hidden API you have to use a modified version of the [android.jar](https://github.com/anggrayudi/android-hidden-api) file for compiling. For this download the required android.jar file from the linked repo and replace it with original one in `<SDK location>/platforms/android-<API version>/`.

If you change the `targetSdk` or `minimumSdk` make sure to use the proper file.

If Gradle asks you to update to a newer version, DO NOT UPDATE IT. Newer versions of Gradle appear to have android.jar verification measures that prevent the hidden API JAR files from working.

### Testing
You can test this app on an emulator by just giving it the permission as described above. After that you can send long press events of the volume keys to the emulator with following lines:

    adb shell input keyevent --longpress KEYCODE_VOLUME_UP
    adb shell input keyevent --longpress KEYCODE_VOLUME_DOWN

### ToDo
Currently the application is using a `NotificationListenerService` to deal with the new [background limitations](https://developer.android.com/about/versions/oreo/background.html) introduced in Android Oreo. This requires the user to manually enable it in the system settings but has the advantage that it doesn't display an ongoing notification. The problem with this solution is that the application is always running in the background and consumes (very less but measurable) system resources.

If anyone has a better solution for this (i.e. detect a new `MediaSession` and start the service in response) please open an issue for this or send me a pull request. Remember that this should act as a system feature so a notification (e.g. in ForegroundServices) should be avoided.
