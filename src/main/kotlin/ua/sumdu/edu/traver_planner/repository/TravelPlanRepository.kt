package ua.sumdu.edu.traver_planner.repository

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import ua.sumdu.edu.traver_planner.api.dto.TravelPlanEntity
import java.util.UUID

interface TravelPlanRepository : JpaRepository<TravelPlanEntity, UUID> {
    @Query(value = "SELECT * FROM travel_plans WHERE data->>'title' ILIKE %:title%", nativeQuery = true)
    fun searchByTitle(title: String): List<TravelPlanEntity>
}




