/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.inria.corese.kgengine.junit;

import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Entity;
import fr.inria.corese.kgram.api.core.Expr;
import fr.inria.corese.kgram.api.core.Regex;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.core.Exp;
import fr.inria.corese.kgram.event.ResultListener;
import fr.inria.corese.kgram.path.Path;

class ResultListen implements ResultListener {

        @Override
        public boolean process(Environment env) {
            
            return true;
        }

        @Override
        public boolean process(Path path) {
            System.out.println(path);
             return true;
        }

        @Override
        public boolean enter(Entity ent, Regex exp, int size) {
             return true;
        }

        @Override
        public boolean leave(Entity ent, Regex exp, int size) {
             return true;
        }

        @Override
        public boolean listen(Edge edge, Entity ent) {
             return true;
        }

        @Override
        public Exp listen(Exp exp, int n) {
            return exp;
        }

        @Override
        public void listen(Expr exp) {
            
        }
    }
      
