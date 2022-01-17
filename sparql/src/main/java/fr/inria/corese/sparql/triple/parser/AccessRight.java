package fr.inria.corese.sparql.triple.parser;

import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.sparql.api.IDatatype;

/**
 * 
 * @author Olivier Corby, INRIA 2020
 */
public class AccessRight {
    
    private static boolean active = false;
    // @deprecated
    private static boolean inheritDefault = false;
    
    // NONE means no access right 
    public static final byte NONE       =-1;
    public static final byte UNDEFINED  = 0;
    public static final byte PUBLIC     = 1;
    public static final byte PROTECTED  = 2;
    public static final byte RESTRICTED = 3;
    public static final byte PRIVATE    = 4;
    public static final byte SUPER_USER = 5;
      
    public static final int GT_MODE  = 0;
    public static final int EQ_MODE  = 1;
    public static final int BI_MODE  = 2;
    
    public static final byte ZERO = 0b0000000;
    // available for access right:
    public static final byte ONE  = 0b0000001;
    public static final byte TWO  = 0b0000010;
    public static final byte THREE= 0b0000100;
    public static final byte FOUR = 0b0001000;
    public static final byte FIVE = 0b0010000;
    public static final byte SIX  = 0b0100000;
    public static final byte SEVEN= 0b1000000;
    
    public static final byte ACCESS_MAX = PRIVATE;
    public static final byte ACCESS_MAX_BI = SEVEN;
    
    public static final byte[] BINARY = {ZERO, ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN};
    
    public static final int DEFAULT_MODE = GT_MODE;  
    
    public static final String NONE_ACCESS       = NSManager.EXT+"none";
    public static final String UNDEFINED_ACCESS  = NSManager.EXT+"undefined";
    public static final String PUBLIC_ACCESS     = NSManager.EXT+"public";
    public static final String PROTECTED_ACCESS  = NSManager.EXT+"protected";
    public static final String RESTRICTED_ACCESS = NSManager.EXT+"restricted";
    public static final String PRIVATE_ACCESS    = NSManager.EXT+"private";
    
    public static final String GT_ACCESS_MODE    = NSManager.EXT+"gt";
    public static final String EQ_ACCESS_MODE    = NSManager.EXT+"eq";
    public static final String BI_ACCESS_MODE    = NSManager.EXT+"binary";

    
    // REJECTED means do not insert edge
    public static final byte REJECTED   = Byte.MAX_VALUE;
    
    public static final byte DEFAULT    = PUBLIC;

    // update authorized
    private boolean update = true;
    
    // default access right assigned to inserted/loaded triple
    private byte define = DEFAULT;
    
    // access granted to delete clause
    private byte delete = DEFAULT;
    // access granted to insert clause
    private byte insert = DEFAULT;
    // access granted to where clause
    private byte whereMin = UNDEFINED;
    private byte whereMax = UNDEFINED;
    private byte[] whereList = new byte[0];
    private byte where    = DEFAULT;
        
    private static int mode = DEFAULT_MODE;
    
    private AccessRightDefinition insertRightDefinition, deleteRightDefinition;
    
    private boolean debug = false;
    
    
    
    /**
     * 
     */
    public AccessRight() {
        split();
    }
    
    public AccessRight(byte access) {
        this();
        setAccess(access);
    }

    public AccessRight(byte delete, byte insert, byte where) {
        this();
        setDefine(insert);
        setDelete(delete);
        setInsert(insert);
        setWhere(where);
    }
    
    @Override
    public String toString() {
        return "access right:\n".concat(getAccessRightDefinition().toString());
    }
       
    
    // insert and delete have different access right
    public AccessRight split() {
        setInsertRightDefinition(new AccessRightDefinition());
        setDeleteRightDefinition(new AccessRightDefinition());
        return this;
    }
    
    public void inheritDefault() {
        getAccessRightDefinition().inheritDefault();
    }
    
    public void splitInheritDefault() {
        getInsertRightDefinition().inheritDefault();
        getDeleteRightDefinition().inheritDefault();
    }
      
    
    public static boolean accept(byte b) {
        return b != NONE;
    }
    public static boolean reject(byte b) {
        return b == NONE;
    }
    
//    public static boolean acceptWhere(byte query, byte target) {
//        return accept(query, target);
//    }
    
    
    
