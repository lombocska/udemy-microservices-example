package com.example.accountcore.web;

import com.example.accountcore.service.SearchPreferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/search-preference")
@RequiredArgsConstructor
public class SearchPreferenceResource {

    private final SearchPreferenceService service;

    @PostMapping
    public ResponseEntity<Long> save(@RequestBody SearchPreferenceDTO dto) {
        this.service.save(dto);
        return ResponseEntity.ok().build();
    }

}
