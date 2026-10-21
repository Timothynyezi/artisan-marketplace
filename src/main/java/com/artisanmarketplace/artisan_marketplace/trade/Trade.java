package com.artisanmarketplace.artisan_marketplace.trade;

import jakarta.persistance.*;
import lombok.Getter;

import java.util.UUID;

@Entity
@Table(name = "trades")
@Getter

public class Trade {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private TradeName name;

    protected TradeName() {
        // Required by JPA
    }

    public Trade(TradeName name) {
        this.name = name;
    }
}
