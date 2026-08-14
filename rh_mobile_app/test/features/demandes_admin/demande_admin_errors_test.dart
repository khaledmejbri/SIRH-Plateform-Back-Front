import 'package:flutter_test/flutter_test.dart';
import 'package:dio/dio.dart';

import 'package:rh_mobile_app/features/demandes_admin/data/demande_admin_errors.dart';

void main() {
  test('403 expose le message serveur valideur nœud', () {
    final e = DioException(
      requestOptions: RequestOptions(path: '/x'),
      response: Response(
        requestOptions: RequestOptions(path: '/x'),
        statusCode: 403,
        data: {
          'erreur':
              'Seul le manager actif du nœud d\'unité du demandeur peut valider cette étape.',
        },
      ),
      type: DioExceptionType.badResponse,
    );
    expect(
      messageErreurDemandeAdmin(e),
      contains('manager actif du nœud'),
    );
  });

  test('403 sans corps utilise le fallback métier', () {
    final e = DioException(
      requestOptions: RequestOptions(path: '/x'),
      response: Response(
        requestOptions: RequestOptions(path: '/x'),
        statusCode: 403,
      ),
      type: DioExceptionType.badResponse,
    );
    expect(messageErreurDemandeAdmin(e), contains('manager actif'));
  });
}
