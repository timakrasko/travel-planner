package ua.sumdu.edu.traver_planner.service

import jakarta.persistence.EntityManager
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ua.sumdu.edu.traver_planner.api.dto.BudgetInfo
import ua.sumdu.edu.traver_planner.api.dto.CreateTravelPlanRequest
import ua.sumdu.edu.traver_planner.api.dto.DateRange
import ua.sumdu.edu.traver_planner.api.dto.LocationDto
import ua.sumdu.edu.traver_planner.api.dto.MetaInfo
import ua.sumdu.edu.traver_planner.api.dto.TravelPlanData
import ua.sumdu.edu.traver_planner.api.dto.TravelPlanDetailsDto
import ua.sumdu.edu.traver_planner.api.dto.TravelPlanDto
import ua.sumdu.edu.traver_planner.api.dto.TravelPlanEntity
import ua.sumdu.edu.traver_planner.api.dto.UpdateTravelPlanRequest
import ua.sumdu.edu.traver_planner.domain.Location
import ua.sumdu.edu.traver_planner.repository.TravelPlanRepository
import java.time.OffsetDateTime
import java.util.UUID

@Service
class TravelPlanService(
    private val plans: TravelPlanRepository,
    private val entityManager: EntityManager
) {
    fun listPlans(): List<TravelPlanDto> = plans.findAll().map { it.toDto() }

    fun getPlanDetails(id: UUID): TravelPlanDetailsDto {
        val entity = plans.findById(id).orElseThrow { NotFound("Travel plan not found") }
        return entity.toDetailsDto()
    }

    @Transactional
    fun createPlan(req: CreateTravelPlanRequest): TravelPlanDto {
        val now = OffsetDateTime.now()

        val data = TravelPlanData(
            id = UUID.randomUUID(),
            title = req.title,
            description = req.description,
            isPublic = req.is_public,
            dates = DateRange(req.start_date, req.end_date),
            budgetInfo = BudgetInfo(req.budget, req.currency),
            meta = MetaInfo(createdAt = now, updatedAt = now, version = 1),
            locations = mutableListOf()
        )

        val entity = TravelPlanEntity(id = data.id, data = data)
        return plans.save(entity).toDto()
    }

    @Transactional
    fun updatePlan(id: UUID, req: UpdateTravelPlanRequest): TravelPlanDto {
        val entity = plans.findById(id).orElseThrow { NotFound("Travel plan not found") }

        if (entity.version != req.version) {
            throw Conflict(entity.version)
        }

        entity.data.apply {
            setIfNotNull(req.title) { title = it }
            setIfNotNull(req.description) { description = it }
            setIfNotNull(req.is_public) { isPublic = it }

            if (req.start_date != null || req.end_date != null) {
                dates = DateRange(
                    start = req.start_date ?: dates?.start,
                    end = req.end_date ?: dates?.end
                )
            }

            if (req.budget != null || req.currency != null) {
                budgetInfo = BudgetInfo(
                    amount = req.budget ?: budgetInfo?.amount,
                    currency = req.currency ?: budgetInfo?.currency ?: "USD"
                )
            }

            meta?.updatedAt = OffsetDateTime.now()
        }

        val saved = plans.save(entity)
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
class Conflict(val currentVersion: Int) : RuntimeException("Conflict detected")

private inline fun <T> setIfNotNull(value: T?, setter: (T) -> Unit) {
    if (value != null) setter(value)
}


private fun TravelPlanEntity.toDto() = TravelPlanDto(
    id = id,
    title = data.title,
    description = data.description,
    start_date = data.dates?.start,
    end_date = data.dates?.end,
    budget = data.budgetInfo?.amount,
    currency = data.budgetInfo?.currency ?: "USD",
    is_public = data.isPublic,
    version = version,
    created_at = data.meta?.createdAt,
    updated_at = data.meta?.updatedAt,
)

private fun TravelPlanEntity.toDetailsDto() = TravelPlanDetailsDto(
    id = id,
    title = data.title,
    description = data.description,
    start_date = data.dates?.start,
    end_date = data.dates?.end,
    budget = data.budgetInfo?.amount,
    currency = data.budgetInfo?.currency ?: "USD",
    is_public = data.isPublic,
    version = version,
    created_at = data.meta?.createdAt,
    updated_at = data.meta?.updatedAt,
    locations = data.locations.map { it.toDto(this.id) }
)

fun Location.toDto(planId: UUID) = LocationDto(
    id = id,
    travel_plan_id = planId,
    name = name,
    address = address,
    latitude = coordinates?.lat,
    longitude = coordinates?.lng,
    visit_order = visitOrder,
    arrival_date = timing?.arrival,
    departure_date = timing?.departure,
    budget = budget,
    notes = notes,
    created_at = createdAt,
    version = 1
)




