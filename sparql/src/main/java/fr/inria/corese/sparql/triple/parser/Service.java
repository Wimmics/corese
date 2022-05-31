package fr.inria.corese.sparql.triple.parser;

import java.util.ArrayList;
import java.util.List;

/**
 * SPARQL 1.1 Service
 *
 * @author Olivier Corby, Edelweiss, INRIA 2011
 */
public class Service extends SourceExp {
    private static final String UNDEFINED_SERVER = "http://example.org/sparl"; //?_undef_serv";
    public static String SERVER_SEED = "?_server_";
    public static String SERVER_VAR = SERVER_SEED+"0";
    private int number = 0;

    private List<Atom> serviceList;
    private URLServer url;
    boolean silent;

    Service() {}
    
    Service(Atom serv, Exp exp, boolean b) {
        super(serv, exp);
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
        if (list.isEmpty()) {
            list = List.of(getDefaultService(list));
        } 
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
            //return Variable.create(UNDEFINED_SERVER);           
            return Constant.create(UNDEFINED_SERVER);           
        }
        return list.get(0);     
    }

    public static Service create(Atom serv, Exp body) {
        return create(serv, body, false);
    }
    
    public static Service create(List<Atom> list, Exp body) {
        return create(list, body, false);
    }
    
    public static Service newInstance(List<String> list, Exp body) {
        return create(getList(list), body, false);
    }
    
    public boolean isUndefined() {
        return getServiceName().getLabel().equals(UNDEFINED_SERVER);
    }
    
    static List<Atom> getList(List<String> list) {
        ArrayList<Atom> alist = new ArrayList<>();
        for (String uri : list) {
            alist.add(Constant.create(uri));
        }
        return alist;
    }
    
    public URLServer getCreateURL() {
        if (getURL() == null) {
            if (getServiceName().isConstant()) {
                setURL(new URLServer(getServiceName().getLabel()));
            }
        }
        return getURL();
    }
    
    @Override
    public Service copy() {
        Service exp = super.copy().getService();
        exp.setSource(getSource());
        exp.silent = this.silent;
        exp.serviceList = this.serviceList;
        return exp;
    }
    
    @Override
    public Service getService(){
            return this;
    }
    
    @Override
    public BasicGraphPattern getBasicGraphPattern() {
        return getBodyExp().getBasicGraphPattern();        
    }
    
    public BasicGraphPattern bgp() {
        return getBodyExp().getBasicGraphPattern();        
    }
    
    @Override
    public ASTBuffer toString(ASTBuffer sb) {
        if (sb.isService()) {
            if (getServiceList().size()>1) {
                return union().toString(sb);
            }
        }
        return basicToString(sb);
    }
    
    ASTBuffer basicToString(ASTBuffer sb) {
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
    
    public Exp union() {
        Exp res = null;
        for (int i = getServiceList().size()-1; i>= 0; i--) {
            Atom at  = getServiceList().get(i);
            Exp serv = Service.create(at, getBodyExp());
            if (res == null) {
                res = serv;
            }
            else {
                res = Union.create(serv, res);
            }
        }
        return res;
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
        return getSource();
    }
    
    public void setServiceName(Atom at) {
        setSource(at);
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
    
    public void insert(Exp exp) {
        Exp body = getBodyExp();
        if (body.size() == 0) {
            body.add(exp);
        }
        else {
            Exp ee = body.get(0);
            if (ee.isQuery()) {
                ASTQuery q = ee.getAST();
                q.getBody().add(0, exp);
            }
            else {
               body.add(0, exp); 
            }
        }
    }

    public List<Atom> getServiceList() {
        return serviceList;
    }

    public void setServiceList(List<Atom> serviceList) {
        this.serviceList = serviceList;
    }
    
    public void setURLList(List<Atom> serviceList) {
        setServiceList(serviceList);
        setSource(serviceList.get(0));
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public void setURL(URLServer url) {
        this.url = url;
    }
    
     public URLServer getURL() {
        return this.url;
    }
}
