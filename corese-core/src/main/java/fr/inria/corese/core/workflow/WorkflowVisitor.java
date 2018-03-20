package fr.inria.corese.core.workflow;

/**
 *
 * @author corby
 */
public interface WorkflowVisitor {
    
    void before(WorkflowProcess wp, Data data);
    
    void after(WorkflowProcess wp, Data data);
    
}
