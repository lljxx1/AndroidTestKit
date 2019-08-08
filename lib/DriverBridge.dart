import 'dart:async';

import 'package:flutter/services.dart';
import 'package:uuid/uuid.dart';


class Driver {
  static final MethodChannel _methodChannel =
  const MethodChannel('samples.flutter.dev/startApp')
    ..setMethodCallHandler(_platformCallHandler);

  static final _uuid = new Uuid();
  static final _instances = new Map<String, Driver>();

  String _instanceId;
  String _uri;
  bool _isStarted = false;
  bool _hasExit = false;

  /// Creates a new MicroService instance.
  /// [uri] The URI (can be a network URL or local file/resource) of the MicroService code.
  ///
  /// Example local file URIs are as follows:
  ///   Flutter asset: '@flutter_assets/path/to/asset.js'
  ///   Raw Android assets:
  ///     - 'android.resource://$android_package_name$/raw/script' (without the .js extension)
  ///     - 'file:///android_asset/script.js'
  ///   Raw iOS bundle resource formats:
  ///     - 'Resources/script.js'
  Driver() {
    _instanceId = _uuid.v4();
    _instances[_instanceId] = this;
  }

  /// Return the current Dart UUID.
  String id() {
    return _instanceId;
  }

  /// Find By Text
  Future<dynamic> findByText(String text) async {
    return _invokeMethod('findElement', {
      'strategy': 'text',
      'selector': text
    });
  }

  /// Find By id
  Future<dynamic> findById(String id) async {
    return _invokeMethod('findElement', {
      'strategy': 'id',
      'selector': id
    });
  }


  Future<dynamic> checkAccessibilityIsEnabled() async {
    return _invokeMethod('checkAccessibilityIsEnabled');
  }

  Future<dynamic> goAccessibilitySetting() async {
    return _invokeMethod('goAccessibilitySetting');
  }


  Future<dynamic> doActionToElement(Map data) async {
    return _invokeMethod('doActionToElement', data);
  }

  Future<dynamic> getSource() async {
    return _invokeMethod('getSource');
  }

  /// Send a message over to the native implementation.
  Future<dynamic> _invokeMethod(String method,
      [Map<String, dynamic> arguments = const {}]) {
    Map<String, dynamic> argWithIds = Map.of(arguments);
    argWithIds['serviceId'] = _instanceId;
    return _methodChannel.invokeMethod(method, argWithIds);
  }

  static Future<void> _platformCallHandler(MethodCall call) async {


  }

  bool get hasExit => _hasExit;
  bool get isStarted => _isStarted;
}
