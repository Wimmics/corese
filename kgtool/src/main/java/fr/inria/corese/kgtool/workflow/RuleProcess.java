package fr.inria.corese.kgtool.workflow;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgraph.api.Loader;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.rule.Rule;
import fr.inria.edelweiss.kgraph.rule.RuleEngine;
import fr.inria.edelweiss.kgtool.load.Load;
import fr.inria.edelweiss.kgtool.load.LoadException;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public class RuleProcess extends  AbstractProcess {

    String path;
    int profile = -1;
    
    public RuleProcess(String p){
        path = p;
    }
    
    public RuleProcess(int p){
        profile = p;
    }
    
    @Override
    public Data process(Data data) throws EngineException {
        try {
            RuleEngine re = create(data.getGraph());            
            re.process();
            Data res = new Data(data.getGraph());
            res.setProcess(this);
            setData(res);
            return res;
        } catch (LoadException ex) {
            throw new EngineException(ex);
        }
    }
    
    @Override
    public String stringValue(Data data){
        return data.getGraph().toString();
    }
    
    
    RuleEngine create(Graph g) throws LoadException {
        RuleEngine re;
        if (profile == -1) {
            re = init(g, path);
        } else {
            re = RuleEngine.create(g);
            re.setProfile(profile);
        }
        init(re);
        return re;
    }
    
    void init(RuleEngine re){
        if (getContext() != null){
            for (Rule r : re.getRules()){
                r.getQuery().setContext(getContext());
            }
        }
    }
    
    RuleEngine init(Graph g, String p) throws LoadException{
        Load ld = Load.create(g);
        ld.parse(p, Loader.RULE_FORMAT);
        return ld.getRuleEngine();
    }

  
    
}