    public boolean acceptWhere(byte target) {
        //return acceptWhereBasic(target);
        return acceptWhereGeneric(target);
    }
    
    public boolean acceptWhereGeneric(byte target) {
        if (getWhereMax() != UNDEFINED) {
            return acceptWhereMinMax(target);
        }
        if (getWhereList().length > 0) {
            return acceptWhereList(target);
        }
        return acceptWhereBasic(target);
    }
    
    public boolean acceptWhereBasic(byte target) {
        return accept(getWhere(), target);
    }
    
    public boolean acceptWhereMinMax(byte target) {
        return getWhereMin() <= target && target <= getWhereMax();
    }
    
    public boolean acceptWhereList(byte target) {
        for (byte b : getWhereList()) {
            if (b == target) {
                return true;
            }
        }
        return false;
    }
    
    public static boolean accept(byte query, byte target) {
        switch (mode) {
            case EQ_MODE:
                return acceptEQ(query, target);
            case BI_MODE:
                return acceptBI(query, target);    
            default:
                return acceptGT(query, target);
        }
    }
    
    public static boolean reject(byte query, byte target) {
        return ! accept(query, target);
    }
    
    /**
     * Use case: delete target edge
     */
    public static boolean acceptDelete(Edge query, Edge target) {
        return ! isActive() || accept(query.getLevel(), target.getLevel());
    } 
    
    public static boolean acceptDeleteStatus(Edge query, Edge target) {
        return isSuperUser(query.getLevel());
    }  
    
    // specific test for query = target = 0
    public static boolean acceptBI(byte query, byte target) {
        return (query & target) > 0;
    } 
//    public static boolean rejectBI(byte query, byte target) {
//        return  (query & target) == 0;
//    } 
    
    public static boolean acceptGT(byte query, byte target) {
        return query >= target;
    } 
//    public static boolean rejectLT(byte query, byte target) {
//        return query < target;
//    } 
    
    public static boolean isSuperUser(byte query) {
        return query == SUPER_USER;
    }
    
    public static boolean acceptEQ(byte query, byte target) {
        return query == SUPER_USER || query == target;
    } 
//    public static boolean rejectEQ(byte query, byte target) {
//        return query != target;
//    } 
    
    /**
     * Construct call setDelete and setInsert
     * Load CreateTriple call setInsert
     */
    public boolean setDelete(Edge edge) {
        setDeleteNS(edge);
        return accept(edge.getLevel());
    }
    
    public boolean setInsert(Edge edge) {
        //setInsertBasic(edge);
        setInsertNS(edge);
        return accept(edge.getLevel()) && accept(getInsert(), edge.getLevel());
    }
   
    
    public void setDeleteBasic(Edge edge) {
        edge.setLevel(getDelete());
    }
    
    public void setInsertBasic(Edge edge) {
        edge.setLevel(getInsert());
    }
    
    public void setInsertNS(Edge edge) {
        edge.setLevel(getInsertRightDefinition().getAccess(edge, getDefine()));
        if (isDebug()) System.out.println(edge.getLevel()+ " " + edge);
    }
        
    public void setDeleteNS(Edge edge) {
        edge.setLevel(getDeleteRightDefinition().getAccess(edge, getDelete()));
        if (isDebug()) System.out.println(edge.getLevel() + " " + edge);
    }
    
    /**
     * @return the update
     */
    public boolean isUpdate() {
        return update;
    }
    
    // Called by Construct
    public boolean isInsert() {
        return accept(getInsert());
    }
    
    public boolean isDelete() {
        return accept(getDelete());
    }

    /**
     * @param update the update to set
     */
    public void setUpdate(boolean update) {
        this.update = update;
    }

    /**
     * @return the delete
     */
    public byte getDelete() {
        return delete;
    }

    /**
     * @param delete the delete to set
     */
    public void setDelete(byte delete) {
        this.delete = delete;
    }

    /**
     * @return the insert
     */
    public byte getInsert() {
        return insert;
    }

    /**
     * @param insert the insert to set
     */
    public void setInsert(byte insert) {
        this.insert = insert;
    }
    
    /**
     * to be used in order to align the insert clause
     * with the define clause
     * 
     */
    public void setDefineInsert(byte insert) {
        setDefine(insert);
        setInsert(insert);
    }

