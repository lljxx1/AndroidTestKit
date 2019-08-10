package com.`fun`.android_test_kit
import android.accessibilityservice.AccessibilityService;
import android.util.Log
import android.util.Xml
import android.view.accessibility.AccessibilityEvent
import org.json.JSONObject
import java.io.IOException
import java.io.StringWriter

import android.R.attr.rotation
class MyAccessibilityService: AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {

        val ch = MainActivity.channel;
        if(ch != null && event != null){
            val eventJSON = JSONObject();
            var xmlView = AccessibilityNodeInfoDumper.dumpWindowXmlString(event.source, 0, 1024, 720);
            eventJSON.put("eventString", event.toString());
            eventJSON.put("xml", xmlView.toString());
            ch.invokeMethod("onAccessibilityEvent", eventJSON.toString());
            Log.d("MyAccessibilityService", "onAccessibilityEvent channel ready try to brod");
        }


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