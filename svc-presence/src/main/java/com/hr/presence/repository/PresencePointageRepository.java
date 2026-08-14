package com.hr.presence.repository;

import com.hr.presence.domain.PointageStatut;
import com.hr.presence.entity.PresencePointage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface PresencePointageRepository extends JpaRepository<PresencePointage, UUID> {

	Optional<PresencePointage> findByCollaborateurIdAndIdempotencyKey(String collaborateurId, String idempotencyKey);

	Page<PresencePointage> findByCollaborateurIdOrderByServerTsDesc(String collaborateurId, Pageable pageable);

	@Query("""
			select p from PresencePointage p
			where (:siteId is null or p.siteId = :siteId)
			  and (:statut is null or p.statut = :statut)
			  and (:from is null or p.serverTs >= :from)
			  and (:to is null or p.serverTs <= :to)
			order by p.serverTs desc
			""")
	Page<PresencePointage> searchAdmin(
			@Param("siteId") UUID siteId,
			@Param("statut") PointageStatut statut,
			@Param("from") Instant from,
			@Param("to") Instant to,
			Pageable pageable);
}
