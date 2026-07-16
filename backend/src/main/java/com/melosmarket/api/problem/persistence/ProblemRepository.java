package com.melosmarket.api.problem.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProblemRepository extends JpaRepository<ProblemEntity, Long>, JpaSpecificationExecutor<ProblemEntity> {

    List<ProblemEntity> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
}
