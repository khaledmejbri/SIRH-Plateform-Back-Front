package com.hr.referentiel.repository;

import com.hr.referentiel.entity.FamilleMetier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FamilleMetierRepository extends JpaRepository<FamilleMetier, String> {

	List<FamilleMetier> findByActifTrueOrderByCodeAsc();

	List<FamilleMetier> findAllByOrderByCodeAsc();

	boolean existsByCodeIgnoreCase(String code);
}
