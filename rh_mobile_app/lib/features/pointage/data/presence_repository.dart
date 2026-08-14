import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/constants/api_constants.dart';
import '../../../core/network/api_client.dart';
import 'presence_models.dart';

final presenceRepositoryProvider = Provider<PresenceRepository>((ref) {
  return PresenceRepository(ref.watch(dioProvider));
});

class PresenceRepository {
  PresenceRepository(this._dio);

  final Dio _dio;

  static String get _mobileBase => ApiConstants.presenceMobile;
  static String get _adminBase => ApiConstants.presenceAdmin;

  /// POST pointage collaborateur — HTTP 200 même pour `REJETE_*`.
  Future<PointageResult> creerPointage(PointageRequest request) async {
    final res = await _dio.post<Map<String, dynamic>>(
      '$_mobileBase/pointages',
      data: request.toJson(),
    );
    return PointageResult.fromJson(res.data ?? const {});
  }

  /// Historique personnel (soi).
  Future<List<PointageHistoriqueItem>> mesPointages({
    int page = 0,
    int size = 20,
  }) async {
    final res = await _dio.get<dynamic>(
      '$_mobileBase/pointages/me',
      queryParameters: {'page': page, 'size': size},
    );
    return _parseList(res.data, PointageHistoriqueItem.fromJson);
  }

  /// Liste sites (RH | ADMIN | DIRECTION lecture).
  Future<List<PresenceSite>> listerSites({int page = 0, int size = 50}) async {
    final res = await _dio.get<dynamic>(
      '$_adminBase/sites',
      queryParameters: {'page': page, 'size': size},
    );
    return _parseList(res.data, PresenceSite.fromJson);
  }

  /// Pose GPS du site (appareil RH/ADMIN).
  Future<PresenceSite> enregistrerEmplacement({
    required String siteId,
    required double latitude,
    required double longitude,
  }) async {
    final res = await _dio.put<Map<String, dynamic>>(
      '$_adminBase/sites/$siteId/emplacement',
      data: {
        'latitude': latitude,
        'longitude': longitude,
      },
    );
    return PresenceSite.fromJson(res.data ?? const {});
  }

  List<T> _parseList<T>(
    dynamic data,
    T Function(Map<String, dynamic>) fromJson,
  ) {
    final items = <dynamic>[];
    if (data is List) {
      items.addAll(data);
    } else if (data is Map) {
      final content = data['content'] ?? data['items'] ?? data['data'];
      if (content is List) {
        items.addAll(content);
      }
    }
    return items
        .whereType<Map>()
        .map((e) => fromJson(Map<String, dynamic>.from(e)))
        .toList();
  }
}
