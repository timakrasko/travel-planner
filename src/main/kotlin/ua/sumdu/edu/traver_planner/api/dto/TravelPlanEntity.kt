package ua.sumdu.edu.traver_planner.api.dto

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import ua.sumdu.edu.traver_planner.domain.Location
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "travel_plans")
class TravelPlanEntity(
    @Id
    @Column(name = "id")
    var id: UUID = UUID.randomUUID(),

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "data", columnDefinition = "jsonb")
    var data: TravelPlanData,

    @Version
    @Column(name = "version")
    var version: Int = 1
)

data class TravelPlanData(
    var id: UUID,
    var title: String,
    var description: String? = null,

    @JsonProperty("is_public")
    var isPublic: Boolean = false,

    var dates: DateRange? = null,

    @JsonProperty("budget_info")
    var budgetInfo: BudgetInfo? = null,

    var meta: MetaInfo? = null,

    var locations: MutableList<Location> = mutableListOf()
)

data class DateRange(
    var start: LocalDate? = null,
    var end: LocalDate? = null
)

data class BudgetInfo(
    var amount: BigDecimal? = null,
    var currency: String? = "USD"
)

data class MetaInfo(
    @JsonProperty("created_at")
    var createdAt: OffsetDateTime? = null,
    @JsonProperty("updated_at")
    var updatedAt: OffsetDateTime? = null,
    var version: Int = 1
)