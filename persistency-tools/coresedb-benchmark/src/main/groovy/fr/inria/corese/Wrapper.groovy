package fr.inria.corese

import com.thinkaurelius.titan.graphdb.database.management.ManagementSystem
import org.apache.tinkerpop.gremlin.structure.Edge
import org.apache.tinkerpop.gremlin.structure.Graph
import org.apache.tinkerpop.gremlin.structure.Vertex

class WrapperImpl {
    String path
    Graph graph
    ManagementSystem mgmt
    def vertexIndex
    def edgeIndex

    void connect() {
        graph = TitanFactory.open(path + "/conf.properties")
        mgmt = graph.openManagement()
    }

    def listVertexIndex() {
        vertexIndex = mgmt.getGraphIndexes(Vertex)
        vertexIndex.each {
            println "Vertex index: ${it}"
            showIndexInfos(it)
        }
    }

    def listEdgeIndex() {
        edgeIndex = mgmt.getGraphIndexes(Edge)
        edgeIndex.each {
            println "Edge index: ${it}"
            showIndexInfos(it)
        }
    }

    def showIndexInfos(index) {
        println index.getBackingIndex()
        println index.getFieldKeys()
    }

    void finalize() {
        graph.close()
    }
}

w = new Wrapper(path: "/Users/edemairy/Developpement/Corese-master/persistency-tools/coreseTimer-common/data/test1_db/")
w.connect()
w.listVertexIndex()
w.listEdgeIndex()
