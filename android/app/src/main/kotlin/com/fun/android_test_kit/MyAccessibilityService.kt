package com.`fun`.android_test_kit
import android.accessibilityservice.AccessibilityService;
import android.util.Log
import android.view.accessibility.AccessibilityEvent


class MyAccessibilityService: AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        instance = this;
        Log.d("MyAccessibilityService", "onAccessibilityEvent reived new");
        Log.d("MyAccessibilityService", event.toString());

    }

    override fun onInterrupt() {
        Log.d("MyAccessibilityService", "onInterrupt");
    }

    companion object {
        var instance: AccessibilityService? = null;
    }

}