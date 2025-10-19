package com.example.todoapp.app.auth.roles.annotations

import org.springframework.security.access.prepost.PreAuthorize

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@PreAuthorize("hasRole('ADMIN') or hasRole('WRITER')")
annotation class RequireRole
