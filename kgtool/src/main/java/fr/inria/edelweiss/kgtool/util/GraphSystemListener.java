package fr.inria.edelweiss.kgtool.util;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.ExpType;
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.edelweiss.kgraph.api.GraphListener;
import fr.inria.edelweiss.kgraph.core.Graph;

/**
 * Listener for the named graph kg:system
 * Tune the system:
 * insert data { graph kg:system { 
 *   kg:kgram kg:skolem true 
 * }}
 * 
 * @author Olivier Corby, Wimmics Inria I3S, 2013
 *
 */
class GraphSystemListener implements GraphListener {
    
    private static final String SKOLEM = ExpType.KGRAM + "skolem";

    Graph graph;
    
    GraphSystemListener(Graph g){
        graph = g;
    }

        @Override
        public void addSource(Graph g) {

        }

        @Override
        public boolean onInsert(Graph g, Entity ent) {
            return true;
        }

        @Override
        public void insert(Graph g, Entity ent) {
            System.out.println("** Listen: " + ent);
            process(ent);
        }
        
        /**
         * Some edges may tune kgram:
         * kg:kgram kg:skolem true
         */
        void process(Entity ent){
            Edge edge = ent.getEdge();
            String label = edge.getLabel();
            
            if (label.equals(SKOLEM)){
                IDatatype dt = (IDatatype) edge.getNode(1).getValue();
                graph.setSkolem(dt.booleanValue());
            }
        }

        @Override
        public void delete(Graph g, Entity ent) {
        }

        @Override
        public void start(Graph g, Query q) {
        }

        @Override
        public void finish(Graph g, Query q) {
        }
        
    }