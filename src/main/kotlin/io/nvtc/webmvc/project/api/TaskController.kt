package io.nvtc.webmvc.project.api

import io.nvtc.webmvc.common.ResourceType.Task
import io.nvtc.webmvc.common.utils.ApiUtils.Companion.dummyResponse
import io.nvtc.webmvc.config.ApiVersioned
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@ApiVersioned(UntilVersion = 1)
@RestController
@RequestMapping("/tasks")
class TaskControllerV1 {

  @GetMapping fun getTasks() = dummyResponse(Task)
}

@ApiVersioned(FromVersion = 2)
@RestController
@RequestMapping("/tasks")
class TaskController {

  @GetMapping fun getTasks() = dummyResponse(Task)
}
