package com.`fun`.android_test_kit


import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.view.ViewConfiguration;
import android.R.attr.scaleY
import android.R.attr.scaleX
import android.R.attr.scaleY
import android.R.attr.scaleX

class GlobalActionAutomator(private val mHandler: Handler?, private var mService: AccessibilityService?) {

    fun setService(service: AccessibilityService) {
        mService = service
    }

    fun back(): Boolean {
        return performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
    }

    fun home(): Boolean {
        return performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME)
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    fun powerDialog(): Boolean {
        return performGlobalAction(AccessibilityService.GLOBAL_ACTION_POWER_DIALOG)
    }

    private fun performGlobalAction(globalAction: Int): Boolean {
        return if (mService == null) false else mService!!.performGlobalAction(globalAction)
    }

    fun notifications(): Boolean {
        return performGlobalAction(AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS)
    }

    fun quickSettings(): Boolean {
        return performGlobalAction(AccessibilityService.GLOBAL_ACTION_QUICK_SETTINGS)
    }

    fun recents(): Boolean {
        return performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS)
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    fun splitScreen(): Boolean {
        return performGlobalAction(AccessibilityService.GLOBAL_ACTION_TOGGLE_SPLIT_SCREEN)
    }



    @RequiresApi(api = Build.VERSION_CODES.N)
    fun click(x: Int, y: Int): Boolean {
        return press(x, y, ViewConfiguration.getTapTimeout() + 50)
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    fun press(x: Int, y: Int, delay: Int): Boolean {
        val path = Path()
        path.lineTo(x.toFloat(), y.toFloat());
        val description = GestureDescription.StrokeDescription(path, 0, delay.toLong());
        mService!!.dispatchGesture(GestureDescription.Builder().addStroke(description).build(), null, null);
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    fun longClick(x: Int, y: Int): Boolean {
//        return gesture(0, (ViewConfiguration.getLongPressTimeout() + 200).toLong(), *intArrayOf(x, y))
        return true;
    }

//    private fun scaleX(x: Int): Int {
//        return if (mScreenMetrics == null) x else mScreenMetrics!!.scaleX(x)
//    }
//
//    private fun scaleY(y: Int): Int {
//        return if (mScreenMetrics == null) y else mScreenMetrics!!.scaleY(y)
//    }

//    @RequiresApi(api = Build.VERSION_CODES.N)
//    fun swipe(x1: Int, y1: Int, x2: Int, y2: Int, delay: Int): Boolean {
//        return gesture(0, delay, intArrayOf(x1, y1), intArrayOf(x2, y2))
//    }

}
