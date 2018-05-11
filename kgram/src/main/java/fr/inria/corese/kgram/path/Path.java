package fr.inria.corese.kgram.path;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Entity;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.kgram.tool.EdgeInv;
import fr.inria.corese.kgram.tool.EntityImpl;
import fr.inria.corese.kgram.tool.ProducerDefault;

/**
 *
 * List of relations between two resources found by path Can be used as a
 * Producer to enumerate path edges/nodes
 *
 * @author Olivier Corby, Edelweiss, INRIA 2010
 *
 */
public class Path extends ProducerDefault {

    boolean loopNode = true,
            isShort = false,
            isReverse = false;
    int max = Integer.MAX_VALUE;
    int weight = 0;
    ArrayList<Entity> path;
    int radius = 0;

    public Path() {
        setMode(Producer.EXTENSION);
        path = new ArrayList<Entity>();
    }

    public Path(boolean b) {
        this();
        isReverse = b;
    }

    Path(int n) {
        setMode(Producer.EXTENSION);
        path = new ArrayList<Entity>(n);
    }

    public ArrayList<Entity> getEdges() {
        return path;
    }

    void setIsShort(boolean b) {
        isShort = b;
    }

    void setMax(int m) {
        max = m;
    }

    int getMax() {
        return max;
    }

    void checkLoopNode(boolean b) {
        loopNode = b;
    }

    public void clear() {
        path.clear();
    }

    public void add(Entity ent) {
        path.add(ent);
    }

    public void add(Entity ent, int w) {
        path.add(ent);
        weight += w;
    }

    public void remove(Entity ent, int w) {
        path.remove(path.size() - 1);
        weight -= w;
    }

    public void remove() {
        path.remove(path.size() - 1);
    }

    // after reverse path
    public Node getSource() {
        return getEdge(0).getNode(0);
    }

    public Node getTarget() {
        return getEdge(size() - 1).getNode(1);
    }

    // before reverse path
    // edge may be EdgeInv in case of ^p
    // firstNode is the SPARQL binding of subject node
    public Node firstNode() {
        int fst = 0;
        if (isReverse) {
            fst = size() - 1;
        }
        return get(fst).getNode(0);
    }

    // lastNode is the SPARQL binding of object node
    public Node lastNode() {
        int lst = size() - 1;
        if (isReverse) {
            lst = 0;
        }
        return get(lst).getNode(1);
    }

    public Entity get(int n) {
        return path.get(n);
    }

    // Edge or EdgeInv
    public Edge getEdge(int n) {
        Entity ent = path.get(n);
        if (ent instanceof EdgeInv) {
            return (EdgeInv) ent;
        }
        return ent.getEdge();
    }

    public Entity last() {
        if (size() > 0) {
            return get(size() - 1);
        } else {
            return null;
        }
    }

    public Path copy() {
        Path path = new Path(size());
        for (Entity ent : this.path) {
            // when r is reverse, add real target relation
            if (ent instanceof EdgeInv) {
                EdgeInv ee = (EdgeInv) ent;
                path.add(ee.getEntity());
            } else {
                path.add(ent);
            }
        }
        path.setWeight(weight);
        return path;
    }

    public Path copy(Producer p) {
        Path path = new Path(size());
        for (Entity ent : this.path) {
            // when r is reverse, add real target relation
            if (ent instanceof EdgeInv) {
                ent = ((EdgeInv) ent).getEntity();
            } 
            path.add(p.copy(ent));
        }
        path.setWeight(weight);
        return path;
    }

    public int length() {
        return path.size();
    }

    public int size() {
        return path.size();
    }

    public int weight() {
        return weight;
    }

    void setWeight(int w) {
        weight = w;
    }

    // nb getResultValues()
    public int nbValues() {
        return 1 + 2 * path.size();
    }

    public void setRadius(int d) {
        radius = d;
    }

    public int radius() {
        return radius;
    }

    public Path reverse() {
        for (int i = 0; i < length() / 2; i++) {
            Entity tmp = path.get(i);
            path.set(i, path.get(length() - i - 1));
            path.set(length() - i - 1, tmp);
        }
        return this;
    }

    public Iterable<Entity> nodes() {

        return () -> elements();
    }

    Entity entity(Node node) {
        return EntityImpl.create(null, node);
    }

    /**
     * Enumerate resources and properties of the path in order first and last
     * included
     */
    public Iterator<Entity> elements() {

        return new Iterator<Entity>() {
            private int i = 0;
            private int j = 0;
            private int ii;
            private boolean hasNext = length() > 0 ? true : false;

            @Override
            public boolean hasNext() {
                return hasNext;
            }

            @Override
            public Entity next() {
                switch (j) {
                    case 0:
                        j = 1;
                        return entity(path.get(i).getEdge().getNode(0));
                    case 1:
                        ii = i;
                        if (i == path.size() - 1) {
                            j = 2;
                        } else {
                            j = 0;
                            i++;
                        }
                        return entity(path.get(ii).getEdge().getEdgeNode());
                    case 2:
                        hasNext = false;
                        j = -1;
                        return entity(path.get(i).getEdge().getNode(1));
                }
                return null;
            }

            @Override
            public void remove() {
            }
        };

    }
    
    public Iterator<Node> nodeIterator() {

        return new Iterator<Node>() {
            private int i = 0;
            private int j = 0;
            private int ii;
            private boolean hasNext = length() > 0 ? true : false;

            @Override
            public boolean hasNext() {
                return hasNext;
            }

            @Override
            public Node next() {
                switch (j) {
                    case 0:
                        j = 1;
                        return path.get(i).getEdge().getNode(0);
                    case 1:
                        ii = i;
                        if (i == path.size() - 1) {
                            j = 2;
                        } else {
                            j = 0;
                            i++;
                        }
                        return path.get(ii).getEdge().getEdgeNode();
                    case 2:
                        hasNext = false;
                        j = -1;
                        return path.get(i).getEdge().getNode(1);
                }
                return null;
            }

            @Override
            public void remove() {
            }
        };

    }
    

    @Override
    public String toString() {
        String str = "path[" + path.size() + "]{";
        if (path.size() > 1) {
            str += "\n";
        }
        for (Entity edge : path) {
            str += edge + "\n";
        }
        str += "}";
        return str;
    }

    public void trace() {
        int i = 0;
        for (Iterator<Entity> it = elements(); it.hasNext();) {
            Node cc = it.next().getNode();
            System.out.println(i++ + " " + cc + " ");
        }
        System.out.println();
    }

    @Override
    public Iterable<Entity> getEdges(Node gNode, List<Node> from, Edge qEdge, Environment env) {
        return path;
    }

    @Override
    public Iterable<Entity> getNodes(Node gNode, List<Node> from, Node qNode,
            Environment env) {
        return nodes();
    }
    
}
