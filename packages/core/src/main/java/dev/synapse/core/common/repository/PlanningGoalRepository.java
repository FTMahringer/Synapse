package dev.synapse.core.common.repository;

import dev.synapse.core.common.domain.PlanningGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PlanningGoalRepository extends JpaRepository<PlanningGoal, UUID> {
    List<PlanningGoal> findByTeamIdOrderByCreatedAtDesc(String teamId);
}
