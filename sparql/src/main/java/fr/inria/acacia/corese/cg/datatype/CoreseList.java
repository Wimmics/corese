package fr.inria.acacia.corese.cg.datatype;

import java.util.List;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.exceptions.CoreseDatatypeException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class CoreseList extends CoreseUndefLiteral {

    private List<IDatatype> list;
    private static int count = 0;
    private static final String SEED = "_l_";
    private static final IDatatype dt = getGenericDatatype(IDatatype.LIST_DATATYPE);

    public CoreseList(String value) {
        super(value);
    }

    public CoreseList() {
        super(SEED + count++);
    }

    public CoreseList(IDatatype[] dt) {
        super(SEED + count++);
        List<IDatatype> ll = Arrays.asList(dt);
        list = new ArrayList<IDatatype>(ll.size());
        list.addAll(ll);    
    }
    
     public CoreseList(List<IDatatype> vec) {
        super(SEED + count++);
        list = vec;
    }

    @Override
    public IDatatype getDatatype() {
        return dt;
    }

    public static CoreseList create(List<IDatatype> vec) {        
        return new CoreseList(vec);
    }
    
     public static CoreseList create(Collection<IDatatype> vec) {
        ArrayList<IDatatype> list = new ArrayList<IDatatype>(vec.size());
        list.addAll(vec);
        return new CoreseList(list);
    }
    
     // cannot add()
    public static CoreseList create(IDatatype[] dts) {
        return new CoreseList(dts);
    }

    public static CoreseList create() {
        return  new CoreseList(new ArrayList<IDatatype>());
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("\"");
        getContent(sb);
        sb.append("\"^^").append(nsm.toPrefix(getDatatypeURI()));
        return sb.toString();
    }
    
    @Override
    public IDatatype display(){
       return DatatypeMap.createUndef(getContent(), getDatatypeURI());
    }
    
    void getContent(StringBuffer sb) {
        sb.append("(");
        for (IDatatype dt : list) {
            sb.append((dt.isList()) ? dt.getContent() : dt);
            sb.append(" ");
        }
        sb.append(")");
    }
    
    @Override
    public String getContent() {
        StringBuffer sb = new StringBuffer();
        getContent(sb);
        return sb.toString();
    }
    
    @Override
    public String toSparql(boolean prefix, boolean xsd) {
        return toString();
    }

    @Override
    public boolean isList() {
        return true;
    }

    public CoreseList getList() {
        return this;
    }
    
    public Object getObject(){
        return list;
    }

    public void set(IDatatype[] dts) {
        list = Arrays.asList(dts);
    }

    @Override
    public List<IDatatype> getValues() {
        return list;
    }
    
    @Override
    public Iterable<IDatatype> getLoop(){
        return list;
    }
    
    @Override
    public boolean isLoop(){
        return true;
    }

    @Override
    public IDatatype get(int n) {
        return list.get(n);
    }

    @Override
    public int size() {
        if (list == null) {
            return 0;
        } else {
            return list.size();
        }
    }

    @Override
    public boolean isTrue() throws CoreseDatatypeException {
        return list != null && list.size() > 0;
    }

    @Override
    public boolean isTrueAble() {
        return true;
    }
    
    @Override
    public boolean booleanValue(){
        try {
            return isTrue();
        } catch (CoreseDatatypeException ex) {
            return false;
        }
    }
}
