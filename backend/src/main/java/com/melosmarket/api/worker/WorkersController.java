package com.melosmarket.api.worker;

import java.util.List;

import com.melosmarket.api.generated.WorkersApi;
import com.melosmarket.api.generated.model.CreateWorkerRequest;
import com.melosmarket.api.generated.model.County;
import com.melosmarket.api.generated.model.CreateWorkerReviewRequest;
import com.melosmarket.api.generated.model.Trade;
import com.melosmarket.api.generated.model.UpdateWorkerProfileRequest;
import com.melosmarket.api.generated.model.Worker;
import com.melosmarket.api.generated.model.WorkerReferenceImage;
import com.melosmarket.api.generated.model.WorkerReview;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class WorkersController implements WorkersApi {

    private final WorkerService workerService;

    public WorkersController(WorkerService workerService) {
        this.workerService = workerService;
    }

    @Override
    public ResponseEntity<Worker> createWorker(CreateWorkerRequest createWorkerRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(workerService.createWorker(createWorkerRequest));
    }

    @Override
    public ResponseEntity<Worker> getWorker(Long workerId) {
        return ResponseEntity.ok(workerService.getWorker(workerId));
    }

    @Override
    public ResponseEntity<Worker> getMyWorkerProfile() {
        return ResponseEntity.ok(workerService.getMyWorkerProfile());
    }

    @Override
    public ResponseEntity<List<Worker>> searchWorkers(Trade trade, County county) {
        return ResponseEntity.ok(workerService.searchWorkers(trade, county));
    }

    @Override
    public ResponseEntity<Long> getWorkerCount() {
        return ResponseEntity.ok(workerService.countWorkers());
    }

    @Override
    public ResponseEntity<Worker> updateMyWorkerProfile(UpdateWorkerProfileRequest updateWorkerProfileRequest) {
        return ResponseEntity.ok(workerService.updateMyWorkerProfile(updateWorkerProfileRequest));
    }

    @Override
    public ResponseEntity<Worker> uploadMyWorkerProfileImage(MultipartFile image) {
        return ResponseEntity.ok(workerService.uploadMyWorkerProfileImage(image));
    }

    @Override
    public ResponseEntity<WorkerReferenceImage> uploadMyWorkerReference(MultipartFile image, String title) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(workerService.uploadMyWorkerReference(title, image));
    }

    @Override
    public ResponseEntity<Void> deleteMyWorkerReference(Long referenceId) {
        workerService.deleteMyWorkerReference(referenceId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<WorkerReview> createWorkerReview(Long workerId, CreateWorkerReviewRequest createWorkerReviewRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(workerService.createWorkerReview(workerId, createWorkerReviewRequest));
    }
}
