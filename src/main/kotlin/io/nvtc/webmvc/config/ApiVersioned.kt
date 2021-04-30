package io.nvtc.webmvc.config

import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.annotation.AnnotationTarget.FUNCTION

/**
 * Annotation to mark a class or function as api versioned.
 *
 * @param FromVersion the api version when the endpoint was introduced
 * @param UntilVersion the api version until the endpoint is available (inclusive)
 */
@Target(CLASS, FUNCTION)
@Retention(RUNTIME)
annotation class ApiVersioned(val FromVersion: Int = 1, val UntilVersion: Int = 0)
