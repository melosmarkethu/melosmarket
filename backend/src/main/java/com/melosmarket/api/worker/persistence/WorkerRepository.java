package com.melosmarket.api.worker.persistence;

import java.util.Optional;

import com.melosmarket.api.worker.domain.WorkerTradeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface WorkerRepository extends JpaRepository<WorkerEntity, Long>, JpaSpecificationExecutor<WorkerEntity> {

    boolean existsByEmailIgnoreCase(String email);

    Optional<WorkerEntity> findByEmailIgnoreCase(String email);

    Optional<WorkerEntity> findByUserId(Long userId);
}
