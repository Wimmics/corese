package fr.inria.edelweiss.kgraph.core;

/**
 * Collection of events trapped by graph event manager
 * in order to ensure consistency of graph index and entailments 
 * w.r.t graph modifications
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public enum Event { 
    Start, Finish, 
    Process,     // alternative to start/finish and also general purpose graph access -> graph.index() 
    
    Query, Construct,
    LoadAPI, // load()
    Update,
    LoadUpdate, // SPARQL Update load
    DeleteInsert, DeleteData, InsertData, 
    Delete, Insert,
    RuleEngine, Workflow, Format,
    // (des)activate existing processor for future process cycle, does not process yet
    ActivateRDFSEntailment, ActivateRuleEngine, 
    // activate entailment processing for next cycle (e.g. next query)
    ActivateEntailment
    
}
