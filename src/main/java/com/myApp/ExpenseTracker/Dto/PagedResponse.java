package com.myApp.ExpenseTracker.Dto;

import java.util.List;

public record PagedResponse<T>(
        List<T> content,
        int pageNumber,
        int pageSize,
        long totalElements,
        int totalPages,
        boolean hasNext,      // Flag indicating if a next page exists, if true not last
        boolean hasPrevious   // Flag indicating if a previous page exists, if true not first page
) {}
