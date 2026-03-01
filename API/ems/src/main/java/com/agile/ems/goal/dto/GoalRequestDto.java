package com.agile.ems.goal.dto;

import com.agile.ems.goal.enums.Quarter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GoalRequestDto {

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Quarter is required")
    private Quarter quarter;

    @NotNull(message = "Year is required")
    private Integer year;

    private Long employeeId;
}
