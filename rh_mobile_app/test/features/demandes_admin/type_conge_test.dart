import 'package:flutter_test/flutter_test.dart';

import 'package:rh_mobile_app/features/demandes_admin/data/type_conge.dart';

void main() {
  test('catalogue fermé contient exactement 5 types dont AUTRE', () {
    expect(kTypesConge, hasLength(5));
    expect(kTypesConge.map((t) => t.code).toList(), [
      'ANNUEL',
      'MALADIE',
      'MATERNITE',
      'SANS_SOLDE',
      'AUTRE',
    ]);
  });

  test('PJ obligatoire uniquement pour MALADIE et MATERNITE', () {
    expect(typeCongeExigePieceJointe('MALADIE'), isTrue);
    expect(typeCongeExigePieceJointe('MATERNITE'), isTrue);
    expect(typeCongeExigePieceJointe('ANNUEL'), isFalse);
    expect(typeCongeExigePieceJointe('SANS_SOLDE'), isFalse);
    expect(typeCongeExigePieceJointe('AUTRE'), isFalse);
  });

  test('libellés FR professionnels', () {
    expect(libelleTypeConge('ANNUEL'), 'Congé annuel');
    expect(libelleTypeConge('MATERNITE'), 'Congé maternité');
    expect(libelleTypeConge('SANS_SOLDE'), 'Congé sans solde');
    expect(libelleTypeConge('AUTRE'), 'Autre');
  });
}
