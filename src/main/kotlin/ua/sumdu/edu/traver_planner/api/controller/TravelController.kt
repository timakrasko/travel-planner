package ua.sumdu.edu.traver_planner.api.controller

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import ua.sumdu.edu.traver_planner.api.dto.CreateLocationRequest
import ua.sumdu.edu.traver_planner.api.dto.CreateTravelPlanRequest
import ua.sumdu.edu.traver_planner.api.dto.LocationDto
import ua.sumdu.edu.traver_planner.api.dto.TravelPlanDto
import ua.sumdu.edu.traver_planner.api.dto.UpdateLocationRequest
import ua.sumdu.edu.traver_planner.api.dto.UpdateTravelPlanRequest
import ua.sumdu.edu.traver_planner.service.LocationService
import ua.sumdu.edu.traver_planner.service.TravelPlanService
import java.util.UUID

@RestController
@RequestMapping("/api")
class TravelController(private val service: TravelPlanService, private val locationService: LocationService) {

    @GetMapping("/travel-plans")
    fun listPlans(): List<TravelPlanDto> = service.listPlans()

    @PostMapping("/travel-plans")
    @ResponseStatus(HttpStatus.CREATED)
    fun createPlan(@Valid @RequestBody req: CreateTravelPlanRequest): TravelPlanDto = service.createPlan(req)

    @GetMapping("/travel-plans/{id}")
    fun getPlan(@PathVariable id: UUID) = service.getPlanDetails(id)

    @PutMapping("/travel-plans/{id}")
    fun updatePlan(@PathVariable id: UUID, @Valid @RequestBody req: UpdateTravelPlanRequest): TravelPlanDto = service.updatePlan(id, req)

    @DeleteMapping("/travel-plans/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deletePlan(@PathVariable id: UUID) = service.deletePlan(id)

    @PostMapping("/travel-plans/{id}/locations")
    @ResponseStatus(HttpStatus.CREATED)
    fun addLocation(@PathVariable id: UUID, @Valid @RequestBody req: CreateLocationRequest): LocationDto = locationService.addLocation(id, req)

    @PutMapping("/locations/{id}")
    fun updateLocation(@PathVariable id: UUID, @Valid @RequestBody req: UpdateLocationRequest): LocationDto = locationService.updateLocation(id, req)

    @DeleteMapping("/locations/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteLocation(@PathVariable id: UUID) = locationService.deleteLocation(id)
}