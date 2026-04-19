package com.fastfood.controller;

import com.fastfood.dto.ApiResponse;
import com.fastfood.dto.response.AdminDashboardResponse;
import com.fastfood.service.IAdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final IAdminDashboardService adminDashboardService;

    @GetMapping("/dashboard")
    public ApiResponse<AdminDashboardResponse> getDashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false, defaultValue = "6") int topN
    ) {
        return ApiResponse.<AdminDashboardResponse>builder()
                .code(200)
                .message("Lấy dữ liệu dashboard admin thành công")
                .data(adminDashboardService.getDashboard(fromDate, toDate, topN))
                .build();
    }
}

