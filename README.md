# tvOS 27-style Android TV Launcher

Open this folder in Android Studio (Koala or newer), let Gradle sync, then Build > Build APK(s).
Install on the box, press Home and choose "tvOS Launcher" as the default.

* Left click  = open app (hover moves the focus highlight)
* Right click = back (returns to the first icon)
* "Antenna TV" button = opens the box's tuner / TV app.
  If not found it asks you to choose the TV app; long-press the button any time to change it.
  Find your box's TV app package:  adb shell pm list packages | grep -i -E "tv|source"

## Get the APK without installing anything (GitHub)
1. Create a free GitHub account, make a new repository, and upload ALL files of this folder
   (including the hidden .github folder).
2. Open the "Actions" tab > "Build APK" > wait about 5 minutes.
3. Open the finished run, download "tvOS-launcher-apk" -> contains app-debug.apk.
4. Copy app-debug.apk to a USB stick and install it on the TV box.
