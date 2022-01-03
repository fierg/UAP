package uap.exceptions

internal class UnknownCharacterException(unknownInput: String) : Exception("Unknown character « $unknownInput »")