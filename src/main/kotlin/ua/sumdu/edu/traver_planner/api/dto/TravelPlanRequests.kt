package ua.sumdu.edu.traver_planner.api.dto

import jakarta.validation.constraints.*
import java.math.BigDecimal
import java.time.LocalDate

data class CreateTravelPlanRequest(
    @field:NotBlank @field:Size(max = 200)
    val title: String,
    val description: String? = null,
    val start_date: LocalDate? = null,
    val end_date: LocalDate? = null,
    @field:PositiveOrZero(message = "Budget must be positive or zero")
    @field:Digits(integer = 8, fraction = 2, message = "Budget must have up to 2 decimal places") // <-- ДОДАНО
    val budget: BigDecimal? = null,
    @field:Pattern(regexp = "^[A-Z]{3}$")
    val currency: String = "USD",
    val is_public: Boolean = false,
)

data class UpdateTravelPlanRequest(
    val version: Int,
    val title: String? = null,
    val description: String? = null,
    val start_date: LocalDate? = null,
    val end_date: LocalDate? = null,
    @field:PositiveOrZero(message = "Budget must be positive or zero")
    @field:Digits(integer = 8, fraction = 2, message = "Budget must have up to 2 decimal places") // <-- ДОДАНО
    val budget: BigDecimal? = null,
    val currency: String? = null,
    val is_public: Boolean? = null,
)



