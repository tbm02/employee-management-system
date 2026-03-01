package com.agile.ems.departments.service;

import com.agile.ems.departments.dto.DepartmentRequestDto;
import com.agile.ems.departments.dto.DepartmentResponseDto;
import java.util.List;

public interface DepartmentService {
    DepartmentResponseDto create(DepartmentRequestDto requestDto);

    DepartmentResponseDto getById(Long id);

    List<DepartmentResponseDto> getAll();

    DepartmentResponseDto update(Long id, DepartmentRequestDto requestDto);

    void delete(Long id);
}
