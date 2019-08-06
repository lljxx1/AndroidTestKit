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

class MainActivity: FlutterActivity() {

    private val CHANNEL = "samples.flutter.dev/startApp"

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        GeneratedPluginRegistrant.registerWith(this);
        MethodChannel(getFlutterView(), CHANNEL)
                    .setMethodCallHandler(object : MethodCallHandler {
                    override fun onMethodCall(call: MethodCall, result: Result) {
                        if (call.method.equals("launchPackage")) {
                            var app = call.argument<String>("appName") as String;
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
