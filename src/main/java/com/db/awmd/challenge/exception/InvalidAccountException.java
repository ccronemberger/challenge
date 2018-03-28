package com.db.awmd.challenge.exception;

public class InvalidAccountException extends Exception {
    public InvalidAccountException(String accountId) {
        super("invalid account " + accountId);
    }
}
