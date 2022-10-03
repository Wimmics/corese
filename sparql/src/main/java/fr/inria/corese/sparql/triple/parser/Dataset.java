package fr.inria.corese.sparql.triple.parser;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.triple.function.term.Binding;
import java.util.ArrayList;
import java.util.List;

import fr.inria.corese.kgram.api.core.ExpType;
import fr.inria.corese.kgram.api.core.PointerType;
import static fr.inria.corese.kgram.api.core.PointerType.DATASET;
import fr.inria.corese.kgram.api.query.ProcessVisitor;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.sparql.triple.parser.Access.Level;
import static fr.inria.corese.sparql.triple.parser.URLParam.INDEX;
import static fr.inria.corese.sparql.triple.parser.URLServer.STORE;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 *
 * SPARQL Dataset from or named may be null
 *
 * @author Olivier Corby, Wimmics, INRIA 2012
 *
 */
public class Dataset extends ASTObject {

    protected static final String KG = ExpType.KGRAM;
    static final String EMPTY = KG + "empty";
    static final Constant CEMPTY = Constant.create(EMPTY);
    private List<Constant> from;
    private List<Constant> named;
    private List<String> index;
    List<Constant> with;
    private Context context;
    private Binding binding;
    private Metadata metadata;
    private Object templateVisitor;
    private ProcessVisitor visitor;
    private String base;
    private List<String> uriList;
    // select * from <store:/my/path> where {}
    private String storagePath;

    // true when used by update (delete in default graph specified by from)
    // W3C test case is true
    // Protocol is false
    boolean isUpdate = false;
    // sparql/turtle parser must parse query string 
    // with load() function instead of parse()
    // parse turtle as sparql where graph pattern
    private boolean load = false;

    public Dataset() {
        this(new ArrayList<>(), new ArrayList<>());
        index = new ArrayList<>();
    }

    public Dataset(Context c) {
        this();
        context = c;
    }

    Dataset(List<Constant> f, List<Constant> n) {
        from = f;
        named = n;
    }

    public static Dataset create() {
        return new Dataset();
    }

    public static Dataset create(Context c) {
        return new Dataset(c);
    }
    
    public static Dataset create(ProcessVisitor vis) {
        Dataset ds = new Dataset();
        ds.setVisitor(vis);
        return ds;
    }
    
    public static Dataset create(Binding b) {
        Dataset ds = new Dataset();
        ds.setBinding(b);
        return ds;
    }

    public static Dataset create(List<Constant> f, List<Constant> n) {
        if (f == null && n == null) {
            return null;
        }
        return new Dataset(f, n);
    }

    public static Dataset newInstance(List<String> f, List<String> n) {
        if (f == null && n == null) {
            return null;
        }
        return newInstance(f, n);
    }

    public static Dataset instance(List<String> f, List<String> n) {
        List<Constant> from = null, named = null;
        if (f != null) {
            from = cast(f);
        }
        if (n != null) {
            named = cast(n);
        }
        return new Dataset(from, named);
    }
    
    static List<Constant> cast(List<String> list) {
        ArrayList<Constant> from = new ArrayList<>();
        for (String s : list) {
            from.add(Constant.create(s));
        }
        return from;
    }

    @Override
    public String toString() {
        String str = "";
        str += "from:  " + getFrom() + "\n";
        str += "named: " + getNamed();
        return str;
    }

    public void defFrom() {
        setFrom(new ArrayList<>());
    }

    public void defNamed() {
        setNamed(new ArrayList<>());
    }

    public boolean isUpdate() {
        return isUpdate;
    }

    public boolean isEmpty() {
        return !hasFrom() && !hasNamed();
    }

    public boolean hasFrom() {
        return getFrom() != null && getFrom().size() > 0;
    }

    public boolean hasNamed() {
        return getNamed() != null && getNamed().size() > 0;
    }

    public boolean hasWith() {
        return with != null && with.size() > 0;
    }

    public void setUpdate(boolean b) {
        isUpdate = b;
    }

    public List<Constant> getFrom() {
        return from;
    }

    public List<Constant> getNamed() {
        return named;
    }

    public List<Constant> getWith() {
        return with;
    }

    public void setWith(Constant w) {
        with = new ArrayList<>(1);
        with.add(w);
    }

    public void clean() {
        getFrom().remove(CEMPTY);
    }

    public Dataset addFrom(String s) {
        addFrom(Constant.create(s));
        return this;
    }

    public Dataset addNamed(String s) {
        addNamed(Constant.create(s));
        return this;

    }

    public Dataset remFrom(String s) {
        if (getFrom() != null) {
            getFrom().remove(Constant.create(s));
        }
        return this;
    }

    public Dataset remNamed(String s) {
        if (getNamed() != null) {
            getNamed().remove(Constant.create(s));
        }
        return this;
    }

    public void addFrom(Constant s) {
        if (getFrom() == null) {
            defFrom();
        }
        if (!from.contains(s)) {
            getFrom().add(s);
        }
    }

    public void addNamed(Constant s) {
        if (getNamed() == null) {
            defNamed();
        }
        if (!named.contains(s)) {
            getNamed().add(s);
        }
    }