    /**
     * @return the where
     */
    public byte getWhere() {
        return where;
    }

    /**
     * @param where the where to set
     */
    public void setWhere(byte where) {
        this.where = where;
    }
    
    public void setAccess(byte b) {
        setDefine(b);
        setDelete(b);
        setInsert(b);
        setWhere(b);
    }

    /**
     * @return the active
     */
    public static boolean isActive() {
        return active;
    }

    /**
     * @param aActive the active to set
     */
    public static void setActive(boolean aActive) {
        active = aActive;
    }

    // case where insert and delete have the same access right
    public AccessRightDefinition getAccessRightDefinition() {
        return getInsertRightDefinition();
    }
     
    public AccessRightDefinition getInsertRightDefinition() {
        return insertRightDefinition;
    }

    
    public void setInsertRightDefinition(AccessRightDefinition accessRightDefinition) {
        this.insertRightDefinition = accessRightDefinition;
    }
    
     public AccessRightDefinition getDeleteRightDefinition() {
        return deleteRightDefinition;
    }

    
    public void setDeleteRightDefinition(AccessRightDefinition accessRightDefinition) {
        this.deleteRightDefinition = accessRightDefinition;
    }
    
        /**
     * Basic access right 
     */
    public byte getLevel(String level) {
        switch (level) {
            case UNDEFINED_ACCESS:
                return UNDEFINED;
            case PUBLIC_ACCESS:
                return PUBLIC;
            case PRIVATE_ACCESS:
                return PRIVATE;
            case PROTECTED_ACCESS:
                return PROTECTED;  
            case RESTRICTED_ACCESS:
                return RESTRICTED; 
                
            default:
                return NONE;
        }
    }
    
    // level must be a binary number
    public byte getLevel(int level) {
        if (level>= 0 && level<Byte.MAX_VALUE) {
            return (byte)level;
        }
        return ZERO;
    }
    
    public static void setMode(String mode) {
        switch (mode) {
            case EQ_ACCESS_MODE: eqMode(); break;
            case BI_ACCESS_MODE: biMode(); break;
            default: gtMode(); break;
        }
    }
    
    public byte getLevel(IDatatype dt) {
        if (dt.isNumber()) {
            return getLevel(dt.intValue());
        }
        return getLevel(dt.getLabel());
    }
   
    public static int getMode() {
        return mode;
    }
    
    public static void setMode(int m) {
        mode = m;
    }
    
    public static void gtMode() {
        setMode(GT_MODE);
    }
    
    public static void eqMode() {
        setMode(EQ_MODE);
    }
    
     public static void biMode() {
        setMode(BI_MODE);
    }
    

    /**
     * @return the debug
     */
    public boolean isDebug() {
        return debug;
    }

    /**
     * @param debug the debug to set
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
        getAccessRightDefinition().setDebug(debug);
    }

    /**
     * @return the inheritDefault
     */
    public static boolean isInheritDefault() {
        return inheritDefault;
    }

    /**
     * @param aInheritDefault the inheritDefault to set
     */
    public static void setInheritDefault(boolean aInheritDefault) {
        inheritDefault = aInheritDefault;
    }

    /**
     * @return the define
     */
    public byte getDefine() {
        return define;
    }

    /**
     * Default access right for inserted/loaded triple
     * The insert access right must permit this default access
     * Use: setDefineInsert 
     */
    public void setDefine(byte define) {
        this.define = define;
    }

    /**
     * @return the whereMin
     */
    public byte getWhereMin() {
        return whereMin;
    }

    /**
     * @param whereMin the whereMin to set
     */
    public void setWhereMin(byte whereMin) {
        this.whereMin = whereMin;
    }

    /**
     * @return the whereMax
     */
    public byte getWhereMax() {
        return whereMax;
    }

    /**
     * @param whereMax the whereMax to set
     */
    public void setWhereMax(byte whereMax) {
        this.whereMax = whereMax;
    }
    
    public void setWhere(byte min, byte max) {
        setWhereMin(min);
        setWhereMax(max);
    }

    /**
     * @return the whereList
     */
    public byte[] getWhereList() {
        return whereList;
    }

    /**
     * @param whereList the whereList to set
     */
    public void setWhereList(byte... whereList) {
        this.whereList = whereList;
    }

   
}
