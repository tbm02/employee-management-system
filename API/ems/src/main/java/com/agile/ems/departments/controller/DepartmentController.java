package com.agile.ems.departments.controller;

import com.agile.ems.departments.dto.DepartmentRequestDto;
import com.agile.ems.departments.dto.DepartmentResponseDto;
import com.agile.ems.departments.service.DepartmentService;
import com.agile.ems.utils.ApiResponseDto;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @PostMapping
    public ResponseEntity<ApiResponseDto<DepartmentResponseDto>> create(@Valid @RequestBody DepartmentRequestDto requestDto) {
        DepartmentResponseDto data = departmentService.create(requestDto);
        ApiResponseDto<DepartmentResponseDto> response = ApiResponseDto.success(
                HttpStatus.CREATED.value(),
                "Department created successfully",
                data
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<DepartmentResponseDto>> getById(@PathVariable Long id) {
        DepartmentResponseDto data = departmentService.getById(id);
        ApiResponseDto<DepartmentResponseDto> response = ApiResponseDto.success(
                HttpStatus.OK.value(),
                "Department fetched successfully",
                data
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<ApiResponseDto<List<DepartmentResponseDto>>> getAll() {
        List<DepartmentResponseDto> data = departmentService.getAll();
        ApiResponseDto<List<DepartmentResponseDto>> response = ApiResponseDto.success(
                HttpStatus.OK.value(),
                "Departments fetched successfully",
                data
        );
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDto<DepartmentResponseDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody DepartmentRequestDto requestDto
    ) {
        DepartmentResponseDto data = departmentService.update(id, requestDto);
        ApiResponseDto<DepartmentResponseDto> response = ApiResponseDto.success(
                HttpStatus.OK.value(),
                "Department updated successfully",
                data
        );
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDto<Object>> delete(@PathVariable Long id) {
        departmentService.delete(id);
        ApiResponseDto<Object> response = ApiResponseDto.success(
                HttpStatus.OK.value(),
                "Department deleted successfully",
                null
        );
        return ResponseEntity.ok(response);
    }
}
