package com.sanastasov.birthdaykata

import arrow.core.Nel
import arrow.core.flatMap
import java.time.LocalDate

interface Env : EmployeeRepository,
    BirthdayService,
    EmailService

suspend fun main() {
    val env: Env = object : Env,
        EmployeeRepository by FileEmployeeRepository("input.txt"),
        BirthdayService by BirthdayServiceInterpreter(),
        EmailService by SmtpEmailService("localhost", 8080) {}

    env.sendGreetingsUseCase(date = LocalDate.now())
}

suspend fun Env.sendGreetingsUseCase(date: LocalDate): Unit {
    allEmployees().map { allEmployees -> birthdayMessages(allEmployees, date) }
        .flatMap { sendGreetings(it) }

}

sealed class KataException

data class EmployeeRepositoryException(
    val errors: Nel<String>
) : KataException()

data class EmailSendError(
    val it: Throwable
) : KataException()