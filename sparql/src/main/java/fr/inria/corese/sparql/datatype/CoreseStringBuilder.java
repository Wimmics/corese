package fr.inria.corese.sparql.datatype;

public class CoreseStringBuilder extends CoreseString {
	
	StringBuilder sb;
	
	CoreseStringBuilder(StringBuilder s){
		sb = s;
		value = null;
	}
	
	public static CoreseStringBuilder create(StringBuilder s){
		return new CoreseStringBuilder(s);
	}
	
        @Override
	public String getLabel(){
		if (value == null){
			value = sb.toString();
		}
		return value;
	}
	
        @Override
	public StringBuilder getStringBuilder(){
		return sb;
	}
        
        @Override
        public void setStringBuilder(StringBuilder s){
            sb = s;
	}
        

}
