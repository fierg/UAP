package uap.export

import uap.node.Node
import java.io.BufferedWriter
import java.io.FileWriter
import java.io.PrintWriter

class ASTWriter {
    companion object {
        fun printAST(fileName: String, ast: Node) {
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
}