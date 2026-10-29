package com.artisanmarketplace.artisan_marketplace.common.exception;

public class AccessDeniedException extends org.springframework.security.access.AccessDeniedException {
    public AccessDeniedException(String message) {
        super(message);
    }
}