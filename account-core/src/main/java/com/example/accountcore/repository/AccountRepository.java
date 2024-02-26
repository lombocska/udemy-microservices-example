package com.example.accountcore.repository;

import com.example.accountcore.model.Account;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findAccountByEmail(String email);
}

