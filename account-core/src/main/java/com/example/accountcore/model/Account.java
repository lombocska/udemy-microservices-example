package com.example.accountcore.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.csv.CSVRecord;

@Entity
@Table(name = "account")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;
    private String firstName;
    private String lastName;
    private String email;

    public static Account buildFromCsv(CSVRecord record) {
        return Account.builder()
                .id(Long.valueOf(record.get("id")))
                .firstName(record.get("first_name"))
                .lastName(record.get("last_name"))
                .email(record.get("email"))
                .build();
    }

}
