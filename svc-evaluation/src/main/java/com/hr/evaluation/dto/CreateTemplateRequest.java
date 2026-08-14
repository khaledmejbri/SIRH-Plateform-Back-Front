package com.hr.evaluation.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public class CreateTemplateRequest {

    @NotBlank(message = "Template name is required")
    private String nom;

    private String description;

    @NotNull(message = "Template type is required")
    private String type; // GENERIC or TECHNICAL

    @JsonProperty("niveau_seniorite")
    @JsonAlias({"niveauSeniorite"})
    private String niveauSeniorite;

    /** Must — clé matching TECHNICAL. */
    @JsonProperty("famille_metier_code")
    @JsonAlias({"familleMetierCode"})
    private String familleMetierCode;

    /** Legacy déprécié — mappé vers famille_metier_code si celle-ci absente. */
    @JsonAlias({"roleMetier"})
    private String role;

    private String domaine;

    @JsonProperty("cree_par")
    @JsonAlias({"creePar"})
    private UUID creePar;

    private List<CreateQuestionRequest> questions;

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getNiveauSeniorite() { return niveauSeniorite; }
    public void setNiveauSeniorite(String niveauSeniorite) { this.niveauSeniorite = niveauSeniorite; }

    public String getFamilleMetierCode() { return familleMetierCode; }
    public void setFamilleMetierCode(String familleMetierCode) { this.familleMetierCode = familleMetierCode; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getDomaine() { return domaine; }
    public void setDomaine(String domaine) { this.domaine = domaine; }

    public UUID getCreePar() { return creePar; }
    public void setCreePar(UUID creePar) { this.creePar = creePar; }

    public List<CreateQuestionRequest> getQuestions() { return questions; }
    public void setQuestions(List<CreateQuestionRequest> questions) { this.questions = questions; }

    /** Famille effective : champ Must, sinon alias legacy {@code role}. */
    public String resolveFamilleMetierCode() {
        if (familleMetierCode != null && !familleMetierCode.isBlank()) {
            return familleMetierCode.trim();
        }
        if (role != null && !role.isBlank()) {
            return role.trim();
        }
        return null;
    }
}
