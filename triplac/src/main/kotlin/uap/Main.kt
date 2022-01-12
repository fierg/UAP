package uap;

import de.unitrier.st.uap.*
import uap.flattener.Flattener
import uap.tram.abstractMachine.AbstractMachine
import uap.generator.TramCodeGenerator
import uap.node.Node
import java.io.BufferedWriter
import java.io.FileReader
import java.io.FileWriter
import java.io.PrintWriter

internal object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        if (args.isEmpty()) {
            System.err.println("Please provide a file.")
            return
        }
        val ast: Node
        val fileName = args[0]
        val triplaParser = Parser(de.unitrier.st.uap.Lexer(FileReader(fileName)))
        ast = triplaParser.parse().value as Node

        printAST(fileName, ast)

        val f = Flattener()
        ast.accept(f)

        printAST(fileName,ast)

        val t = TramCodeGenerator(ast)
        val output = t.generate()

        for (i in output) {
            println(i.toString())
        }

        val abstractMachine = AbstractMachine(output.toTypedArray(),true)
        abstractMachine.run()

    }

    private fun printAST(fileName: String, ast: Node) {
        var fileName1 = fileName
        var pw: PrintWriter? = null
        try {
            fileName1 = fileName1.substring(fileName1.lastIndexOf("/") + 1, fileName1.lastIndexOf("."))
            fileName1 = String.format("%s-ast.xml", fileName1)
            pw = PrintWriter(BufferedWriter(FileWriter(fileName1)))
            pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>")
            pw.print(ast.toString())
            System.out.printf("\"%s\" file created\n", fileName1)
        } catch (e: Exception) {
            System.err.println(e.message)
        } finally {
            pw?.close()
        }
    }
}