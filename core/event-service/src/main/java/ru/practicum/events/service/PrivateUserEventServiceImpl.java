package ru.practicum.events.service;

import feign.FeignException;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.commons.config.DateConfig;
import ru.practicum.commons.errors.ForbiddenActionException;
import ru.practicum.event.constants.StateEvent;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.dto.UpdateEventUserRequest;
import ru.practicum.events.mapper.EventMapper;
import ru.practicum.events.model.Event;
import ru.practicum.events.repository.EventRepository;
import ru.practicum.request.client.ParticipationRequestClient;
import ru.practicum.request.constants.ParticipationRequestStatus;
import ru.practicum.request.constants.RequestUpdateStatus;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.dto.ParticipationRequestUpdateStatusDto;
import ru.practicum.user.client.UserClient;
import ru.practicum.user.dto.EventRequestStatusUpdateRequest;
import ru.practicum.user.dto.EventRequestStatusUpdateResult;
import ru.practicum.user.dto.GetUserEventsDto;
import ru.practicum.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PrivateUserEventServiceImpl implements PrivateUserEventService {
    private EventRepository eventRepository;
    private UserClient userClient;
    private CategoryRepository categoryRepository;
    private ParticipationRequestClient requestClient;

    @Override
    public List<EventShortDto> getUsersEvents(GetUserEventsDto dto) {
        UserDto user = userClient.getUser(dto.getUserId());
        PageRequest page = PageRequest.of(dto.getFrom() > 0 ? dto.getFrom() / dto.getSize() : 0, dto.getSize());
        return eventRepository.findAllByInitiatorId(user.getId(), page).stream()
                .map(EventMapper::toEventShortDto)
                .toList();
    }

    @Override
    public EventFullDto getUserEventById(Long userId, Long eventId) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found with id: " + eventId));
        return EventMapper.toEventFullDto(event);
    }

    @Override
    @Transactional
    public EventFullDto addNewEvent(UserDto user, NewEventDto eventDto) {
        Event event = EventMapper.dtoToEvent(eventDto, user);
        eventRepository.save(event);

        return EventMapper.toEventFullDto(event);
    }

    @Override
    @Transactional
    public EventFullDto updateUserEvent(UserDto user, Long eventId, UpdateEventUserRequest updateDto) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found with id: " + eventId));

        if (!Objects.equals(event.getInitiatorId(), user.getId())) {
            throw new ForbiddenActionException("User is not the event creator");
        }

        if (Objects.equals(event.getState(), StateEvent.PUBLISHED)) {
            throw new ForbiddenActionException("Changing of published event is forbidden.");
        }
        Optional.ofNullable(updateDto.getTitle()).ifPresent(event::setTitle);
        Optional.ofNullable(updateDto.getAnnotation()).ifPresent(event::setAnnotation);
        Optional.ofNullable(updateDto.getDescription()).ifPresent(event::setDescription);
        Optional.ofNullable(updateDto.getEventDate()).map(this::parseEventDate).ifPresent(event::setEventDate);

        if (updateDto.getLocation() != null) {
            event.setLocation(EventMapper.toLocation(updateDto.getLocation()));
        }

        if (updateDto.getCategory() != 0) {
            event.setCategory(categoryRepository.findById((long) updateDto.getCategory())
                    .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + updateDto.getCategory())));
        }

        updateEventState(event, updateDto.getStateAction());

        event.setRequestModeration(updateDto.isRequestModeration());
        event.setInitiatorId(user.getId());

        return EventMapper.toEventFullDto(eventRepository.save(event));
    }

    @Override
    public List<ParticipationRequestDto> getUserEventRequests(Long userId, Long eventId) {
        eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found with id: " + eventId + " for user " + userId));
        return requestClient.getRequestsByEventId(eventId);
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateUserEventRequest(UserDto user, Long eventId, EventRequestStatusUpdateRequest request) {
        Event event = getEventWithConfirmedRequests(eventId);

        List<ParticipationRequestDto> participation = requestClient.getRequestsByIds(request.getRequestIds());
        for (ParticipationRequestDto req : participation) {
            if (!req.getStatus().equals(ParticipationRequestStatus.PENDING)) {
                throw new ForbiddenActionException("request status should be PENDING");
            }
        }

        int partLimit = event.getParticipantLimit();
        int confPart = Objects.nonNull(event.getConfirmedRequests()) ? event.getConfirmedRequests() : 0;
        int diff = partLimit - confPart;

        if (diff >= request.getRequestIds().size()) {

            ParticipationRequestUpdateStatusDto requestUpdateStatusDto =
                    ParticipationRequestUpdateStatusDto.builder()
                            .status(ParticipationRequestStatus.valueOf(request.getStatus()))
                            .requestIds(request.getRequestIds())
                            .build();

            requestClient.updateStatusByIds(requestUpdateStatusDto);
            if (RequestUpdateStatus.valueOf(request.getStatus()).equals(RequestUpdateStatus.CONFIRMED)) {

                for (ParticipationRequestDto req : participation) {
                    req.setStatus(ParticipationRequestStatus.CONFIRMED);
                }

                return EventRequestStatusUpdateResult.builder()
                        .confirmedRequests(participation)
                        .build();
            } else {
                for (ParticipationRequestDto req : participation) {
                    req.setStatus(ParticipationRequestStatus.REJECTED);
                }

                return EventRequestStatusUpdateResult.builder()
                        .rejectedRequests(participation)
                        .build();
            }
        } else if (diff == 0) {
            throw new ForbiddenActionException("Participation limit is 0");
        } else {
            List<Long> confirmed = new ArrayList<>();
            List<Long> rejected = new ArrayList<>();
            for (int i = 1; i <= request.getRequestIds().size(); i++) {
                if (i > diff) {
                    rejected.add(request.getRequestIds().get(i));
                } else confirmed.add(request.getRequestIds().get(i));
            }

            ParticipationRequestUpdateStatusDto updateStatusDtoForConfirmed =
                    ParticipationRequestUpdateStatusDto.builder()
                            .status(ParticipationRequestStatus.CONFIRMED)
                            .requestIds(confirmed)
                            .build();
            ParticipationRequestUpdateStatusDto updateStatusDtoForRejected =
                    ParticipationRequestUpdateStatusDto.builder()
                            .status(ParticipationRequestStatus.REJECTED)
                            .requestIds(rejected)
                            .build();

            requestClient.updateStatusByIds(updateStatusDtoForConfirmed);
            requestClient.updateStatusByIds(updateStatusDtoForRejected);

            EventRequestStatusUpdateResult res = new EventRequestStatusUpdateResult();

            for (ParticipationRequestDto req : participation) {
                for (Long id : confirmed) {
                    if (Objects.equals(req.getId(), id)) {
                        req.setStatus(ParticipationRequestStatus.CONFIRMED);
                    }
                }
                req.setStatus(ParticipationRequestStatus.CANCELED);
            }

            List<ParticipationRequestDto> updatedRequestsConfirmed = participation.stream()
                    .filter(req -> confirmed.contains(req.getId()))
                    .peek(req -> req.setStatus(ParticipationRequestStatus.CONFIRMED))
                    .toList();

            List<ParticipationRequestDto> updatedRequestsRejected = participation.stream()
                    .filter(req -> rejected.contains(req.getId()))
                    .peek(req -> req.setStatus(ParticipationRequestStatus.REJECTED))
                    .toList();

            res.setConfirmedRequests(updatedRequestsConfirmed);
            res.setRejectedRequests(updatedRequestsRejected);

            return res;
        }
    }

    private LocalDateTime parseEventDate(String date) {
        return LocalDateTime.parse(date, DateConfig.FORMATTER);
    }

    private void updateEventState(Event event, String stateAction) {
        if (stateAction == null) return;

        if ("PUBLISH_REVIEW".equals(stateAction)) {
            throw new ForbiddenActionException("Publishing this event is forbidden.");
        }

        switch (stateAction) {
            case "CANCEL_REVIEW":
                event.setState(StateEvent.CANCELED);
                break;
            case "SEND_TO_REVIEW":
                event.setState(StateEvent.PENDING);
                break;
            default:
                throw new IllegalArgumentException("Invalid state action: " + stateAction);
        }
    }

    public Event getEventWithConfirmedRequests(Long eventId) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event with id=" + eventId + " was not found"));

        Integer confirmedCount = null;

        try {
            confirmedCount = requestClient.getConfirmedRequestsCount(eventId);
        } catch (FeignException e) {
            log.error("ParticipationRequestClient error: getConfirmedRequestsCount(eventId) with {}", e);
        }

        event.setConfirmedRequests(confirmedCount);

        return event;
    }
}
