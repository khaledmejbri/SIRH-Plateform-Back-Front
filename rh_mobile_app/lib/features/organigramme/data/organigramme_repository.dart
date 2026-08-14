import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/network/api_client.dart';
import 'organigramme_models.dart';

final organigrammeRepositoryProvider = Provider<OrganigrammeRepository>((ref) {
  return OrganigrammeRepository(ref.watch(dioProvider));
});

class OrganigrammeRepository {
  OrganigrammeRepository(this._dio);

  final Dio _dio;

  Future<Organigramme> fetch() async {
    final res = await _dio.get<Map<String, dynamic>>(
      '/api/referentiel/v1/organigramme',
    );
    return Organigramme.fromJson(res.data ?? const {});
  }
}
