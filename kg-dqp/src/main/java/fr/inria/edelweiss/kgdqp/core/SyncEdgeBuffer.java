package fr.inria.edelweiss.kgdqp.core;

import fr.inria.edelweiss.kgram.api.core.Entity;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.log4j.Logger;

/**
 * Synchronized buffer to put/get path edges
 * Edges are consumed by an iterator
 * 
 * @author Olivier Corby, Edelweiss, INRIA 2010
 *
 */
public class SyncEdgeBuffer implements Iterable<Entity>, Iterator<Entity> {

    private final Logger logger = Logger.getLogger(SyncEdgeBuffer.class);
    private BlockingQueue<Entity> queue = new LinkedBlockingQueue<Entity>(100);
    private ExecutorService executorService = null;
    private int nbPendingTasks = 0;

    SyncEdgeBuffer(int nbTasks, ExecutorService exec) {
        this.executorService = exec;
        nbPendingTasks = nbTasks;
    }

    @Override
    public Entity next() {
        Entity res = null;
        try {
//            logger.info("pre-take on "+this.hashCode());
            res = queue.take();
//            logger.info("end-take on "+this.hashCode());
            while (res instanceof Stop) {
                nbPendingTasks--;
//                logger.info(this.hashCode()+" content : "+this.toString());
                res = queue.peek();
                if (res instanceof Stop) {
                    queue.poll();
                }
            }
        } catch (InterruptedException ex) {
            logger.error("Interrupted next " + ex.getMessage());
        }
        return res;
    }

    @Override
    public boolean hasNext() {
//        logger.info("HasNext "+nbPendingTasks.get()+" pending tasks on "+this.hashCode());
        Entity next = queue.peek();
        if (next == null) {
            if (nbPendingTasks == 0) {
//                logger.info("FINNISHED "+this.hashCode());
                executorService.shutdown();
                return false;
            } else {
                return true;
            }
        } else {
            while (next instanceof Stop) {
                nbPendingTasks--;
//                logger.info("pending tasks -- = "+nbPendingTasks.get()+ " on "+this.hashCode());
                queue.poll();
                next = queue.peek();
//                logger.info("Removed stop ; next is "+next+" pending tasks = "+nbPendingTasks.get());
//                logger.info(this.toString());
            }
            if ((next == null) && (nbPendingTasks == 0)) {
//                logger.info("FINNISHED after cleaning STOPs on "+this.hashCode());
                executorService.shutdown();
                return false;
            } else {
                return true;
            }
        }
    }

    public void put(Entity ent) {
        try {
//            logger.info("pre-put on "+this.hashCode());
            queue.put(ent);
//            logger.info("end-put on "+this.hashCode());
        } catch (Exception ex) {
            logger.error("Interrupted next " + ex.getMessage());
        }
    }

    @Override
    public Iterator<Entity> iterator() {
        // TODO Auto-generated method stub
        return this;
    }

    @Override
    public void remove() {
        // TODO Auto-generated method stub
    }

    @Override
    public String toString() {
        String res = "{";
        Iterator it = queue.iterator();
        while (it.hasNext()) {
            res += it.next().getClass().getSimpleName() + " : ";
        }
        return res + "}";
    }
}
