package com.db.awmd.challenge.service;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.exception.InvalidAccountException;
import com.db.awmd.challenge.exception.InvalidAmountException;
import com.db.awmd.challenge.exception.NotEnoughBalanceException;
import com.db.awmd.challenge.repository.AccountsRepository;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class AccountsService {

    @Getter
    private final AccountsRepository accountsRepository;

    private final NotificationService notificationService;

    @Autowired
    public AccountsService(AccountsRepository accountsRepository, NotificationService notificationService) {
        this.accountsRepository = accountsRepository;
        this.notificationService = notificationService;
    }

    public void createAccount(Account account) {
        this.accountsRepository.createAccount(account);
    }

    public Account getAccount(String accountId) throws InvalidAccountException{
        return accountsRepository.getAccount(accountId);
    }

    public void transfer(String fromAccountId, String toAccountId, BigDecimal amount) throws InvalidAmountException, NotEnoughBalanceException, InvalidAccountException {

        Account fromAccount = accountsRepository.getAccount(fromAccountId);
        Account toAccount = accountsRepository.getAccount(toAccountId);

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException(amount);
        }

        addToBalance(amount.negate(), fromAccount);
        // this will not throw NotEnoughBalanceException because amount is positive
        addToBalance(amount, toAccount);

        notificationService.notifyAboutTransfer(fromAccount, amount + " transferred to " + toAccount.getAccountId());
        notificationService.notifyAboutTransfer(toAccount, amount + " received from " + fromAccount.getAccountId());
    }

    /**
     * Calculate new balance and try to atomically set it. Retry until it works.
     * @param amount to be added
     * @param account the account
     * @throws NotEnoughBalanceException when amount is negative it may throw this exception if the balance is not enough
     */
    private void addToBalance(BigDecimal amount, Account account) throws NotEnoughBalanceException {
        BigDecimal originalBalance;
        BigDecimal newBalance;
        do {
            originalBalance = account.getBalance();
            newBalance = originalBalance.add(amount);
            if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                throw new NotEnoughBalanceException(account.getBalance(), amount);
            }
        } while (!account.tryToSet(originalBalance, newBalance));
    }
}
