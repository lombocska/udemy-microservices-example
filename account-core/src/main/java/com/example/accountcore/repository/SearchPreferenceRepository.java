package com.example.accountcore.repository;

import com.example.accountcore.model.SearchPreference;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SearchPreferenceRepository extends JpaRepository<SearchPreference, Long> {
}

