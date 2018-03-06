package kgraph;

import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.edelweiss.kgenv.eval.ProxyImpl;
import fr.inria.edelweiss.kgram.api.core.Expr;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.api.query.Producer;
import fr.inria.edelweiss.kgram.core.Eval;
import fr.inria.edelweiss.kgram.core.Mapping;
import fr.inria.edelweiss.kgram.core.Memory;

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
