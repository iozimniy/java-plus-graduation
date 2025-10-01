package ru.practicum.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.commons.config.DateConfig;
import ru.practicum.user.dto.UserShortDto;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class EventShortDto {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;
    private String annotation;
    private CategoryDto category;
    private int confirmedRequests;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateConfig.FORMAT)
    private String eventDate;
    private UserShortDto initiator;
    private boolean paid;
    private String title;
    private double rating;
}
