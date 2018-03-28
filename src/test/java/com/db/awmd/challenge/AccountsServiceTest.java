package com.db.awmd.challenge;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.exception.InvalidAccountException;
import com.db.awmd.challenge.exception.InvalidAmountException;
import com.db.awmd.challenge.exception.NotEnoughBalanceException;
import com.db.awmd.challenge.repository.AccountsRepositoryInMemory;
import com.db.awmd.challenge.service.AccountsService;
import com.db.awmd.challenge.service.NotificationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {AccountsService.class, AccountsRepositoryInMemory.class })
public class AccountsServiceTest {

    @Autowired
    private AccountsService accountsService;

    // used by AccountsService
    @MockBean
    private NotificationService notificationService;

    @Before
    public void prepareMockMvc() {
        // Reset the existing accounts before each test.
        accountsService.getAccountsRepository().clearAccounts();
    }

    @Test
    public void addAccount() throws Exception {
        Account account = new Account("Id-123");
        account.setBalance(new BigDecimal(1000));
        this.accountsService.createAccount(account);

        assertThat(accountsService.getAccount("Id-123")).isEqualTo(account);
    }

    @Test
    public void addAccount_failsOnDuplicateId() throws Exception {
        String uniqueId = "Id-" + System.currentTimeMillis();
        Account account = new Account(uniqueId);
        accountsService.createAccount(account);

        try {
            accountsService.createAccount(account);
            fail("Should have failed when adding duplicate account");
        } catch (DuplicateAccountIdException ex) {
            assertThat(ex.getMessage()).isEqualTo("Account id " + uniqueId + " already exists!");
        }
    }

    @Test
    public void transfersWithInvalidAccount() throws Exception {
        try {
            accountsService.transfer("a", "b", BigDecimal.ZERO);
            fail("Should have failed with the invalid accounts");
        } catch (InvalidAccountException ex) {
            // this is the expected outcome
        }

        Account account = new Account("Id-123", BigDecimal.TEN);
        try {
            accountsService.transfer(account.getAccountId(), "b", BigDecimal.ZERO);
            fail("Should have failed with the invalid accounts");
        } catch (InvalidAccountException ex) {
            // this is the expected outcome
        }
    }

    @Test
    public void transfersWithInvalidAmount() throws Exception {
        Account fromAccount = new Account("Id-123", BigDecimal.TEN);
        accountsService.createAccount(fromAccount);
        Account toAccount = new Account("Id-432", BigDecimal.TEN);
        accountsService.createAccount(toAccount);
        try {
            accountsService.transfer(fromAccount.getAccountId(), toAccount.getAccountId(), BigDecimal.ZERO);
            fail("Should have failed with the invalid amount");
        } catch (InvalidAmountException ex) {
            // this is the expected outcome
        }
        try {
            accountsService.transfer(fromAccount.getAccountId(), toAccount.getAccountId(), BigDecimal.ONE.negate());
            fail("Should have failed with the invalid amount");
        } catch (InvalidAmountException ex) {
            // this is the expected outcome
        }

        try {
            accountsService.transfer(fromAccount.getAccountId(), toAccount.getAccountId(), BigDecimal.ONE);
            accountsService.transfer(fromAccount.getAccountId(), toAccount.getAccountId(), BigDecimal.TEN);
            fail("Should have failed with not enough balance");
        } catch (NotEnoughBalanceException ex) {
            // this is the expected outcome
        }
    }

    @Test
    public void successfulTransfer() throws Exception {
        Account fromAccount = new Account("Id-123", BigDecimal.TEN);
        accountsService.createAccount(fromAccount);
        Account toAccount = new Account("Id-432", BigDecimal.TEN);
        accountsService.createAccount(toAccount);

        accountsService.transfer(fromAccount.getAccountId(), toAccount.getAccountId(), BigDecimal.ONE);
        assertEquals(fromAccount.getBalance(),BigDecimal.valueOf(9));
        assertEquals(toAccount.getBalance(),BigDecimal.valueOf(11));
    }
}
