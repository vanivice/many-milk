package com.application.manymilk.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class PaginationUtil {

    private static final int DEFAULT_PAGE_SIZE = 20;

    private PaginationUtil() {
        // приватный конструктор, чтобы нельзя было создать экземпляр
    }

    public static Pageable createPageable(int pageNumber, int pageSize, boolean sortAsc) {
        int size = pageSize > 0 ? pageSize : DEFAULT_PAGE_SIZE;
        Sort sort = sortAsc ? Sort.by("id").ascending() : Sort.by("id").descending();
        return PageRequest.of(pageNumber, size, sort);
    }

    public static Pageable createPageable(int pageNumber) {
        return createPageable(pageNumber, DEFAULT_PAGE_SIZE, true);
    }
}
