package com.myApp.ExpenseTracker.Dto;

public record AppInfoResponse(
        String latestVersion,
        String minimumSupportedVersion,
        boolean maintenance,
        String url
) {
}
