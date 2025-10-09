package ua.sumdu.edu.traver_planner.api.controller

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.OffsetDateTime

@RestController
@RequestMapping("/health")
class HealthController(private val jdbcTemplate: JdbcTemplate) {
    data class Health(val status: String, val timestamp: OffsetDateTime, val uptime: Long? = null, val database: Map<String, Any>? = null)

    private val start = System.currentTimeMillis()

    @GetMapping
    fun health(): Health {
        val t0 = System.currentTimeMillis()
        val dbOk = try {
            jdbcTemplate.queryForObject("select 1", Int::class.java)
            true
        } catch (_: Exception) { false }
        val t1 = System.currentTimeMillis()
        val db = mapOf("status" to if (dbOk) "healthy" else "unhealthy", "responseTime" to (t1 - t0))
        return Health(status = if (dbOk) "healthy" else "unhealthy", timestamp = OffsetDateTime.now(), uptime = System.currentTimeMillis() - start, database = db)
    }
}