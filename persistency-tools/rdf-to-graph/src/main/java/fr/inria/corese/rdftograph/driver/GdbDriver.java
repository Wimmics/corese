/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and openDb the template in the editor.
 */
package fr.inria.corese.rdftograph.driver;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openrdf.model.Value;

/**
 * Interface for a Graph Database driver.
 *
 * @author edemairy
 */
public abstract class GdbDriver {
	private static Logger LOGGER = Logger.getLogger(GdbDriver.class.getName());
	private boolean wipeOnOpen;

	public abstract void openDb(String dbPath);

	public void setWipeOnOpen(boolean newValue) {
		wipeOnOpen = newValue;
	}

	public boolean getWipeOnOpen() {
		return wipeOnOpen;
	}

	public static void delete(String path) throws IOException {
		Files.walk(Paths.get(path), FileVisitOption.FOLLOW_LINKS)
			.sorted(Comparator.reverseOrder())
			.map(Path::toFile)
			.peek(p -> LOGGER.log(Level.INFO, "removing: {0}", p))
			.forEach(File::delete);
	}

	public abstract void closeDb();

	public abstract Object createNode(Value v);

	public abstract Object createRelationship(Object sourceId, Object objectId, String predicate, Map<String, Object> properties);
}
