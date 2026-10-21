package com.artisanmarketplace.artisan_marketplace.trade;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public class TradeRepository {
    public interface TradeRepository extends JpaRepository<Trade, UUID> {
    Optional<Trade> findByName(TradeName name);
    boolean existsByName(TradeName name);
}
}
