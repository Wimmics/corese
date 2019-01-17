package fr.inria.corese.sparql.triple.parser;

import java.util.ArrayList;
import java.util.List;

/**
 * Draft SPARQL 1.1 Service
 *
 * @author Olivier Corby, Edelweiss, INRIA 2011
 */
public class Service extends And {

    Atom uri;
    private List<Atom> serviceList;
    boolean silent;

    Service() {}
    
    Service(Atom serv, Exp exp, boolean b) {
        super(exp);
        uri = serv;
        silent = b;
    }

    public static Service create(Atom serv, Exp body, boolean b) {
        Service s = new Service(serv, body, b);
        ArrayList<Atom> list = new ArrayList<>();
        list.add(serv);
        s.setServiceList(list);
        return s;
    }

    public static Service create(List<Atom> list, Exp body, boolean b) {
        Service s = new Service(getDefaultService(list), body, b);
        s.setServiceList(list);
        return s;
    }
    
    public static Service create(Atom serv, List<Atom> list, Exp body, boolean b) {
        Service s = new Service(serv, body, b);
        s.setServiceList(list);
        return s;
    }
    
    static Atom getDefaultService(List<Atom> list) {
        if (list.isEmpty()) {
            return Variable.create("?undef_serv");           
        }
        return list.get(0);     
    }

    public static Service create(Atom serv, Exp body) {
        return create(serv, body, false);
    }
    
    public static Service create(List<Atom> list, Exp body) {
        return create(list, body, false);
    }
    
    @Override
    public Service copy() {
        Service exp = super.copy().getService();
        exp.uri = this.uri;
        exp.silent = this.silent;
        exp.serviceList = this.serviceList;
        return exp;
    }
    
    @Override
    public Service getService(){
            return this;
    }
    
    @Override
    void getVariables(List<Variable> list) {
        super.getVariables(list);
        getServiceName().getVariables(list);
    }

    @Override
    public ASTBuffer toString(ASTBuffer sb) {
        sb.append(Term.SERVICE);
        int i = 0;
        if (getServiceName().isVariable()) {
            sb.append(" ");
            getServiceName().getVariable().toString(sb);
        }
        else for (Atom at : getServiceList()) {
            if (i++ == 0) {
                sb.append(" ");
            }
            else {
                sb.nl().indent();
            }
            at.toString(sb);           
        }
        sb.append(" ");
        getBodyExp().pretty(sb);
        return sb;
    }

    public boolean isSilent() {
        return silent;
    }

    @Override
    public boolean isService() {
        return true;
    }
    
    public boolean isFederate() {
        return getServiceList().size() > 1;
    }

    public Atom getServiceName() {
        return uri;
    }
    
    public void setServiceName(Atom at) {
        uri = at;
    }

    @Override
    public boolean validate(ASTQuery ast, boolean exist) {
        if (uri.isVariable()) {
            ast.bind(uri.getVariable());
            if (!exist) {
                ast.defSelect(uri.getVariable());
            }
        }
        return super.validate(ast, exist);
    }
    
    public void clearServiceList() {
        setServiceList(new ArrayList<>(0));
    }
   
    public List<Constant> getServiceConstantList() {
        ArrayList<Constant> list = new ArrayList<>();
        for (Atom at : getServiceList()) {
            list.add(at.getConstant());
        }
        return list;
    }

    /**
     * @return the serviceList
     */
    public List<Atom> getServiceList() {
        return serviceList;
    }

    /**
     * @param serviceList the serviceList to set
     */
    public void setServiceList(List<Atom> serviceList) {
        this.serviceList = serviceList;
    }
}
