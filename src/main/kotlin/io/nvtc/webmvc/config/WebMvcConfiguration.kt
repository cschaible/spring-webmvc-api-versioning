package io.nvtc.webmvc.config

import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class WebMvcConfiguration {

  @Bean
  fun webMvcRegistrations(): WebMvcRegistrations {
    return object : WebMvcRegistrations {
      override fun getRequestMappingHandlerMapping() = ApiVersioningRequestMappingHandlerMapping()
    }
  }
}
