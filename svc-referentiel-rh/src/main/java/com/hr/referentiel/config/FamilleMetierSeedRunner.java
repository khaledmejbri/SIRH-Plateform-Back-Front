package com.hr.referentiel.config;

import com.hr.referentiel.entity.FamilleMetier;
import com.hr.referentiel.repository.FamilleMetierRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Seed H-B1 — 6 familles métier système si absentes (catalogue éditable ensuite).
 */
@Component
public class FamilleMetierSeedRunner implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(FamilleMetierSeedRunner.class);

	static final Map<String, String> SEED = new LinkedHashMap<>();

	static {
		SEED.put("EXPLOITATION", "Exploitation / terrain assainissement");
		SEED.put("GENIE_CIVIL", "Génie civil / travaux");
		SEED.put("DEV_LOGICIEL", "Développement logiciel / SI");
		SEED.put("SUPPORT_ADMIN", "Support administratif");
		SEED.put("MAINTENANCE", "Maintenance technique");
		SEED.put("HSE_QUALITE", "HSE / qualité");
	}

	private final FamilleMetierRepository familleMetierRepository;

	public FamilleMetierSeedRunner(FamilleMetierRepository familleMetierRepository) {
		this.familleMetierRepository = familleMetierRepository;
	}

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		int inserted = 0;
		for (Map.Entry<String, String> e : SEED.entrySet()) {
			if (!familleMetierRepository.existsById(e.getKey())) {
				FamilleMetier f = new FamilleMetier();
				f.setCode(e.getKey());
				f.setLibelle(e.getValue());
				f.setActif(true);
				f.setSysteme(true);
				familleMetierRepository.save(f);
				inserted++;
			}
		}
		if (inserted > 0) {
			log.info("Seed familles métier H-B1 : {} code(s) inséré(s)", inserted);
		}
	}
}
