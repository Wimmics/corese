package fr.inria.corese.coresetimer.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class MergedIterators<T> implements Iterator<T> {
    private ArrayList<Iterator<T>> listIterators = new ArrayList<>();
    private int currentIterator = 0;

    public MergedIterators(Iterator<T>... args) {
        for (int i = 0; i < args.length; i++) {
            listIterators.add(args[i]);
        }
    }

    @Override
    public boolean hasNext() {
        while (currentIterator < listIterators.size()) {
            if (listIterators.get(currentIterator).hasNext()) {
                return true;
            } else {
                currentIterator++;
            }
        }
        return false;
    }

    @Override
    public T next() {
        if (hasNext()) {
            return listIterators.get(currentIterator).next();
        } else {
            throw new NoSuchElementException();
        }
    }

}
