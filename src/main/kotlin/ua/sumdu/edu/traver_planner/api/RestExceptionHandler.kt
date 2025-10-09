package ua.sumdu.edu.traver_planner.api

import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import ua.sumdu.edu.traver_planner.service.Conflict
import ua.sumdu.edu.traver_planner.service.NotFound
import ua.sumdu.edu.traver_planner.service.Validation
import java.time.OffsetDateTime

data class ErrorResponse(val error: String, val details: List<String> = emptyList(), val timestamp: OffsetDateTime = OffsetDateTime.now())
data class ConflictResponse(val error: String, val current_version: Int, val message: String)

@RestControllerAdvice
class RestExceptionHandler {
    @ExceptionHandler(NotFound::class)
    fun notFound(ex: NotFound) = ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(ErrorResponse(error = ex.message ?: "Not found"))

    @ExceptionHandler(Conflict::class)
    fun conflict(ex: Conflict) = ResponseEntity.status(HttpStatus.CONFLICT)
        .body(ConflictResponse(error = ex.message ?: "Conflict", current_version = ex.currentVersion, message = "Please refresh and try again"))

    @ExceptionHandler(Validation::class)
    fun validation(ex: Validation) = ResponseEntity.badRequest()
        .body(ErrorResponse(error = "Validation error", details = listOf(ex.message ?: "Invalid request")))

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun methodArgInvalid(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val msgs = ex.bindingResult.fieldErrors.map { "${it.field}: ${it.defaultMessage}" }
        return ResponseEntity.badRequest().body(ErrorResponse(error = "Validation error", details = msgs))
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun constraintViolation(ex: ConstraintViolationException): ResponseEntity<ErrorResponse> {
        val msgs = ex.constraintViolations.map { it.message }
        return ResponseEntity.badRequest().body(ErrorResponse(error = "Validation error", details = msgs))
    }
}



