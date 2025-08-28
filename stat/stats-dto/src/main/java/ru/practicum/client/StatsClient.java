package ru.practicum.client;

import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.config.DateConfig;
import ru.practicum.dto.CreateEndpointHitDto;
import ru.practicum.dto.ManyEndPointDto;
import ru.practicum.dto.ReadEndpointHitDto;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@FeignClient(name = "stats-server")
public interface StatsClient {
    @PostMapping("/hit")
    void saveHit(@Valid @RequestBody CreateEndpointHitDto dto);

    @GetMapping("/stats")
    ResponseEntity<Collection<ReadEndpointHitDto>> getHits(@RequestParam
                                                           @DateTimeFormat(pattern = DateConfig.FORMAT)
                                                           LocalDateTime start,
                                                           @RequestParam
                                                           @DateTimeFormat(pattern = DateConfig.FORMAT)
                                                           LocalDateTime end,
                                                           @RequestParam(required = false)
                                                           Optional<List<String>> uris,
                                                           @RequestParam(required = false, defaultValue = "false")
                                                           boolean unique);

    @PostMapping("/hit/group")
    void saveHitGroup(@RequestBody ManyEndPointDto many);
}
