package com.example.todoapp.core.webhook

import com.example.todoapp.core.webhook.exception.DuplicateWebhookException
import com.example.todoapp.core.webhook.exception.*
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class Webhook (secret : String, private val webhookIdCache: WebhookIdCache) {
	private val key: ByteArray
	private val logger = LoggerFactory.getLogger(Webhook::class.java)
	init {
		var sec = secret
		if (sec.startsWith(SECRET_PREFIX)) {
			sec = sec.substring(SECRET_PREFIX.length)
		}
		key = Base64.getDecoder().decode(sec)
	}

	@Throws(WebhookVerificationException::class)
	/**
	 * Verifies the authenticity of a webhook payload using the provided headers.
	 *
	 * @param payload The raw payload string received from the webhook.
	 * @param headers The HTTP headers of the webhook request.
	 * @return The message ID if verification is successful.
	 * @throws WebhookVerificationException if any part of the verification fails (e.g., missing headers, invalid signature, or timestamp issues).
	 */
	fun verify(payload: String, headers: HttpHeaders) : String{
		val msgId = headers.getFirst(UNBRANDED_MSG_ID_KEY)
		val msgSignature = headers.getFirst(UNBRANDED_MSG_SIGNATURE_KEY)
		val msgTimestamp = headers.getFirst(UNBRANDED_MSG_TIMESTAMP_KEY)

		if (msgId == null || msgSignature == null || msgTimestamp == null) {
			throw WebhookVerificationException("Missing required headers")
		}

		val timestamp = verifyTimestamp(msgTimestamp)

		val expectedSignature = try {
			sign(msgId, timestamp, payload).split(",")[1]
		} catch (e: WebhookSigningException) {
			throw WebhookVerificationException("Failed to generate expected signature")
		}

		val msgSignatures = msgSignature.split(" ")
		for (versionedSignature in msgSignatures) {
			val sigParts = versionedSignature.split(",")
			if (sigParts.size < 2) continue
			val version = sigParts[0]
			if (version != "v1") continue
			val signature = sigParts[1]
			if (MessageDigest.isEqual(signature.toByteArray(StandardCharsets.UTF_8), expectedSignature.toByteArray(StandardCharsets.UTF_8))) {
				return msgId
			}
		}
		throw WebhookVerificationException("No matching signature found")
	}

	@Throws(WebhookSigningException::class)
	fun sign(msgId: String, timestamp: Long, payload: String): String {
		try {
			val toSign = "$msgId.$timestamp.$payload"
			val mac = Mac.getInstance(HMAC_SHA256)
			val keySpec = SecretKeySpec(key, HMAC_SHA256)
			mac.init(keySpec)
			val macData = mac.doFinal(toSign.toByteArray(StandardCharsets.UTF_8))
			val signature = Base64.getEncoder().encodeToString(macData)
			return "v1,$signature"
		} catch (e: Exception) {
			// Mac.init can throw InvalidKeyException, Mac.getInstance can throw NoSuchAlgorithmException
			throw WebhookSigningException(e.message ?: e.toString())
		}
	}

	suspend fun verifyAndDedupe(payload: String, headers: HttpHeaders) {
		val webhookId = verify(payload, headers)
		if(webhookIdCache.get(webhookId) == null) {
			webhookIdCache.put(webhookId, true)
		} else {
			throw DuplicateWebhookException(webhookId)
		}
	}

	companion object {
		const val SECRET_PREFIX = "whsec_"
		const val UNBRANDED_MSG_ID_KEY = "webhook-id"
		const val UNBRANDED_MSG_SIGNATURE_KEY = "webhook-signature"
		const val UNBRANDED_MSG_TIMESTAMP_KEY = "webhook-timestamp"
		const val TOLERANCE_IN_SECONDS = 5 * 60 // 5 minutes
		private const val HMAC_SHA256 = "HmacSHA256"
		private const val SECOND_IN_MS = 1000L

		@Throws(WebhookVerificationException::class)
		fun verifyTimestamp(timestampHeader: String): Long {
			val now = System.currentTimeMillis() / SECOND_IN_MS
			val timestamp = runCatching {
				timestampHeader.toLong()
			}.getOrElse {
				throw WebhookVerificationException("timestamp $timestampHeader could not be parsed")
			}


			if (timestamp < (now - TOLERANCE_IN_SECONDS)) {
				throw WebhookVerificationException("Message timestamp too old")
			}

			if (timestamp > (now + TOLERANCE_IN_SECONDS)) {
				throw WebhookVerificationException("Message timestamp too new")
			}

			return timestamp
		}
	}
}
