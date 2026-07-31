package com.medtrack.mobile.utils.exceptions

class TokenNaoEncontradoException : Exception("Token nao encontrado")
class MedicamentoNaoEncontradoException : Exception("Medicamento nao encontrado")
class ConfirmacaoExistenteException : Exception("Confirmacao ja existe")
class DoseForaDoHorarioException : Exception("Dose fora do horario permitido")
