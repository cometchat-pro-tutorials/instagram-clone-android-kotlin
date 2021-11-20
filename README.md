# How to Build an Instagram Clone Android App with Kotlin & Firebase

Read the full tutorial here: [**>> How to Build an Instagram Clone Android App with Kotlin & Firebase**](https://www.cometchat.com/tutorials/#)

## Technology

This demo uses:

- Android
- CometChat Android UI Kit
- Firebase
- Glide
- RecyclerView

## Running the demo

To run the demo, you need to have Android Studio installed on your computer and follow these steps:

1. [Head to CometChat Pro and create an account](https://app.cometchat.com/signup)
2. From the [dashboard](https://app.cometchat.com/apps), add a new app called **"instagram-clone-android-kotlin"**
3. Select this newly added app from the list.
4. From the Quick Start copy the **APP_ID, APP_REGION and AUTH_KEY**. These will be used later.
5. Also copy the **REST_API_KEY** from the API & Auth Key tab.
6. Navigate to the Users tab, and delete all the default users and groups leaving it clean **(very important)**.
7. Download the repository [here](https://github.com/hieptl/instagram-clone-android-kotlin/archive/main.zip) or by running `git clone https://github.com/hieptl/instagram-clone-android-kotlin.git` and open it in a code editor.
8. [Head to Firebase and create a new project](https://console.firebase.google.com)
9. Create a file called Constants.kt in the package folder of your project.
10. Import and inject your secret keys in the Constants.kt file containing your CometChat and Firebase in this manner.

```js
interface Constants {
    companion object {
        const val COMETCHAT_APP_ID = "xxx-xxx-xxx-xxx-xxx-xxx-xxx-xxx"
        const val COMETCHAT_REGION = "xxx-xxx-xxx-xxx-xxx-xxx-xxx-xxx"
        const val COMETCHAT_AUTH_KEY = "xxx-xxx-xxx-xxx-xxx-xxx-xxx-xxx"
        const val COMETCHAT_API_KEY = "xxx-xxx-xxx-xxx-xxx-xxx-xxx-xxx"
        const val FIREBASE_REALTIME_DATABASE_URL = "xxx-xxx-xxx-xxx-xxx-xxx-xxx-xxx"
        const val FIREBASE_EMAIL_KEY = "email" // this is not a secret value, it is just a constant variable that will be accessed from different places of the application.
        const val FIREBASE_USERS = "users" // this is not a secret value, it is just a constant variable that will be accessed from different places of the application.
        const val FIREBASE_POSTS = "posts" // this is not a secret value, it is just a constant variable that will be accessed from different places of the application.
        const val FIREBASE_NOTIFICATIONS = "notifications" // this is not a secret value, it is just a constant variable that will be accessed from different places of the application.
        const val FIREBASE_ID_KEY = "id" // this is not a secret value, it is just a constant variable that will be accessed from different places of the application.
    }
}
```

Questions about running the demo? [Open an issue](https://github.com/hieptl/instagram-clone-android-kotlin/issues). We're here to help ✌️

## Useful links

- 🏠 [CometChat Homepage](https://app.cometchat.com/signup)
- 🚀 [Create your free account](https://app.cometchat.com/apps)
- 📚 [Documentation](https://www.cometchat.com/docs/home/welcome)
- 👾 [GitHub](https://www.github.com/cometchat-pro)
- 🔥 [Firebase](https://console.firebase.google.com)
- 🔷 [Android](https://developer.android.com)