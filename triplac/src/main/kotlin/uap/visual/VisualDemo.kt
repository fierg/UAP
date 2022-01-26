package uap.visual

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout
import com.mxgraph.layout.mxIGraphLayout
import com.mxgraph.util.mxCellRenderer
import org.jgrapht.ext.JGraphXAdapter
import uap.cfg.CFG
import uap.cfg.CFGNode
import uap.cfg.Edge
import java.awt.Color
import java.io.File
import javax.imageio.ImageIO


class VisualDemo {
    companion object {
        fun printGraphToImage(graph: CFG) {
            val graphAdapter: JGraphXAdapter<CFGNode, Edge> = JGraphXAdapter(graph.graph)
            val layout: mxIGraphLayout = mxHierarchicalLayout(graphAdapter)
            //val layout: mxIGraphLayout = mxFastOrganicLayout(graphAdapter)
            layout.execute(graphAdapter.getDefaultParent())

            val image = mxCellRenderer.createBufferedImage(graphAdapter, null, 2.0, Color.WHITE, true, null)
            val imgFile = File("triplac/data/graph.png")
            ImageIO.write(image, "PNG", imgFile)

        }
    }
}