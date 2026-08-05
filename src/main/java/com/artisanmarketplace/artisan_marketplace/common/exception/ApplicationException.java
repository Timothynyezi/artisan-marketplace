package com.artisanmarketplace.artisan_marketplace.common.exception;

public abstract class ApplicationException extends RuntimeException {
    public ApplicationException(String message) {
        super(message);
    }
}
