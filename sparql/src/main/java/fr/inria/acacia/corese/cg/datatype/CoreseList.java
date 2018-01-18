package fr.inria.acacia.corese.cg.datatype;

import java.util.List;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.api.IDatatypeList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

public class CoreseList extends CoreseUndefLiteral implements IDatatypeList {

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
        list = new ArrayList<IDatatype>(dt.length);
        for (IDatatype val : dt) {
            list.add(val);
        }
    }

    public CoreseList(List<IDatatype> vec) {
        super(SEED + count++);
        list = vec;
    }

    @Override
    public IDatatype getDatatype() {
        return dt;
    }

    @Override
    public IDatatypeList getList() {
        return this;
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
        return new CoreseList(new ArrayList<IDatatype>());
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
    public IDatatype display() {
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

    @Override
    public Object getObject() {
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
    public List<IDatatype> getValueList() {
        return list;
    }

    @Override
    public IDatatype toList() {
        return this;
    }

    @Override
    public Iterator<IDatatype> iterator() {
        return getValues().iterator();
    }

    @Override
    public Iterable<IDatatype> getLoop() {
        return list;
    }

    @Override
    public boolean isLoop() {
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
    public boolean isTrue() {
        return list != null && list.size() > 0;
    }

    @Override
    public boolean isTrueAble() {
        return true;
    }

    @Override
    public boolean booleanValue() {
        return isTrue();
    }

    @Override
    public IDatatype length() {
        return DatatypeMap.newInstance(list.size());
    }

    @Override
    public IDatatype first() {
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    @Override
    public IDatatype rest() {
        ArrayList<IDatatype> res = new ArrayList(list.size() - 1);
        int i = 0;
        for (IDatatype val : list) {
            if (i++ > 0) {
                res.add(val);
            }
        }
        return new CoreseList(res);
    }

    @Override
    public IDatatype add(IDatatype elem) {
        list.add(elem);
        return this;
    }

    @Override
    public IDatatype add(IDatatype n, IDatatype elem) {
        list.add(n.intValue(), elem);
        return this;
    }

    @Override
    public IDatatype swap(IDatatype i1, IDatatype i2) {
        IDatatype dt = list.get(i1.intValue());
        list.set(i1.intValue(), list.get(i2.intValue()));
        list.set(i2.intValue(), dt);
        return this;
    }

    // copy
    @Override
    public IDatatype cons(IDatatype elem) {
        ArrayList<IDatatype> res = new ArrayList(list.size() + 1);
        res.add(elem);
        res.addAll(list);
        return new CoreseList(res);
    }

    @Override
    public IDatatype append(IDatatype dtlist) {
        if (!dtlist.isList()) {
            return null;
        }
        ArrayList<IDatatype> res = new ArrayList(list.size() + dtlist.size());
        res.addAll(list);
        res.addAll(dtlist.getValues());
        return new CoreseList(res);
    }

    // remove duplicates
    @Override
    public IDatatype merge(IDatatype dtlist) {
        if (!dtlist.isList()) {
            return null;
        }
        ArrayList<IDatatype> res = new ArrayList();
        for (IDatatype dt : list) {
            if (!res.contains(dt)) {
                res.add(dt);
            }
        }
        for (IDatatype dt : dtlist.getValues()) {
            if (!res.contains(dt)) {
                res.add(dt);
            }
        }
        return new CoreseList(res);
    }

    // this is a list, possibly list of lists
    // merge lists and remove duplicates
    @Override
    public IDatatype merge() {
        ArrayList<IDatatype> res = new ArrayList();

        for (IDatatype dt : list) {
            if (dt.isList()) {
                for (IDatatype elem : dt.getValues()) {
                    if (!res.contains(elem)) {
                        res.add(elem);
                    }
                }
            } else if (!res.contains(dt)) {
                res.add(dt);
            }
        }

        return new CoreseList(res);
    }

    @Override
    public IDatatype get(IDatatype n) {
        if (n.intValue() >= list.size()) {
            return null;
        }
        return list.get(n.intValue());
    }

    @Override
    public IDatatype set(IDatatype n, IDatatype val) {
        if (n.intValue() >= list.size()) {
            return null;
        }
        list.set(n.intValue(), val);
        return val;
    }

    @Override
    public IDatatype reverse() {
        ArrayList<IDatatype> res = new ArrayList(list.size());
        int n = list.size() - 1;
        for (int i = 0; i < list.size(); i++) {
            res.add(list.get(n - i));
        }
        return new CoreseList(res);
    }

    // modify list
    @Override
    public IDatatype sort() {
        Collections.sort(list);
        return this;
    }

    @Override
    public IDatatype member(IDatatype elem) {
        return list.contains(elem) ? TRUE : FALSE;
    }
}
