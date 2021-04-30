package io.nvtc.webmvc.project.api

import io.nvtc.webmvc.common.ResourceType.Project
import io.nvtc.webmvc.common.utils.ApiUtils.Companion.dummyResponse
import io.nvtc.webmvc.config.ApiVersioned
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@ApiVersioned
@RestController
class ProjectController {

  @GetMapping("/projects") fun getProjects() = dummyResponse(Project)
}
