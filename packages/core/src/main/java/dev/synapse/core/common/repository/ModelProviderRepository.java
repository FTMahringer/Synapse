package dev.synapse.core.common.repository;

import dev.synapse.core.common.domain.ModelProvider;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ModelProviderRepository extends JpaRepository<ModelProvider, UUID> {
    
    Optional<ModelProvider> findByName(String name);
    
    List<ModelProvider> findByEnabledTrue();
    List<ModelProvider> findByEnabledTrue(Pageable pageable);
    Optional<ModelProvider> findFirstByTypeAndEnabledTrueOrderByCreatedAtAsc(ModelProvider.ProviderType type);
    
    List<ModelProvider> findByType(ModelProvider.ProviderType type);
    
    boolean existsByName(String name);
}
