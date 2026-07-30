package com.melosmarket.api.problem;

import java.util.List;

import com.melosmarket.api.generated.ProblemsApi;
import com.melosmarket.api.generated.model.CreateProblemRequest;
import com.melosmarket.api.generated.model.County;
import com.melosmarket.api.generated.model.Problem;
import com.melosmarket.api.generated.model.ProblemImage;
import com.melosmarket.api.generated.model.ProblemStatus;
import com.melosmarket.api.generated.model.Trade;
import com.melosmarket.api.generated.model.UpdateProblemRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class ProblemsController implements ProblemsApi {

    private final ProblemService problemService;

    public ProblemsController(ProblemService problemService) {
        this.problemService = problemService;
    }

    @Override
    public ResponseEntity<Problem> createProblem(CreateProblemRequest createProblemRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(problemService.createProblem(createProblemRequest));
    }

    @Override
    public ResponseEntity<Problem> getProblem(Long problemId) {
        return ResponseEntity.ok(problemService.getProblem(problemId));
    }

    @Override
    public ResponseEntity<Problem> updateMyProblem(Long problemId, UpdateProblemRequest updateProblemRequest) {
        return ResponseEntity.ok(problemService.updateMyProblem(problemId, updateProblemRequest));
    }

    @Override
    public ResponseEntity<Void> deleteMyProblem(Long problemId) {
        problemService.deleteMyProblem(problemId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<List<Problem>> getMyProblems() {
        return ResponseEntity.ok(problemService.getMyProblems());
    }

    @Override
    public ResponseEntity<List<Problem>> searchProblems(Trade trade, ProblemStatus status, County county) {
        return ResponseEntity.ok(problemService.searchProblems(trade, status, county));
    }

    @Override
    public ResponseEntity<ProblemImage> uploadProblemPhoto(Long problemId, MultipartFile image, String title) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(problemService.uploadProblemPhoto(problemId, title, image));
    }

    @Override
    public ResponseEntity<Void> deleteProblemPhoto(Long problemId, Long imageId) {
        problemService.deleteProblemPhoto(problemId, imageId);
        return ResponseEntity.noContent().build();
    }
}
