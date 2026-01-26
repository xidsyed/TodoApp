package com.example.todoapp.app.quiz.domain.validation

import com.example.todoapp.app.quiz.model.dto.*
import io.konform.validation.*
import io.konform.validation.constraints.*


val newzleChannelQuizValidator = Validation {
	QuizItemTagsDto::quiz {
		constrain("must have a description") {
			!it.description.isNullOrBlank()
		}
	}

	QuizItemTagsDto::items {
		minItems(9)
		maxItems(9) hint "Must contain exactly 9 items"

		onEach {
			ItemTagsDto::item {

				ItemDto::title {
					constrain("must not be blank") {
						!it.isNullOrBlank()
					}
				}

				ItemDto::description {
					constrain("must not be blank") {
						!it.isNullOrBlank()
					}
				}

				ItemDto::question {
					minLength(3)
					maxLength(180)
				}

				ItemDto::options {
					minItems(3)
					maxItems(3) hint "Must contain exactly 3 options"
				}

				ItemDto::imageId {
					constrain("must contain an image") {
						!it.isNullOrEmpty()
					}
				}

				ItemDto::story {
					constrain("must contain a story") {
						it != null
					}
				}
			}
		}
	}
}