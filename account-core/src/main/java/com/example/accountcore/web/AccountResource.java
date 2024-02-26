package com.example.accountcore.web;

import com.example.accountcore.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/account")
@RequiredArgsConstructor
public class AccountResource {

    private final AccountService accountService;

    @PostMapping("/create-mock-accounts")
    public ResponseEntity index() {
        this.accountService.mockAccounts();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountService.MockAccountDTO> getAccount(@PathVariable("id") Long id) {
        AccountService.MockAccountDTO dto = this.accountService.getAccount(id);
        return ResponseEntity.ok().body(dto);
    }
}
