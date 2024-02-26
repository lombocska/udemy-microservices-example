package com.example.accountcore.model;

import com.example.common.v1.book.BookType;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "search-preference")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;
    private String title;
    private String email;
    @Embedded
    private Criteria criteria;

    public record Criteria(String author, BigDecimal minimumPrice, BigDecimal maximumPrice, BookType[] types) {
    }
}
