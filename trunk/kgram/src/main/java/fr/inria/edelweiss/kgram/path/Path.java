package fr.inria.edelweiss.kgram.path;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.tool.EdgeInv;
import fr.inria.edelweiss.kgram.tool.EntityImpl;
import fr.inria.edelweiss.kgram.tool.ProducerDefault;

/**
 * 
 * List of relations between two resources found by path
 * Can be used as a Producer to enumerate path edges/nodes
 * 
 * @author Olivier Corby, Edelweiss, INRIA 2010
 *
 */
public class Path extends ProducerDefault
{
	boolean loopNode = true,
	isShort = false, 
	isReverse = false;
	int max = Integer.MAX_VALUE;
	int weight = 0;
	
	ArrayList<Entity> path;
	
	int radius = 0;

	public Path(){
		path = new ArrayList<Entity>();
	}
	
	public Path(boolean b){
		this();
		isReverse = b;
	}
	
	Path(int n){
		path = new ArrayList<Entity>(n);
	}
	
	public ArrayList<Entity> getEdges(){
		return path;
	}
	
	void setIsShort(boolean b){
		isShort = b;
	}
	
	void setMax(int m){
		max = m;
	}
	
	int getMax(){
		return max;
	}
	
	void checkLoopNode(boolean b){
		loopNode = b;
	}
	
	public void clear(){
		path.clear();
	}
	
	public void add(Entity ent){
		path.add(ent);
	}

	public void add(Entity ent, int w){
		path.add(ent);
		weight += w;
	}
	
	public void remove(Entity ent, int w){
		path.remove(path.size()-1);
		weight -= w;
	}
	
	public void remove(){
		path.remove(path.size()-1);
	}

	// after reverse path
	public Node getSource(){
		return getEdge(0).getNode(0);
	}
	
	public Node getTarget() {
		return getEdge(size()-1).getNode(1);
	}
	
	// before reverse path
	// edge may be EdgeInv in case of ^p
	// firstNode is the SPARQL binding of subject node
	public Node firstNode(){
		int fst = 0;
		if (isReverse){
			fst = size()-1;
		}
		return get(fst).getNode(0);
	}
	
	// lastNode is the SPARQL binding of object node
	public Node lastNode() {
		int lst = size()-1;
		if (isReverse){
			lst = 0;
		}
		return get(lst).getNode(1);
	}
	
	
	public Entity get(int n){
		return path.get(n);
	}
	
	// Edge or EdgeInv
	public Edge getEdge(int n){
		Entity ent = path.get(n);
		if (ent instanceof EdgeInv){
			return (EdgeInv) ent;
		}
		return ent.getEdge();
	}
	
	public Entity last(){
		if (size()>0) return get(size()-1);
		else return null;
	}
	
	public Path copy(){
		Path path = new Path(size());
		for (Entity ent : this.path){
			// when r is reverse, add real target relation
			if (ent instanceof EdgeInv){
				EdgeInv ee = (EdgeInv) ent;
				path.add(ee.getEntity());
			}
			else {
				path.add(ent);
			}		
		}
		path.setWeight(weight);
		return path;
	}
	
	public int length(){
		return path.size();
	}
	
	public int size(){
		return path.size();
	}	

	public int weight(){
		return weight;
	}
	
	void setWeight(int w){
		weight = w;
	}
	
	// nb getResultValues()
	public int nbValues(){
		return 1 + 2 * path.size();
	}
	
	public void setRadius(int d){
		radius = d;
	}
	
	public int radius(){
		return radius;
	}
	
	public Path reverse(){
		for (int i=0; i < length() / 2; i++){
			Entity tmp = path.get(i);
			path.set(i, path.get(length()-i-1));
			path.set(length()-i-1, tmp);
		}
		return this;
	}
	
	
	
	/**
	 * Check that last edge does not loop on previous Nodes
	 * TODO:
	 * This check is too restrictive
	 * Should check nodes of current exp* only
	 * Should check intermediate nodes of exp only
	 */
//	public boolean loop(int index, int other){
//		if (path.size()<=1) return false;
//		Edge edge = last();
//		for (int i = 0; i < size()-1; i++){
//			Edge r = path.get(i);
//			if (edge.getNode(other).same(r.getNode(other))){
//				return true;
//			}
//		}
//		return false;
//	}
	
