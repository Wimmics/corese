package fr.inria.corese.core.print;

import java.util.ArrayList;
import java.util.List;

import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.api.core.Edge;

public class Mapper {
	
	List<Mapping> lMap;
	
	Mapper (){
		lMap = new ArrayList<Mapping>();
	}
		
	Mapper (Mapping m){
		this();
		add(m);
	}
	
	public static Mapper create(Mapping m){
		return new Mapper(m);
	}
	
	public static Mapper create(){
		return new Mapper();
	}
	
	void add(Mapping m){
		lMap.add(m);
	}
	
	
	Node getMapType(Node node){
		return null;
	}
	
	Iterable<Node> getMapNodes(){
		ArrayList<Node> list = new ArrayList<>();
		//getMapNodes(lMap.get(0), list);
		for (Mapping map : lMap){
			getMapNodes(map, list);
		}
		return list;
	}
	
	Iterable<Node> getMapNodes(Mapping map, ArrayList<Node> list){
		if (map.getMappings() != null){
			for (Mapping m : map.getMappings()){
				getMapNodes(m, list);
			}
		}
		else {
			getNodes(map, list);
		}
		return list;
	}
	
	Iterable<Node> getNodes(Mapping m, List<Node> list){
            for (Edge edge : m.getEdges()) {
                Node node = edge.getNode(0);
                if (!list.contains(node)) {
                    list.add(node);
                }
            }
            return list;
	}
	
	
	Iterable<Edge> getMapEdges(Node node){
		ArrayList<Edge> list = new ArrayList<Edge>();
		//getMapEdges(lMap.get(0), node, list);
		for (Mapping map : lMap){
			getMapEdges(map, node, list);
		}
		return list;
	}
	
	
	Iterable<Edge> getMapEdges(Mapping map, Node node, ArrayList<Edge> list){
		if (map.getMappings() != null){
			for (Mapping m : map.getMappings()){
				getMapEdges(node, m, list);
			}
		}
		else {
			getMapEdges(node, map, list);
		}
		return list;
	}
	
	void getMapEdges(Node node, Mapping map, List<Edge> list){
		for (Edge edge : map.getEdges()){
			if (edge instanceof Edge){
				Edge ent = (Edge) edge;
				if (edge.getNode(0).equals(node) && ! list.contains(ent)){
					list.add(ent);
				}
			}
		}
	}
	
	
	
	
	
	
	
	
	

}
