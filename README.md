# **Remindly - Android Reminder Library**

---
A powerful and easy-to-use Android library for managing reminders with notifications, repeat intervals, and a beautiful Material Design UI.

---

## ✨ **Features**

- ✅ Self-contained UI - No need to write UI code
- ✅ Add, Edit, Delete reminders
- ✅ Repeat intervals - Minutes, Hours, Days, Weeks, Months
- ✅ Notification support with sound and vibration
- ✅ Persistent storage - Reminders survive app restarts
- ✅ Material Design - Beautiful dark theme interface
- ✅ Easy integration - Just add XML and request permissions



  ---

# **Preview**
---

<p align="center">
  <img src="https://github.com/user-attachments/assets/3403d99b-ea18-426c-bf77-6c860437fb2c"
       alt="Demo GIF"
       width="200">


</p>


## ⚡ **Installation**

**Step 1:** Add JitPack repository to your root build.gradle:

```gradle
maven { url = uri("https://jitpack.io") }
```

**Step 2:** Add the dependency in your app `build.gradle` (example if hosted on JitPack):  

```gradle
dependencies {
	      	        implementation 'com.github.Excelsior-Technologies-Community:Android_Remindly:1.0.0'


}
```
## ⚡ **Permissions**

```
<!-- Required permissions -->
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    <uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
    <uses-permission android:name="android.permission.USE_EXACT_ALARM" />
    <uses-permission android:name="android.permission.VIBRATE" />

<!-- Required: Register the Broadcast Receiver -->
        <receiver
            android:name="com.ext.android_remindly.ReminderReceiver"
            android:enabled="true"
            android:exported="false" />
        
        <!-- Required: Register the Notification Service -->
        <service
            android:name="com.ext.android_remindly.ReminderNotificationService"
            android:enabled="true"
            android:exported="false" />

```

## ⚡ **Usage**

1. Add in XML

```
<!-- Optional: Add a Toolbar -->
    <androidx.appcompat.widget.Toolbar
        android:id="@+id/toolbar"
        android:layout_width="match_parent"
        android:layout_height="?attr/actionBarSize"
        android:background="#455A64"
        android:elevation="4dp">

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Reminders"
            android:textColor="#FFFFFF"
            android:textSize="20sp"
            android:textStyle="bold" />
    </androidx.appcompat.widget.Toolbar>

    <!-- Required: Add the ReminderListView -->
    <com.ext.android_remindly.ReminderListView
        android:id="@+id/reminderListView"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />


```

## **2. Setup in Activity**
```
 package com.yourpackage

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    companion object {
        private const val NOTIFICATION_PERMISSION_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Request notification permission (Required for Android 13+)
        requestNotificationPermission()
        
        // That's it! The library handles everything else
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_CODE
                )
            }
        }
    }
}

```



## **📄 License**

**MIT License**  
```
Copyright (c) 2025 Excelsior Technologies

Permission is hereby granted, free of charge, to any person obtaining a copy  
of this software and associated documentation files (the "Software"), to deal  
in the Software without restriction, including without limitation the rights  
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell  
copies of the Software, and to permit persons to whom the Software is  
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all  
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED **"AS IS"**, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR  
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,  
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
```



  
