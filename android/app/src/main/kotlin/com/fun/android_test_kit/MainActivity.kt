package com.`fun`.android_test_kit

import android.os.Bundle

import io.flutter.app.FlutterActivity
import io.flutter.plugins.GeneratedPluginRegistrant
import android.R.attr.start

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build
import android.os.Handler
import android.util.Log
import android.util.Xml
import android.view.accessibility.AccessibilityNodeInfo
import org.json.JSONArray
import org.json.JSONObject
import android.util.Xml.newSerializer
import android.R.attr.rotation
import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.util.DisplayMetrics
import org.xmlpull.v1.XmlSerializer
import java.io.IOException
import java.io.StringWriter;
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class MainActivity: FlutterActivity() {

    private val CHANNEL = "samples.flutter.dev/startApp";

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        GeneratedPluginRegistrant.registerWith(this);
        val atm = GlobalActionAutomator(Handler(), null);

        AppRuntime.context = applicationContext;


        val ch = MethodChannel(getFlutterView(), CHANNEL);
        ch.setMethodCallHandler(object : MethodCallHandler {
                    override fun onMethodCall(call: MethodCall, result: Result) {

                        if (call.method.equals("startService")) {
                            var url = call.argument<String>("url") as String;
                            MyAccessibilityService.startScript(url);
                            return result.success(true);
                        }


                        if (call.method.equals("checkAccessibilityIsEnabled")) {
                            val appContext = AppRuntime.context;
                            if(appContext != null){
                                return result.success(AccessibilityServiceUtils.isAccessibilityServiceEnabled(appContext, MyAccessibilityService::class.java));
                            }
                            return result.success(false);
                        }


                        if (call.method.equals("goAccessibilitySetting")) {
                            val appContext = AppRuntime.context;
                            if(appContext != null){
                                AccessibilityServiceUtils.goToAccessibilitySetting(appContext);
                                return result.success(true);
                            }
                            return result.success(false);
                        }



                        var acs = MyAccessibilityService.instance;
                        if(acs != null) atm.setService(acs);
                        if(acs != null){

                            if (call.method.equals("click")) {
                                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    var x = call.argument<String>("x") as Int;
                                    var y = call.argument<String>("y") as Int;
                                    return result.success(atm.click(x, y));
                                }
                            }

                            if (call.method.equals("home")) {
                                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    return result.success(atm.home());
                                }
                            }


                            if (call.method.equals("doGlobalAction")) {
                                var action = call.argument<String>("action") as String;
                                if(action.equals("home")){
                                    return result.success(atm.home());
                                }
                                if(action.equals("back")){
                                    return result.success(atm.back());
                                }
                            }


                            if (call.method.equals("findElement")) {
                                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    var strategy = call.argument<String>("strategy") as String;
                                    var selector = call.argument<String>("selector") as String;
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
                                        serializer.attribute("", "rotation", Integer.toString(rotation))
                                        nodes.forEach {
                                            AccessibilityNodeInfoDumper.dumpNodeRec(it, serializer, 0, 1280, 920, true);
                                            val id = UUID.randomUUID().toString();
                                            knowElements.put(it.hashCode().toString(), it);
                                            val jsonEL = accessibilityNodeToJson(it);
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
                                    return result.success(re.toString());
                                }
                            }


                            if (call.method.equals("doActionToElement")) {
                                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

                                    var elementId = call.argument<String>("elementId") as String;
                                    var action = call.argument<String>("action") as String;

                                    val element = knowElements.get(elementId);

                                    Log.d("MainActivity", elementId);
                                    Log.d("MainActivity", action);

                                    if(element == null){
                                        Log.d("MainActivity", "element not found");
                                        return result.success(false);
                                    }

                                    Log.d("MainActivity", element.hashCode().toString());
                                    Log.d("MainActivity", accessibilityNodeToJson(element).toString());

                                    if(action.equals("click")){
                                        if(element.isClickable){
                                            element.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                        }else{
                                            return result.success(false);
                                        }
                                    }


                                    if(action.equals("long-click")){
                                        if(element.isLongClickable){
                                            element.performAction(AccessibilityNodeInfo.ACTION_LONG_CLICK);
                                        }else{
                                            return result.success(false);
                                        }
                                    }


                                    if(action.equals("scroll-backward")){
                                        if(element.isScrollable){
                                            element.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);
                                        }else{
                                            return result.success(false);
                                        }
                                    }

                                    if(action.equals("scroll-forward")){
                                        if(element.isScrollable){
                                            element.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
                                        }else{
                                            return result.success(false);
                                        }
                                    }


                                    return result.success(true);
                                }
                            }


                            if (call.method.equals("getSource")) {
                                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    var xmlView = AccessibilityNodeInfoDumper.dumpWindowXmlString(acs.rootInActiveWindow, 0, 1024, 720);
                                    Log.d("MainActivity", xmlView);
                                    return result.success(xmlView);
                                }
                            }

                        }

                        if (call.method.equals("getAppList")) {
                            Log.d("MainActivity", "getAppList called");
                            return result.success(getAppList().toString())
                        }

                        val runService = MyAccessibilityService.microService;
                        if(runService != null){
                            if(call.method.equals("startRecord")){
                                runService.emit(call.method);
                                return result.success(true);
                            }

                            if(call.method.equals("stopRecord")){
                                runService.emit(call.method);
                                return result.success(true);
                            }
                        }

                        if (call.method.equals("launchPackage")) {
                            var app = call.argument<String>("appName") as String;
                            if(acs != null){
                                atm.setService(acs);
                                Log.d("MainActivity", acs.rootInActiveWindow.childCount.toString());
                            }
                            result.success(launchApp(app))
                        } else {
                            result.notImplemented()
                        }






                    }
                });




        channel = ch;
    }



    fun launchPackage(packageName: String): Boolean {
        try {
            val packageManager = this.getPackageManager()
            this.startActivity(packageManager.getLaunchIntentForPackage(packageName)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            return true
        } catch (e: Exception) {
            return false
        }
    }

    fun getAppList(): JSONArray {
        val apps = JSONArray();
        val packageManager = this.getPackageManager()
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
        val packageManager = this.getPackageManager()
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

        val knowElements:WeakHashMap<String, AccessibilityNodeInfo> = WeakHashMap();
        var channel:MethodChannel ?= null;
//        val knowElements:HashMap<String, AndroidElement> = HashMap();

        fun accessibilityNodeToJson(it: AccessibilityNodeInfo): JSONObject {

            val element = JSONObject();
            element.put("childCount", it.childCount);
            element.put("packageName", it.packageName);
            element.put("class", it.className);
            element.put("text", it.className);
            element.put("desc", it.contentDescription);

            element.put("clickable", it.isClickable);
            element.put("long-clickable", it.isLongClickable);
            element.put("password", it.isPassword);
            element.put("scrollable", it.isScrollable);
            element.put("displayed", it.isVisibleToUser);

            element.put("focusable", it.isFocusable);
            element.put("focused", it.isFocused);

            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                element.put("resource-id", it.viewIdResourceName);
            }


            return element;
        }
    }
}
