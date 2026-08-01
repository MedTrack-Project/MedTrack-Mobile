package com.medtrack.mobile.domain.error

sealed class DomainException(message: String? = null, cause: Throwable? = null) : Exception(message, cause)

class InvalidCredentialsException : DomainException()
class InvalidSessionException : DomainException()
class MedicationNotFoundException : DomainException()
class ConfirmationAlreadyExistsException : DomainException()
class DoseOutsideAllowedTimeException : DomainException()
class RemoteDataException(cause: Throwable? = null) : DomainException(cause = cause)
class InvalidDoseException(cause: Throwable? = null) : DomainException(cause = cause)
class ScanProcessingException(cause: Throwable? = null) : DomainException(cause = cause)
