package com.usjchatapp.presentation.controller

import com.usjchatapp.common.constants.ApiPaths
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(ApiPaths.Health.BASE)
class HealthController {

    @GetMapping(ApiPaths.Health.CHECK)
    fun healthCheck(): ResponseEntity<Map<String, String>> {
        return ResponseEntity.ok(
                mapOf(
                        "status" to "UP",
                        "service" to "USJ Chat App Backend",
                        "timestamp" to System.currentTimeMillis().toString()
                )
        )
    }
}
