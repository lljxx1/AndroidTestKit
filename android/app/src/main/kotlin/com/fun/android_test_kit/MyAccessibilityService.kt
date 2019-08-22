package com.`fun`.android_test_kit
import android.accessibilityservice.AccessibilityService;
import android.util.Log
import android.util.Xml
import android.view.accessibility.AccessibilityEvent
import org.json.JSONObject
import java.io.IOException
import java.io.StringWriter
import java.lang.IllegalStateException
import org.liquidplayer.service.MicroService

import android.R.attr.rotation
import android.content.ComponentName
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.LruCache
import org.liquidplayer.javascript.JSContext
import org.liquidplayer.javascript.JSFunction
import org.liquidplayer.node.Process
import java.net.URI



class MyAccessibilityService: AccessibilityService() {


    private fun tryGetActivity(componentName: ComponentName): ActivityInfo? {
        try {
            return packageManager.getActivityInfo(componentName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            return null
        }

    }


    override fun onAccessibilityEvent(event: AccessibilityEvent?) {

        instance = this;
        Log.d("MyAccessibilityService", "onAccessibilityEvent reived new");
        Log.d("MyAccessibilityService", event.toString());

        val ch = MainActivity.channel;
        if(event != null){
            val eventJSON = JSONObject();
            eventJSON.put("eventString", event.toString());

            if(event.packageName != null && event.className != null){
                val componentName = ComponentName(
                        event.packageName.toString(),
                        event.className.toString()
                )

                val activityInfo = tryGetActivity(componentName)
                val isActivity = activityInfo != null

                if (isActivity){
                    eventJSON.put("isActivity", componentName.flattenToShortString());
                    Log.i("CurrentActivity", componentName.flattenToShortString())
                }
            }


            if(event.source != null){
                try {
                    var xmlView = AccessibilityNodeInfoDumper.dumpWindowXmlString(event.source, 0, 1024, 720);
                    eventJSON.put("xml", xmlView.toString());
                }catch (e: IllegalStateException){

                }
            }

            microService?.emit("onAccessibilityEvent", eventJSON.toString());
            ch?.invokeMethod("onAccessibilityEvent", eventJSON.toString());
            Log.d("MyAccessibilityService", "onAccessibilityEvent channel ready try to brod");
        }

    }

    override fun onInterrupt() {


        Log.d("MyAccessibilityService", "onInterrupt");
    }

    companion object {
        var microService: MicroService? = null;
        var instance: AccessibilityService? = null;
        var scriptThread:Runnable ?= null;
        var mHandle = Handler()

        fun startScript(uri: String){

            if(scriptThread != null){
                mHandle.removeCallbacks(scriptThread);
            }

            scriptThread = object: Runnable {
                override fun run() {


                    if (Looper.myLooper() == null){
                        Looper.prepare();
                    }

                    if(microService != null){
                        microService?.emit("exit");
                        microService?.process?.exit(0);
                    }


                    var acs = instance;
                    if(acs != null){
                        Log.d("MyAccessibilityService", "startScript uri="+uri);
                        MainActivity.channel?.invokeMethod("onMicroServiceStatus", "startScript uri="+uri);
                        microService = MicroService(instance, URI.create(uri), object : MicroService.ServiceStartListener {
                            override fun onStart(service: MicroService) {

                                if (Looper.myLooper() == null){
                                    Looper.prepare();
                                }

                                MainActivity.channel?.invokeMethod("onMicroServiceStatus", "started");

                                val methods = CallHandler.methods;
                                val callHandler = CallHandler(acs);
                                methods.forEach {
                                    service?.addEventListener(it, object: MicroService.EventListener{
                                        override fun onEvent(service:MicroService , event:String , payload:JSONObject ){

                                            if (Looper.myLooper() == null){
                                                Looper.prepare();
                                            }

                                            // MainActivity.channel?.invokeMethod("onMicroServiceStatus", event+" called");

                                            try{
                                                val callResult = callHandler.handleNativeCall(service, event, payload);
                                                var result = JSONObject();
                                                result.put("event", payload);
                                                result.put("result", callResult);
                                                service.emit("actionResponse", result);
                                            }catch (e:Exception){
                                                MainActivity.channel?.invokeMethod("onMicroServiceStatus", e.message);
                                            }
                                        }
                                    })
                                }
                            }
                        }, object: MicroService.ServiceErrorListener {
                            override fun onError(service: MicroService?, e: java.lang.Exception?) {
                                MainActivity.channel?.invokeMethod("onMicroServiceStatus", "script error="+e?.message);
                            }
                        }, object: MicroService.ServiceExitListener {
                            override fun onExit(service: MicroService?, exitCode: Int?) {
                                MainActivity.channel?.invokeMethod("onMicroServiceStatus", "script exit code="+exitCode);
                            }
                        });

                        microService?.start();
                        microService?.process?.addEventListener(object: Process.EventListener {
                            override fun onProcessStart(process: Process?, ctx: JSContext?) {
                                Log.d("MyAccessibilityService", "hook stdout");
                                // intercept stdout and stderr
                                val stdout = ctx?.property("process")?.toObject()?.property("stdout")?.toObject()
                                stdout?.property("write", object : JSFunction(stdout.getContext(), "write") {
                                    fun write(string: String) {
                                        MainActivity.channel?.invokeMethod("onMicroServiceStatus", string);
                                        microService?.emit("onStdout", string);
                                    }
                                })

                                val stderr = ctx?.property("process")?.toObject()?.property("stderr")?.toObject()
                                stderr?.property("write", object : JSFunction(stderr.getContext(), "write") {
                                    fun write(string: String) {
                                        MainActivity.channel?.invokeMethod("onMicroServiceStatus", string);
                                        microService?.emit("onStderr", string);
                                    }
                                })
                            }

                            override fun onProcessAboutToExit(process: Process?, exitCode: Int) {
                                Log.d("MyAccessibilityService", "onProcessAboutToExit");
                            }

                            override fun onProcessExit(process: Process?, exitCode: Int) {
                                Log.d("MyAccessibilityService", "onProcessExit");
                            }

                            override fun onProcessFailed(process: Process?, error: java.lang.Exception?) {
                                Log.d("MyAccessibilityService", "onProcessFailed");

                            }
                        })
                    }else{
                        MainActivity.channel?.invokeMethod("onMicroServiceStatus", "AccessibilityService is not started");
                    }
                }
            };


            mHandle.post(scriptThread);

        }
    }

}