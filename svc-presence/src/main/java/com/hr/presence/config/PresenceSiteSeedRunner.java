package com.hr.presence.config;

import com.hr.presence.entity.PresenceSite;
import com.hr.presence.repository.PresenceSiteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Seed démo H-BA-01 — 1–2 sites si catalogue vide.
 */
@Component
public class PresenceSiteSeedRunner implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(PresenceSiteSeedRunner.class);

	private final PresenceSiteRepository siteRepository;

	public PresenceSiteSeedRunner(PresenceSiteRepository siteRepository) {
		this.siteRepository = siteRepository;
	}

	@Override
	public void run(ApplicationArguments args) {
		if (siteRepository.count() > 0) {
			return;
		}
		PresenceSite hq = new PresenceSite();
		hq.setCode("HQ-TUNIS");
		hq.setLibelle("Siège Tunis — Accueil");
		hq.setLatitude(36.8065);
		hq.setLongitude(10.1815);
		hq.setRayonMetres(50);
		hq.setActif(true);
		hq.setUpdatedBy("seed");

		PresenceSite site2 = new PresenceSite();
		site2.setCode("SITE-SANS-GPS");
		site2.setLibelle("Site démo sans emplacement");
		site2.setRayonMetres(50);
		site2.setActif(true);
		site2.setUpdatedBy("seed");

		siteRepository.save(hq);
		siteRepository.save(site2);
		log.info("Seed présence : 2 sites démo créés");
	}
}
