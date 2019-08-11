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
  bool isRecord = false;

  TextEditingController urlC = TextEditingController(text:  "http://192.168.1.6:8080/dist/main.js");

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

    List<Widget> childs = [];
    List<Map> data = [];


    data.add({
      'name': "测试",
      'url':  'http://192.168.1.6:8080/dist/main.js'
    });

    data.add({
      'name': "pc-home",
      'url':  'http://192.168.31.211:8080/dist/main.js'
    });

    data.add({
      'name': "测试",
      'url':  'http://192.168.1.6:8080/dist/main.js'
    });

    data.forEach((f) {
      childs.add(ListTile(
          onTap: (){
            initMicroService(f['url']);
          },
          title: Text(f['name']),
          subtitle: Text(f['url']),
          trailing: IconButton(
              icon: Icon(Icons.play_arrow)
          )
      ));
    });

    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('TestKit'),
          actions: <Widget>[
            isRecord ? IconButton(
              icon: Icon(Icons.stop),
              onPressed: (){
                setState(() {
                  isRecord = false;
                });
                _engine.microService.emit("stopRecord");
              },
            ) : IconButton(
              icon: Icon(Icons.fiber_manual_record),
              onPressed: (){
                if(_engine == null){
                  print('start service');
                  return;
                }
                setState(() {
                  isRecord = true;
                });
                _engine.microService.emit("startRecord");
              },
            ),
            IconButton(
              icon: Icon(Icons.add),
              onPressed: () async {



              },
            )
          ],
        ),
        body: ListView(
          children: childs
        ),
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
  void initMicroService(url) async {

      platform.invokeMethod("startService", {
        "url": url
      });

      return;
      if(_engine != null){
        try{
          _engine.stop();
        }catch(e){
        }
      }


      print(urlC.text);
      _engine = new ScriptEngine(url);
      await _engine.start();
  }



}