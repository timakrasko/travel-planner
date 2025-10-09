package ua.sumdu.edu.traver_planner.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import ua.sumdu.edu.traver_planner.domain.Location
import java.util.UUID

@Repository
interface LocationRepository : JpaRepository<Location, UUID> {
    fun findByTravelPlanIdOrderByVisitOrderAsc(travelPlanId: UUID): List<Location>

    @Query("select coalesce(max(l.visitOrder),0) from Location l where l.travelPlan.id = :planId")
    fun maxOrderForPlan(@Param("planId") planId: UUID): Int
}




