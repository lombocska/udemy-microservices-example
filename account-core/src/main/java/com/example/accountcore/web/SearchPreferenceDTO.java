package com.example.accountcore.web;

import com.example.common.v1.book.BookType;
import java.math.BigDecimal;

public record SearchPreferenceDTO(String title, String email, String author, BigDecimal minimumPrice, BigDecimal maximumPrice, BookType[] types) {
}
