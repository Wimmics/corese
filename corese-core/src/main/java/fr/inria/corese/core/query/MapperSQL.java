package fr.inria.corese.core.query;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.compiler.eval.SQLResult;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;


/**
 * Translation from SQL Java ResultSet to KGRAM Mapping
 * 
 * @author Olivier Corby, Edelweiss, INRIA 2010
 *
 */
public class MapperSQL {
	
	Producer producer;
	
	public MapperSQL(Producer p){
		producer = p;
	}
	
	public Producer getProducer(){
		return producer;
	}
	
	Mappings map(List<Node> lNodes, Object object){
		if (object instanceof SQLResult){
			return sql(lNodes, (SQLResult) object);
		}
		else return new Mappings();
	}
	
	public Mappings sql(List<Node> lNodes, SQLResult res){
		ResultSet rs = res.getResultSet();
		if (rs == null) return new Mappings();
		
		if (res.isSort()){
			lNodes = sort(lNodes, rs);
		}
		Mappings lMap = new Mappings();
		Node[] qNodes = new Node[lNodes.size()];
		int j = 0;
		for (Node n : lNodes){
			qNodes[j++] = n;
		}
		
    	try {
			int size = rs.getMetaData().getColumnCount();
			while (rs.next()) {
				Node[] nodes = new Node[size];
				
				for (int i=1; i<=size; i++){
					Node node = getNode(rs, i);
					nodes[i-1] = node;
				}
				
				Mapping map =  Mapping.create(qNodes, nodes);
				lMap.add(map);
			}

		} 
    	catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			lMap = null;
		}
    	return lMap;
	}
	
	/**
	 * Use case:
	 * r2rml may have generated lNode in arbitrary order wrt SQL query
	 * sort nodes according to sql result
	 */
	List<Node> sort(List<Node> lNode, ResultSet rs){
		List<Node> list = new ArrayList<Node>();
		try {
			int size = rs.getMetaData().getColumnCount();
			for (int i=0; i<size; i++){
				String name = rs.getMetaData().getColumnLabel(i);
				Node node = getNode(lNode, name);
				if (node == null){
					return lNode;
				}
				list.add(node);
			}
		} catch (SQLException e) {
			return lNode;
		}
		
		return list;
		
	}
	
	Node getNode(List<Node> list, String name){
		String str = "?"+name;
		for (Node node : list){
			if (node.getLabel().equals(str)){
				return node;
			}
		}
		return null;
	}
		
	public Node getNode(ResultSet rs, int i) throws SQLException{
            
                if (rs.getBytes(i) == null){
                    return null;
                }
                
		IDatatype dt = null;
		
		switch(rs.getMetaData().getColumnType(i)){
		
		case java.sql.Types.NULL: break;

		case java.sql.Types.INTEGER:
		case java.sql.Types.NUMERIC:
		case java.sql.Types.DECIMAL:
			dt = DatatypeMap.newInstance(rs.getInt(i));break;
			
		case java.sql.Types.BIGINT:
			dt = DatatypeMap.newInstance(rs.getLong(i));break;
			
		case java.sql.Types.FLOAT: 
			dt = DatatypeMap.newInstance(rs.getFloat(i));break;
			
		case java.sql.Types.DOUBLE:
			dt = DatatypeMap.newInstance(rs.getDouble(i));break;
			
		case java.sql.Types.BOOLEAN: 
			dt = DatatypeMap.newInstance(rs.getBoolean(i));break;
			
		case java.sql.Types.DATE: 
			if (rs.getDate(i) != null){
				dt = DatatypeMap.newDate(rs.getDate(i).toString());
			}
			break;
			
		default: 
			if (rs.getString(i) != null){
				dt = DatatypeMap.newInstance(rs.getString(i).trim());
			}
		}
		
		Node node = null;
		if (dt != null){
			node = producer.getNode(dt);
		}
		return node;
	}
		
	
	Mappings map(List<Node> nodes, IDatatype dt){
		Node[] qNodes = new Node[nodes.size()];
		int i = 0;
		for (Node qNode : nodes){
			qNodes[i++] = qNode;
		}
		Mappings lMap = new Mappings();
		List<Node> lNode = producer.toNodeList(dt);
		for (Node node : lNode){
			Node[] tNodes = new Node[1];
			tNodes[0] = node;
			Mapping map =  Mapping.create(qNodes, tNodes);
			lMap.add(map);
		}
		return lMap;
	}
	
	
	
	
	
	

}
