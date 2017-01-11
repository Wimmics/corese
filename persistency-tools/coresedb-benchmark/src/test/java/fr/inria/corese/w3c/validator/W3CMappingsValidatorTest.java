/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.corese.w3c.validator;

import fr.inria.acacia.corese.cg.datatype.CoreseString;
import fr.inria.acacia.corese.exceptions.CoreseDatatypeException;
import fr.inria.acacia.corese.triple.parser.Variable;
import fr.inria.edelweiss.kgram.core.Mapping;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.edelweiss.kgraph.core.NodeImpl;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

/**
 *
 * @author edemairy
 */
public class W3CMappingsValidatorTest {

	public W3CMappingsValidatorTest() {
	}

	@Test
	public static void testReflexivity() throws CoreseDatatypeException {
		String[][] map1 = {{"v1", "s1"}};
		Mappings maps1 = createMappings(map1);

		W3CMappingsValidator validator = new W3CMappingsValidator();
		assertTrue(validator.validate(maps1, maps1));
	}

	@Test
	public static void testEquality() throws CoreseDatatypeException {
		String[][] map1 = {{"v1", "s1"}};
		Mappings maps1 = createMappings(map1);

		String[][] map2 = {{"v1", "s1"}};
		Mappings maps2 = createMappings(map2);

		W3CMappingsValidator validator = new W3CMappingsValidator();
		assertTrue(validator.validate(maps1, maps2));
		assertTrue(validator.validate(maps2, maps1));
	}

	@Test
	public static void testDifferent() throws CoreseDatatypeException {
		String[][] map1 = {{"v1", "s1"}};
		Mappings maps1 = createMappings(map1);

		String[][] map2 = {{"v2", "s1"}};
		Mappings maps2 = createMappings(map2);

		W3CMappingsValidator validator = new W3CMappingsValidator();
		assertFalse(validator.validate(maps1, maps2));
		assertFalse(validator.validate(maps2, maps1));
	}

	@Test
	public static void testInclusion() throws CoreseDatatypeException {
		String[][] map1 = {{"v1", "s1"}, {"v2", "s2"}};
		Mappings maps1 = createMappings(map1);

		String[][] map2 = {{"v1", "s1"}};
		Mappings maps2 = createMappings(map2);

		W3CMappingsValidator validator = new W3CMappingsValidator();
		assertFalse(validator.validate(maps1, maps2));
		assertFalse(validator.validate(maps2, maps1));
	}

	@Test
	public static void testEqualWithDouble() throws CoreseDatatypeException {
		String[][] map1 = {{"v1", "s1"}, {"v2", "s2"}};
		Mappings maps1 = createMappings(map1);

		String[][] map2 = {{"v1", "s1"}, {"v2", "s2"}, {"v2", "s2"}};
		Mappings maps2 = createMappings(map2);

		W3CMappingsValidator validator = new W3CMappingsValidator();
		assertFalse(validator.validate(maps1, maps2));
		assertFalse(validator.validate(maps2, maps1));
	}

	public static Mappings createMappings(String[][] mapArray) {
		Mappings newMappings = Mappings.create(Query.create(0));
		for (String[] oneMap : mapArray) {
			Mapping mapping = Mapping.create();
			String varName = oneMap[0];
			String varValue = oneMap[1];
			mapping.addNode(NodeImpl.create(Variable.create(varName).getDatatypeValue()), NodeImpl.create(CoreseString.create(varValue)));
			newMappings.add(mapping);
		}
		return newMappings;
	}
}
