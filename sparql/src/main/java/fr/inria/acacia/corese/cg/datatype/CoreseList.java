package fr.inria.acacia.corese.cg.datatype;

import java.util.List;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.exceptions.CoreseDatatypeException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CoreseList extends CoreseUndefLiteral {

    private IDatatype[] array;
    private static int count = 0;
    private static final String SEED = "_l_";
    private static final IDatatype dt = getGenericDatatype(IDatatype.LIST);

    public CoreseList(String value) {
        super(value);
    }

    public CoreseList() {
        super(SEED + count++);
    }

    public CoreseList(IDatatype[] dt) {
        super(SEED + count++);
        array = dt;
    }

    public IDatatype getDatatype() {
        return dt;
    }

    public static CoreseList create(List<IDatatype> vec) {
        IDatatype[] adt = new IDatatype[vec.size()];
        vec.toArray(adt);
        return new CoreseList(adt);
    }
    
     public static CoreseList create(Collection<IDatatype> vec) {
        IDatatype[] adt = new IDatatype[vec.size()];
        vec.toArray(adt);
        return new CoreseList(adt);
    }
    

    public static CoreseList create(IDatatype[] dts) {
        return new CoreseList(dts);
    }

    public static CoreseList create() {
        return  new CoreseList(new IDatatype[0]);
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("(");
        for (IDatatype dt : array) {
            sb.append(dt);
            sb.append(" ");
        }
        sb.append(")");
        return sb.toString();
    }

    public boolean isList() {
        return true;
    }

    public CoreseList getList() {
        return this;
    }

    public void set(IDatatype[] dts) {
        array = dts;
    }

    @Override
    public IDatatype[] getValues() {
        return array;
    }

    @Override
    public IDatatype get(int n) {
        return array[n];
    }

    @Override
    public int size() {
        if (array == null) {
            return 0;
        } else {
            return array.length;
        }
    }

    @Override
    public boolean isTrue() throws CoreseDatatypeException {
        return array != null && array.length > 0;
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
