package com.abhinav.lms.common.constants;

public final class AppConstants {

    private AppConstants() {
        // Prevent instantiation
    }

    // ═══════════════════════ API ═══════════════════════

    public static final String API_V1 = "/api/v1";

    // ═══════════════════════ Pagination ═══════════════════════

    public static final String DEFAULT_PAGE_NUMBER = "0";
    public static final String DEFAULT_PAGE_SIZE = "20";
    public static final String DEFAULT_SORT_BY = "createdAt";
    public static final String DEFAULT_SORT_DIR = "desc";
    public static final int MAX_PAGE_SIZE = 100;
}
