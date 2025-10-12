package com.example.todoapp.app.invitations.controller

import com.example.todoapp.app.invitations.CreateInvitationRequest
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/invitations")
@Validated
class InvitationsController {

	@PostMapping("")
	suspend fun createInvitation(@Valid @RequestBody request: CreateInvitationRequest): ResponseEntity<String> {
		return ResponseEntity
			.status(HttpStatus.CREATED)
			.contentType(MediaType.TEXT_PLAIN)
			.body("something")
	}

}