import 'package:flutter/services.dart';
import 'package:permission_handler/permission_handler.dart';

/// Position GPS appareil (lat/lon/accuracy) pour pointage / pose site.
class GpsFix {
  const GpsFix({
    required this.latitude,
    required this.longitude,
    this.accuracyMeters,
  });

  final double latitude;
  final double longitude;
  final double? accuracyMeters;
}

/// GPS appareil : permission_handler + canal natif Android `rh_connect/location`.
class PresenceGps {
  PresenceGps._();

  static const _channel = MethodChannel('rh_connect/location');

  /// Position courante ou message d’erreur métier (jamais silent success).
  static Future<({GpsFix? position, String? error})> obtain() async {
    try {
      final serviceOk = await _channel.invokeMethod<bool>('isLocationEnabled');
      if (serviceOk != true) {
        return (
          position: null,
          error:
              'La localisation est désactivée. Activez le GPS pour pointer.',
        );
      }
    } on MissingPluginException {
      return (
        position: null,
        error:
            'Localisation indisponible sur cette plateforme. Utilisez un iPhone ou un Android.',
      );
    } on PlatformException catch (e) {
      if (e.code == 'DISABLED') {
        return (
          position: null,
          error:
              'La localisation est désactivée. Activez le GPS pour pointer.',
        );
      }
      return (
        position: null,
        error: e.message ??
            'Impossible d’obtenir la position GPS. Réessayez à l’extérieur.',
      );
    }

    var status = await Permission.locationWhenInUse.status;
    if (status.isDenied) {
      status = await Permission.locationWhenInUse.request();
    }
    if (status.isPermanentlyDenied) {
      return (
        position: null,
        error:
            'Localisation définitivement refusée. Autorisez-la dans les réglages de l’appareil.',
      );
    }
    if (!status.isGranted) {
      return (
        position: null,
        error:
            'Autorisation de localisation refusée. Activez-la dans les réglages.',
      );
    }

    try {
      final raw = await _channel.invokeMethod<Map<dynamic, dynamic>>(
        'getCurrentPosition',
      );
      if (raw == null) {
        return (
          position: null,
          error:
              'Impossible d’obtenir la position GPS. Réessayez à l’extérieur.',
        );
      }
      final lat = (raw['latitude'] as num?)?.toDouble();
      final lon = (raw['longitude'] as num?)?.toDouble();
      if (lat == null || lon == null) {
        return (
          position: null,
          error:
              'Impossible d’obtenir la position GPS. Réessayez à l’extérieur.',
        );
      }
      final accuracy = (raw['accuracyMeters'] as num?)?.toDouble();
      return (
        position: GpsFix(
          latitude: lat,
          longitude: lon,
          accuracyMeters: accuracy,
        ),
        error: null,
      );
    } on PlatformException catch (e) {
      if (e.code == 'DISABLED') {
        return (
          position: null,
          error:
              'La localisation est désactivée. Activez le GPS pour pointer.',
        );
      }
      return (
        position: null,
        error: e.message ??
            'Impossible d’obtenir la position GPS. Réessayez à l’extérieur.',
      );
    } catch (_) {
      return (
        position: null,
        error:
            'Impossible d’obtenir la position GPS. Réessayez à l’extérieur.',
      );
    }
  }
}
