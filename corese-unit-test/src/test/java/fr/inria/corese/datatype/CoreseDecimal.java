package fr.inria.corese.datatype;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;

import org.junit.Test;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;

public class CoreseDecimal {

    @Test
    public void newInstanceDecimalTest() {

        String number = "1.234567890987654321";
        BigDecimal big_decimal_number = new BigDecimal(number);
        Double double_number = Double.valueOf(number);

        IDatatype corese_decimal = DatatypeMap.newDecimal(big_decimal_number);

        assertEquals(big_decimal_number, corese_decimal.decimalValue());
        assertEquals(double_number, corese_decimal.doubleValue(), 0);
    }

    @Test
    public void newInstanceDoubleTest() {
        String number = "1.234567890987654321";
        Double double_number = Double.valueOf(number);

        IDatatype corese_decimal = DatatypeMap.newDecimal(double_number);

        assertEquals(double_number, corese_decimal.decimalValue().doubleValue(), 0);
        assertEquals(double_number, corese_decimal.doubleValue(), 0);
    }

    @Test
    public void addDecimalDecimal() {
        String number = "1.22222222222222222222";
        Double double_number = Double.valueOf(number);

        IDatatype corese_decimal_1 = DatatypeMap.newDecimal(double_number);
        IDatatype corese_decimal_2 = DatatypeMap.newDecimal(double_number);

        // …
    }
    

}
