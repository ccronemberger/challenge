package com.db.awmd.challenge.exception;

import java.math.BigDecimal;

public class NotEnoughBalanceException extends Exception {

    public NotEnoughBalanceException(BigDecimal currentBalance, BigDecimal amount) {
        super("current balance is " + currentBalance + ", so this amount can't be debited: " + amount);
    }
}
