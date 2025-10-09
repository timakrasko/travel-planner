package ua.sumdu.edu.traver_planner.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "locations")
class Location(
    @Id
    @Column(name = "id", nullable = false)
    var id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "travel_plan_id", nullable = false)
    var travelPlan: TravelPlan,

    @Column(name = "name", nullable = false, length = 200)
    var name: String,

    @Column(name = "address")
    var address: String? = null,

    @Column(name = "latitude", precision = 10, scale = 6)
    var latitude: BigDecimal? = null,

    @Column(name = "longitude", precision = 11, scale = 6)
    var longitude: BigDecimal? = null,

    @Column(name = "visit_order")
    var visitOrder: Int? = null,

    @Column(name = "arrival_date")
    var arrivalDate: OffsetDateTime? = null,

    @Column(name = "departure_date")
    var departureDate: OffsetDateTime? = null,

    @Column(name = "budget")
    var budget: BigDecimal? = null,

    @Column(name = "notes")
    var notes: String? = null,

    @Column(name = "created_at")
    var createdAt: OffsetDateTime? = null,
)



