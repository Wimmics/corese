package fr.inria.corese.kgengine.kgraph;

import java.sql.ResultSet;
import java.sql.SQLException;

import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.kgraph.query.MapperSQL;


public class MapperImpl extends MapperSQL {
	
	MapperImpl(Producer p){
		super(p);
	}
	
	
	public Node getNode(ResultSet rs, int i) throws SQLException{
		getProducer();
		return super.getNode(rs, i);
	}

}
