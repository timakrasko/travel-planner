package ua.sumdu.edu.traver_planner.service

import jakarta.persistence.EntityManager
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ua.sumdu.edu.traver_planner.api.dto.*
import ua.sumdu.edu.traver_planner.domain.Location
import ua.sumdu.edu.traver_planner.domain.TravelPlan
import ua.sumdu.edu.traver_planner.repository.LocationRepository
import ua.sumdu.edu.traver_planner.repository.TravelPlanRepository
import java.util.UUID

@Service
class TravelPlanService(
    private val plans: TravelPlanRepository,
    private val locations: LocationRepository,
    private val entityManager: EntityManager,
) {
    fun listPlans(): List<TravelPlanDto> = plans.findAll().map { it.toDto() }

    fun getPlanDetails(id: UUID): TravelPlanDetailsDto {
        val plan = plans.findById(id).orElseThrow { NotFound("Travel plan not found") }
        val locs = locations.findByTravelPlanIdOrderByVisitOrderAsc(id)
        return plan.toDetailsDto(locs)
    }

    @Transactional
    fun createPlan(req: CreateTravelPlanRequest): TravelPlanDto {
        val plan = TravelPlan(
            title = req.title,
            description = req.description,
            startDate = req.start_date,
            endDate = req.end_date,
            budget = req.budget,
            currency = req.currency,
            isPublic = req.is_public,
        )
        return plans.save(plan).toDto()
    }

    @Transactional
    fun updatePlan(id: UUID, req: UpdateTravelPlanRequest): TravelPlanDto {
        if (req.version <= 0) {
            throw Validation("Version must be positive")
        }

        val existing = plans.findWithOptimisticLock(id) ?: throw NotFound("Travel plan not found")
        if (existing.version != req.version) throw Conflict(existing.version)

        existing.apply {
            setIfNotNull(req.title) { title = it }
            setIfNotNull(req.description) { description = it }
            setIfNotNull(req.start_date) { startDate = it }
            setIfNotNull(req.end_date) { endDate = it }
            setIfNotNull(req.budget) { budget = it }
            setIfNotNull(req.currency) { currency = it }
            setIfNotNull(req.is_public) { isPublic = it }
        }

        val saved = plans.save(existing)
        entityManager.flush()       // Примусово записати в БД
        entityManager.refresh(saved) // Перечитати з БД (оновить version)

        return saved.toDto()
    }

    @Transactional
    fun deletePlan(id: UUID) {
        if (!plans.existsById(id)) throw NotFound("Travel plan not found")
        plans.deleteById(id)
    }
}

class NotFound(message: String) : RuntimeException(message)
class Validation(message: String) : RuntimeException(message)
class Conflict(val currentVersion: Int) : RuntimeException("Conflict: Travel plan was modified by another user")

private inline fun <T> setIfNotNull(value: T?, setter: (T) -> Unit) {
    if (value != null) setter(value)
}

private fun TravelPlan.toDto() = TravelPlanDto(
    id = id,
    title = title,
    description = description,
    start_date = startDate,
    end_date = endDate,
    budget = budget,
    currency = currency,
    is_public = isPublic,
    version = version,
    created_at = createdAt,
    updated_at = updatedAt,
)

private fun TravelPlan.toDetailsDto(locs: List<Location>) = TravelPlanDetailsDto(
    id = id,
    title = title,
    description = description,
    start_date = startDate,
    end_date = endDate,
    budget = budget,
    currency = currency,
    is_public = isPublic,
    version = version,
    created_at = createdAt,
    updated_at = updatedAt,
    locations = locs.map { it.toDto() },
)

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
    version = version,
)




