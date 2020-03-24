package fr.inria.corese.sparql.triple.parser;

import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.sparql.api.IDatatype;

/**
 * 
 * @author Olivier Corby, INRIA 2020
 */
public class AccessRight {
    
    private static boolean active = false;
    
    // NONE means no access right 
    public static final byte NONE       =-1;
    //public static final byte UNDEFINED  = 0;
    public static final byte PUBLIC     = 1;
    public static final byte PROTECTED  = 2;
    public static final byte PRIVATE    = 3;
    
    public static final int GT_MODE  = 0;
    public static final int EQ_MODE  = 1;
    public static final int DEFAULT_MODE = GT_MODE;
    
    
    
    public static final String NONE_ACCESS       = NSManager.EXT+"node";
    public static final String PUBLIC_ACCESS     = NSManager.EXT+"public";
    public static final String PROTECTED_ACCESS  = NSManager.EXT+"protected";
    public static final String PRIVATE_ACCESS    = NSManager.EXT+"private";
    
    public static final String GT_ACCESS_MODE    = NSManager.EXT+"gt";
    public static final String EQ_ACCESS_MODE    = NSManager.EXT+"eq";

    
    // REJECTED means do not insert edge
    public static final byte REJECTED   = Byte.MAX_VALUE;
    
    public static final byte DEFAULT    = PUBLIC;

    // update authorized
    private boolean update = true;
       
    // access granted to the delete clause
    private byte delete = DEFAULT;
    // access level assigned to inserted edge 
    private byte insert = DEFAULT;
    // access granted to the where clause
    private byte where  = DEFAULT;
    
    private static int mode = DEFAULT_MODE;
    
    private AccessRightDefinition insertRightDefinition, deleteRightDefinition;
    
    private boolean debug = false;
    
    
    // by default insert and delete access right are the same
    public AccessRight() {
        AccessRightDefinition a = new AccessRightDefinition();
        setInsertRightDefinition(a);
        setDeleteRightDefinition(a);
    }
    
    public AccessRight(byte access) {
        this();
        setAccess(access);
    }

    public AccessRight(byte delete, byte insert, byte where) {
        this();
        setDelete(delete);
        setInsert(insert);
        setWhere(where);
    }
    
    // insert and delete have different access right
    public AccessRight split() {
        setInsertRightDefinition(new AccessRightDefinition());
        setDeleteRightDefinition(new AccessRightDefinition());
        return this;
    }
    
    /**
     *  
     */
    public boolean setDelete(Edge edge) {
        setDeleteNS(edge);
        return accept(edge.getLevel());
    }
    
    public boolean setInsert(Edge edge) {
        //setInsertBasic(edge);
        setInsertNS(edge);
        return accept(edge.getLevel());
    }
    
    
    
    public static boolean accept(byte b) {
        return b != NONE;
    }
    public static boolean reject(byte b) {
        return b == NONE;
    }
    
    public static boolean accept(byte query, byte target) {
        switch (mode) {
            case EQ_MODE:
                return acceptEQ(query, target);
            default:
                return acceptGT(query, target);
        }
    }
    public static boolean reject(byte query, byte target) {
        switch (mode) {
            case EQ_MODE:
                return rejectEQ(query, target);
            default:
                return rejectLT(query, target);
        }
    }
    
    public static boolean acceptGT(byte query, byte target) {
        return query >= target;
    } 
    public static boolean rejectLT(byte query, byte target) {
        return query < target;
    } 
    
    public static boolean acceptEQ(byte query, byte target) {
        return query == target;
    } 
    public static boolean rejectEQ(byte query, byte target) {
        return query != target;
    } 
    
    public void setDeleteBasic(Edge edge) {
        edge.setLevel(getDelete());
    }
    
    public void setInsertBasic(Edge edge) {
        edge.setLevel(getInsert());
    }
    
    public void setInsertNS(Edge edge) {
        edge.setLevel(getInsertRightDefinition().getAccess(edge, getInsert()));
    }
    
    public void setDeleteNS(Edge edge) {
        edge.setLevel(getDeleteRightDefinition().getAccess(edge, getDelete()));
    }
    
    /**
     * @return the update
     */
    public boolean isUpdate() {
        return update;
    }
    
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
            case PUBLIC_ACCESS:
                return PUBLIC;
            case PRIVATE_ACCESS:
                return PRIVATE;
            case PROTECTED_ACCESS:
                return PROTECTED;  
            default:
                return NONE;
        }
    }
    
    public static void setMode(String mode) {
        switch (mode) {
            case EQ_ACCESS_MODE: setMode(EQ_MODE); break;
            default: setMode(GT_MODE); break;
        }
    }
    
    public byte getLevel(IDatatype dt) {
        return getLevel(dt.getLabel());
    }
   
    public static int getMode() {
        return mode;
    }
    
    public static void setMode(int m) {
        mode = m;
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

   
}
