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
import android.os.Looper
import java.net.URI

class MyAccessibilityService: AccessibilityService() {



    override fun onAccessibilityEvent(event: AccessibilityEvent?) {

        val ch = MainActivity.channel;
        if(ch != null && event != null){
            val eventJSON = JSONObject();

            eventJSON.put("eventString", event.toString());
            if(event.source != null){

                try {
                    var xmlView = AccessibilityNodeInfoDumper.dumpWindowXmlString(event.source, 0, 1024, 720);
                    eventJSON.put("xml", xmlView.toString());

                }catch (e: IllegalStateException){

                }

            }

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
        var microService: MicroService? = null;
        var instance: AccessibilityService? = null;

        fun startScript(uri: String){

            Log.d("MyAccessibilityService", "startScript uri="+uri);
            microService = MicroService(instance, URI.create(uri), object : MicroService.ServiceStartListener {
                override fun onStart(service: MicroService) {
                    val methods = arrayListOf<String>("doActionToElement",
                            "click", "findElement", "home", "launchPackage", "getSource", "getAppList");

                    methods.forEach {
                        service?.addEventListener(it, object: MicroService.EventListener{
                            override fun onEvent(service:MicroService , event:String , payload:JSONObject ){
                                val callResult = CallHandler.handleNativeCall(service, event, payload);
                                var result = JSONObject();
                                result.put("event", payload);
                                result.put("result", callResult);
                                service.emit("actionResponse", result);
                            }
                        })

                    }


                }
            });

            microService?.start();
        }
    }

}