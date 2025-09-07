package ru.practicum.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.event.client.EventClient;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.mapper.ParticipationRequestToDtoMapper;
import ru.practicum.model.ParticipationRequest;
import ru.practicum.repository.ParticipationRequestRepository;
import ru.practicum.request.constants.ParticipationRequestStatus;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.user.client.UserClient;
import ru.practicum.user.dto.UserDto;
import ru.practicum.validation.ParticipationRequestValidator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ParticipationRequestService {

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

    public int getConfirmedRequests(long eventId) {
        return requestRepository
                .countConfirmedRequestsByStatusAndEventId(ParticipationRequestStatus.CONFIRMED, eventId);
    }

    @Transactional
    public ParticipationRequestDto addParticipationRequest(Long userId, Long eventId) {
        UserDto userDto = userClient.getUser(userId);
        EventFullDto event = eventClient.getEventById(eventId);
        long confirmedRequestsCount = getConfirmedRequests(eventId);

        RuntimeException validationError =
                participationRequestValidator.checkRequest(userDto, event, confirmedRequestsCount);

        if (validationError != null)
            throw validationError;

        ParticipationRequest request = new ParticipationRequest();
        request.setUserId(userDto.getId());
        request.setEventId(event.getId());
        if (event.getParticipantLimit() == 0) {
            request.setStatus(ParticipationRequestStatus.CONFIRMED);
        } else {
            request.setStatus(event.isRequestModeration() ? ParticipationRequestStatus.PENDING : ParticipationRequestStatus.CONFIRMED);
        }
        request.setCreated(LocalDateTime.now());


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


    public List<ParticipationRequestDto> getUserRequestsByEventId(Long userId, Long eventId) {
        return requestRepository.findByEventId(eventId).stream()
                .map(request -> ParticipationRequestToDtoMapper.mapToDto(request))
                .toList();
    }

    public List<ParticipationRequestDto> getRequestsByIds(List<Long> ids) {
        return requestRepository.findByIds(ids).stream()
                .map(request -> ParticipationRequestToDtoMapper.mapToDto(request))
                .toList();
    }

    public void updateStatusByIds(ParticipationRequestStatus status, List<Long> ids) {
        requestRepository.updateStatusByIds(status, ids);
    }

    public Map<Long, Integer> getConfirmedRequestsForList(List<Long> ids) {
        return ids.stream().collect(Collectors.toMap(
                s -> s,
                s -> getConfirmedRequests(s)
        ));
    }
}