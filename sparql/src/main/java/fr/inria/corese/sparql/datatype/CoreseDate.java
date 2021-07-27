package fr.inria.corese.sparql.datatype;

import java.math.BigDecimal;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.eclipse.rdf4j.model.Literal;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.exceptions.CoreseDatatypeException;
import fr.inria.corese.sparql.rdf4j.CoreseDatatypeToRdf4jValue;

/**
 * An implementation of the xsd:date datatype used by Corese
 */
public class CoreseDate extends CoreseDatatype {

    private XMLGregorianCalendar cal;
    private static final String TODAY = "today";
    private static int code = DATE;
    private static final CoreseURI datatype = new CoreseURI(RDF.xsddate);

    public CoreseDate() throws DatatypeConfigurationException {
        this.cal = DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar());
    }

    public CoreseDate(XMLGregorianCalendar cal) {
        this.cal = cal;
    }

    public CoreseDate(String label) throws DatatypeConfigurationException {
        if (label.equals(TODAY)) {
            this.cal = DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar());
        } else {
            this.cal = DatatypeFactory.newInstance().newXMLGregorianCalendar(label);
        }
    }

    public static CoreseDateOld today() {
        try {
            return new CoreseDateOld(TODAY);
        } catch (CoreseDatatypeException e) {
            return null; // never happens
        }
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public boolean isDate() {
        return true;
    }

    @Override
    public boolean isNumber() {
        return false;
    }

    @Override
    public IDatatype getDatatype() {
        return datatype;
    }

    @Override
    public String getLabel() {
        return this.cal.toString();
    }

    @Override
    public String getNormalizedLabel() {
        return this.getLabel();
    }

    @Override
    public String getLowerCaseLabel() {
        return this.getLabel();
    }

    @Override
    public Literal getRdf4jValue() {
        return CoreseDatatypeToRdf4jValue.convertLiteral(this);
    }

    public IDatatype getYear() {
        return DatatypeMap.newInstance(this.cal.getYear(), RDF.xsdint);
    }

    public IDatatype getMonth() {
        return DatatypeMap.newInstance(this.cal.getMonth(), RDF.xsdint);
    }

    public IDatatype getDay() {
        return DatatypeMap.newInstance(this.cal.getDay(), RDF.xsdint);
    }

    public IDatatype getHour() {
        return DatatypeMap.newInstance(this.cal.getHour(), RDF.xsdint);
    }

    public IDatatype getMinute() {
        return DatatypeMap.newInstance(this.cal.getMinute(), RDF.xsdint);
    }

    public CoreseDecimal getSecond() {
        if (this.cal.getFractionalSecond() == null) {
            return new CoreseDecimal(this.cal.getSecond());
        }
        return new CoreseDecimal(BigDecimal.valueOf(this.cal.getSecond()).add(this.cal.getFractionalSecond()));
    }

    public IDatatype getTZ() {
        if (cal.getTimezone() == DatatypeConstants.FIELD_UNDEFINED) {
            return DatatypeMap.newLiteral("");
        } else if (cal.getTimezone() == 0) {
            return DatatypeMap.newLiteral("Z");
        } else {
            int tz = cal.getTimezone() / 60;
            String result;
            if (tz > 0) {
                result = String.format("+%02d:00", tz);
            } else {
                result = String.format("%03d:00", tz);
            }
            return DatatypeMap.newLiteral(result);
        }
    }

    public IDatatype getTimezone() {
        if (cal.getTimezone() == DatatypeConstants.FIELD_UNDEFINED) {
            return null;
        } else if (cal.getTimezone() == 0) {
            return DatatypeMap.newInstance("PT0S", RDF.xsddaytimeduration);
        } else {
            int tz = cal.getTimezone() / 60;
            String result;
            if (tz > 0) {
                result = String.format("+PT%dH", tz);
            } else {
                result = String.format("-PT%dH", Math.abs(tz));
            }
            return DatatypeMap.newInstance(result, RDF.xsddaytimeduration);
        }

    }

    public XMLGregorianCalendar getCalendar() {
        return cal;
    }

    int compare(XMLGregorianCalendar cal1, XMLGregorianCalendar cal2) throws CoreseDatatypeException {
        int res = cal1.compare(cal2);
        if (res == DatatypeConstants.INDETERMINATE) {
            throw failure();
        }

        return res;
    }

    void check(IDatatype icod) throws CoreseDatatypeException {
        if (DatatypeMap.SPARQLCompliant && this.getClass() != icod.getClass()) {
            throw failure();
        }
    }

    @Override
    public int compare(IDatatype iod) throws CoreseDatatypeException {
        switch (iod.getCode()) {
            case DATE:
            case DATETIME:
                CoreseDate dt = (CoreseDate) iod;
                XMLGregorianCalendar cal1 = this.getCalendar();
                XMLGregorianCalendar cal2 = dt.getCalendar();
                return this.compare(cal1, cal2);
        }
        throw failure();
    }

    @Override
    public boolean less(IDatatype iod) throws CoreseDatatypeException {
        switch (iod.getCode()) {
            case DATE:
            case DATETIME:
                this.check(iod);
                CoreseDate dt = (CoreseDate) iod;
                XMLGregorianCalendar cal1 = this.getCalendar();
                XMLGregorianCalendar cal2 = dt.getCalendar();
                return this.compare(cal1, cal2) < 0;
        }
        throw failure();
    }

    @Override
    public boolean lessOrEqual(IDatatype iod) throws CoreseDatatypeException {
        switch (iod.getCode()) {
            case DATE:
            case DATETIME:
                this.check(iod);
                CoreseDate dt = (CoreseDate) iod;
                XMLGregorianCalendar cal1 = this.getCalendar();
                XMLGregorianCalendar cal2 = dt.getCalendar();
                return this.compare(cal1, cal2) <= 0;
        }
        throw failure();
    }

    @Override
    public boolean greater(IDatatype iod) throws CoreseDatatypeException {
        switch (iod.getCode()) {
            case DATE:
            case DATETIME:
                this.check(iod);
                CoreseDate dt = (CoreseDate) iod;
                XMLGregorianCalendar cal1 = this.getCalendar();
                XMLGregorianCalendar cal2 = dt.getCalendar();
                return this.compare(cal1, cal2) > 0;
        }
        throw failure();
    }

    @Override
    public boolean greaterOrEqual(IDatatype iod) throws CoreseDatatypeException {
        switch (iod.getCode()) {
            case DATE:
            case DATETIME:
                this.check(iod);
                CoreseDate dt = (CoreseDate) iod;
                XMLGregorianCalendar cal1 = this.getCalendar();
                XMLGregorianCalendar cal2 = dt.getCalendar();
                return this.compare(cal1, cal2) >= 0;
        }
        throw failure();
    }

    @Override
    public boolean equalsWE(IDatatype iod) throws CoreseDatatypeException {
        switch (iod.getCode()) {
            case DATE:
            case DATETIME:
                CoreseDate dt = (CoreseDate) iod;
                XMLGregorianCalendar cal1 = this.getCalendar();
                XMLGregorianCalendar cal2 = dt.getCalendar();
                return this.compare(cal1, cal2) == 0;

            case URI:
            case BLANK:
                return false;
        }
        throw failure();
    }

}