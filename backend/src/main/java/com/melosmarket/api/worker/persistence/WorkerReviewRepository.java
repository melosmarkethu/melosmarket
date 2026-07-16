package com.melosmarket.api.worker.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkerReviewRepository extends JpaRepository<WorkerReviewEntity, Long> {

    Optional<WorkerReviewEntity> findByReviewerIdAndTargetId(Long reviewerId, Long targetId);
}
