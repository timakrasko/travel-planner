package ua.sumdu.edu.traver_planner.domain

import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime

// Це чистий Kotlin об'єкт, без анотацій @Entity
data class TravelPlanData(
    var title: String,
    var description: String? = null,
    var isPublic: Boolean = false,

    // Групи полів з вашого SQL скрипта
    var dates: DateRange? = null,
    var budgetInfo: BudgetInfo? = null,
    var meta: MetaInfo? = null,

    var locations: List<Location> = emptyList() // Ваш клас Location з минулої відповіді
)

data class DateRange(
    var start: LocalDate? = null,
    var end: LocalDate? = null
)

data class BudgetInfo(
    var amount: BigDecimal? = null,
    var currency: String = "USD"
)

data class MetaInfo(
    var version: Int = 1,
    var createdAt: OffsetDateTime? = null,
    var updatedAt: OffsetDateTime? = null
)