    /**
     * Std SPARQL Dataset requires that if from (resp named) is empty in a
     * Dataset simple query triple (resp graph query triple) fail In order to
     * make kgram fail accordingly, we add a fake from (resp named)
     */
    public void complete() {
        if (hasFrom() && !hasNamed()) {
            addNamed(CEMPTY);
        } else if (!hasFrom() && hasNamed()) {
            addFrom(CEMPTY);
        }
    }

   
    public Context getContext() {
        return context;
    }

    public Context getCreateContext() {
        if (getContext() == null) {
            setContext(new Context());
        }
        return getContext();
    }

   
    public void setContext(Context context) {
        this.context = context;
    }

    public Dataset setLevel(Level level) {
        if (getContext() == null) {
            setContext(Context.create());
        }
        getContext().setLevel(level);
        return this;
    }

    public Level getLevel() {
        if (getContext() == null) {
            return Level.USER_DEFAULT;
        }
        return getContext().getLevel();
    }

    public Dataset set(Context c) {
        setContext(c);
        return this;
    }

    @Override
    public PointerType pointerType() {
        return DATASET;
    }

    @Override
    public IDatatype getList() {
        return getNamedList();
    }

    public IDatatype getNamedList() {
        ArrayList<IDatatype> list = new ArrayList<>();
        if (getNamed() != null) {
            for (Constant g : getNamed()) {
                list.add(g.getDatatypeValue());
            }
        }
        return DatatypeMap.createList(list);
    }

    public IDatatype getFromList() {
        ArrayList<IDatatype> list = new ArrayList<>();
        if (getFrom() != null) {
            for (Constant g : getFrom()) {
                list.add(g.getDatatypeValue());
            }
        }
        return DatatypeMap.createList(list);
    }
   
    public List<String> getFromStringList() {
        return getStringList(getFrom());
    }

    public List<String> getNamedStringList() {
        return getStringList(getNamed());
    }
    
    List<String> getStringList(List<Constant> alist) {
        ArrayList<String> list = new ArrayList<>();
        if (alist != null) {
            for (Constant g : alist) {
                list.add(g.getLabel());
            }
        }
        return list;
    }
    
    
    public Mapping call(Mapping m) {
        if (getBinding() != null) {
            // use case: share workflow Binding
            if (m.getBind() == null) {
                m.setBind(getBinding());
            }
        }
        if (getContext() != null) {
            if (m.getBind() == null) {
                m.setBind(Binding.create());
            }
            m.getBind().share(getContext());
        }
        return m;
    }

    public void setTemplateVisitor(Object vis) {
        templateVisitor = vis;
    }

    public Object getTemplateVisitor() {
        return templateVisitor;
    }

   
    public void setFrom(List<Constant> from) {
        this.from = from;
    }

   
    public void setNamed(List<Constant> named) {
        this.named = named;
    }

    
    public String getBase() {
        return base;
    }

    
    public Dataset setBase(String base) {
        this.base = base;
        return this;
    }

    
    public Binding getBinding() {
        return binding;
    }

    
    public void setBinding(Binding binding) {
        this.binding = binding;
    }

    
    public List<String> getUriList() {
        return uriList;
    }

    
    public void setUriList(List<String> uriList) {
        this.uriList = uriList;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public Dataset setMetadata(Metadata metadata) {
        this.metadata = metadata;
        return this;
    }

    public boolean isLoad() {
        return load;
    }

    public Dataset setLoad(boolean load) {
        this.load = load;
        return this;
    }

    public ProcessVisitor getVisitor() {
        return visitor;
    }

    public void setVisitor(ProcessVisitor visitor) {
        this.visitor = visitor;
    }
    
    public String getURLParameter() {
        StringBuilder sb = new StringBuilder();
        for (Atom at : getFrom()) {
            if (sb.length()>0) {
                sb.append("&");
            }
            sb.append(String.format("default-graph-uri=%s", encode(at.getLabel())));
        }
        for (Atom at : getNamed()) {
            if (sb.length()>0) {
                sb.append("&");
            }
            sb.append(String.format("named-graph-uri=%s", encode(at.getLabel())));
        }
        return sb.toString();
    }
    
    String encode(String url) {
        try {
            return URLEncoder.encode(url, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            return url;
        }
    }
    
    // select *
    // from <index:http://prod-dekalog.inria.fr>
    // where 
    public boolean index() {
        boolean hasIndex = false;
        
        for (int i = 0; i < getFrom().size();) {
            Atom at = getFrom().get(i);
            if (at.getLabel().startsWith(INDEX)) {
                hasIndex = true;
                String uri = at.getLabel().substring(INDEX.length()+1);
                getIndex().add(uri);
                getFrom().remove(i);
            }
            else {
                i++;
            }
        }
        
        return hasIndex;
    }
    
    // select * from <store:/my/path>
    public void storage() {        
        for (int i = 0; i < getFrom().size();) {
            Atom at = getFrom().get(i);
            if (at.getLabel().startsWith(STORE)) {
                String uri = at.getLabel().substring(STORE.length());
                setStoragePath(uri);
                getFrom().remove(i);
                return;
            }
            else {
                i++;
            }
        }
    }
    

    public List<String> getIndex() {
        return index;
    }

    public void setIndex(List<String> index) {
        this.index = index;
    }

    public String getStoragePath() {
        return storagePath;
    }

    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }

}
