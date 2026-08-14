import 'dart:convert';

import 'package:flutter_test/flutter_test.dart';
import 'package:rh_mobile_app/core/auth/jwt_access_roles.dart';
import 'package:rh_mobile_app/features/auth/providers/collaborateur_notifier.dart';

String _jwt(Map<String, dynamic> payload) {
  final header = base64Url
      .encode(utf8.encode('{"alg":"none","typ":"JWT"}'))
      .replaceAll('=', '');
  final body =
      base64Url.encode(utf8.encode(jsonEncode(payload))).replaceAll('=', '');
  return '$header.$body.sig';
}

void main() {
  group('JwtAccessRoles', () {
    test('isRo depuis claim roles RO, pas depuis profil_acces', () {
      final roles = JwtAccessRoles.fromAccessToken(_jwt({
        'roles': ['USER', 'RO'],
      }));
      expect(roles.isRo, isTrue);
      expect(roles.isChefDept, isFalse);
      expect(roles.isRh, isFalse);
      expect(roles.peutValiderFile, isTrue);
      expect(roles.libelleHabilitation, 'Responsable opérationnel');
    });

    test('isChefDept depuis JWT RESPONSABLE', () {
      final roles = JwtAccessRoles.fromAccessToken(_jwt({
        'roles': ['USER', 'RESPONSABLE'],
      }));
      expect(roles.isChefDept, isTrue);
      expect(roles.isRo, isFalse);
      expect(roles.peutValiderFile, isTrue);
      expect(roles.libelleHabilitation, 'Chef de département');
    });

    test('isRh ne donne pas la file M01', () {
      final roles = JwtAccessRoles.fromAccessToken(_jwt({
        'roles': ['USER', 'RH'],
      }));
      expect(roles.isRh, isTrue);
      expect(roles.peutConfigurerPresence, isTrue);
      expect(roles.peutValiderFile, isFalse);
      expect(roles.libelleHabilitation, isNull);
    });

    test('ADMIN peut configurer présence', () {
      final roles = JwtAccessRoles.fromAccessToken(_jwt({
        'roles': ['ADMIN'],
      }));
      expect(roles.isAdmin, isTrue);
      expect(roles.peutConfigurerPresence, isTrue);
    });

    test('USER seul ne configure pas la présence', () {
      final roles = JwtAccessRoles.fromAccessToken(_jwt({
        'roles': ['USER'],
      }));
      expect(roles.peutConfigurerPresence, isFalse);
    });

    test('scope ROLE_RO est reconnu', () {
      final roles = JwtAccessRoles.fromAccessToken(_jwt({
        'scope': 'ROLE_USER ROLE_RO',
      }));
      expect(roles.isRo, isTrue);
      expect(roles.peutValiderFile, isTrue);
    });

    test('sans roles ni scope catalogue : file masquée', () {
      final roles = JwtAccessRoles.fromAccessToken(_jwt({
        'sub': 'matricule1',
      }));
      expect(roles.isRo, isFalse);
      expect(roles.isChefDept, isFalse);
      expect(roles.peutValiderFile, isFalse);
    });

    test('token non JWT (démo) : file masquée', () {
      final roles = JwtAccessRoles.fromAccessToken('demo_token_123');
      expect(roles.peutValiderFile, isFalse);
    });

    test('RESPONSABLE_OPERATIONNEL dans le JWT n’active pas isRo', () {
      final roles = JwtAccessRoles.fromAccessToken(_jwt({
        'roles': ['USER', 'RESPONSABLE_OPERATIONNEL'],
      }));
      expect(roles.isRo, isFalse);
      expect(roles.peutValiderFile, isFalse);
    });

    test('token null ou vide : file masquée', () {
      expect(JwtAccessRoles.fromAccessToken(null).peutValiderFile, isFalse);
      expect(JwtAccessRoles.fromAccessToken('').peutValiderFile, isFalse);
    });
  });

  group('CollaborateurInfo', () {
    test('flags viennent du JWT, pas de profil_acces', () {
      final info = CollaborateurInfo.fromMap(
        {
          'identifiant': 'c1',
          'name': 'Dupont',
          'prenom': 'Marie',
          'profil_acces': 'RO',
        },
        jwtRoles: JwtAccessRoles.empty,
      );
      expect(info.profilAcces, 'RO');
      expect(info.isRo, isFalse);
      expect(info.peutValiderFile, isFalse);
    });

    test('profil_acces RESPONSABLE_OPERATIONNEL n’active pas isRo', () {
      final info = CollaborateurInfo.fromMap({
        'identifiant': 'c1',
        'name': 'Dupont',
        'prenom': 'Marie',
        'profil_acces': 'RESPONSABLE_OPERATIONNEL',
      });
      expect(info.isRo, isFalse);
      expect(info.peutValiderFile, isFalse);
    });

    test('JWT RO active isRo même si profil_acces collaborateur', () {
      final info = CollaborateurInfo.fromMap(
        {
          'identifiant': 'c1',
          'name': 'Dupont',
          'prenom': 'Marie',
          'profil_acces': 'COLLABORATEUR',
        },
        jwtRoles: JwtAccessRoles.fromPayload({
          'roles': ['USER', 'RO'],
        }),
      );
      expect(info.isRo, isTrue);
      expect(info.peutValiderFile, isTrue);
    });
  });
}
