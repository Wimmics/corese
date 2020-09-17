package fr.inria.corese.test.engine;

import fr.inria.corese.compiler.eval.Interpreter;
import fr.inria.corese.compiler.parser.FunctionCompiler;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.parser.Access;
import fr.inria.corese.sparql.triple.parser.AccessNamespace;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author corby
 */
public class TestAccessRightNS {

   void clear() {
        FunctionCompiler.clean();
        AccessNamespace.clean();
        Interpreter.getExtension().removeNamespace("/user/corby/home/AATest/data/junit/function/");
    }


    @Test
    public void mytest9() throws EngineException {
        String q = "prefix ff: </user/corby/home/AATest/data/junit/function/accept.rq/>"
                // + "@import ff: "
                + "select (coalesce(funcall(ff:test), false) as ?t) where {}";

                clear();

        AccessNamespace.define("/user/corby/home/AATest/data/junit/function/accept", true);
        AccessNamespace.define("/user/corby/home/AATest/data/junit/function/reject", !true);
        Access.setLinkedFunction(true);
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        Mappings map = exec.query(q);
        //System.out.println(map);
        IDatatype dt = (IDatatype) map.getValue("?t");
        Assert.assertEquals(false, dt.booleanValue());
    }

 

    @Test
    public void mytest8() throws EngineException {
        System.out.println("TEST8");
        String q = "prefix ff: </user/corby/home/AATest/data/junit/function/accept.rq/>"
                // + "@import ff: "
                + "select (coalesce(funcall(ff:test), false) as ?t) where {}";

        clear();
        AccessNamespace.define("/user/corby/home/AATest/data/junit/function/accept", true);
        AccessNamespace.define("/user/corby/home/AATest/data/junit/function/reject", true);
        Access.setLinkedFunction(true);
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        Mappings map = exec.query(q);
        //System.out.println(map);
        IDatatype dt = (IDatatype) map.getValue("?t");
        Assert.assertEquals(true, dt.booleanValue());
    }

    @Test
    public void mytest7() throws EngineException {
        String q = "prefix ff: </user/corby/home/AATest/data/junit/function/accept.rq/>"
                // + "@import ff: "
                + "select (coalesce(funcall(ff:test), false) as ?t) where {}";

        clear();

        AccessNamespace.define("/user/corby/home/AATest/data/junit/function/accept", true);
//        AccessNamespace.define("/user/corby/home/AATest/data/junit/function/reject", true);
        Access.setLinkedFunction(true);
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        Mappings map = exec.query(q);
        //System.out.println(map);
        IDatatype dt = (IDatatype) map.getValue("?t");
        Assert.assertEquals(false, dt.booleanValue());
    }

    @Test
    public void mytest6() throws EngineException {
        String q = "prefix ff: </user/corby/home/AATest/data/junit/function/accept.rq/>"
                // + "@import ff: "
                + "select (coalesce(funcall(ff:test), false) as ?t) where {}";

        clear();

//        AccessNamespace.define("/user/corby/home/AATest/data/junit/function/accept", true);
//        AccessNamespace.define("/user/corby/home/AATest/data/junit/function/reject", true);
        Access.setLinkedFunction(true);
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        Mappings map = exec.query(q);
        //System.out.println(map);
        IDatatype dt = (IDatatype) map.getValue("?t");
        Assert.assertEquals(true, dt.booleanValue());
    }

    @Test
    public void mytest5() throws EngineException {
        String q = "prefix ff: </user/corby/home/AATest/data/junit/function/accept.rq/>"
                // + "@import ff: "
                + "select (coalesce(funcall(ff:test), false) as ?t) where {}";

        clear();

//        AccessNamespace.define("/user/corby/home/AATest/data/junit/function/accept", true);
//        AccessNamespace.define("/user/corby/home/AATest/data/junit/function/reject", true);
        //Access.setLinkedFunction(true);
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        Mappings map = exec.query(q);
        //System.out.println(map);
        IDatatype dt = (IDatatype) map.getValue("?t");
        Assert.assertEquals(false, dt.booleanValue());
    }

     @Test
   public void mytest4() throws EngineException {
        String q = "prefix ff: </user/corby/home/AATest/data/junit/function/accept.rq/>"
                + "@import ff: "
                + "select (coalesce(funcall(ff:test), false) as ?t) where {}";

        clear();

        Interpreter.getExtension().removeNamespace("/user/corby/home/AATest/data/junit/function/");
        AccessNamespace.define("/user/corby/home/AATest/data/junit/function/reject", !true);
        //Access.setLinkedFunction(true);
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        Mappings map = exec.query(q);
        //System.out.println(map);
        IDatatype dt = (IDatatype) map.getValue("?t");
        Assert.assertEquals(false, dt.booleanValue());
    }

    @Test
    public void mytest3() throws EngineException {
        String q = "prefix ff: </user/corby/home/AATest/data/junit/function/accept.rq/>"
                + "@import ff: "
                + "select (coalesce(funcall(ff:test), false) as ?t) where {}";

        clear();

        AccessNamespace.define("/user/corby/home/AATest/data/junit/function/accept", true);
        AccessNamespace.define("/user/corby/home/AATest/data/junit/function/reject", true);
        //Access.setLinkedFunction(true);
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        Mappings map = exec.query(q);
        //System.out.println(map);
        IDatatype dt = (IDatatype) map.getValue("?t");
        Assert.assertEquals(true, dt.booleanValue());
    }

     @Test
   public void mytest2() throws EngineException {
        String q = "prefix ff: </user/corby/home/AATest/data/junit/function/accept.rq/>"
                + "@import ff: "
                + "select (coalesce(funcall(ff:test), false) as ?t) where {}";

        clear();

        AccessNamespace.define("/user/corby/home/AATest/data/junit/function/accept", true);
//        AccessNamespace.define("/user/corby/home/AATest/data/junit/function/reject", true);
        //Access.setLinkedFunction(true);
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        Mappings map = exec.query(q);
        //System.out.println(map);
        IDatatype dt = (IDatatype) map.getValue("?t");
        Assert.assertEquals(false, dt.booleanValue());
    }

    @Test
    public void mytest1() throws EngineException {
        String q = "prefix ff: </user/corby/home/AATest/data/junit/function/accept.rq/>"
                + "@import ff: "
                + "select (coalesce(funcall(ff:test), false) as ?t) where {}";
        clear();

//        AccessNamespace.define("/user/corby/home/AATest/data/junit/function/accept", true);
//        AccessNamespace.define("/user/corby/home/AATest/data/junit/function/reject", true);
        //Access.setLinkedFunction(true);
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        Mappings map = exec.query(q);
        //System.out.println(map);
        IDatatype dt = (IDatatype) map.getValue("?t");
        Assert.assertEquals(true, dt.booleanValue());
    }

}
