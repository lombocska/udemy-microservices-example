package com.example.accountcore.service;

import com.example.accountcore.dispatcher.KafkaDispatcher;
import com.example.accountcore.model.Account;
import com.example.accountcore.repository.AccountRepository;
import com.example.common.v1.account.AccountCreated;
import com.example.common.v1.account.AccountDomainEvent;
import com.example.common.v1.account.AccountEvent;
import jakarta.persistence.EntityNotFoundException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(transactionManager = "transactionManager")
public class AccountService {

    private final ResourceLoader resourceLoader;
    private final AccountRepository repository;
    private final KafkaDispatcher dispatcher;

    public void mockAccounts() {
        // fb, linkedin, twitter API stream usage would be here
        //as for the test example, we use mock data from MOCK_DATA.csv
        Resource resource = resourceLoader.getResource("classpath:MOCK_DATA.csv");
        try (BufferedReader br = new BufferedReader(new FileReader(resource.getFile())); CSVParser parser = CSVFormat.DEFAULT.withDelimiter(',').withHeader().parse(br)) {
            parser.stream()
                    .map(Account::buildFromCsv)
                    .forEach(account -> {
                        // in production, outboxing pattern should be here
                        Account acc = repository.save(account);
                        AccountDomainEvent<AccountCreated> accountCreatedDomainEvent = AccountDomainEvent.<AccountCreated>builder()
                                .id(UUID.randomUUID())
                                .type(AccountEvent.EventType.ACCOUNT_CREATED.getEventName())
                                .aggregateId(UUID.randomUUID().toString())
                                .created(Instant.now())
                                .source(AccountDomainEvent.SOURCE)
                                .payload(new AccountCreated(acc.getId(), acc.getLastName(), acc.getFirstName(), acc.getEmail()))
                                .build();
                        this.dispatcher.send(accountCreatedDomainEvent);
                    });
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    public MockAccountDTO getAccount(Long id) {
        return this.repository.findById(id)
                .map(acc -> new MockAccountDTO(acc.getId(), acc.getLastName(), acc.getFirstName()))
                .orElseThrow(() -> {throw new EntityNotFoundException("Account hasn't been found with id " + id);});
    }

    public MockAccountDTO getAccount(String email) {
        return this.repository.findAccountByEmail(email).map(acc -> new MockAccountDTO(acc.getId(), acc.getLastName(), acc.getFirstName()))
                .orElseThrow(() -> {
                    throw new EntityNotFoundException("Account entity with email " + email + " hasn't been found.");
                });
    }

    public record MockAccountDTO(Long id, String lastName, String firstName) {}
}
