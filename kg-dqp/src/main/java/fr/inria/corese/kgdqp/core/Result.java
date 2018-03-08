/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.corese.kgdqp.core;

import fr.inria.corese.kgram.api.core.Entity;
import fr.inria.corese.kgram.core.Mappings;
import java.util.ArrayList;

/**
 * Helper class to provide the endpoint of provenance and to handle the retrieving of results in:
 * Iterable<Entity> for getEdges()
 * Mappings for getMappings()
 * 
 * @author Abdoul Macina, macina@i3s.unice.fr
 */
public class Result {
    private Iterable<Entity> entities;
    private Mappings mappings;
    private RemoteProducerWSImpl producer;

    public Result( RemoteProducerWSImpl producer) {
        this.entities = new ArrayList<Entity>();
        this.producer = producer;
        this.mappings = new Mappings();
    }

    public Iterable<Entity> getEntities() {
        return entities;
    }

    public RemoteProducerWSImpl getProducer() {
        return producer;
    }

    public void setEntities(Iterable<Entity> entities) {
        this.entities = entities;
    }

    public void setProducer(RemoteProducerWSImpl producer) {
        this.producer = producer;
    }

    public Mappings getMappings() {
        return mappings;
    }

    public void setMappings(Mappings mappings) {
        this.mappings = mappings;
    }
    
    
}
