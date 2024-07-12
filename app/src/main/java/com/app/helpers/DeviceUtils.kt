package com.app.helpers

import android.content.Context
import android.provider.Settings

object DeviceUtils {
    fun getDeviceID(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }
}