package ua.sumdu.edu.traver_planner.api.dto

import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

data class TravelPlanDto(
    val id: UUID,
    val title: String,
    val description: String?,
    val start_date: LocalDate?,
    val end_date: LocalDate?,
    val budget: BigDecimal?,
    val currency: String,
    val is_public: Boolean,
    val version: Int,
    val created_at: OffsetDateTime?,
    val updated_at: OffsetDateTime?,
)

data class TravelPlanDetailsDto(
    val id: UUID,
    val title: String,
    val description: String?,
    val start_date: LocalDate?,
    val end_date: LocalDate?,
    val budget: BigDecimal?,
    val currency: String,
    val is_public: Boolean,
    val version: Int,
    val created_at: OffsetDateTime?,
    val updated_at: OffsetDateTime?,
    val locations: List<LocationDto>,
)