package uap;

import de.unitrier.st.uap.*
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
        var pw: PrintWriter? = null
        val ast: Node
        try {
            var fileName = args[0]
            val triplaParser = Parser(de.unitrier.st.uap.Lexer(FileReader(fileName)))
            ast = triplaParser.parse().value as Node
            fileName = fileName.substring(fileName.lastIndexOf("/") + 1, fileName.lastIndexOf("."))
            fileName = String.format("%s-ast.xml", fileName)
            pw = PrintWriter(BufferedWriter(FileWriter(fileName)))
            pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>")
            pw.print(ast.toString())
            System.out.printf("\"%s\" file created\n", fileName)
        } catch (e: Exception) {
            System.err.println(e.message)
        } finally {
            pw?.close()
        }
    }
}