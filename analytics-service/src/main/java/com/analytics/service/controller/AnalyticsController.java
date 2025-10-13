package com.analytics.service.controller;

import com.analytics.service.service.AnalyticalService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticalService analyticalService;

    public AnalyticsController(AnalyticalService analyticalService) {
        this.analyticalService = analyticalService;
    }

    @GetMapping("/get")
    public String getAnalytics(){
        return analyticalService.getCounters();
    }
}
