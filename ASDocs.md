# Android Project Setup and Configuration Guide

### This guide will help you:

1. Import a new project from version control.
2. Change the package name of the application.
3. Change the application name.
4. Build a signed bundle and create a new keystore.

### 1. Import New Project from Version Control

Open Android Studio: Launch Android Studio on your machine.
- Go to File > New > Project from Version Control.
- Select your Version Control System (e.g., Git, GitHub, Bitbucket, etc.).
- Enter Repository URL: Copy the repository URL and paste it into the URL field.
- Choose Directory: Select a local directory where you want the project to be cloned.
- Click "Clone": Android Studio will now clone the project from version control and set it up for you.

### 2. Changing the Package Name of the Application

Changing the package name involves refactoring the current package in Android Studio.

- Open the Project in Android Studio.
- Navigate to the Package: In the Project view, expand `app > java > com.yourcurrentpackagename`.
- Right-click the Package Name: Select `Refactor > Rename`.

#### Rename the Package in Parts:

- Android Studio may prompt you to rename the package in parts (e.g., `com, yourcurrentpackagename, etc.`). Rename each part as needed.
- Update Package in `build.gradle`:
- Open `app/build.gradle`.
- Update the `applicationId` to match the new package name.
- Sync Project: Go to `File > Sync Project with Gradle Files` to apply changes.
- Clean and Rebuild Project: Go to `Build > Clean Project` and then `Build > Rebuild Project` to ensure everything is updated.

### 3. Changing the App Name

- Open `res/values/strings.xml`: Find `strings.xml` in the `res > values` directory.
- Locate `app_name`:
- You should see a string resource with the name `app_name`.
- Update its value to the new app name you want.
```
   <string name="app_name">New App Name</string>
```
- Save Changes and Sync Project: The app name change will be applied the next time you build and run the app.

### 4. Building a Signed Bundle and Creating a New Keystore

To release your app, you need to build a signed APK or App Bundle and create a new keystore.

#### Creating a New Keystore
- Open Build Menu: Go to `Build > Generate Signed Bundle / APK...`.
- Select Bundle or APK: Choose either Android App Bundle or APK, then click `Next`.
- Select or Create a Keystore:
    - If you need a new keystore, click Create new....
- Fill Keystore Information:
    - Provide the keystore location, password, alias, key password, and validity (usually 25+ years).
    - Fill in other required information (like name, organization, etc.).
- Finish and Save Keystore: Save the keystore file in a secure location.

#### Building a Signed Bundle

- Complete Signing Information:
    - Once you've created or selected a keystore, complete the remaining fields with keystore alias and passwords.
- Select Build Variants: Choose the build variant, usually `release`.
- Build the Bundle: Click Finish to start building your signed APK or App Bundle.
- Locate the Output:
    - Once the process completes, your signed bundle or APK will be available in the `app/release` folder.

#### Additional Notes

- Always back up your keystore: Losing your keystore will make it impossible to update your app on the Play Store.
- Double-check package names and app name changes: Ensure that all instances are updated as desired to avoid potential issues.