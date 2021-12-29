package com.sanastasov.birthdaykata

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.identity
import java.time.LocalDate
import java.time.Month

interface BirthdayService {

    fun birthdayMessages(employees: List<Employee>, date: LocalDate): Either<KataException, List<EmailMessage>>
}

class BirthdayServiceInterpreter : BirthdayService {
    private val senderAddress = "birthday@corp.com"
    override fun birthdayMessages(
        employees: List<Employee>,
        date: LocalDate
    ): Either<KataException, List<EmailMessage>> {
        val targets = employees.filter { employeeFilter(date, it.dateOfBirth) }
        return EmailAddress(senderAddress).toEither().bimap({ EmployeeRepositoryException(it) }, { senderMail ->
            targets.map {
                EmailMessage(
                    senderMail,
                    it.emailAddress,
                    "Happy Birthday!",
                    "Happy birthday, dear ${it.firstName}!"
                )
            }
        })
    }

    private fun employeeFilter(date: LocalDate, birthday: LocalDate): Boolean =
        if (!date.isLeapYear && date.isFeb28th) birthday.isSameDay(date) || birthday.isFeb29th
        else birthday.isSameDay(date)


    private fun LocalDate.isSameDay(date: LocalDate): Boolean =
        this.month == date.month && this.dayOfMonth == date.dayOfMonth

    private val LocalDate.isFeb28th: Boolean
        get() = this.month == Month.FEBRUARY && this.dayOfMonth == 28

    private val LocalDate.isFeb29th: Boolean
        get() = this.month == Month.FEBRUARY && this.dayOfMonth == 29
}