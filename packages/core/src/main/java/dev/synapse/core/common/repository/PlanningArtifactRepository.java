package dev.synapse.core.common.repository;

import dev.synapse.core.common.domain.PlanningArtifact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlanningArtifactRepository extends JpaRepository<PlanningArtifact, UUID> {
    List<PlanningArtifact> findByGoalIdOrderByPlanVersionDesc(UUID goalId);
    Optional<PlanningArtifact> findFirstByGoalIdAndStatusOrderByPlanVersionDesc(UUID goalId, PlanningArtifact.PlanStatus status);
}
