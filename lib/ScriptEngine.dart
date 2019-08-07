import 'dart:async';

import 'package:flutter/services.dart';
import 'package:uuid/uuid.dart';
import 'package:flutter_liquidcore/liquidcore.dart';


class ScriptEngine {

  static final MethodChannel _methodChannel = const MethodChannel('samples.flutter.dev/startApp');
  MicroService _microService;

  ScriptEngine(String uri){
    print(uri);
    _microService = new MicroService(uri);
    proxy();
  }

  proxy(){
    List<String> methods = ['doActionToElement', 'click', 'findElement', 'home', 'launchPackage', 'getSource'];
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
}