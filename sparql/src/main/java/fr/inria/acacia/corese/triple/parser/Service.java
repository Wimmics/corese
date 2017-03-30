package fr.inria.acacia.corese.triple.parser;

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

    Service(Atom serv, Exp exp, boolean b) {
        super(exp);
        uri = serv;
        silent = b;
    }

    public static Service create(Atom serv, Exp body, boolean b) {
        return new Service(serv, body, b);
    }

    public static Service create(List<Atom> list, Exp body, boolean b) {
        Service s = new Service(list.get(0), body, b);
        s.setServiceList(list);
        return s;
    }

    public static Service create(Atom serv, Exp body) {
        return new Service(serv, body, false);
    }

    @Override
    public StringBuffer toString(StringBuffer sb) {
        sb.append(Term.SERVICE);
        for (Atom at : serviceList) {
            sb.append(" ");
            at.toString(sb);
        }
        sb.append(" ");
        return super.toString(sb);
    }

    public boolean isSilent() {
        return silent;
    }

    @Override
    public boolean isService() {
        return true;
    }

    public Atom getService() {
        return uri;
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
