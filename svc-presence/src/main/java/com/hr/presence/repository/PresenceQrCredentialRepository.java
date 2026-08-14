package com.hr.presence.repository;

import com.hr.presence.domain.QrCredentialStatut;
import com.hr.presence.entity.PresenceQrCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PresenceQrCredentialRepository extends JpaRepository<PresenceQrCredential, UUID> {

	Optional<PresenceQrCredential> findBySiteIdAndStatut(UUID siteId, QrCredentialStatut statut);

	Optional<PresenceQrCredential> findByJti(UUID jti);

	@Query("select coalesce(max(c.qrVersion), 0) from PresenceQrCredential c where c.site.id = :siteId")
	int findMaxVersionBySiteId(@Param("siteId") UUID siteId);

	@Query("""
			select c from PresenceQrCredential c
			join fetch c.site s
			where c.statut = com.hr.presence.domain.QrCredentialStatut.ACTIF
			  and c.alerteJ30Envoyee = false
			  and c.validUntil >= :from
			  and c.validUntil < :to
			""")
	List<PresenceQrCredential> findActifsPourAlerteJ30(@Param("from") Instant from, @Param("to") Instant to);

	@Query("""
			select c from PresenceQrCredential c
			where c.statut = com.hr.presence.domain.QrCredentialStatut.ACTIF
			  and c.validUntil < :now
			""")
	List<PresenceQrCredential> findActifsExpires(@Param("now") Instant now);
}
