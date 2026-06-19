package com.novafx.core.error;

/**
 * Categorized error codes for the NovaFX project system.
 */
public enum ErrorCode {

    // ── Schema / structure ──
    MISSING_VERSION,
    UNSUPPORTED_VERSION,
    MISSING_SECTION,
    MISSING_FIELD,
    DUPLICATE_FIELD,

    // ── Expression ──
    EXPRESSION_PARSE_FAILED,
    INVALID_IDENTIFIER,
    UNDEFINED_PARAMETER,

    // ── Range / sampling ──
    INVALID_RANGE,
    SAMPLE_COUNT_EXCEEDED,
    EMPTY_POINT_CLOUD,

    // ── Project meta ──
    EMPTY_PROJECT_NAME,
    INVALID_PROJECT_NAME,

    // ── Compilation / I/O ──
    COMPILATION_FAILED,
    CACHE_WRITE_FAILED,
    CACHE_READ_FAILED,
    FILE_READ_FAILED,
    FILE_WRITE_FAILED,
    FILE_NOT_FOUND,

    // ── Migration ──
    MIGRATION_FAILED,
    UNKNOWN_VERSION,

    // ── Pipeline ──
    PIPELINE_ABORTED,

    // ── Hot reload ──
    WATCHER_FAILED
}
