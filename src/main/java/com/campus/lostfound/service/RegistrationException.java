package com.campus.lostfound.service;

/**
 * Thrown when registration cannot be completed, for example a duplicate email.
 */
public class RegistrationException extends RuntimeException {

    public RegistrationException(String message) {
        super(message);
    }
}
