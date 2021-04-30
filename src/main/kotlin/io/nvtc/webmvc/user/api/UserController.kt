package io.nvtc.webmvc.user.api

import io.nvtc.webmvc.common.ResourceType.User
import io.nvtc.webmvc.common.utils.ApiUtils.Companion.dummyResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController {

  @GetMapping("/v2/users", "/v3/users") fun getUser() = dummyResponse(User)

  @GetMapping("/v1/users") fun getUserV1() = dummyResponse(User)
}
