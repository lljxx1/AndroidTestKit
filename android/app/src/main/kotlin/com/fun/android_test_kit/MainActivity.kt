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
import android.util.DisplayMetrics
import org.xmlpull.v1.XmlSerializer
import java.io.IOException
import java.io.StringWriter;
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class MainActivity: FlutterActivity() {

    private val CHANNEL = "samples.flutter.dev/startApp"




    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        GeneratedPluginRegistrant.registerWith(this);

        val atm = GlobalActionAutomator(Handler());



        MethodChannel(getFlutterView(), CHANNEL)
                    .setMethodCallHandler(object : MethodCallHandler {
                    override fun onMethodCall(call: MethodCall, result: Result) {

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
                                    nodes.forEach {
                                        val id = UUID.randomUUID().toString();
                                        knowElements.put(id, AndroidElement(id, it));
                                        val jsonEL = accessibilityNodeToJson(it);
                                        jsonEL.put("elementId", id);
                                        data.put(jsonEL);
                                    }

                                    return result.success(data.toString());
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

                                    Log.d("MainActivity", element.key);

                                    Log.d("MainActivity", accessibilityNodeToJson(element.node).toString());
                                    if(action.equals("click")){
                                        element.node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                    }

                                    if(action.equals("long-click")){
                                        element.node.performAction(AccessibilityNodeInfo.ACTION_LONG_CLICK);
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
                })

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

//        val knowElements:WeakHashMap<String, AccessibilityNodeInfo> = WeakHashMap();
        val knowElements:HashMap<String, AndroidElement> = HashMap();

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
