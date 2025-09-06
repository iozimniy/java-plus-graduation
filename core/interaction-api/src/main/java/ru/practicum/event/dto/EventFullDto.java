package ru.practicum.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import ru.practicum.commons.config.DateConfig;
import ru.practicum.event.model.Location;
import ru.practicum.event.constants.StateEvent;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class EventFullDto extends EventShortDto {
    private String createdOn;
    private String description;
    private Location location;
    private int participantLimit;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateConfig.FORMAT)
    private String publishedOn;
    private boolean requestModeration;
    private StateEvent state;
}
