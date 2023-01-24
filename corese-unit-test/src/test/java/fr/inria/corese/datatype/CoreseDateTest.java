package fr.inria.corese.datatype;

import static org.junit.Assert.assertEquals;

import javax.xml.datatype.DatatypeConfigurationException;

import org.junit.Test;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.CoreseDate;
import fr.inria.corese.sparql.datatype.CoreseDateTime;
import fr.inria.corese.sparql.datatype.CoreseDecimal;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.datatype.XSD;

public class CoreseDateTest {

    @Test
    public void testGetSecond() {
        CoreseDate corese_dt = (CoreseDate) DatatypeMap.newDateTime("2021-07-20T10:00:00.12");
        assertEquals(new CoreseDecimal(0.12), corese_dt.getSecond());
    }

    @Test
    public void compareLessGreaterTimeZone() {
        IDatatype dt = DatatypeMap.newDateTime("2002-10-10T12:00:00+00:00");

        IDatatype dt1 = DatatypeMap.newDateTime("2002-10-10T12:00:00-06:00");
        IDatatype dt2 = DatatypeMap.newDateTime("2002-10-10T12:00:00+00:00");
        IDatatype dt3 = DatatypeMap.newDateTime("2002-10-10T12:00:00+06:00");

        assertEquals(true, dt.lt(dt1).booleanValue());
        assertEquals(true, dt.le(dt1).booleanValue());
        assertEquals(false, dt.eq(dt1).booleanValue());
        assertEquals(false, dt.ge(dt1).booleanValue());
        assertEquals(false, dt.gt(dt1).booleanValue());

        assertEquals(false, dt.lt(dt2).booleanValue());
        assertEquals(true, dt.le(dt2).booleanValue());
        assertEquals(true, dt.eq(dt2).booleanValue());
        assertEquals(true, dt.ge(dt2).booleanValue());
        assertEquals(false, dt.gt(dt2).booleanValue());

        assertEquals(false, dt.lt(dt3).booleanValue());
        assertEquals(false, dt.le(dt3).booleanValue());
        assertEquals(false, dt.eq(dt3).booleanValue());
        assertEquals(true, dt.ge(dt3).booleanValue());
        assertEquals(true, dt.gt(dt3).booleanValue());
    }

    @Test
    public void compareLessGreater() {
        IDatatype dt = DatatypeMap.newDateTime("2002-10-10T12:00:00");

        IDatatype dt1 = DatatypeMap.newDateTime("2002-10-10T12:00:00.1");
        IDatatype dt2 = DatatypeMap.newDateTime("2002-10-10T12:00:00");
        IDatatype dt3 = DatatypeMap.newDateTime("2002-10-10T11:59:00.999");

        assertEquals(true, dt.lt(dt1).booleanValue());
        assertEquals(true, dt.le(dt1).booleanValue());
        assertEquals(false, dt.eq(dt1).booleanValue());
        assertEquals(false, dt.ge(dt1).booleanValue());
        assertEquals(false, dt.gt(dt1).booleanValue());

        assertEquals(false, dt.lt(dt2).booleanValue());
        assertEquals(true, dt.le(dt2).booleanValue());
        assertEquals(true, dt.eq(dt2).booleanValue());
        assertEquals(true, dt.ge(dt2).booleanValue());
        assertEquals(false, dt.gt(dt2).booleanValue());

        assertEquals(false, dt.lt(dt3).booleanValue());
        assertEquals(false, dt.le(dt3).booleanValue());
        assertEquals(false, dt.eq(dt3).booleanValue());
        assertEquals(true, dt.ge(dt3).booleanValue());
        assertEquals(true, dt.gt(dt3).booleanValue());
    }

    @Test
    public void compareEqual() {
        IDatatype dt1 = DatatypeMap.newDate("2002-10-10T12:00:00");
        IDatatype dt2 = DatatypeMap.newDate("2002-10-10T12:00:00");
        IDatatype dt3 = DatatypeMap.newDate("2002-10-10T12:00:00.001");
        assertEquals(true, dt1.eq(dt2).booleanValue());
        assertEquals(false, dt1.eq(dt3).booleanValue());
    }

    @Test
    public void constructorEmpty() throws DatatypeConfigurationException {
        new CoreseDate();
    }

    @Test
    public void getCode() throws DatatypeConfigurationException {
        IDatatype date = DatatypeMap.newDate();
        assertEquals(IDatatype.DATETIME, date.getCode());
    }

    @Test
    public void isDate() throws DatatypeConfigurationException {
        IDatatype date = DatatypeMap.newDate();
        assertEquals(true, date.isDate());
    }

    @Test
    public void getDatatype() throws DatatypeConfigurationException {
        IDatatype date = DatatypeMap.newDate();
        assertEquals(XSD.xsddateTime, date.getDatatypeURI());
    }

    @Test
    public void getters() throws DatatypeConfigurationException {
        CoreseDateTime date = (CoreseDateTime) DatatypeMap.newDateTime("2021-07-20T10:23:54.005-05:00");
        assertEquals(DatatypeMap.newInstance(2021), date.getYear());
        assertEquals(DatatypeMap.newInstance(07), date.getMonth());
        assertEquals(DatatypeMap.newInstance(20), date.getDay());
        assertEquals(DatatypeMap.newInstance(10), date.getHour());
        assertEquals(DatatypeMap.newInstance(23), date.getMinute());
        assertEquals(DatatypeMap.newDecimal(54.005), date.getSecond());
    }

    @Test
    public void getTZ() throws DatatypeConfigurationException {
        CoreseDateTime date = (CoreseDateTime) DatatypeMap.newDateTime("2021-07-20T10:23:54.5");
        assertEquals(DatatypeMap.newLiteral(""), date.getTZ());

        date = (CoreseDateTime) DatatypeMap.newDateTime("2021-07-23T13:01:44Z");
        assertEquals(DatatypeMap.newLiteral("Z"), date.getTZ());

        date = (CoreseDateTime) DatatypeMap.newDateTime("2021-07-20T10:23:54.5-05:00");
        assertEquals(DatatypeMap.newLiteral("-05:00"), date.getTZ());

        date = (CoreseDateTime) DatatypeMap.newDateTime("2021-07-20T10:23:54.5+11:00");
        assertEquals(DatatypeMap.newLiteral("+11:00"), date.getTZ());
    }

    @Test
    public void getTimezone() throws DatatypeConfigurationException {
        CoreseDateTime date = (CoreseDateTime) DatatypeMap.newDateTime("2021-07-20T10:23:54.5");
        assertEquals(null, date.getTimezone());

        date = (CoreseDateTime) DatatypeMap.newDateTime("2021-07-23T13:01:44Z");
        assertEquals(DatatypeMap.newInstance("PT0S"), date.getTimezone());

        date = (CoreseDateTime) DatatypeMap.newDateTime("2021-07-20T10:23:54.5-05:00");
        assertEquals(DatatypeMap.newLiteral("-PT5H"), date.getTimezone());

        date = (CoreseDateTime) DatatypeMap.newDateTime("2021-07-20T10:23:54.5+11:00");
        assertEquals(DatatypeMap.newLiteral("+PT11H"), date.getTimezone());
    }

}