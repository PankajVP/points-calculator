package com.anymind.points.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class DateRangeInput {
    @Id @JsonProperty("from") private ZonedDateTime from;
    @JsonProperty("to") private ZonedDateTime to;

    public DateRangeInput(DateRangeInput dateRangeInput) {
        from = Objects.requireNonNull(from);
        to = Objects.requireNonNull(to);
    }
}
