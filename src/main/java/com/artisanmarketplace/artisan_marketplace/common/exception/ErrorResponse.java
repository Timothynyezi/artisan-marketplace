package com.artisanmarketplace.artisan_marketplace.common.exception;

import java.time.LocalDateTime;

public record ErrorResponse(String message, String errorCode, LocalDateTime timestamp) {}
