package uap.tram.filereader

import uap.Instruction
import uap.tram.exception.UnexpectedInstructionException
import java.io.File

class FileReader {
    private val regex = Regex("^(\\d+): (\\w+) ?(-?\\d+)? ?(-?\\d+)? ?(-?\\d+)?\$")

    private fun getReader(filename: String) = File(filename).bufferedReader()

    fun getInstructions(filename: String): Array<Instruction> {
        val instructions = mutableListOf<Instruction>()

        getReader(filename).forEachLine { line ->
            if (regex.matches(line)){
                val groups = regex.find(line)!!.groupValues.filter { it.isNotEmpty() }

                when (groups.size) {
                    3 -> instructions.add(Instruction(getOptCodeFromString(groups[2])))
                    4 -> instructions.add(Instruction(getOptCodeFromString(groups[2]), groups[3].toInt()))
                    5 -> instructions.add(Instruction(getOptCodeFromString(groups[2]), groups[3].toInt(), groups[4].toInt()))
                    6 -> instructions.add(Instruction(getOptCodeFromString(groups[2]), groups[3].toInt(), groups[4].toInt(), groups[5].toInt()))
                    else -> throw UnexpectedInstructionException("unexpected instruction size: ${groups.size}")
                }
            } else {
                throw UnexpectedInstructionException("unexpected input for line: $line")
            }
        }
        return instructions.toTypedArray()
    }

    private fun getOptCodeFromString(s: String): Int {
        return when(s) {
            "const" -> Instruction.CONST
            "load" -> Instruction.LOAD
            "mult" -> Instruction.MUL
            "add" -> Instruction.ADD
            "store" -> Instruction.STORE
            "halt" -> Instruction.HALT
            "ifzero" -> Instruction.IFZERO
            "invoke" -> Instruction.INVOKE
            "return" -> Instruction.RETURN
            "mod" -> Instruction.MOD

            else -> throw UnexpectedInstructionException("unexpected opt code string: $s")
        }
    }
}