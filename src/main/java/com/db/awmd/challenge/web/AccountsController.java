package com.db.awmd.challenge.web;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.exception.InvalidAccountException;
import com.db.awmd.challenge.exception.InvalidAmountException;
import com.db.awmd.challenge.exception.NotEnoughBalanceException;
import com.db.awmd.challenge.service.AccountsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;

@RestController
@RequestMapping("/v1/accounts")
@Slf4j
public class AccountsController {

    private final AccountsService accountsService;

    @Autowired
    public AccountsController(AccountsService accountsService) {
        this.accountsService = accountsService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> createAccount(@RequestBody @Valid Account account) {
        log.info("Creating account {}", account);

        try {
            this.accountsService.createAccount(account);
        } catch (DuplicateAccountIdException daie) {
            return new ResponseEntity<>(daie.getMessage(), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping(path = "/{accountId}")
    public ResponseEntity getAccount(@PathVariable String accountId) {
        log.info("Retrieving account for id {}", accountId);
        try {
            return new ResponseEntity(accountsService.getAccount(accountId), HttpStatus.OK);
        } catch (InvalidAccountException e) {
            log.error("error", e);
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, path = "/{fromAccountId}/{toAccountId}/{amount}")
    public ResponseEntity transfer(@PathVariable String fromAccountId, @PathVariable String toAccountId,
                                   @PathVariable BigDecimal amount) {
        log.info("Transferring " + amount + " from " + fromAccountId + " to " + toAccountId);
        try {
            accountsService.transfer(fromAccountId, toAccountId, amount);
            return new ResponseEntity(HttpStatus.OK);
        } catch (InvalidAmountException | NotEnoughBalanceException | InvalidAccountException e) {
            log.error("error", e);
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
