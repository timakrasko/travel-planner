package ua.sumdu.edu.traver_planner.repository

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import ua.sumdu.edu.traver_planner.domain.TravelPlan
import java.util.UUID

@Repository
interface TravelPlanRepository : JpaRepository<TravelPlan, UUID> {
    @Lock(LockModeType.OPTIMISTIC)
    @Query("select p from TravelPlan p where p.id = :id")
    fun findWithOptimisticLock(@Param("id") id: UUID): TravelPlan?
}




