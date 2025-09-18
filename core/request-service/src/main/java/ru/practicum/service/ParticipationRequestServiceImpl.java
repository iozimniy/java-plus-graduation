package ru.practicum.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.event.client.EventClient;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.mapper.ParticipationRequestToDtoMapper;
import ru.practicum.model.ParticipationRequest;
import ru.practicum.repository.ParticipationRequestRepository;
import ru.practicum.request.constants.ParticipationRequestStatus;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.dto.ParticipationRequestUpdateStatusDto;
import ru.practicum.user.client.UserClient;
import ru.practicum.user.dto.UserDto;
import ru.practicum.validation.ParticipationRequestValidator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ParticipationRequestServiceImpl implements ParticipationRequestService {

    private final ParticipationRequestRepository requestRepository;
    private final EventClient eventClient;

    private final UserClient userClient;

    private final ParticipationRequestValidator participationRequestValidator;

    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        userClient.getUser(userId);
        return requestRepository.findByUserId(userId)
                .stream()
                .map(ParticipationRequestToDtoMapper::mapToDto)
                .toList();
    }

    public int getConfirmedRequests(Long eventId) {
        return requestRepository
                .countConfirmedRequestsByStatusAndEventId(ParticipationRequestStatus.CONFIRMED, eventId);
    }

    @Transactional
    public ParticipationRequestDto addParticipationRequest(Long userId, Long eventId) {
        log.info("Request for add ParticipationRequest with user id {} and event id {}", userId, eventId);
        UserDto userDto = null;

        try {
            userDto = userClient.getUser(userId);
        } catch (Exception e) {
            log.error("Request for get user with id {} to userClient is failed with message {}", userId, e);
        }


        EventFullDto event = null;

        try {
           event = eventClient.getEventById(eventId);
           log.debug("Event from EventClient: {}", event);
        } catch (Exception e) {
            log.error("Request for get event with id {} to eventClient is failed with message {}", eventId, e);
        }

        long confirmedRequestsCount = getConfirmedRequests(eventId);

        RuntimeException validationError =
                participationRequestValidator.checkRequest(userDto, event, confirmedRequestsCount);

        if (validationError != null)
            throw validationError;

        ParticipationRequest request = ParticipationRequest.builder()
                .userId(userDto.getId())
                .eventId(eventId)
                .build();

        if (event.getParticipantLimit() == 0) {
            request.setStatus(ParticipationRequestStatus.CONFIRMED);
        } else {
            request.setStatus(event.isRequestModeration() ? ParticipationRequestStatus.PENDING : ParticipationRequestStatus.CONFIRMED);
        }

        request.setCreated(LocalDateTime.now());
        log.debug("New ParticipationRequest for create: {}", request);


        ParticipationRequest savedRequest = requestRepository.save(request);
        return ParticipationRequestToDtoMapper.mapToDto(savedRequest);
    }

    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        ParticipationRequest request = requestRepository.findByIdAndUserId(requestId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Request with id=" + requestId + " was not found"));

        request.setStatus(ParticipationRequestStatus.CANCELED);
        requestRepository.save(request);

        return ParticipationRequestToDtoMapper.mapToDto(request);
    }


    public List<ParticipationRequestDto> getUserRequestsByEventId(Long eventId) {
        return requestRepository.findByEventId(eventId).stream()
                .map(request -> ParticipationRequestToDtoMapper.mapToDto(request))
                .toList();
    }

    public List<ParticipationRequestDto> getRequestsByIds(List<Long> ids) {
        return requestRepository.findByIds(ids).stream()
                .map(request -> ParticipationRequestToDtoMapper.mapToDto(request))
                .toList();
    }

    @Transactional
    public void updateStatusByIds(ParticipationRequestUpdateStatusDto requestUpdateStatusDto) {
        requestRepository.updateStatusByIds(requestUpdateStatusDto.getStatus(), requestUpdateStatusDto.getRequestIds());
    }

    public Map<Long, Integer> getConfirmedRequestsForList(List<Long> ids) {
        return ids.stream().collect(Collectors.toMap(
                s -> s,
                s -> getConfirmedRequests(s)
        ));
    }
}