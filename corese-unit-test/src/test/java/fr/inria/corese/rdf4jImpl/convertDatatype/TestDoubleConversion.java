package fr.inria.corese.rdf4jImpl.convertDatatype;

import static org.junit.Assert.assertEquals;

import org.eclipse.rdf4j.model.Literal;
import org.junit.Test;

import fr.inria.corese.rdf4j.convert.datatypes.CoreseDatatypeToRdf4jValue;
import fr.inria.corese.rdf4j.convert.datatypes.Rdf4jValueToCoreseDatatype;
import fr.inria.corese.sparql.datatype.CoreseDate;
import fr.inria.corese.sparql.datatype.DatatypeMap;

public class TestDoubleConversion {

    @Test
    public void testDoubleConversionDateNow() {
        CoreseDate corese_dt = (CoreseDate) DatatypeMap.newDate();
        Literal rdf4j_dt = (Literal) CoreseDatatypeToRdf4jValue.convert(corese_dt);
        CoreseDate corese_dt_2 = (CoreseDate) Rdf4jValueToCoreseDatatype.convert(rdf4j_dt);

        assertEquals(corese_dt, corese_dt_2);
    }

    @Test
    public void testDoubleConversionDate() {
        CoreseDate corese_dt = (CoreseDate) DatatypeMap.newDate("2021-07-21T08:08:08.4");
        assertEquals(400, corese_dt.getCalendar().getMillisecond());

        Literal rdf4j_dt = (Literal) CoreseDatatypeToRdf4jValue.convert(corese_dt);
        assertEquals(400, rdf4j_dt.calendarValue().getMillisecond());

        CoreseDate corese_dt_2 = (CoreseDate) Rdf4jValueToCoreseDatatype.convert(rdf4j_dt);
        assertEquals(400, corese_dt_2.getCalendar().getMillisecond());

        assertEquals(corese_dt, corese_dt_2);
    }

}
