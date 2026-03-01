package com.agile.ems.user.dto;

import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PersonalDetailsRequestDto {
    private String phoneNumber;
    private String address;
    private LocalDate dateOfBirth;
}
