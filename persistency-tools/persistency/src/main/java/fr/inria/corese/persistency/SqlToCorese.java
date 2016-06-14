/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.corese.persistency;

import com.orientechnologies.orient.core.record.impl.ODocument;
import com.tinkerpop.blueprints.Vertex;
import static fr.inria.corese.persistency.Mapper.*;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgraph.core.EdgeQuad;
import fr.inria.edelweiss.kgraph.core.Graph;
import java.sql.Connection;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author edemairy
 */
public class SqlToCorese {

	private Graph coreseGraph;
	private Connection connection;

	public SqlToCorese(Graph g, Connection connection) {
		this.coreseGraph = g;
		this.connection = connection;
	}

	/**
	 * Returns a Corese Entity from a Tinkerpop edge.
	 *
	 * @param e
	 */
	public Entity buildEntity(ResultSet rs) {
		String context = "";
		ODocument orientSource;
		ODocument orientObject;
		String value = "";
		try {
			context = rs.getString(CONTEXT);
			orientSource = (ODocument) rs.getObject(IN);
			orientObject = (ODocument) rs.getObject(OUT);
			value = rs.getString(VALUE);
		} catch (SQLException ex) {
			Logger.getLogger(SqlToCorese.class.getName()).log(Level.SEVERE, null, ex);
			throw new RuntimeException("", ex);
		}
		Entity result = EdgeQuad.create(
			coreseGraph.createNode(context),
			unmapNode(orientSource.getIdentity().toString()),
			coreseGraph.createNode(value),
			unmapNode(orientObject.getIdentity().toString())
		);
		return result;
	}

	private Node unmapNode(String nodeId) throws RuntimeException {
		try {
			Statement statement = connection.createStatement();
			ResultSet rs = statement.executeQuery("SELECT " + VALUE + ", " + KIND + ", " + TYPE + " FROM V WHERE " + "@RID=" + nodeId + " limit 1" );
			String kind = rs.getString(KIND);
			String value = rs.getString(VALUE);
			String type = rs.getString(TYPE);
			String lang = rs.getString(LANG);
			switch (kind) {
				case IRI:
					return coreseGraph.createNode(value);
				case BNODE:
					return coreseGraph.createBlank(value);
				case LITERAL:
					if (lang != null) {
						return coreseGraph.addLiteral(value, type, lang);
					} else {
						return coreseGraph.addLiteral(value, type);
					}
				default:
					throw new IllegalArgumentException("node " + value + " type is unknown.");
			}
		} catch (SQLException ex) {
			Logger.getLogger(SqlToCorese.class.getName()).log(Level.SEVERE, null, ex);
			throw new RuntimeException("Something bad happended when reading data on a node: ", ex);
		}
	}
}
