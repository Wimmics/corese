package fr.inria.corese.kgram.tool;

import java.util.Iterator;

/**
 * Iterator over iterators
 * meta.next(meta)
 * 
 * @author Olivier Corby, Edelweiss, INRIA 2010
 *
 */
public class MetaIteratorCast<T1, T2> implements Iterator<T2>, Iterable<T2> {
	// definition
	Iterable<T1> first;
	MetaIteratorCast<T1, T2> rest;
	
	// runtime
	Iterator<T1> it;
	MetaIteratorCast<T1, T2> next;
	
	public MetaIteratorCast(){}
	
	public MetaIteratorCast(Iterable<T1> tt){
		first = tt;
	}
	
	public boolean isEmpty(){
		return first == null;
	}
	
	public void next(MetaIteratorCast<T1, T2> m){
		set(m);
	}
	
	public void next(Iterable<T1> m){
		if (first == null) first = m;
		else set(new MetaIteratorCast<T1, T2>(m));
	}
	
	
	Iterator<T1> getIterator(){
		return first.iterator();
	}
	
	MetaIteratorCast<T1, T2> getRest(){
		return rest;
	}
	
	void set(MetaIteratorCast<T1, T2> m){
		if (rest == null) rest = m;
		else rest.set(m);
	}

	
	@Override
	public boolean hasNext() {
		if (it.hasNext()){
			return true;
		}
		
		if (next == null) return false;
		it = next.getIterator();
		next = next.getRest();
		return hasNext();
	}

	@Override
	public T2 next() {
		T2 obj = (T2)it.next();
		if (obj == null){
			// current iterator has completed; check next
			if (hasNext()){
				return next();
			}
		}
		return obj;
	}

	@Override
	public void remove() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Iterator<T2> iterator() {
		// TODO Auto-generated method stub
		it = getIterator();
		next = getRest();
		return this;
	}
	
	

}
