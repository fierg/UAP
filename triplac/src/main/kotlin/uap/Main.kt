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
import uap.analysis.DataFlowAnalysis
import uap.generator.TramCodeGenerator

internal object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val ast: Node
        val export = args.any { Regex("-expcfg").matches(it) }
        val opt = args.any { Regex("-opt").matches(it) }
        val fileName = args.last()

        val triplaParser = Parser(de.unitrier.st.uap.Lexer(FileReader(fileName)))
        ast = triplaParser.parse().value as Node
        printAST(fileName, ast)

        //AST Strukturverbesserung
        val f = Flattener()
        f.flatten(ast)
        val cfg = ControlFlowGraphGenerator(ast)
        val cfgGraph = cfg.generate()

        if (export) {
            println("Pure CFG")
            DOTWriter.exportGraph(cfgGraph)
        }
        DataFlowAnalysis.analyzeLiveVariables(cfgGraph, export)
        DataFlowAnalysis.analyzeReachedUses(cfgGraph,export)

        if(opt) {
            DataFlowAnalysis.optimize(cfgGraph, ast)
            val cfg1 = ControlFlowGraphGenerator(ast)
            val cfgGraph1 = cfg1.generate()
            //DataFlowAnalysis.analyzeLiveVariables(cfgGraph, export)
            //DataFlowAnalysis.analyzeReachedUses(cfgGraph, export)
            if (export) {
                println("Optimized CFG")
                DOTWriter.exportGraph(cfgGraph1)
            }
        }

        val t = TramCodeGenerator(ast)
        val instructions = t.generate()

        for (instruction in instructions) {
            println(instruction.toString())
        }
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