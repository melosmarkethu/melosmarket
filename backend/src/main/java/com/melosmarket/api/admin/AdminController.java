package com.melosmarket.api.admin;

import java.util.List;

import com.melosmarket.api.generated.AdminApi;
import com.melosmarket.api.generated.model.Problem;
import com.melosmarket.api.generated.model.UpdateWorkerBadgesRequest;
import com.melosmarket.api.generated.model.Worker;
import com.melosmarket.api.problem.ProblemService;
import com.melosmarket.api.worker.WorkerService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminController implements AdminApi {

    private final WorkerService workerService;
    private final ProblemService problemService;

    public AdminController(WorkerService workerService, ProblemService problemService) {
        this.workerService = workerService;
        this.problemService = problemService;
    }

    @Override
    public ResponseEntity<List<Worker>> listAdminWorkers() {
        return ResponseEntity.ok(workerService.listAdminWorkers());
    }

    @Override
    public ResponseEntity<Void> deleteAdminWorker(Long workerId) {
        workerService.deleteAdminWorker(workerId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Worker> updateAdminWorkerBadges(
            Long workerId,
            UpdateWorkerBadgesRequest updateWorkerBadgesRequest) {
        return ResponseEntity.ok(workerService.updateAdminWorkerBadges(workerId, updateWorkerBadgesRequest));
    }

    @Override
    public ResponseEntity<List<Problem>> listAdminProblems() {
        return ResponseEntity.ok(problemService.listAdminProblems());
    }

    @Override
    public ResponseEntity<Void> deleteAdminProblem(Long problemId) {
        problemService.deleteAdminProblem(problemId);
        return ResponseEntity.noContent().build();
    }
}
