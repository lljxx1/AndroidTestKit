import 'dart:math';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_liquidcore/liquidcore.dart';
import 'DriverBridge.dart';
import 'ScriptEngine.dart';

void main() {
  //enableLiquidCoreLogging = true;
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  Random _rng;
  MicroService _microService;
  JSContext _jsContext;

  String _jsContextResponse = '<empty>';
  String _microServiceResponse = '<empty>';
  int _microServiceWorld = 0;
  ScriptEngine _engine;

  static const platform = const MethodChannel('samples.flutter.dev/startApp');

  @override
  void initState() {
    super.initState();
  }

  testLanuch() async {
    try {
      final bool result = await platform.invokeMethod('launchPackage', {
        "appName": "微信"
      });
      print(result);
      print("lanuch=");
    } on PlatformException catch (e) {
      print(e);
    }
  }

  testGoHome() async {
    try {
      final bool result = await platform.invokeMethod('click', {
        "x": 1,
        "y": 1
      });
      print(result);
      print("testGoHome=");
    } on PlatformException catch (e) {
      print(e);
    }
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('FlutterLiquidcore App'),
        ),
        body: Column(
            mainAxisAlignment: MainAxisAlignment.spaceEvenly,
            children: <Widget>[
              RaisedButton(
                child: const Text('MicroService'),
                onPressed: initMicroService,
              ),
              Center(
                child: Text('MicroService response: $_microServiceResponse\n'),
              ),
              RaisedButton(
                child: const Text('Test App'),
                onPressed: testLanuch,
              ),
              RaisedButton(
                child: const Text('Test Click'),
                onPressed: testGoHome,
              ),
              RaisedButton(
                child: const Text('getSource'),
                onPressed: () async {
                  print('findByText');
                  var driver = Driver();
                  var source = await driver.getSource();
                  print(source);
                },
              ),
              RaisedButton(
                child: const Text('findByText Click'),
                onPressed: () async {
                  print('findByText');
                  var driver = Driver();
                  var res = await driver.findByText('App');
                  print(res);
                },
              ),
              Center(
                child: Text('JSContext response: $_jsContextResponse\n'),
              )
            ]),
      ),
    );
  }

  @override
  void dispose() {
    if (_microService != null) {
      // Exit and free up the resources.
      // _microService.exitProcess(0); // This API call might not always be available.
      _microService.emit('exit');
    }
    super.dispose();
  }


  // Platform messages are asynchronous, so we initialize in an async method.
  void initMicroService() async {
//    if (_microService == null) {
      String uri;

      // Android doesn't allow dashes in the res/raw directory.
      //uri = "android.resource://io.jojodev.flutter.liquidcoreexample/raw/liquidcore_sample";
//      uri = "@flutter_assets/Resources/liquidcore_sample.js";
      uri = "http://192.168.1.6:8080/dist/main.js";

      if(_engine != null){
        _engine.stop();
      }

      _engine = new ScriptEngine(uri);
//      await _microService.addEventListener('ready',
//              (service, event, eventPayload) {
//            // The service is ready.
//            if (!mounted) {
//              return;
//            }
//
//            print('ready '+uri);
//            //_emit();
//      });
//
//    var driver = Driver();
//
//
//
//      await _microService.addEventListener('sendClick', (service, event, eventPayload) {
//            // The service is ready.
//            if (!mounted) {
//              return;
//            }
//            print('sendClick '+uri);
//            print(eventPayload);
//            platform.invokeMethod('click', {
//              "x": eventPayload['x'],
//              "y": eventPayload['y']
//            });
//      });
//
//      await _microService.addEventListener('doActionToElement', (service, event, eventPayload) async {
//        var res = await driver.doActionToElement(eventPayload);
//        var result = {};
//        result['event'] = eventPayload;
//        result['result'] = res;
//        _microService.emit("doActionToElementResponse", res);
//      });
//
//      await _microService.addEventListener('findByText', (service, event, eventPayload) async {
//        var res = await driver.findByText(eventPayload['text']);
//        var result = {};
//        result['event'] = eventPayload;
//        result['result'] = res;
//        _microService.emit("findByTextResponse", result);
//      });



      // Start the service.
      await _engine.start();
//    }

  }



  void _setMicroServiceResponse(message) {
    if (!mounted) {
      print("microService: widget not mounted");
      return;
    }
    setState(() {
      _microServiceResponse = message;
    });
  }

  void _setJsContextResponse(value) {
    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) {
      print("jsContext: widget not mounted");
      return;
    }

    setState(() {
      _jsContextResponse = value;
    });
  }
}