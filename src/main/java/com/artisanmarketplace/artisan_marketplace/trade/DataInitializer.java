package com.artisanmarketplace.artisan_marketplace.trade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final TradeRepository tradeRepository;

    @Override
    public void run(String... args) {
        Arrays.stream(TradeName.values())
                .filter(name -> !tradeRepository.existsByName(name))
                .forEach(name -> {
                    tradeRepository.save(new Trade(name));
                    log.info("Seeded trade: {}", name);
                });
    }
}