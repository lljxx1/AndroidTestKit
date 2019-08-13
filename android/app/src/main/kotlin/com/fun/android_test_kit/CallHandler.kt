package com.`fun`.android_test_kit

import android.R
import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.util.Xml
import android.view.accessibility.AccessibilityNodeInfo
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import org.json.JSONArray
import org.json.JSONObject
import org.liquidplayer.service.MicroService
import java.io.IOException
import java.io.StringWriter
import java.util.*
import kotlin.collections.ArrayList


class CallHandler(val acs: AccessibilityService) {

    val atm = GlobalActionAutomator(Handler());

    fun handleNativeCall(service: MicroService, event:String, payload: JSONObject): Any {

//        if(event == "getSource"){
//            return getSource();
//        }
//
//

        when(event) {

            in "getSource" -> {
                return getSource();
            }

            in "doActionToElement" -> {
                Log.d("stdout", "doActionToElement "+payload.toString())
                var elementId = payload.optString("elementId");
                var action = payload.optString("action");

                var actionData = JSONObject();
                if(payload.has("data")){
                    actionData = payload.optJSONObject("data");
                }

                Log.d("stdout", "doActionToElement before");

                return doActionToElement(elementId, action, actionData);
            }

            in "findElement" -> {
                var strategy = payload.optString("strategy");
                var selector = payload.optString("selector");
                return findElement(strategy, selector);
            }


            in "launchPackage" -> {
                var appName = payload.optString("appName");
                return launchApp(appName);
            }

            in "getAppList" -> {
                return getAppList();
            }

            in "doGlobalAction" -> {
                var action = payload.optString("action");
                return doGlobalAction(action);
            }

            in "click" -> {

            }
        }


        return JSONObject();
    }


    fun doGlobalAction(action: String): Boolean {

        if(action.equals("home")){
            return atm.home();
        }
        if(action.equals("back")){
            return (atm.back());
        }
        return false;
    }

    fun findElement(strategy: String, selector: String): String {

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            var nodes: List<AccessibilityNodeInfo> = ArrayList();
            if(strategy.contains("text")){
                nodes = acs.rootInActiveWindow.findAccessibilityNodeInfosByText(selector);
            }else{
                nodes = acs.rootInActiveWindow.findAccessibilityNodeInfosByViewId(selector);
            }

            Log.d("MainActivity", "selector="+selector+" strategy="+strategy+" nodes="+nodes.size);

            val data = JSONArray();
            val stringWriter = StringWriter()

            try {
                val serializer = Xml.newSerializer()
                serializer.setOutput(stringWriter)
                serializer.startDocument("UTF-8", true)
                serializer.startTag("", "hierarchy")
                serializer.attribute("", "rotation", Integer.toString(R.attr.rotation))
                nodes.forEach {
                    AccessibilityNodeInfoDumper.dumpNodeRec(it, serializer, 0, 1280, 920, true);
                    val id = UUID.randomUUID().toString();
                    MainActivity.knowElements.put(it.hashCode().toString(), it);
                    val jsonEL = MainActivity.accessibilityNodeToJson(it);
                    jsonEL.put("elementId", id);
                    data.put(jsonEL);
                }
                serializer.endTag("", "hierarchy")
                serializer.endDocument()
            } catch (e: IOException) {
            }

            var re = JSONObject();
            re.put("data", data);
            re.put("xml", stringWriter.toString());
            return re.toString();
        }

        return "";
    }

    fun doActionToElement(elementId: String, action: String, actionData: JSONObject): Boolean {

        Log.d("stdout", "handle doActionToElement ");

        val element = MainActivity.knowElements.get(elementId);

        Log.d("stdout", elementId);
        Log.d("stdout", action);

        if(element == null){
            Log.d("MainActivity", "element not found");
            return false;
        }

        Log.d("stdout", element.hashCode().toString());
        Log.d("stdout", MainActivity.accessibilityNodeToJson(element).toString());

        if(action.equals("click")){
            if(element.isClickable){
                element.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }else{
                return false;
            }
        }


        if(action.equals("long-click")){
            if(element.isLongClickable){
                element.performAction(AccessibilityNodeInfo.ACTION_LONG_CLICK);
            }else{
                return false;
            }
        }

        if(action.equals("scroll-backward")){
            if(element.isScrollable){
                element.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);
            }else{
                return false;
            }
        }

        if(action.equals("scroll-forward")){
            if(element.isScrollable){
                element.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
            }else{
                return false;
            }
        }


        if(action.equals("setText")){
            var text = actionData.optString("text");
            val arguments = Bundle()
            arguments.putCharSequence(AccessibilityNodeInfo
                    .ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text);
            element.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
        }


        return true;
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
        var xmlView = AccessibilityNodeInfoDumper.dumpWindowXmlString(MyAccessibilityService.instance?.rootInActiveWindow, 0, 1024, 720);
        return xmlView.toString();
    }


    fun launchPackage(packageName: String): Boolean {
        try {
            val packageManager = acs.getPackageManager()
            acs.startActivity(packageManager.getLaunchIntentForPackage(packageName)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            return true
        } catch (e: Exception) {
            return false
        }
    }

    fun getAppList(): JSONArray {
        val apps = JSONArray();
        val packageManager = acs.getPackageManager()
        val installedApplications = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        for (applicationInfo in installedApplications) {
            val appData = JSONObject();
            appData.put("package", applicationInfo.packageName)
            appData.put("appName", packageManager.getApplicationLabel(applicationInfo).toString())
            apps.put(appData);
        }
        Log.d("MainActivity", "getAppList app="+apps.length());

        return apps;
    }

    fun getPackageName(appName: String): String? {
        val packageManager = acs.getPackageManager()
        val installedApplications = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        for (applicationInfo in installedApplications) {
            if (packageManager.getApplicationLabel(applicationInfo).toString().equals(appName)) {
                return applicationInfo.packageName
            }
        }
        return null
    }

    fun launchApp(appName: String): Boolean {
        val pkg = getPackageName(appName) ?: return false
        return launchPackage(pkg)
    }


    companion object {

        val methods = arrayListOf<String>("doActionToElement", "click", "findElement", "home", "launchPackage", "getSource", "getAppList", "doGlobalAction");
    }

}