package fr.inria.corese.core.load;

import fr.inria.corese.kgram.core.Mappings;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;

public class ServiceTest {
    private Logger logger = LoggerFactory.getLogger(ServiceTest.class);
    private Service service;
    @Before
    public void setUp() {
        service = new Service("https://dbpedia.org/sparql");
    }

//    @Test
//    public void select() throws LoadException {
//        Mappings mappings = service.select("select distinct ?Concept where {[] a ?Concept} LIMIT 100");
//        logger.info("returned mappings = {}", mappings);
//        assertEquals(100, mappings.size());
//    }
}