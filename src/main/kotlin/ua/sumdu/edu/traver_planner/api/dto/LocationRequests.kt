package ua.sumdu.edu.traver_planner.api.dto

import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Digits
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.PositiveOrZero
import jakarta.validation.constraints.Size
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
    @field:PositiveOrZero(message = "Budget must be positive or zero")
    @field:Digits(integer = 8, fraction = 2, message = "Budget must have up to 2 decimal places") // <-- ДОДАНО
    val budget: BigDecimal? = null,
    val notes: String? = null,
)

data class UpdateLocationRequest(
    @field:Positive(message = "Version must be positive")
    val version: Int,
    val name: String? = null,
    val address: String? = null,
    val latitude: BigDecimal? = null,
    val longitude: BigDecimal? = null,
    val arrival_date: OffsetDateTime? = null,
    val departure_date: OffsetDateTime? = null,
    @field:PositiveOrZero(message = "Budget must be positive or zero")
    @field:Digits(integer = 8, fraction = 2, message = "Budget must have up to 2 decimal places") // <-- ДОДАНО
    val budget: BigDecimal? = null,
    val notes: String? = null,
)



