package com.pmggroup.quickqrcodescannergenerator

import android.annotation.SuppressLint
import android.app.Activity
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat

class Utilities {

    companion object {
        @SuppressLint("NewApi")
        fun statusBarColor(id: Int, activity: Activity) {
            activity.window.decorView.systemUiVisibility = 0
            activity.window.statusBarColor = ContextCompat.getColor(activity, id)
        }

        @SuppressLint("NewApi")
        fun statusBarColor(id: Int, boolean: Boolean, activity: Activity) {
            if (boolean) {
                activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                activity.window.statusBarColor = ContextCompat.getColor(activity, id)
            }
        }

        fun hideSoftKeyboard(activity: Activity) {
            val imm: InputMethodManager =
                activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            var view = activity.currentFocus
            if (view == null) {
                view = View(activity)
            }
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

}

