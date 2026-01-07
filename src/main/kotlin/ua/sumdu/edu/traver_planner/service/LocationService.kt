package ua.sumdu.edu.traver_planner.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ua.sumdu.edu.traver_planner.api.dto.CreateLocationRequest
import ua.sumdu.edu.traver_planner.api.dto.LocationDto
import ua.sumdu.edu.traver_planner.api.dto.UpdateLocationRequest
import ua.sumdu.edu.traver_planner.domain.Coordinates
import ua.sumdu.edu.traver_planner.domain.Location
import ua.sumdu.edu.traver_planner.domain.Timing
import ua.sumdu.edu.traver_planner.repository.TravelPlanRepository
import java.time.OffsetDateTime
import java.util.UUID

@Service
class LocationService(
    private val plans: TravelPlanRepository
) {

    @Transactional
    fun addLocation(planId: UUID, req: CreateLocationRequest): LocationDto {
        val planEntity = plans.findById(planId).orElseThrow { NotFound("Plan not found") }

        // Вираховуємо наступний порядок
        val nextOrder = (planEntity.data.locations.maxOfOrNull { it.visitOrder ?: 0 } ?: 0) + 1

        val newLocation = Location(
            id = UUID.randomUUID(),
            name = req.name,
            address = req.address,
            visitOrder = nextOrder,
            coordinates = Coordinates(req.latitude, req.longitude),
            timing = Timing(req.arrival_date, req.departure_date),
            budget = req.budget,
            notes = req.notes,
            createdAt = OffsetDateTime.now()
        )

        // Додаємо в список
        planEntity.data.locations.add(newLocation)
        planEntity.data.meta?.updatedAt = OffsetDateTime.now() // Оновлюємо час зміни плану

        plans.save(planEntity)

        return newLocation.toDto(planId)
    }

    @Transactional
    fun updateLocation(id: UUID, req: UpdateLocationRequest): LocationDto {
        val allPlans = plans.findAll()
        val planEntity = allPlans.find { plan -> plan.data.locations.any { loc -> loc.id == id } }
            ?: throw NotFound("Location not found")

        val location = planEntity.data.locations.find { it.id == id }!!

        location.apply {
            if (req.name != null) name = req.name
            if (req.address != null) address = req.address
            if (req.budget != null) budget = req.budget
            if (req.notes != null) notes = req.notes

            if (req.latitude != null || req.longitude != null) {
                coordinates = Coordinates(
                    lat = req.latitude ?: coordinates?.lat,
                    lng = req.longitude ?: coordinates?.lng
                )
            }

            if (req.arrival_date != null || req.departure_date != null) {
                timing = Timing(
                    arrival = req.arrival_date ?: timing?.arrival,
                    departure = req.departure_date ?: timing?.departure
                )
            }
        }

        planEntity.data.meta?.updatedAt = OffsetDateTime.now()
        plans.save(planEntity)

        return location.toDto(planEntity.id)
    }

    @Transactional
    fun deleteLocation(id: UUID) {
        val allPlans = plans.findAll()
        val planEntity = allPlans.find { plan -> plan.data.locations.any { loc -> loc.id == id } }
            ?: throw NotFound("Location not found")

        planEntity.data.locations.removeIf { it.id == id }
        planEntity.data.meta?.updatedAt = OffsetDateTime.now()

        plans.save(planEntity)
    }
}





