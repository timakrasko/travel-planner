package ua.sumdu.edu.traver_planner.api.dto

import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.UUID

data class LocationDto(
    val id: UUID,
    val version: Int,
    val travel_plan_id: UUID,
    val name: String,
    val address: String?,
    val latitude: BigDecimal?,
    val longitude: BigDecimal?,
    val visit_order: Int?,
    val arrival_date: OffsetDateTime?,
    val departure_date: OffsetDateTime?,
    val budget: BigDecimal?,
    val notes: String?,
    val created_at: OffsetDateTime?,
)




