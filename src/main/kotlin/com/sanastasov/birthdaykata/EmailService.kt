package com.sanastasov.birthdaykata

import arrow.core.Either
import arrow.core.traverseEither
import java.util.*
import javax.mail.Message
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage


interface EmailService {

    suspend fun sendGreeting(emailMessage: EmailMessage): Either<EmailSendError, Unit>

    suspend fun sendGreetings(greetings: List<EmailMessage>): Either<EmailSendError, List<Unit>> =
        greetings.traverseEither { sendGreeting(it) }
}

class SmtpEmailService(private val host: String, private val port: Int) : EmailService {

    override suspend fun sendGreeting(emailMessage: EmailMessage) = Either.catch {
        val session = buildSession()
        val message = createMessage(session, emailMessage)
        Transport.send(message)
    }.mapLeft { EmailSendError(it) }

    private suspend fun buildSession(): Session {
        val props = Properties().apply {
            put("mail.smtp.host", host)
            put("mail.smtp.port", port.toString())
        }

        return Session.getInstance(props, null)
    }

    private suspend fun createMessage(session: Session, emailMessage: EmailMessage): Message =
        MimeMessage(session).apply {
            setFrom(emailMessage.from.toInternetAddress())
            setRecipient(Message.RecipientType.TO, emailMessage.to.toInternetAddress())
            subject = emailMessage.subject
            setText(emailMessage.message)
        }

    private fun EmailAddress.toInternetAddress() = InternetAddress(email)
}