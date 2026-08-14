package com.hr.evaluation.web;

import com.hr.evaluation.config.SecurityConfig;
import com.hr.evaluation.security.EvaluationAccessService;
import com.hr.evaluation.service.EvaluationCampaignService;
import com.hr.evaluation.service.EvaluationScoringService;
import com.hr.evaluation.service.EvaluationTemplateService;
import com.hr.evaluation.service.EvaluationWorkflowService;
import com.hr.evaluation.service.TechnicalTemplateService;
import com.hr.evaluation.service.TemplateService;
import com.hr.evaluation.repository.EvaluationCampaignRepository;
import com.hr.evaluation.repository.EvaluationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = EvaluationAdminController.class)
@Import(SecurityConfig.class)
class EvaluationAdminControllerSecurityTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private EvaluationCampaignService campaignService;
	@MockitoBean
	private EvaluationTemplateService templateService;
	@MockitoBean
	private TechnicalTemplateService technicalTemplateService;
	@MockitoBean
	private EvaluationWorkflowService workflowService;
	@MockitoBean
	private TemplateService enhancedTemplateService;
	@MockitoBean
	private EvaluationScoringService scoringService;
	@MockitoBean
	private EvaluationRepository evaluationRepository;
	@MockitoBean
	private EvaluationCampaignRepository campaignRepository;
	@MockitoBean
	private EvaluationAccessService evaluationAccess;
	@MockitoBean
	private JwtDecoder jwtDecoder;

	@Test
	void directionPeutLireCampagnes() throws Exception {
		when(campaignService.listerCampagnes(null)).thenReturn(List.of());

		mockMvc.perform(get("/api/rh/v1/admin/evaluations/campaigns")
						.with(jwt().authorities(
								new SimpleGrantedAuthority("ROLE_USER"),
								new SimpleGrantedAuthority("ROLE_DIRECTION"))))
				.andExpect(status().isOk());
	}

	@Test
	void directionNePeutPasCreerCampagne() throws Exception {
		mockMvc.perform(post("/api/rh/v1/admin/evaluations/campaigns")
						.contentType("application/json")
						.content("""
								{"nom":"C","description":"d","type":"ANNUELLE","annee":2026,"moisDebut":1,"moisFin":12,"creePar":"11111111-1111-1111-1111-111111111111"}
								""")
						.with(jwt().authorities(
								new SimpleGrantedAuthority("ROLE_USER"),
								new SimpleGrantedAuthority("ROLE_DIRECTION"))))
				.andExpect(status().isForbidden());
	}

	@Test
	void userSansBackofficeRefuseLectureAdmin() throws Exception {
		mockMvc.perform(get("/api/rh/v1/admin/evaluations/campaigns")
						.with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
				.andExpect(status().isForbidden());
	}

	@Test
	void roleFantomeCollaboratorRefuse() throws Exception {
		mockMvc.perform(get("/api/rh/v1/admin/evaluations/v2")
						.with(jwt().authorities(
								new SimpleGrantedAuthority("ROLE_USER"),
								new SimpleGrantedAuthority("ROLE_COLLABORATOR"))))
				.andExpect(status().isForbidden());
	}
}
