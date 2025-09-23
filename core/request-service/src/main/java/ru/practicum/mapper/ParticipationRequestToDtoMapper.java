package ru.practicum.mapper;

import ru.practicum.model.ParticipationRequest;
import ru.practicum.request.dto.ParticipationRequestDto;

public class ParticipationRequestToDtoMapper {

    public static ParticipationRequestDto mapToDto(ParticipationRequest request) {
        ParticipationRequestDto dto = new ParticipationRequestDto();
        dto.setId(request.getId());
        dto.setCreated(request.getCreated());
        dto.setStatus(request.getStatus());
        dto.setRequester(request.getUserId());
        dto.setEvent(request.getEventId());
        return dto;
    }

}
