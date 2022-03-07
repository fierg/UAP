package uap;

import de.unitrier.st.uap.*
import uap.export.DOTWriter
import uap.flattener.Flattener
import uap.generator.ControlFlowGraphGenerator
import uap.node.Node
import uap.visual.VisualDemo
import java.io.BufferedWriter
import java.io.FileReader
import java.io.FileWriter
import java.io.PrintWriter
import de.unitrier.st.uap.*
import uap.analysis.DataFlowAnalysis

internal object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        Thread.sleep(100)

        val ast: Node
        val export = args[0].matches(Regex("-expcfg"))
        val fileName = if (export) args[1] else args[0]

        val triplaParser = Parser(de.unitrier.st.uap.Lexer(FileReader(fileName)))
        ast = triplaParser.parse().value as Node

        printAST(fileName, ast)

        //AST Strukturverbesserung
        val f = Flattener()
        f.flatten(ast)

        val cfg = ControlFlowGraphGenerator(ast)
        val cfgGraph = cfg.generate()

        if (export) {
            VisualDemo.printGraphToImage(cfgGraph)
            DOTWriter.exportGraph(cfgGraph)
        }

        DataFlowAnalysis.analyzeLiveVariables(cfgGraph)

        if (export) {
            VisualDemo.printGraphToImage(cfgGraph)
            DOTWriter.exportGraph(cfgGraph)
        }

        /*
        val t = TramCodeGenerator(ast)
        val instructions = t.generate()

        for (instruction in instructions) {
            println(instruction.toString())
        }
        val abstractMachine = AbstractMachine(instructions.map { it.first }.toTypedArray(),true)
        abstractMachine.run()
         */
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