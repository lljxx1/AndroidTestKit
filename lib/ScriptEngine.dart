import 'dart:async';

import 'package:flutter/services.dart';
import 'package:uuid/uuid.dart';
import 'package:flutter_liquidcore/liquidcore.dart';
import 'Bus.dart';

class ScriptEngine {

  static final MethodChannel _methodChannel = const MethodChannel('samples.flutter.dev/startApp');
  MicroService _microService;

  ScriptEngine(String uri){
    print(uri);
    _microService = new MicroService(uri);
    proxy();
    _methodChannel.setMethodCallHandler((MethodCall call) {
      print(call.method);
      if(call.method.contains("onMicroServiceStatus")){
        print("emit log");
        Bus.log.fire({
          'log': call.arguments
        });
//        _microService.emit(call.method, call.arguments);

      }else{
        _microService.emit(call.method, call.arguments);
      }

    });
  }

  proxy(){
    List<String> methods = ['doActionToElement', 'click', 'findElement', 'home', 'launchPackage', 'getSource', 'getAppList'];
    methods.forEach((action) => {
        _microService.addEventListener(action,  (service, event, eventPayload) async {
          print("on "+action);
          var res = await _methodChannel.invokeMethod(action, eventPayload);
          var result = {};
          var respName = action+"Response";
          result['event'] = eventPayload;
          result['result'] = res;
          _microService.emit('actionResponse', result);
        })
    });
  }

  start() {
    return _microService.start();
  }

  stop() {
    return _microService.exitProcess(0);
  }

  MicroService get microService => _microService;
}