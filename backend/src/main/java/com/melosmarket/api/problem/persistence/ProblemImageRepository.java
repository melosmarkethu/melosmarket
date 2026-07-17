package com.melosmarket.api.problem.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProblemImageRepository extends JpaRepository<ProblemImageEntity, Long> {
    boolean existsByProblemIdAndId(Long problemId, Long id);
}
