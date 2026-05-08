package dev.synapse.core.repository;

import dev.synapse.core.domain.ModelProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ModelProviderRepository extends JpaRepository<ModelProvider, UUID> {
    
    Optional<ModelProvider> findByName(String name);
    
    List<ModelProvider> findByEnabledTrue();
    
    List<ModelProvider> findByType(ModelProvider.ProviderType type);
    
    boolean existsByName(String name);
}
