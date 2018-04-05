package com.db.awmd.challenge.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicReference;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

@Data
public class Account {

    @NotNull
    @NotEmpty
    private final String accountId;

    private AtomicReference<BigDecimal> balance;

    public void setBalance(BigDecimal balance) {
        this.balance = new AtomicReference<>(balance);
    }

    @NotNull
    @Min(value = 0, message = "Initial balance must be positive.")
    public BigDecimal getBalance() {
        return balance.get();
    }

    /**
     * This is used to update the value atomically, because we don't want to use locks that would have lower performance
     * (but also a simpler code).
     * @param expectedCurrentBalance
     * @param newBalance
     * @return returns false if the current value is not the expected one, so the operation is not performed
     */
    public boolean tryToSet(BigDecimal expectedCurrentBalance, BigDecimal newBalance) {
        return balance.compareAndSet(expectedCurrentBalance, newBalance);
    }

    public Account(String accountId) {
        this.accountId = accountId;
        this.balance = new AtomicReference<>(BigDecimal.ZERO);
    }

    @JsonCreator
    public Account(@JsonProperty("accountId") String accountId,
                   @JsonProperty("balance") BigDecimal balance) {
        this.accountId = accountId;
        this.balance = new AtomicReference<>(balance);
    }
}
