package com.artisanmarketplace.artisan_marketplace.artisan;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository

public interface ArtisanProfileRepository extends JpaRepository<ArtisanProfile, UUID>{
    Optional<ArtisanProfile> findByUserId(UUID userId);

    boolean existsBYUserId(UUID userId);
}
