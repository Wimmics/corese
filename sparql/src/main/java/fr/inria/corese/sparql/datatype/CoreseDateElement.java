package fr.inria.corese.sparql.datatype;

public abstract class CoreseDateElement extends CoreseInteger {

    public CoreseDateElement(String value) {
        super(value);
    }

    @Override
    public boolean isDateElement() {
        return true;
    }

}
