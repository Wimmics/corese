package fr.inria.corese.sparql.triple.parser.visitor;

import fr.inria.corese.sparql.triple.parser.TopExp;

public class Record {

    private String message;
    private TopExp expression;
    private String uri;
    
    Record(TopExp exp, String mes) {
        setExpression(exp);
        setMessage(mes);
    }
    
    @Override
    public String toString() {
        return getMessage().concat(": ").concat(getExpression().toString());
    }
    
    
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public TopExp getExpression() {
        return expression;
    }

    public void setExpression(TopExp expression) {
        this.expression = expression;
    }

    /**
     * @return the uri
     */
    public String getUri() {
        return uri;
    }

    /**
     * @param uri the uri to set
     */
    public void setUri(String uri) {
        this.uri = uri;
    }

}
