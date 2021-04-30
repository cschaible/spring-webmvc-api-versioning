package io.nvtc.webmvc.common.utils

import io.nvtc.webmvc.common.ResourceType
import java.util.regex.Pattern
import java.util.regex.Pattern.compile
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.context.request.RequestContextHolder.getRequestAttributes
import org.springframework.web.context.request.ServletRequestAttributes

class ApiUtils {
  companion object {
    private val PATTERN: Pattern = compile(".*/v([1-9][0-9]*)/.*")

    fun currentApiVersion(): Int {
      val attributes = getRequestAttributes()
      val request = (attributes as ServletRequestAttributes).request
      val matcher = PATTERN.matcher(request.requestURI)
      if (matcher.find()) {
        return matcher.group(1).toInt()
      }
      throw IllegalArgumentException("Invalid version detected")
    }

    fun dummyResponse(resourceType: ResourceType) =
        ok("$resourceType from API with Version: ${currentApiVersion()}")
  }
}
