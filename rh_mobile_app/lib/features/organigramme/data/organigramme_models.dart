class OrganigrammeMembre {
  final String identifiant;
  final String matricule;
  final String prenom;
  final String nom;
  final String? posteLibelle;
  final String? profilAcces;

  const OrganigrammeMembre({
    required this.identifiant,
    required this.matricule,
    required this.prenom,
    required this.nom,
    this.posteLibelle,
    this.profilAcces,
  });

  factory OrganigrammeMembre.fromJson(Map<String, dynamic> json) {
    return OrganigrammeMembre(
      identifiant: json['identifiant'] as String? ?? '',
      matricule: json['matricule'] as String? ?? '',
      prenom: json['prenom'] as String? ?? '',
      nom: json['nom'] as String? ?? '',
      posteLibelle: json['poste_libelle'] as String?,
      profilAcces: json['profil_acces'] as String?,
    );
  }

  String get displayName => '$prenom $nom'.trim();
}

class OrganigrammeNoeud {
  final String identifiant;
  final String code;
  final String libelle;
  final String typeNoeud;
  final String? titrePoste;
  final String? parentIdentifiant;
  final bool actif;
  final OrganigrammeMembre? manager;
  final List<OrganigrammeMembre> membres;
  final List<OrganigrammeNoeud> enfants;

  const OrganigrammeNoeud({
    required this.identifiant,
    required this.code,
    required this.libelle,
    required this.typeNoeud,
    this.titrePoste,
    this.parentIdentifiant,
    required this.actif,
    this.manager,
    required this.membres,
    required this.enfants,
  });

  factory OrganigrammeNoeud.fromJson(Map<String, dynamic> json) {
    final enfantsRaw = json['enfants'] as List<dynamic>? ?? const [];
    final membresRaw = json['membres'] as List<dynamic>? ?? const [];
    final managerRaw = json['manager'];
    return OrganigrammeNoeud(
      identifiant: json['identifiant'] as String? ?? '',
      code: json['code'] as String? ?? '',
      libelle: json['libelle'] as String? ?? '',
      typeNoeud: json['type_noeud'] as String? ?? 'Nœud',
      titrePoste: json['titre_poste'] as String?,
      parentIdentifiant: json['parent_identifiant'] as String?,
      actif: json['actif'] as bool? ?? true,
      manager: managerRaw is Map
          ? OrganigrammeMembre.fromJson(Map<String, dynamic>.from(managerRaw))
          : null,
      membres: membresRaw
          .whereType<Map>()
          .map((e) => OrganigrammeMembre.fromJson(Map<String, dynamic>.from(e)))
          .toList(),
      enfants: enfantsRaw
          .whereType<Map>()
          .map((e) => OrganigrammeNoeud.fromJson(Map<String, dynamic>.from(e)))
          .toList(),
    );
  }
}

class Organigramme {
  final List<OrganigrammeNoeud> racines;

  const Organigramme({required this.racines});

  factory Organigramme.fromJson(Map<String, dynamic> json) {
    final racinesRaw = json['racines'] as List<dynamic>? ?? const [];
    return Organigramme(
      racines: racinesRaw
          .whereType<Map>()
          .map((e) => OrganigrammeNoeud.fromJson(Map<String, dynamic>.from(e)))
          .toList(),
    );
  }
}
