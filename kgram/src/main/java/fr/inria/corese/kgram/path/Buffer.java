package fr.inria.corese.kgram.path;

import java.util.Iterator;

import fr.inria.corese.kgram.core.Mapping;

/**
 * Synchronized buffer to put/get path edges
 * Edges are consumed by an iterator
 * 
 * @author Olivier Corby, Edelweiss, INRIA 2010
 *
 */
public class Buffer implements  
	Iterable<Mapping>, Iterator<Mapping>

	{
	
	private Mapping map;
	private boolean hasNext = true;
	private boolean available = false; 
	
	
	public synchronized Mapping next(){
		while (available == false) {
			try {
				wait();
			} catch (InterruptedException e) {
			}
		}
		available = false;
		notify();
		return map;
	}
	
	
	public synchronized boolean hasNext(){
		while (available == false) {
			try {
				wait();
			} catch (InterruptedException e) {
			}
		}
		notify();
		return hasNext;
	}

	
	
	public synchronized void put(Mapping val, boolean next){
		while (available == true) {
			try {
				wait();
			} catch (InterruptedException e) {
			}
		}
		map = val;
		hasNext = next;
		available = true;
		notify();
	}

	public Iterator<Mapping> iterator() {
		// TODO Auto-generated method stub
		return this;
	}
	
	
	@Override
	public void remove() {
		// TODO Auto-generated method stub
		
	}
	
}
