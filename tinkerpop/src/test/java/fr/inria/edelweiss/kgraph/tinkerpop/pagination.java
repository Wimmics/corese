package fr.inria.edelweiss.kgraph.tinkerpop;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import org.apache.tinkerpop.gremlin.orientdb.OrientGraph;
import org.apache.tinkerpop.gremlin.orientdb.OrientGraphFactory;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.util.iterator.IteratorUtils;
/**
 *
 * @author edemairy
 */
public class pagination {
public void main(String[] args) {
	OrientGraphFactory factory = new OrientGraphFactory("plocal:/Users/edemairy/btc_bd_odb");
	Graph graph = factory.getTx();
//	graph.traversal().V().hasLabel("person").fold().project("users","userCount").by(range(local, 0, 2)).by(count(local));
}	
}
