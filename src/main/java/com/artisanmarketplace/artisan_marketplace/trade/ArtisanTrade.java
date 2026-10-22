package com.artisanmarketplace.artisan_marketplace.trade;

import com.artisanmarketplace.artisan_marketplace.artisan.ArtisanProfile;
import jakarta.persistence.*;
import lombok.Getter;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "artisan_trades")
@Getter
@IdClass(ArtisanTrade.ArtisanTradeId.class)
public class ArtisanTrade {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artisan_id", nullable = false)
    private ArtisanProfile artisan;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trade_id", nullable = false)
    private Trade trade;

    protected ArtisanTrade() {
        // required by JPA
    }

    public ArtisanTrade(ArtisanProfile artisan, Trade trade) {
        this.artisan = artisan;
        this.trade = trade;
    }

    // Composite primary key class
    public static class ArtisanTradeId implements Serializable {
        private UUID artisan;
        private UUID trade;

        public ArtisanTradeId() {}

        public ArtisanTradeId(UUID artisan, UUID trade) {
            this.artisan = artisan;
            this.trade = trade;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ArtisanTradeId that)) return false;
            return Objects.equals(artisan, that.artisan) && Objects.equals(trade, that.trade);
        }

        @Override
        public int hashCode() {
            return Objects.hash(artisan, trade);
        }
    }
}