package com.sanastasov.birthdaykata

import arrow.core.Either
import arrow.core.Nel
import arrow.core.identity
import arrow.core.sequenceValidated
import arrow.fx.coroutines.Resource
import arrow.fx.coroutines.bracket
import arrow.fx.coroutines.fromAutoCloseable
import java.io.BufferedReader
import java.io.File

interface EmployeeRepository {

    suspend fun allEmployees(): Either<KataException, List<Employee>>
}

class FileEmployeeRepository(fileName: String) : EmployeeRepository {

    private val file = Resource.fromAutoCloseable { File(fileName).inputStream() }

    override suspend fun allEmployees(): Either<KataException, List<Employee>> =
        file.use { readFile()(it.bufferedReader()) }

    private fun readFile(): (BufferedReader) -> Either<KataException, List<Employee>> = { br: BufferedReader ->
            br.readLines()
                .drop(1)
                .map(employeeParser)
                .sequenceValidated().toEither().mapLeft {EmployeeRepositoryException(it) }
    }


    companion object {

        val employeeParser: (String) -> ValidationResult<Employee> = { row ->
            val parts = row.split(", ")
            val lastName = parts.getOrNull(0)
            val firstName = parts.getOrNull(1)
            val dateOfBirth = parts.getOrNull(2)
            val email = parts.getOrNull(3)
            Employee(firstName, lastName, dateOfBirth, email)
        }
    }
}