package com.medtrack.mobile.domain.error

sealed class DomainException(message: String? = null, cause: Throwable? = null) : Exception(message, cause)

class InvalidCredentialsException : DomainException()
class InvalidSessionException : DomainException()
class MedicationNotFoundException : DomainException()
class ConfirmationAlreadyExistsException : DomainException()
class DoseOutsideAllowedTimeException : DomainException()
class RemoteDataException(cause: Throwable? = null) : DomainException(cause = cause)
class NetworkUnavailableException(cause: Throwable? = null) : DomainException(cause = cause)
class ServerUnavailableException : DomainException()
class InvalidRemoteResponseException(cause: Throwable? = null) : DomainException(cause = cause)
class RemoteRequestRejectedException(val statusCode: Int) : DomainException()
class InvalidDoseException(cause: Throwable? = null) : DomainException(cause = cause)
class ScanProcessingException(cause: Throwable? = null) : DomainException(cause = cause)
