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

class MainActivity: FlutterActivity() {

    private val CHANNEL = "samples.flutter.dev/startApp"

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        GeneratedPluginRegistrant.registerWith(this);

        val atm = GlobalActionAutomator(Handler());



        MethodChannel(getFlutterView(), CHANNEL)
                    .setMethodCallHandler(object : MethodCallHandler {
                    override fun onMethodCall(call: MethodCall, result: Result) {

                        if (call.method.equals("click")) {
                            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                var x = call.argument<String>("x") as Int;
                                var y = call.argument<String>("y") as Int;
                                result.success(atm.click(x, y));
                            }
                        }

                        if (call.method.equals("home")) {
                            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                result.success(atm.home());
                            }
                        }

                        if (call.method.equals("launchPackage")) {
                            var app = call.argument<String>("appName") as String;
                            var acs = MyAccessibilityService.instance;
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
}
