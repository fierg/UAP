package uap.visual

import com.mxgraph.layout.mxCircleLayout
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
            val layout: mxIGraphLayout = mxCircleLayout(graphAdapter)
            layout.execute(graphAdapter.getDefaultParent())

            val image = mxCellRenderer.createBufferedImage(graphAdapter, null, 2.0, Color.WHITE, true, null)
            val imgFile = File("data/graph.png")
            ImageIO.write(image, "PNG", imgFile)
        }
    }
}