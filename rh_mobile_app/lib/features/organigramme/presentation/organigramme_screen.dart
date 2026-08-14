import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/theme/app_theme.dart';
import '../data/organigramme_models.dart';
import '../data/organigramme_repository.dart';

final _organigrammeProvider = FutureProvider.autoDispose<Organigramme>((ref) {
  return ref.watch(organigrammeRepositoryProvider).fetch();
});

class OrganigrammeScreen extends ConsumerWidget {
  const OrganigrammeScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final async = ref.watch(_organigrammeProvider);

    return Scaffold(
      backgroundColor: AppTheme.background,
      appBar: AppBar(
        title: const Text('Organigramme'),
        actions: [
          IconButton(
            tooltip: 'Actualiser',
            onPressed: () => ref.invalidate(_organigrammeProvider),
            icon: const Icon(Icons.refresh_rounded),
          ),
        ],
      ),
      body: async.when(
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (e, _) => Center(
          child: Padding(
            padding: const EdgeInsets.all(24),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                Text(
                  'Impossible de charger l\'organigramme.',
                  style: Theme.of(context).textTheme.titleMedium,
                  textAlign: TextAlign.center,
                ),
                const SizedBox(height: 8),
                Text(
                  '$e',
                  style: Theme.of(context).textTheme.bodySmall,
                  textAlign: TextAlign.center,
                ),
                const SizedBox(height: 16),
                FilledButton(
                  onPressed: () => ref.invalidate(_organigrammeProvider),
                  child: const Text('Réessayer'),
                ),
              ],
            ),
          ),
        ),
        data: (org) {
          if (org.racines.isEmpty) {
            return const Center(
              child: Text('Aucune structure organisationnelle publiée.'),
            );
          }
          return ListView(
            padding: const EdgeInsets.fromLTRB(16, 12, 16, 32),
            children: [
              for (final n in org.racines) _NoeudCard(noeud: n, depth: 0),
            ],
          );
        },
      ),
    );
  }
}

class _NoeudCard extends StatefulWidget {
  const _NoeudCard({required this.noeud, required this.depth});

  final OrganigrammeNoeud noeud;
  final int depth;

  @override
  State<_NoeudCard> createState() => _NoeudCardState();
}

class _NoeudCardState extends State<_NoeudCard> {
  late bool _expanded = widget.depth < 1;

  @override
  Widget build(BuildContext context) {
    final noeud = widget.noeud;
    final hasKids = noeud.enfants.isNotEmpty || noeud.membres.isNotEmpty;

    return Padding(
      padding: EdgeInsets.only(left: widget.depth * 12.0, bottom: 10),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Material(
            color: AppTheme.surface,
            borderRadius: BorderRadius.circular(14),
            child: InkWell(
              borderRadius: BorderRadius.circular(14),
              onTap: hasKids ? () => setState(() => _expanded = !_expanded) : null,
              child: Container(
                padding: const EdgeInsets.all(14),
                decoration: BoxDecoration(
                  borderRadius: BorderRadius.circular(14),
                  border: Border.all(color: AppTheme.border),
                ),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      children: [
                        Expanded(
                          child: Text(
                            noeud.typeNoeud.toUpperCase(),
                            style: const TextStyle(
                              fontSize: 11,
                              fontWeight: FontWeight.w700,
                              letterSpacing: 0.4,
                              color: AppTheme.primary,
                            ),
                          ),
                        ),
                        if (hasKids)
                          Icon(
                            _expanded
                                ? Icons.expand_less_rounded
                                : Icons.expand_more_rounded,
                            color: AppTheme.textSecondary,
                          ),
                      ],
                    ),
                    const SizedBox(height: 4),
                    Text(
                      noeud.libelle,
                      style: Theme.of(context).textTheme.titleSmall?.copyWith(
                            fontWeight: FontWeight.w700,
                          ),
                    ),
                    if (noeud.titrePoste != null && noeud.titrePoste!.isNotEmpty) ...[
                      const SizedBox(height: 2),
                      Text(noeud.titrePoste!, style: Theme.of(context).textTheme.bodySmall),
                    ],
                    if (noeud.manager != null) ...[
                      const SizedBox(height: 10),
                      Row(
                        children: [
                          CircleAvatar(
                            radius: 14,
                            backgroundColor: AppTheme.primarySurface,
                            child: Text(
                              _initials(noeud.manager!.displayName),
                              style: const TextStyle(
                                fontSize: 10,
                                fontWeight: FontWeight.w700,
                                color: AppTheme.primary,
                              ),
                            ),
                          ),
                          const SizedBox(width: 8),
                          Expanded(
                            child: Text(
                              noeud.manager!.displayName,
                              style: const TextStyle(fontWeight: FontWeight.w600),
                            ),
                          ),
                        ],
                      ),
                    ],
                  ],
                ),
              ),
            ),
          ),
          if (_expanded) ...[
            for (final e in noeud.enfants)
              _NoeudCard(noeud: e, depth: widget.depth + 1),
            for (final m in noeud.membres)
              Padding(
                padding: EdgeInsets.only(left: (widget.depth + 1) * 12.0, top: 4, bottom: 4),
                child: Row(
                  children: [
                    const Icon(Icons.person_outline, size: 18, color: AppTheme.textSecondary),
                    const SizedBox(width: 8),
                    Expanded(
                      child: Text(
                        (m.posteLibelle == null || m.posteLibelle!.isEmpty)
                            ? m.displayName
                            : '${m.displayName} — ${m.posteLibelle}',
                      ),
                    ),
                  ],
                ),
              ),
          ],
        ],
      ),
    );
  }

  static String _initials(String name) {
    final parts = name.trim().split(RegExp(r'\s+'));
    if (parts.isEmpty) return '?';
    if (parts.length == 1) {
      return parts.first.isEmpty ? '?' : parts.first.substring(0, 1).toUpperCase();
    }
    return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
  }
}
