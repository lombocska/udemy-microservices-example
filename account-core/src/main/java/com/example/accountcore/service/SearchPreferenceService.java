package com.example.accountcore.service;

import com.example.accountcore.dispatcher.KafkaDispatcher;
import com.example.accountcore.model.SearchPreference;
import com.example.accountcore.repository.SearchPreferenceRepository;
import com.example.accountcore.web.SearchPreferenceDTO;
import com.example.common.v1.account.AccountCreated;
import com.example.common.v1.account.AccountDomainEvent;
import com.example.common.v1.account.AccountEvent;
import com.example.common.v1.search_preference.SearchPreferenceCreated;
import com.example.common.v1.search_preference.SearchPreferenceDomainEvent;
import com.example.common.v1.search_preference.SearchPreferenceEvent;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(transactionManager = "transactionManager")
public class SearchPreferenceService {

    private final SearchPreferenceRepository repository;
    private final KafkaDispatcher dispatcher;

    public Long save(SearchPreferenceDTO dto) {
        SearchPreference searchPreference = SearchPreference.builder()
                .title(dto.title())
                .email(dto.email())
                .criteria(new SearchPreference.Criteria(dto.author(), dto.minimumPrice(), dto.maximumPrice(), dto.types()))
                .build();
        SearchPreference savedSP = this.repository.save(searchPreference);
        SearchPreference.Criteria criteria = savedSP.getCriteria();

        SearchPreferenceDomainEvent<SearchPreferenceCreated> domainEvent = SearchPreferenceDomainEvent.<SearchPreferenceCreated>builder()
                .id(UUID.randomUUID())
                .type(SearchPreferenceEvent.EventType.SEARCH_PREFERENCE_CREATED.getEventName())
                .aggregateId(UUID.randomUUID().toString())
                .created(Instant.now())
                .source(SearchPreferenceDomainEvent.SOURCE)
                .payload(new SearchPreferenceCreated(savedSP.getId().toString(), savedSP.getTitle(),
                        criteria.author(), criteria.minimumPrice(), criteria.maximumPrice(), criteria.types()))
                .build();

        this.dispatcher.send(domainEvent);
        return savedSP.getId();
    }

    public SearchPreferenceDTO getSearchPreference(String searchPreferenceId) {
        SearchPreference sp = this.repository.findById(Long.valueOf(searchPreferenceId))
                .orElseThrow(() -> {
                    throw new EntityNotFoundException("SearchPreference entity with id " + searchPreferenceId + " hasn't been found.");
                });
        return new SearchPreferenceDTO(sp.getTitle(), sp.getEmail(), sp.getCriteria().author(),
                sp.getCriteria().minimumPrice(), sp.getCriteria().maximumPrice(), sp.getCriteria().types());
    }
}
