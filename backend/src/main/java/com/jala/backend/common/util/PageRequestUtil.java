package com.jala.backend.common.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 * Additive, non-breaking pagination: when the client sends no paging
 * params the endpoint behaves as before but hard-capped at
 * {@link #MAX_PAGE_SIZE} rows, protecting the service from unbounded
 * result sets.
 */
public final class PageRequestUtil {

    public static final int MAX_PAGE_SIZE = 500;

    private PageRequestUtil() {
    }

    public static Pageable of(Integer page, Integer size) {

        int resolvedPage = page == null ? 0 : Math.max(page, 0);

        int resolvedSize = size == null
                ? MAX_PAGE_SIZE
                : Math.min(Math.max(size, 1), MAX_PAGE_SIZE);

        return PageRequest.of(resolvedPage, resolvedSize);
    }
}
