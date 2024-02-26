package com.example.accountcore.listener;

import com.example.accountcore.dispatcher.KafkaDispatcher;
import com.example.accountcore.service.AccountService;
import com.example.accountcore.service.SearchPreferenceService;
import com.example.accountcore.web.SearchPreferenceDTO;
import com.example.common.v1.notification.EmailTriggered;
import com.example.common.v1.notification.NotificationDomainEvent;
import com.example.common.v1.notification.NotificationEvent;
import com.example.common.v1.notification.NotificationType;
import com.example.common.v1.search_preference.SearchPreferenceDomainEvent;
import com.example.common.v1.search_preference.SearchPreferenceEvent;
import com.example.common.v1.search_preference.SearchPreferenceTriggered;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchPreferenceListener {

    private static final List<String> TRACKED = List.of(SearchPreferenceEvent.EventType.SEARCH_PREFERENCE_TRIGGERED.getEventName());
    private final ObjectMapper om;
    private final AccountService accountService;
    private final SearchPreferenceService searchPreferenceService;
    private final KafkaDispatcher dispatcher;

    @KafkaListener(topics = "${app.kafka.search-preference.topic}", clientIdPrefix = "${spring.kafka.consumer.client-id}-consumer")
    public void process(@Payload final byte[] payload) {
       Optional.ofNullable(payload)
               .ifPresentOrElse(event-> {
                   try {
                       var jsonTree = om.readTree(payload);
                       var shouldProcessEvent = isTracked(jsonTree);
                       if (!shouldProcessEvent) {return;}

                       deserialize(jsonTree).ifPresent(triggered -> {
                           String searchPreferenceId = triggered.getPayload().getId();
                           SearchPreferenceDTO searchPreference = searchPreferenceService.getSearchPreference(searchPreferenceId);
                           AccountService.MockAccountDTO account = accountService.getAccount(searchPreference.email());
                           NotificationDomainEvent<EmailTriggered> emailDomainEvent =
                                   NotificationDomainEvent.<EmailTriggered>builder()
                                   .id(UUID.randomUUID())
                                   .type(NotificationEvent.EventType.EMAIL_TRIGGERED.getEventName())
                                   .created(Instant.now())
                                   .source(NotificationDomainEvent.SOURCE)
                                   .payload(new EmailTriggered(account.lastName(), account.firstName(), searchPreference.email(), NotificationType.SEARCH_PREFERENCE_HIT))
                                   .correlationId(triggered.getCorrelationId()) //chaining domain events
                                   .build();
                           this.dispatcher.send(emailDomainEvent);
                       });
                   } catch (Exception e) {
                       log.error("Error has occurred: {}", e.getMessage());
                   }}, () -> {throw new IllegalArgumentException("Payload cannot be null");
               });
    }

    private Optional<SearchPreferenceDomainEvent<SearchPreferenceTriggered>> deserialize(final JsonNode payload) throws JsonProcessingException {
        JavaType javaType = om.constructType(new TypeReference<SearchPreferenceDomainEvent<SearchPreferenceTriggered>>() {});
        return Optional.of(om.treeToValue(payload, javaType));
    }

    private boolean isTracked(final JsonNode payload) {
        var eventType = payload.findValue("type").textValue();
        return TRACKED.contains(eventType);
    }
}
