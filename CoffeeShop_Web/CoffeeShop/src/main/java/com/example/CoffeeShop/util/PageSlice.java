package com.example.CoffeeShop.util;

import org.springframework.ui.Model;

import java.util.List;

public record PageSlice<T>(
        List<T> items,
        int currentPage,
        int pageSize,
        int totalPages,
        int totalEntries,
        int startEntry,
        int endEntry
) {
    public void applyToModel(Model model, String listAttributeName) {
        model.addAttribute(listAttributeName, items);
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalEntries", totalEntries);
        model.addAttribute("startEntry", startEntry);
        model.addAttribute("endEntry", endEntry);
    }
}

