package fr.inria.corese.sparql.triple.function.core;

import java.util.Optional;

import fr.inria.corese.kgram.api.core.ExprType;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.CoreseDouble;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.triple.function.term.TermEval;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class ZeroaryFunction extends TermEval {

    public ZeroaryFunction() {
    }

    public ZeroaryFunction(String name) {
        super(name);
    }

    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) throws EngineException {
        switch (oper()) {
            case ExprType.RANDOM:
                return CoreseDouble.create(Math.random());
            case ExprType.NOW:
                return this.getOrSetCurrentTime(b);

        }
        return null;
    }

    /**
     * Returns the current time, or sets it if it is not already set.
     * 
     * @param binding The Binding to get or set the current time in
     * @return The current time
     */
    private IDatatype getOrSetCurrentTime(Binding binding) {

        // Check if current time is already set in the Binding
        Optional<IDatatype> savedNowTime = binding.getNowValue();

        if (savedNowTime.isPresent()) {
            return savedNowTime.get();
        } else {
            // If not set, create a new time, save it, and return it
            IDatatype nowValue = DatatypeMap.newDate();
            binding.setNowValue(nowValue);
            return nowValue;
        }
    }
}
