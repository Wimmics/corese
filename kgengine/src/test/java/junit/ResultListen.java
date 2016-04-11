/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package junit;

import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Expr;
import fr.inria.edelweiss.kgram.api.core.Regex;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.core.Exp;
import fr.inria.edelweiss.kgram.event.ResultListener;
import fr.inria.edelweiss.kgram.path.Path;

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
      
