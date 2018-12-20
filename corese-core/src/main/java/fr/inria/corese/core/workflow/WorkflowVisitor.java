package fr.inria.corese.core.workflow;

/**
 *
 * @author corby
 */
public interface WorkflowVisitor {
    
    default void before(WorkflowProcess wp, Data data) {}
    
    default void after(WorkflowProcess wp, Data data) {}
    
    default void visit(WorkflowProcess wp, Data data, double time) {}
    
    default boolean isTest() {
        return false;
    }
    
}
