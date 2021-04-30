package io.nvtc.webmvc.project.api

import io.nvtc.webmvc.common.ResourceType.Description
import io.nvtc.webmvc.common.utils.ApiUtils.Companion.dummyResponse
import io.nvtc.webmvc.config.ApiVersioned
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class TaskDescriptionController {

  @ApiVersioned(UntilVersion = 1)
  @GetMapping("/tasks/descriptions")
  fun getDescriptionsV1() = dummyResponse(Description)

  @ApiVersioned(FromVersion = 2)
  @GetMapping("/tasks/descriptions")
  fun getDescriptions() = dummyResponse(Description)
}
