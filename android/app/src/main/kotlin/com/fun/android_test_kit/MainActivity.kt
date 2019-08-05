package com.`fun`.android_test_kit

import android.os.Bundle

import io.flutter.app.FlutterActivity
import io.flutter.plugins.GeneratedPluginRegistrant
import android.R.attr.start
import android.util.Log
import org.liquidplayer.service.MicroService
import java.net.URI


class MainActivity: FlutterActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    GeneratedPluginRegistrant.registerWith(this);
      Log.d("MicroService", "try start");
    val service = MicroService(
            this,
            URI("http://my.server.com/path/to/code.js"),
            object : MicroService.ServiceStartListener{
              override fun onStart(service: MicroService) {
                Log.d("MicroService", "started");
              }
            }
    )
    //service.start()
  }
}
