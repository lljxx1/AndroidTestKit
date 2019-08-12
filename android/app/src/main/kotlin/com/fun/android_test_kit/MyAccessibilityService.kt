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
import android.os.Handler
import android.os.Looper
import android.os.Message
import java.net.URI

class MyAccessibilityService: AccessibilityService() {



    override fun onAccessibilityEvent(event: AccessibilityEvent?) {

        val ch = MainActivity.channel;
        if(event != null){
            val eventJSON = JSONObject();

            eventJSON.put("eventString", event.toString());
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
                        microService?.process?.exit(0)
                    }

                    var acs = MyAccessibilityService.instance;

                    if(acs != null){

                        Log.d("MyAccessibilityService", "startScript uri="+uri);
                        microService = MicroService(instance, URI.create(uri), object : MicroService.ServiceStartListener {
                            override fun onStart(service: MicroService) {

                                if (Looper.myLooper() == null){
                                    Looper.prepare();
                                }

                                val methods = CallHandler.methods;
                                val callHandler = CallHandler(acs);
                                methods.forEach {
                                    service?.addEventListener(it, object: MicroService.EventListener{
                                        override fun onEvent(service:MicroService , event:String , payload:JSONObject ){

                                            if (Looper.myLooper() == null){
                                                Looper.prepare();
                                            }

                                            try{
                                                val callResult = callHandler.handleNativeCall(service, event, payload);
                                                var result = JSONObject();
                                                result.put("event", payload);
                                                result.put("result", callResult);
                                                service.emit("actionResponse", result);
                                            }catch (e:Exception){}
                                        }
                                    })

                                }


                            }
                        });

                        microService?.start();
                    }
                }
            };


            mHandle.post(scriptThread);


        }
    }

}