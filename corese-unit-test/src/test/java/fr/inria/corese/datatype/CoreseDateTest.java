package fr.inria.corese.datatype;

import static org.junit.Assert.assertEquals;

import com.ibm.icu.util.Calendar;

import org.eclipse.rdf4j.model.Literal;
import org.junit.Test;

import fr.inria.corese.sparql.datatype.CoreseCalendar;
import fr.inria.corese.sparql.datatype.CoreseDate;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.rdf4j.CoreseDatatypeToRdf4jValue;
import fr.inria.corese.sparql.rdf4j.Rdf4jValueToCoreseDatatype;

public class CoreseDateTest {

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
        assertEquals(400, corese_dt.getCalendar().get(Calendar.MILLISECOND));

        Literal rdf4j_dt = (Literal) CoreseDatatypeToRdf4jValue.convert(corese_dt);
        assertEquals(400, rdf4j_dt.calendarValue().getMillisecond());

        CoreseDate corese_dt_2 = (CoreseDate) Rdf4jValueToCoreseDatatype.convert(rdf4j_dt);
        assertEquals(400, corese_dt_2.getCalendar().get(Calendar.MILLISECOND));

        assertEquals(corese_dt, corese_dt_2);
    }

    @Test
    public void testDuplicateCalendar() {
        CoreseDate corese_dt = (CoreseDate) DatatypeMap.newDate("2021-07-21T08:08:08.4");

        CoreseCalendar cal_1 = corese_dt.getCalendar();
        CoreseCalendar cal_2 = cal_1.duplicate();

        assertEquals(cal_1, cal_2);
    }

}