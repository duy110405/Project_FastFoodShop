package com.fastfood.service;

import com.fastfood.dto.response.AdminDashboardResponse;

import java.time.LocalDate;

public interface IAdminDashboardService {
    AdminDashboardResponse getDashboard(LocalDate fromDate, LocalDate toDate, int topN);
}