	/**
	 * Does relation intercept the path, introducing a loop ?
	 * use case:
	 * (p1/p2)*
	 * check that pi:rel is not already reached
	 * 
	 * TODO:
	 * (p1/p2)* / (p2/p3)*
	 * 
	 * This test is too restrictive but it does not loop
	 */
//	public boolean loop2(Edge rel, Node cstart, int index, int other){
//		//if (loopNode) return loop2(rel, cstart, index, other);
//		
//		// 	 Check that it does not loop on Edge
//		if (rel instanceof EdgeInv){
//			rel = ((EdgeInv) rel).getEdge();
//		}
//		for (Edge ee : path){
//			if (ee instanceof EdgeInv){
//				ee = ((EdgeInv) ee).getEdge();
//			}
//			if (rel == ee) return true;
//		}
//		return false;
//	}
	

	
	
//	public boolean loop(Edge rel, Node cstart, int index, int other){
//		if (rel.getNode(index).same(rel.getNode(other))){
//			return true;
//		}
//		for (Edge r : path){
//			if (rel.getNode(other).same(r.getNode(index))){
//				return true;
//			}
//		}
//		return false;
//	}
	
	
	public Iterable<Entity> nodes(){
		
		return new Iterable<Entity>(){

			public Iterator<Entity> iterator() {
				return elements();
			}
			
		};
	}

	Entity entity(Node node){
		return EntityImpl.create(null, node);
	}
	
	/**
	 * Enumerate resources and properties of the path in order
	 * first and last included
	 */
	public Iterator<Entity> elements(){
		
		return new Iterator<Entity>(){
			private int i=0;
			private int j=0;
			private int ii;
			private boolean hasNext = length()>0 ? true : false;
			
			
			public boolean hasNext(){
				return hasNext;
			}
			
			public Entity next(){
				switch(j){
				case 0 : j = 1; return entity(path.get(i).getEdge().getNode(0));
				case 1 : ii = i;
					if (i == path.size()-1){
						j = 2;						
					}
					else {
						j = 0; 
						i++; 
					}
					return entity(path.get(ii).getEdge().getEdgeNode());
				case 2 : hasNext = false;
					j = -1;
					return entity(path.get(i).getEdge().getNode(1));
				}
				return null;
			}
			
			public void remove(){
				
			}
			
		};
		
	}
	
	public String toString(){
		String str = "path[" + path.size() + "]{";
		if (path.size() > 1) str += "\n";
		for (Entity edge : path){
			str += edge + "\n";
		}
		str += "}";
		return str;
	}
	
	public void trace(){
		int i = 0;
		for (Iterator<Entity> it = elements(); it.hasNext();){
			Node cc = it.next().getNode(); 
			System.out.println (i++ + " " + cc + " ");
		}
		System.out.println();
	}

	
	public Iterable<Entity> getEdges(Node gNode, List<Node> from, Edge qEdge, Environment env){
		return path;
	}

//		final Iterator<Edge> ii = path.iterator();
//		
//		return new Iterable<Entity> (){
//
//			@Override
//			public Iterator<Entity> iterator() {
//				// TODO Auto-generated method stub
//				return new Iterator<Entity>(){
//
//					@Override
//					public boolean hasNext() {
//						// TODO Auto-generated method stub
//						return ii.hasNext();
//					}
//
//					@Override
//					public Entity next() {
//						// TODO Auto-generated method stub
//						Edge edge = ii.next();
//						if (edge instanceof Entity){
//							return (Entity) edge;
//						}
//						Node node = null;
//						return EntityImpl.create(node, edge);
//					}
//
//					@Override
//					public void remove() {
//						// TODO Auto-generated method stub
//
//					}
//
//				};
//			}
//		};
//	}

	
	
	
	public Iterable<Entity> getNodes(Node gNode, List<Node> from, Node qNode,
			Environment env) {
		return nodes();
	}
	
	
	
	
	
	
	
	
	
	
	
}
