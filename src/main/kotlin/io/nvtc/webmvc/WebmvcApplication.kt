package io.nvtc.webmvc

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication class WebmvcApplication

fun main(args: Array<String>) {
  runApplication<WebmvcApplication>(*args)
}
