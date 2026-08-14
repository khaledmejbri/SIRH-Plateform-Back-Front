package com.hr.evaluation.repository;

import com.hr.evaluation.domain.TemplateStatus;
import com.hr.evaluation.domain.TemplateType;
import com.hr.evaluation.entity.EvaluationTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EvaluationTemplateRepository extends JpaRepository<EvaluationTemplate, UUID> {

    // Legacy methods (kept for backward compatibility)
    @Deprecated
    default List<EvaluationTemplate> findByActifTrueOrderByNameAsc() {
        return findAllByActifTrue();
    }
    
    @Deprecated
    default List<EvaluationTemplate> findByReutilisableTrueAndActifTrueOrderByNameAsc() {
        return findAllByActifTrue();
    }
    
    // Enhanced methods
    List<EvaluationTemplate> findByType(TemplateType type);
    
    List<EvaluationTemplate> findByStatut(TemplateStatus statut);
    
    List<EvaluationTemplate> findByTypeAndStatut(TemplateType type, TemplateStatus statut);
    
    List<EvaluationTemplate> findAllByActifTrue();
    
    @Query("SELECT t FROM EvaluationTemplate t LEFT JOIN FETCH t.questions WHERE t.actif = true")
    List<EvaluationTemplate> findAllByActifTrueWithQuestions();
    
    @Query("SELECT t FROM EvaluationTemplate t LEFT JOIN FETCH t.questions WHERE t.id = :id")
    Optional<EvaluationTemplate> findByIdWithQuestions(@Param("id") UUID id);
    
    @Query("SELECT t FROM EvaluationTemplate t LEFT JOIN FETCH t.questions WHERE t.type = :type")
    List<EvaluationTemplate> findByTypeWithQuestions(@Param("type") TemplateType type);
    
    @Query("SELECT t FROM EvaluationTemplate t LEFT JOIN FETCH t.questions WHERE t.statut = :statut")
    List<EvaluationTemplate> findByStatutWithQuestions(@Param("statut") TemplateStatus statut);
    
    @Query("SELECT t FROM EvaluationTemplate t LEFT JOIN FETCH t.questions WHERE t.type = :type AND t.statut = :statut")
    List<EvaluationTemplate> findByTypeAndStatutWithQuestions(@Param("type") TemplateType type, @Param("statut") TemplateStatus statut);
    
    List<EvaluationTemplate> findByRoleAndNiveauSeniorite(String role, String niveauSeniorite);

    @Query("""
            SELECT t FROM EvaluationTemplate t
            WHERE t.type = :type AND t.statut = :statut AND t.actif = true
              AND upper(t.niveauSeniorite) = upper(:niveau)
            ORDER BY t.creeLe DESC
            """)
    List<EvaluationTemplate> findPublishedByTypeAndNiveau(
            @Param("type") TemplateType type,
            @Param("statut") TemplateStatus statut,
            @Param("niveau") String niveau);

    @Query("""
            SELECT t FROM EvaluationTemplate t
            WHERE t.type = :type AND t.statut = :statut AND t.actif = true
              AND upper(t.niveauSeniorite) = upper(:niveau)
              AND (t.role IS NULL OR upper(t.role) = upper(:role))
            ORDER BY t.creeLe DESC
            """)
    List<EvaluationTemplate> findPublishedByTypeNiveauAndRole(
            @Param("type") TemplateType type,
            @Param("statut") TemplateStatus statut,
            @Param("niveau") String niveau,
            @Param("role") String role);

    @Query("""
            SELECT t FROM EvaluationTemplate t
            WHERE t.type = :type AND t.statut = :statut AND t.actif = true
              AND upper(coalesce(t.familleMetierCode, t.role)) = upper(:famille)
              AND upper(t.niveauSeniorite) = upper(:niveau)
            ORDER BY t.creeLe DESC
            """)
    List<EvaluationTemplate> findPublishedByTypeFamilleAndNiveau(
            @Param("type") TemplateType type,
            @Param("statut") TemplateStatus statut,
            @Param("famille") String famille,
            @Param("niveau") String niveau);

    @Query("""
            SELECT t FROM EvaluationTemplate t
            WHERE t.type = :type AND t.statut = :statut AND t.actif = true
              AND upper(coalesce(t.familleMetierCode, t.role)) = upper(:famille)
              AND (t.niveauSeniorite IS NULL OR t.niveauSeniorite = '')
            ORDER BY t.creeLe DESC
            """)
    List<EvaluationTemplate> findPublishedByTypeAndFamilleSansNiveau(
            @Param("type") TemplateType type,
            @Param("statut") TemplateStatus statut,
            @Param("famille") String famille);

    @Query("""
            SELECT t FROM EvaluationTemplate t
            WHERE t.type = :type AND t.statut = :statut AND t.actif = true
              AND upper(coalesce(t.familleMetierCode, t.role)) = upper(:famille)
            ORDER BY t.creeLe DESC
            """)
    List<EvaluationTemplate> findPublishedByTypeAndFamille(
            @Param("type") TemplateType type,
            @Param("statut") TemplateStatus statut,
            @Param("famille") String famille);
}
