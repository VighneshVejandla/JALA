package com.jala.backend.dashboard.service;

import com.jala.backend.dashboard.dto.response.PondDashboardResponse;

import java.util.UUID;

public interface PondDashboardService {

    PondDashboardResponse getDashboard(
            UUID pondId);

    
}