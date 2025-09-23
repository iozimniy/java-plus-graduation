package ru.practicum.request.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.request.constants.ParticipationRequestStatus;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParticipationRequestUpdateStatusDto {

    private ParticipationRequestStatus status;
    private List<Long> requestIds;
}
