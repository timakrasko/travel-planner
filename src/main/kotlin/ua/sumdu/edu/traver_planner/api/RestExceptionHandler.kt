package ua.sumdu.edu.traver_planner.api

import jakarta.validation.ConstraintViolationException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import ua.sumdu.edu.traver_planner.service.Conflict
import ua.sumdu.edu.traver_planner.service.NotFound
import ua.sumdu.edu.traver_planner.service.Validation
import java.time.OffsetDateTime

data class ErrorResponse(val error: String, val details: List<String> = emptyList(), val timestamp: OffsetDateTime = OffsetDateTime.now())
data class ConflictResponse(val error: String, val current_version: Int, val message: String)

@RestControllerAdvice
class RestExceptionHandler : ResponseEntityExceptionHandler() {

    @ExceptionHandler(NotFound::class)
    fun notFound(ex: NotFound): ResponseEntity<Any> = ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(ErrorResponse(error = ex.message ?: "Not found"))

    @ExceptionHandler(Conflict::class)
    fun conflict(ex: Conflict): ResponseEntity<Any> = ResponseEntity.status(HttpStatus.CONFLICT)
        .body(ConflictResponse(error = ex.message ?: "Conflict", current_version = ex.currentVersion, message = "Please refresh and try again"))

    @ExceptionHandler(Validation::class)
    fun validation(ex: Validation): ResponseEntity<Any> = ResponseEntity.badRequest()
        .body(ErrorResponse(error = "Validation error", details = listOf(ex.message ?: "Invalid request")))

    @ExceptionHandler(ConstraintViolationException::class)
    fun constraintViolation(ex: ConstraintViolationException): ResponseEntity<Any> {
        val msgs = ex.constraintViolations.map { it.message }
        return ResponseEntity.badRequest().body(ErrorResponse(error = "Validation error", details = msgs))
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun typeMismatch(ex: MethodArgumentTypeMismatchException): ResponseEntity<Any> {
        val error = "Invalid parameter format for '${ex.name}'"
        val detail = "Expected type ${ex.requiredType?.simpleName ?: "unknown"}, but value was '${ex.value}'"
        return ResponseEntity.badRequest().body(ErrorResponse(error = error, details = listOf(detail)))
    }

    @ExceptionHandler(DataIntegrityViolationException::class)
    fun handleDataIntegrityViolation(ex: DataIntegrityViolationException): ResponseEntity<Any> {
        val rootCause = ex.mostSpecificCause
        var detail = "A database constraint was violated."

        if (rootCause.message?.contains("check_dates") == true) {
            detail = "End date must be after or the same as start date."
        } else if (rootCause.message?.contains("check_location_dates") == true) {
            detail = "Departure date must be after or the same as arrival date."
        }

        val body = ErrorResponse(error = "<Validation error", details = listOf(detail))
        return ResponseEntity(body, HttpStatus.BAD_REQUEST)
    }

    override fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any>? {
        val msgs = ex.bindingResult.fieldErrors.map { "${it.field}: ${it.defaultMessage}" }
        val body = ErrorResponse(error = "Validation error", details = msgs)
        return ResponseEntity(body, HttpStatus.BAD_REQUEST)
    }

    override fun handleHttpMessageNotReadable(
        ex: HttpMessageNotReadableException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any>? {
        val body = ErrorResponse(error = "Validation error", details = listOf(ex.mostSpecificCause.message ?: "Could not parse JSON"))
        return ResponseEntity(body, HttpStatus.BAD_REQUEST)
    }
}



