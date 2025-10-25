package com.example.todoapp.common.exception.handler

/*

@ControllerAdvice
class SecurityExceptionHandler {
    private val logger = LoggerFactory.getLogger(SecurityExceptionHandler::class.java)

    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDeniedException(ex: AccessDeniedException): ResponseEntity<Map<String, String>> {
        val body = mapOf(
            "status" to HttpStatus.UNAUTHORIZED.reasonPhrase,
            "message" to "You do not have permission to access this resource."
        )
        logger.error("Access denied: {}", ex.message)

        val headers = HttpHeaders()
        headers.add(HttpHeaders.WWW_AUTHENTICATE, "Bearer realm=\"todoapp\"")

        return ResponseEntity(body, headers, HttpStatus.UNAUTHORIZED)
    }
}
*/
