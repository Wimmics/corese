package fr.inria.corese.kgram.tool;

import java.util.Iterator;

/**
 * Iterator over iterators meta.next(meta)
 *
 * @author Olivier Corby, Edelweiss, INRIA 2010
 *
 */
public class MetaIterator<T> implements Iterator<T>, Iterable<T> {

    Iterable<T> first;
    MetaIterator<T> rest;

    // runtime
    Iterator<T> it;
    MetaIterator<T> next;

    public MetaIterator() {
    }

    public MetaIterator(Iterable<T> tt) {
        first = tt;
    }

    public boolean isEmpty() {
        return first == null;
    }

    public void next(MetaIterator<T> m) {
        set(m);
    }

    public void next(Iterable<T> m) {
        if (first == null) {
            first = m;
        } else {
            set(new MetaIterator<>(m));
        }
    }

    Iterator<T> getIterator() {
        return first.iterator();
    }

    MetaIterator<T> getRest() {
        return rest;
    }

    void set(MetaIterator<T> m) {
        if (rest == null) {
            rest = m;
        } else {
            rest.set(m);
        }
    }

    @Override
    public boolean hasNext() {
        if (it.hasNext()) {
            return true;
        }

        if (next == null) {
            return false;
        }
        it = next.getIterator();
        next = next.getRest();
        return hasNext();
    }

    @Override
    public T next() {
        T obj = it.next();
        if (obj == null) {
            // current iterator has completed; check next
            if (hasNext()) {
                return next();
            }
        }
        return obj;
    }

    @Override
    public void remove() {

    }

    @Override
    public Iterator<T> iterator() {
        it = getIterator();
        next = getRest();
        return this;
    }

}
