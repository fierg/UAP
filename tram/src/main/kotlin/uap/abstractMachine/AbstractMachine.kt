package uap.abstractMachine

import uap.Main
import org.apache.logging.log4j.kotlin.logger
import uap.Instruction
import uap.stack.LinkedListStack
import java.rmi.UnexpectedException

class AbstractMachine(private val program: Array<Instruction>, private val debug: Boolean) {
    private val logger = logger(Main.javaClass.simpleName)

    private val stack = LinkedListStack()
    private var top = 0
    private var pp = 0
    private var fp = 0
    private var pc = 0

    fun run() {
        while (pc >= 0) {
            execute(program[pc])
            if (debug)
                logger.debug(this.toString())
        }
    }

    private fun execute(instruction: Instruction) {
        if (debug)
            println("Instruction: $instruction")

        when (instruction.opcode) {
            Instruction.CONST -> const(instruction)
            Instruction.STORE -> store(instruction)
            Instruction.LOAD -> load(instruction)
            Instruction.GOTO -> goto(instruction)
            Instruction.IFZERO -> ifZero(instruction)
            Instruction.ADD -> add()
            Instruction.MUL -> mul()
            Instruction.DIV -> div()
            Instruction.SUB -> sub()
            Instruction.NOP -> noop()
            Instruction.HALT -> halt()
            Instruction.MOD -> mod()
            Instruction.INVOKE -> invoke(instruction)
            Instruction.RETURN -> returnf(instruction)
            Instruction.EQ -> eq(instruction)
            else -> throw UnexpectedException("Unexpected instruction: $instruction")
        }
    }

    private fun eq(instruction: Instruction) {
        TODO("Not yet implemented")
    }

    private fun returnf(instruction: Instruction) {
        val res = stack[top]
        top = pp
        pc = stack[fp]
        pp = stack[fp + 1]
        fp = stack[fp + 2]
        stack[top] = res
    }

    private fun invoke(instruction: Instruction) {
        stack[top + 1] = pc + 1
        stack[top + 2] = pp
        stack[top + 3] = fp
        stack[top + 4] = spp(instruction.arg3, fp)
        stack[top + 5] = sfp(instruction.arg3, fp)
        pp = top - instruction.arg1 + 1
        fp = top + 1
        pc = instruction.arg2
    }

    private fun sfp(d: Int, fp: Int): Int {
        return if (d == 0) fp
        else sfp(d - 1, stack[fp + 4])
    }

    private fun spp(d: Int, fp: Int): Int {
        return if (d == 0) fp
        else spp(d - 1, stack[fp + 4])
    }

    private fun noop() {
        pc += 1
    }

    private fun ifZero(instruction: Instruction) {
        if (stack.pop() == 0)
            pc = instruction.arg1
        else
            pc += 1
        top -= 1
    }

    private fun goto(instruction: Instruction) {
        pc = instruction.arg1
    }

    private fun halt() {
        pc = -1
    }

    private fun add() {
        stack[top - 1] = stack[top - 1] + stack[top]
        top -= 1
        pc += 1
    }

    private fun sub() {
        stack[top - 1] = stack[top - 1] - stack[top]
        top -= 1
        pc += 1
    }

    private fun mul() {
        stack[top - 1] = stack[top - 1] * stack[top]
        top -= 1
        pc += 1
        stack.pop()
    }

    private fun div() {
        stack[top - 1] = stack[top - 1] / stack[top]
        top -= 1
        pc += 1
        stack.pop()
    }

    private fun mod() {
        stack[top - 1] = stack[top - 1] % stack[top]
        top -= 1
        pc += 1
        stack.pop()
    }


    private fun load(instruction: Instruction) {
        stack[top + 1] = stack[spp(instruction.arg2, fp) + instruction.arg1]
        top += 1
        pc += 1
    }

    private fun store(instruction: Instruction) {
        stack[spp(instruction.arg2, fp) + instruction.arg1] = stack[top]
        top -= 1
        pc += 1
        stack.pop()
    }

    private fun const(instruction: Instruction) {
        stack[top + 1] = instruction.arg1
        top += 1
        pc += 1
    }

    override fun toString(): String {
        return "Configuration: PC = $pc, PP = $pp, FP = $fp, TOP = $top , stack: $stack"
    }

}