package com.`fun`.android_test_kit

import android.content.Context
import android.os.Build
import android.os.Handler
import android.util.Log
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import org.json.JSONObject
import org.liquidplayer.service.MicroService


object CallHandler {

    val atm = GlobalActionAutomator(Handler());

    fun handleNativeCall(service: MicroService, event:String, payload: JSONObject): Any {

        if(event == "getSource"){
            return getSource();
        }

        return JSONObject();
    }

    fun checkAccessibilityIsEnabled(appContext: Context): Boolean {
        return AccessibilityServiceUtils.isAccessibilityServiceEnabled(appContext, MyAccessibilityService::class.java)
    }

    fun goAccessibilitySetting(appContext: Context){
        return AccessibilityServiceUtils.goToAccessibilitySetting(appContext);
    }

    fun click(x:Int, y:Int): Boolean{
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return atm.click(x, y);
        }
        return false;
    }

    fun home(): Boolean {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return atm.home();
        }
        return false;
    }

    fun getSource(): String {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            var xmlView = AccessibilityNodeInfoDumper.dumpWindowXmlString(MyAccessibilityService.instance?.rootInActiveWindow, 0, 1024, 720);
            return xmlView.toString();
        }
        return "";
    }
}