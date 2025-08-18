package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import ru.practicum.client.StatsClient;
import ru.practicum.config.DateConfig;
import ru.practicum.dto.CreateEndpointHitDto;
import ru.practicum.dto.ManyEndPointDto;
import ru.practicum.dto.ReadEndpointHitDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RequiredArgsConstructor
@Slf4j
@EnableFeignClients(clients = {StatsClient.class})
@Component
public class ClientController {

    private final StatsClient statsClient;

    public ResponseEntity<Void> saveView(String addr, String uri) {
        log.info("ClientController.saveView addr {}, uri {}", addr, uri);
        CreateEndpointHitDto dto = new CreateEndpointHitDto(
                "ewm-main-service",
                uri,
                addr,
                LocalDateTime.now()
        );

        statsClient.saveHit(dto);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    public List<ReadEndpointHitDto> getHits(String start, String end, List<String> uris, boolean unique) {
        log.info("\nClientController.getHits start {}, end {}, \nuris {}, unique {}", start, end, uris, unique);

        Map<String, String> params = new HashMap<>();
        params.put("start", start);
        params.put("end", end);
        params.put("uris", String.join(",", uris));
        params.put("unique", String.valueOf(unique));
        // Выполняем запрос и получаем коллекцию объектов ReadEndpointHitDto
        ResponseEntity<Collection<ReadEndpointHitDto>> response
                = statsClient.getHits(
                LocalDateTime.parse(start, DateTimeFormatter.ofPattern(DateConfig.FORMAT)),
                LocalDateTime.parse(end, DateTimeFormatter.ofPattern(DateConfig.FORMAT)),
                Optional.of(uris), unique);

        List<ReadEndpointHitDto> respList = Optional.ofNullable(response.getBody())
                .map(ArrayList::new)
                .orElseGet(ArrayList::new);
        return respList;
    }

    public ResponseEntity<Void> saveHitsGroup(List<String> uris, String ip) {
        log.info("\nClientController.saveHitsGroup uris {}, addr {}", uris, ip);

        ManyEndPointDto manyEndPointDto = ManyEndPointDto.builder()
                .uris(uris)
                .ip(ip)
                .build();

        log.info("\nClientController.saveHitsGroup many {}", manyEndPointDto);

        statsClient.saveHitGroup(manyEndPointDto);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

}



