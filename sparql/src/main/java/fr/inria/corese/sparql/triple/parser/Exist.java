package fr.inria.corese.sparql.triple.parser;

public class Exist extends BasicGraphPattern {
	
        public Exist() {}
    
	Exist(Exp e){
		super(e);
	}

	public static Exist create(Exp e1){
		return new Exist(e1);
	}

        @Override
	public boolean isExist(){
		return true;
	}
        
        public Exist getExist() {
            return this;
        }
	
        @Override
	public StringBuffer toString(StringBuffer sb){
		sb.append(Term.EXIST + " ").append(get(0));
		return sb;
	}
	
        @Override
	public boolean validate(ASTQuery ast, boolean exist) {
		if (getBody().size() > 0){
			return getBody().get(0).validate(ast, true);
		}
		return true;
	}
        
        public Exp getContent(){
            return get(0).get(0);
        }
	
}