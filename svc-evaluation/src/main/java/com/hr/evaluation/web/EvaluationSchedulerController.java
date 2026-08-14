package com.hr.evaluation.web;

import com.hr.evaluation.service.EvaluationSchedulerService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Declenchement manuel du cycle d'evaluations (tests / admin RH).
 */
@RestController
@RequestMapping("/api/rh/v1/evaluations/admin")
public class EvaluationSchedulerController {

    private final EvaluationSchedulerService schedulerService;

    public EvaluationSchedulerController(EvaluationSchedulerService schedulerService) {
        this.schedulerService = schedulerService;
    }

    @PostMapping("/trigger-evaluation-cycle")
    @PreAuthorize(EvaluationSecurityExpressions.BACKOFFICE_ECRITURE)
    public ResponseEntity<Map<String, String>> triggerEvaluationCycle() {
        schedulerService.triggerEvaluationCycle();

        return ResponseEntity.ok(Map.of(
            "status", "success",
            "message", "Evaluation cycle triggered successfully. Check logs for details."
        ));
    }
}
