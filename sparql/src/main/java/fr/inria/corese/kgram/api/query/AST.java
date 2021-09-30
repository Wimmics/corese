package fr.inria.corese.kgram.api.query;

/**
 *
 * Olivier Corby - Wimmics INRIA I3S - 2020
 */
public interface AST {
    
    boolean isSelect();
    boolean isConstruct();
    boolean isUpdate();
    boolean isInsert();
    boolean isDelete();
    // update query starts with insert/delete
    boolean isUpdateInsert();
    boolean isUpdateDelete(); 
    boolean isUpdateInsertData();
    boolean isUpdateDeleteData();  
    boolean isUpdateLoad(); 
    
    boolean hasMetadata(String name);
}
