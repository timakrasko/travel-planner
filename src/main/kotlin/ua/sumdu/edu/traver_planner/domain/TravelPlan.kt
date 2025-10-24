package ua.sumdu.edu.traver_planner.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.Version
import java.time.OffsetDateTime
import java.time.LocalDate
import java.util.UUID

@Entity
@Table(name = "travel_plans")
data class TravelPlan(
    @Id
    @Column(name = "id", nullable = false)
    var id: UUID = UUID.randomUUID(),

    @Column(name = "title", nullable = false, length = 200)
    var title: String,

    @Column(name = "description")
    var description: String? = null,

    @Column(name = "start_date")
    var startDate: LocalDate? = null,

    @Column(name = "end_date")
    var endDate: LocalDate? = null,

    @Column(name = "budget")
    var budget: java.math.BigDecimal? = null,

    @Column(name = "currency", length = 3)
    var currency: String = "USD",

    @Column(name = "is_public")
    var isPublic: Boolean = false,

    @Version
    @Column(name = "version")
    var version: Int = 1,

    @Column(name = "created_at")
    var createdAt: OffsetDateTime? = null,

    @Column(name = "updated_at")
    var updatedAt: OffsetDateTime? = null,
)



