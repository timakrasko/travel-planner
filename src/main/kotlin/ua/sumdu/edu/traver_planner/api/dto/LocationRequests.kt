package ua.sumdu.edu.traver_planner.api.dto

import jakarta.validation.constraints.*
import java.math.BigDecimal
import java.time.OffsetDateTime

data class CreateLocationRequest(
    @field:NotBlank @field:Size(max = 200)
    val name: String,
    val address: String? = null,
    @field:DecimalMin("-90.0") @field:DecimalMax("90.0")
    val latitude: BigDecimal? = null,
    @field:DecimalMin("-180.0") @field:DecimalMax("180.0")
    val longitude: BigDecimal? = null,
    val arrival_date: OffsetDateTime? = null,
    val departure_date: OffsetDateTime? = null,
    @field:PositiveOrZero
    val budget: BigDecimal? = null,
    val notes: String? = null,
)

data class UpdateLocationRequest(
    val name: String? = null,
    val address: String? = null,
    val latitude: BigDecimal? = null,
    val longitude: BigDecimal? = null,
    val arrival_date: OffsetDateTime? = null,
    val departure_date: OffsetDateTime? = null,
    val budget: BigDecimal? = null,
    val notes: String? = null,
)




