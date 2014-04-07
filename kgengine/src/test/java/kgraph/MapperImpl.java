package kgraph;

import java.sql.ResultSet;
import java.sql.SQLException;

import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.query.Producer;
import fr.inria.edelweiss.kgraph.query.Mapper;


public class MapperImpl extends Mapper {
	
	MapperImpl(Producer p){
		super(p);
	}
	
	
	public Node getNode(ResultSet rs, int i) throws SQLException{
		getProducer();
		return super.getNode(rs, i);
	}

}
