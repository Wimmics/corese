package fr.inria.corese.kgram.event;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * Event Manager to trace KGRAM execution
 *
 * @author Olivier Corby, Edelweiss, INRIA 2010
 *
 */
public class EventManager implements Iterable<EventListener> {

    boolean isEval = false;

    List<EventListener> observers = new Vector<EventListener>();

    public static EventManager create() {
        return new EventManager();
    }

    public void addEventListener(EventListener el) {
        observers.add(el);
        isEval = isEval || (el.handle(Event.START));

    }

    public void removeEventListener(EventListener el) {
        observers.remove(el);
    }

    public void removeEventListener(int sort) {
        for (int i = 0; i < observers.size();) {
            EventListener el = observers.get(i);
            if (el.handle(sort)) {
                observers.remove(el);
            } else {
                i++;
            }
        }
    }

    public List<EventListener> getEventListeners() {
        return observers;
    }

    public void setObject(Object obj) {
        for (EventListener el : observers) {
            el.setObject(obj);
        }
    }

    public boolean handle(int sort) {
        switch (sort) {
            case Event.START:
                return isEval;
            default:
                return true;
        }
    }

    @Override
    public Iterator<EventListener> iterator() {
        return observers.iterator();
    }

    public boolean send(Event event) {
        boolean res = true;
        for (EventListener el : observers) {
            if (el.handle(event.getSort())) {
                boolean b = el.send(event);
                res = res && b;
            }
        }
        return res;
    }

}
