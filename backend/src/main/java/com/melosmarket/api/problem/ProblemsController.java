package com.melosmarket.api.problem;

import java.util.List;

import com.melosmarket.api.generated.ProblemsApi;
import com.melosmarket.api.generated.model.CreateProblemRequest;
import com.melosmarket.api.generated.model.Problem;
import com.melosmarket.api.generated.model.ProblemImage;
import com.melosmarket.api.generated.model.ProblemStatus;
import com.melosmarket.api.generated.model.Trade;

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
    public ResponseEntity<List<Problem>> getMyProblems() {
        return ResponseEntity.ok(problemService.getMyProblems());
    }

    @Override
    public ResponseEntity<List<Problem>> searchProblems(Trade trade, ProblemStatus status, String location) {
        return ResponseEntity.ok(problemService.searchProblems(trade, status, location));
    }

    @Override
    public ResponseEntity<ProblemImage> uploadProblemPhoto(Long problemId, MultipartFile image, String title) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(problemService.uploadProblemPhoto(problemId, title, image));
    }
}
