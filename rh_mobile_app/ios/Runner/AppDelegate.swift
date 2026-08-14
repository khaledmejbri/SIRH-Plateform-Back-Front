import CoreLocation
import Flutter
import UIKit

@main
@objc class AppDelegate: FlutterAppDelegate, FlutterImplicitEngineDelegate, CLLocationManagerDelegate {
  private let locationManager = CLLocationManager()
  private var pendingResult: FlutterResult?
  private var timeoutWork: DispatchWorkItem?

  override func application(
    _ application: UIApplication,
    didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
  ) -> Bool {
    locationManager.delegate = self
    locationManager.desiredAccuracy = kCLLocationAccuracyBest
    return super.application(application, didFinishLaunchingWithOptions: launchOptions)
  }

  func didInitializeImplicitFlutterEngine(_ engineBridge: FlutterImplicitEngineBridge) {
    GeneratedPluginRegistrant.register(with: engineBridge.pluginRegistry)
    let channel = FlutterMethodChannel(
      name: "rh_connect/location",
      binaryMessenger: engineBridge.applicationRegistrar.messenger()
    )
    channel.setMethodCallHandler { [weak self] call, result in
      guard let self = self else { return }
      switch call.method {
      case "isLocationEnabled":
        result(CLLocationManager.locationServicesEnabled())
      case "getCurrentPosition":
        self.requestPosition(result)
      default:
        result(FlutterMethodNotImplemented)
      }
    }
  }

  private func requestPosition(_ result: @escaping FlutterResult) {
    if !CLLocationManager.locationServicesEnabled() {
      result(FlutterError(code: "DISABLED", message: "GPS désactivé", details: nil))
      return
    }
    let status = locationManager.authorizationStatus
    if status == .denied || status == .restricted {
      result(FlutterError(code: "PERMISSION", message: "Localisation non autorisée", details: nil))
      return
    }
    pendingResult = result
    if status == .notDetermined {
      locationManager.requestWhenInUseAuthorization()
      return
    }
    startLocationRequest()
  }

  private func startLocationRequest() {
    timeoutWork?.cancel()
    let work = DispatchWorkItem { [weak self] in
      guard let self = self else { return }
      if let last = self.locationManager.location {
        self.finish(location: last)
      } else {
        self.finish(
          error: FlutterError(
            code: "TIMEOUT",
            message: "Impossible d’obtenir la position GPS. Réessayez à l’extérieur.",
            details: nil
          )
        )
      }
    }
    timeoutWork = work
    DispatchQueue.main.asyncAfter(deadline: .now() + 20, execute: work)
    locationManager.requestLocation()
  }

  func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
    guard pendingResult != nil else { return }
    let status = manager.authorizationStatus
    if status == .authorizedWhenInUse || status == .authorizedAlways {
      startLocationRequest()
    } else if status == .denied || status == .restricted {
      finish(error: FlutterError(code: "PERMISSION", message: "Localisation non autorisée", details: nil))
    }
  }

  func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
    if let loc = locations.last {
      finish(location: loc)
    }
  }

  func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
    if let last = manager.location {
      finish(location: last)
    } else {
      finish(error: FlutterError(code: "ERROR", message: error.localizedDescription, details: nil))
    }
  }

  private func finish(location: CLLocation) {
    timeoutWork?.cancel()
    timeoutWork = nil
    let accuracy: Any = location.horizontalAccuracy >= 0 ? location.horizontalAccuracy : NSNull()
    pendingResult?([
      "latitude": location.coordinate.latitude,
      "longitude": location.coordinate.longitude,
      "accuracyMeters": accuracy,
    ])
    pendingResult = nil
  }

  private func finish(error: FlutterError) {
    timeoutWork?.cancel()
    timeoutWork = nil
    pendingResult?(error)
    pendingResult = nil
  }
}
