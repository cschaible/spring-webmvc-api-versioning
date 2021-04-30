package io.nvtc.webmvc.config

import java.lang.reflect.Method
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.util.Assert
import org.springframework.web.servlet.mvc.condition.*
import org.springframework.web.servlet.mvc.method.RequestMappingInfo
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping

class ApiVersioningRequestMappingHandlerMapping : RequestMappingHandlerMapping() {
  override fun getMappingForMethod(method: Method, handlerType: Class<*>): RequestMappingInfo? {
    val superMapping = super.getMappingForMethod(method, handlerType) ?: return null

    // handle annotations defined on methods
    AnnotationUtils.findAnnotation(method, ApiVersioned::class.java)?.let {
      val requestCondition = getCustomMethodCondition(method)
      return getApiVersionInfo(method, it, requestCondition).combine(superMapping)
    }

    // handle annotations defined on classes
    AnnotationUtils.findAnnotation(handlerType, ApiVersioned::class.java)?.let {
      val requestCondition = getCustomTypeCondition(handlerType)
      return getApiVersionInfo(method, it, requestCondition).combine(superMapping)
    }

    // handle unversioned endpoints
    return super.getMappingForMethod(method, handlerType)
  }

  private fun getApiVersionInfo(
      method: Method,
      annotation: ApiVersioned,
      requestCondition: RequestCondition<*>?
  ): RequestMappingInfo {
    val fromApiVersion =
        annotation.FromVersion.let {
          Assert.isTrue(it >= MIN_API_VERSION, "Invalid min api version: $it for endpoint $method")
          it
        }
    val toApiVersion =
        annotation.UntilVersion.let {
          Assert.isTrue(it <= MAX_API_VERSION, "Invalid max api version: $it for endpoint $method")
          when (it) {
            0 -> MAX_API_VERSION
            else -> it
          }
        }

    Assert.isTrue(
        fromApiVersion <= toApiVersion,
        "Invalid api versions for endpoint $method. From: $fromApiVersion, To: $toApiVersion")

    val endpointVersions =
        (fromApiVersion..toApiVersion).map { version -> "$VERSION_PREFIX$version" }

    return RequestMappingInfo(
        PatternsRequestCondition(*endpointVersions.toTypedArray()),
        RequestMethodsRequestCondition(),
        ParamsRequestCondition(),
        HeadersRequestCondition(),
        ConsumesRequestCondition(),
        ProducesRequestCondition(),
        requestCondition)
  }

  companion object {
    const val VERSION_PREFIX = "/v"
    const val MIN_API_VERSION = 1
    const val MAX_API_VERSION = 3
  }
}
