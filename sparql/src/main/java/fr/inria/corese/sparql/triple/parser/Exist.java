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
        
        @Override
        public Exist getExist() {
            return this;
        }
	
        @Override
	public ASTBuffer toString(ASTBuffer sb){
		sb.append(Term.EXIST).append(" ");
                sb.incr();
                get(0).toString(sb);
                sb.decr();
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
        
        public Exp getBGP() {
            return get(0);
        }
        
        public void setBGP(Exp exp) {
            set(0, exp);
        }
	
}