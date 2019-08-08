import 'dart:math';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_liquidcore/liquidcore.dart';
import 'DriverBridge.dart';
import 'ScriptEngine.dart';

void main() => runApp(MyApp());

class MyApp extends StatelessWidget {


  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {

    return MaterialApp(
      title: 'Flutter Demo',
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: MyHomePage(),
    );
  }
}


class MyHomePage extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}


class _MyAppState extends State<MyHomePage> {
  Random _rng;
  MicroService _microService;
  JSContext _jsContext;

  String _jsContextResponse = '<empty>';
  String _microServiceResponse = '<empty>';
  String barcode = "";
  int _microServiceWorld = 0;
  ScriptEngine _engine;
  bool serviceIsEnable = false;
  Driver dr = new Driver();

  TextEditingController urlC = TextEditingController(text:  "http://192.168.41.148:8080/dist/main.js");

  static const platform = const MethodChannel('samples.flutter.dev/startApp');

  @override
  void initState() {
    super.initState();
    checkIsEnable();
  }

  checkIsEnable() async {
    var isenable = await dr.checkAccessibilityIsEnabled();
    this.serviceIsEnable = isenable as bool;
    if(!this.serviceIsEnable){
      showDialog<String>(
          context: context,
          builder: (BuildContext context) {
            return SimpleDialog(
              title: const Text('开启辅助服务！'),
              titlePadding: EdgeInsets.only(
                left: 15,
                top: 15,
              ),
              contentPadding:
              EdgeInsets.only(left: 15, right: 15, top: 15, bottom: 15),
              children: <Widget>[
                Text("执行脚本需要开启辅助功能"),
                SizedBox(height: 15),
                RaisedButton(
                  onPressed: () async {
                    dr.goAccessibilitySetting();
                  },
                  color: Colors.blue,
                  child: Text(
                    '前往开启',
                    style: TextStyle(color: Colors.white),
                  ),
                ),
              ],
            );
          });
    }
    print('isenable');
    print(isenable);
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('TestKit'),
          actions: <Widget>[
            IconButton(
              icon: Icon(Icons.add),
              onPressed: () async {

              },
            )
          ],
        ),
        body: Column(
            mainAxisAlignment: MainAxisAlignment.spaceEvenly,
            children: <Widget>[
              TextField(
                controller: urlC,
              ),
              RaisedButton(
                child: const Text('Run'),
                onPressed: initMicroService,
              )
            ]),
      ),
    );
  }

  @override
  void dispose() {
    if (_engine != null) {
      // Exit and free up the resources.
      // _microService.exitProcess(0); // This API call might not always be available.
      _engine.stop();
    }
    super.dispose();
  }


  // Platform messages are asynchronous, so we initialize in an async method.
  void initMicroService() async {
      if(_engine != null){
        _engine.stop();
      }
      _engine = new ScriptEngine(urlC.text);
      await _engine.start();
  }



}