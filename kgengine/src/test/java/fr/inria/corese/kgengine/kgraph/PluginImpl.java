package fr.inria.corese.kgengine.kgraph;

import fr.inria.corese.sparql.cg.datatype.DatatypeMap;
import fr.inria.corese.kgenv.eval.ProxyImpl;
import fr.inria.corese.kgram.api.core.Expr;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.kgram.core.Eval;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Memory;

public class PluginImpl extends ProxyImpl {
	
	
	public Object eval(Expr exp, Environment env, Object[] args) {
		switch (exp.oper()){
		
		case PROCESS:
			if (env instanceof Memory){
				Memory mem = (Memory) env;
				System.out.println(mem.current().size());
				for (Mapping map : mem.current()){
					//System.out.println(map);
				}
			}
		}
		
		return TRUE;
		
	}
	
	public Object test(Object obj, Object env){
		
		Memory mem = (Memory) env;
		
		System.out.println(mem.current().size());
		
		System.out.println(mem);
								
		mem.getEvaluator();
		
		mem.getEventManager();
		
		mem.getMatcher();
		
		Eval kgram = mem.getEval();
		
		System.out.println(kgram.getStack());
		
		Producer p = kgram.getProducer();	
		
		//kgram.set(ProducerImpl.create(Graph.create()));
		
		return DatatypeMap.TRUE;
	}
	
	

}
