package fr.inria.corese.core.workflow;

/**
 *
 */
public interface PreProcessor {
    
    default String translate(String str) { return str; }
    
}
