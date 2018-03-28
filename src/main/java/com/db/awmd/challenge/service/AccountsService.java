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
import java.util.ArrayList;
import java.util.List;

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

        List<Account> accounts = new ArrayList<>();
        accounts.add(fromAccount);
        accounts.add(toAccount);
        // need to sort to avoid deadlocks
        accounts.sort((a,b) -> a.getAccountId().compareTo(b.getAccountId()));

        // lock both accounts while operating on them
        synchronized (accounts.get(0)) {
            synchronized (accounts.get(1)) {
                BigDecimal newFromBalance = fromAccount.getBalance().add(amount.negate());

                if (newFromBalance.compareTo(BigDecimal.ZERO) < 0) {
                    throw new NotEnoughBalanceException(fromAccount.getBalance(), amount);
                }

                fromAccount.setBalance(newFromBalance);
                toAccount.setBalance(toAccount.getBalance().add(amount));

                notificationService.notifyAboutTransfer(fromAccount, amount + " transferred to " + toAccount.getAccountId());
                notificationService.notifyAboutTransfer(toAccount, amount + " received from " + fromAccount.getAccountId());
            }
        }
    }
}
