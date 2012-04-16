package fr.inria.wimmics.endpoint;


import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgenv.eval.Dataset;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.query.QueryProcess;
import fr.inria.edelweiss.kgtool.load.Load;
import fr.inria.edelweiss.kgtool.print.ResultFormat;


/**
 * SPARQL Endpoint Servlet for Corese
 * implements SPARQL 1.1 Protocol using POST and GET
 * query and update
 * http://www.w3.org/TR/sparql11-protocol/
 */

public class SPARQLEndpointServlet extends HttpServlet {

	private static final String FROM 		= "default-graph-uri";
	private static final String NAMED 		= "named-graph-uri";
	private static final String USING_FROM 	= "using-graph-uri";
	private static final String USING_NAMED = "using-named-graph-uri";
	private static final String QUERY 		= "query";
	private static final String UPDATE 		= "update";
	
	private static final long serialVersionUID = 1L;



	private static Logger logger = Logger.getLogger(SPARQLEndpointServlet.class);
	
	Graph graph;

    @Override
    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
        throws IOException, ServletException
    {
    	boolean isQuery = true;
        List<String> from, named;
        String query  = request.getParameter(QUERY);
        
        if (query != null){
        	from  = getList(request, FROM);
        	named = getList(request, NAMED);
        }
        else {
        	isQuery = false;
        	query = request.getParameter(UPDATE);
           	from  = getList(request, USING_FROM);
        	named = getList(request, USING_NAMED);
        }
        
        Dataset ds = Dataset.create(from, named);
        // comment this to have default graph when there is named and no from
        // if not comment, it is std SPARQL semantics
        //ds.complete();
        
        PrintWriter out = response.getWriter();      

        Graph g = getGraph();
        QueryProcess exec = QueryProcess.create(g);
        try {
            if (query == null){
            	throw new EngineException("Undefined Query");
            }
            
            Mappings map = exec.query(query, ds);
						
			if (map.getQuery().isConstruct()){
		        response.setContentType("application/rdf+xml");
			}
			else {
		        response.setContentType("text/xml");
			}
			
			ResultFormat f = ResultFormat.create(map);
	        out.println(f);
		} catch (EngineException e) {
			// TODO Auto-generated catch block
			response.setStatus(400);
			e.printStackTrace();
			out.println(e.getMessage());
		}
      
    }

    @Override
    public void doPost(HttpServletRequest request,
                      HttpServletResponse response)
        throws IOException, ServletException
    {
        doGet(request, response);
    }
    
    
    List<String> getList(HttpServletRequest req, String name){
    	String[] sfrom = req.getParameterValues(name);
    	if (sfrom == null) return null;
    	ArrayList<String> list = new ArrayList<String>();
    	for (String g : sfrom){
    		list.add(g);

    	}
    	return list;
    }

    
    
    public static void main(String[] args){
    	
    }
    
    Graph getGraph(){
    	if (graph == null){
    		graph = start();
    	}
    	return graph;
    }
    
    Graph start(){
    	String PATH = "webapps/examples/WEB-INF/resources/data/test";
    	Graph g = Graph.create(true);
    	Load ld = Load.create(g);
//    	try {
//			ld.loadWE(PATH);
//		} catch (LoadException e) {
//			// TODO Auto-generated catch block
//			logger.error(PATH);
//			logger.error(e);
//			e.printStackTrace();
//			File f = new File(".");
//			logger.info(f.getAbsolutePath());
//			
//		}
    	return g;
    }

}

