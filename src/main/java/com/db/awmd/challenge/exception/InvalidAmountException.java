package com.db.awmd.challenge.exception;

import java.math.BigDecimal;

public class InvalidAmountException extends Exception {

    public InvalidAmountException(BigDecimal amount) {
        super("invalid amount: " + amount);
    }
}
