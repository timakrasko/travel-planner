package ua.sumdu.edu.traver_planner.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.Version

import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.UUID

data class Location(
    var id: UUID = UUID.randomUUID(),
    var name: String,
    var address: String? = null,

    @JsonProperty("visit_order")
    var visitOrder: Int? = null,

    var coordinates: Coordinates? = null,
    var timing: Timing? = null,
    var budget: BigDecimal? = null,
    var notes: String? = null,

    @JsonProperty("created_at")
    var createdAt: OffsetDateTime? = OffsetDateTime.now()
)

data class Coordinates(
    var lat: BigDecimal? = null,
    var lng: BigDecimal? = null
)

data class Timing(
    var arrival: OffsetDateTime? = null,
    var departure: OffsetDateTime? = null
)



