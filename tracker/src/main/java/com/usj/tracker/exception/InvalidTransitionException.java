package com.usj.tracker.exception;

/**
 * Thrown by Application#transitionTo() when the requested status is not
 * in the current object's nextValidStates(). Kept as an unchecked exception
 * so callers aren't forced to try/catch on every single call - the REST
 * layer catches it once, centrally, in GlobalExceptionHandler.
 */
public class InvalidTransitionException extends RuntimeException {
    public InvalidTransitionException(String message) {
        super(message);
    }
}
