package com.example.CoffeeShop.util;

import java.util.ArrayList;
import java.util.List;

public final class PaginationUtils {
    private PaginationUtils() {
    }

    public static <T> PageSlice<T> slice(List<T> allItems, int page, int size) {
        List<T> safeList = allItems == null ? List.of() : allItems;
        int safeSize = size <= 0 ? 10 : size;

        int totalEntries = safeList.size();
        int totalPages = (int) Math.ceil((double) totalEntries / safeSize);
        if (totalPages == 0) totalPages = 1;

        int safePage = page < 1 ? 1 : Math.min(page, totalPages);

        int startIdx = (safePage - 1) * safeSize;
        int endIdx = Math.min(startIdx + safeSize, totalEntries);

        List<T> pagedItems = new ArrayList<>();
        if (startIdx < totalEntries) {
            pagedItems = safeList.subList(startIdx, endIdx);
        }

        int startEntry = totalEntries == 0 ? 0 : startIdx + 1;
        int endEntry = endIdx;

        return new PageSlice<>(
                pagedItems,
                safePage,
                safeSize,
                totalPages,
                totalEntries,
                startEntry,
                endEntry
        );
    }
}

