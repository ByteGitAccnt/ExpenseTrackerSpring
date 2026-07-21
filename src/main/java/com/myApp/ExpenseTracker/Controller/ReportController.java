package com.myApp.ExpenseTracker.Controller;

import com.myApp.ExpenseTracker.Service.CurrentUserProvider;
import com.myApp.ExpenseTracker.Service.ReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalTime;

@RestController
@RequestMapping("/api/Report")
public class ReportController {
    private final ReportService reportService;
    private final CurrentUserProvider currentUserProvider;
    private static final Logger logger = LoggerFactory.getLogger(ReportController.class);
    public ReportController(ReportService reportService, CurrentUserProvider currentUserProvider) {
        this.reportService = reportService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping("/pdf")
    public ResponseEntity<byte[]> generatePdfReport( @RequestParam LocalDate fromDate,
                                                     @RequestParam LocalDate toDate ) {
        logger.atInfo().log("Request for generating PDF report received for user {}", currentUserProvider.getCurrentUserId());
        Long userid = currentUserProvider.getCurrentUserId();
        byte[] report = reportService.generateReport(userid, fromDate, toDate);
        return ResponseEntity.ok().body(report);
    }
}
