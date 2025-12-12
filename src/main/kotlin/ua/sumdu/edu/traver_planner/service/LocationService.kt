package ua.sumdu.edu.traver_planner.service

import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ua.sumdu.edu.traver_planner.api.dto.CreateLocationRequest
import ua.sumdu.edu.traver_planner.api.dto.LocationDto
import ua.sumdu.edu.traver_planner.api.dto.UpdateLocationRequest
import ua.sumdu.edu.traver_planner.domain.Location
import ua.sumdu.edu.traver_planner.repository.LocationRepository
import ua.sumdu.edu.traver_planner.repository.TravelPlanRepository
import java.util.UUID

@Service
class LocationService(
    private val locations: LocationRepository,
    private val plans: TravelPlanRepository,
) {
    @Transactional
    fun addLocation(planId: UUID, req: CreateLocationRequest): LocationDto {
        val plan = plans.findById(planId).orElseThrow { NotFound("Travel plan not found") }
        var attempts = 3
        while (attempts > 0) {
            try {
                val entity = Location(
                    travelPlan = plan,
                    name = req.name,
                    address = req.address,
                    latitude = req.latitude,
                    longitude = req.longitude,
                    arrivalDate = req.arrival_date,
                    departureDate = req.departure_date,
                    budget = req.budget,
                    notes = req.notes,
                )

                if (entity.visitOrder == null) {
                    entity.visitOrder = locations.maxOrderForPlan(planId) + 1
                }

                return locations.saveAndFlush(entity).toDto()

            } catch (e: DataIntegrityViolationException) {
                attempts--
                if (attempts == 0) {
                    throw e
                }
            }
        }
        throw RuntimeException("Unexpected error during location creation")
    }

    @Transactional
    fun updateLocation(id: UUID, req: UpdateLocationRequest): LocationDto {
        val entity = locations.findById(id).orElseThrow { NotFound("Location not found") }

        if (entity.version != req.version) {
            throw Conflict(entity.version)
        }

        req.name?.let { entity.name = it }
        if (req.arrival_date != null && req.departure_date != null && req.departure_date.isBefore(req.arrival_date)) {
            throw Validation("departure_date must be >= arrival_date")
        }
        entity.apply {
            setIfNotNull(req.address) { address = it }
            setIfNotNull(req.latitude) { latitude = it }
            setIfNotNull(req.longitude) { longitude = it }
            setIfNotNull(req.arrival_date) { arrivalDate = it }
            setIfNotNull(req.departure_date) { departureDate = it }
            setIfNotNull(req.budget) { budget = it }
            setIfNotNull(req.notes) { notes = it }
        }
        return locations.saveAndFlush(entity).toDto()
    }

    private inline fun <T> setIfNotNull(value: T?, setter: (T) -> Unit) {
        if (value != null) setter(value)
    }

    @Transactional
    fun deleteLocation(id: UUID) {
        if (!locations.existsById(id)) throw NotFound("Location not found")
        locations.deleteById(id)
    }
}

private fun Location.toDto() = LocationDto(
    id = id,
    travel_plan_id = travelPlan.id,
    name = name,
    address = address,
    latitude = latitude,
    longitude = longitude,
    visit_order = visitOrder,
    arrival_date = arrivalDate,
    departure_date = departureDate,
    budget = budget,
    notes = notes,
    created_at = createdAt,
    version = version
)





