package fr.inria.corese.datatype;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;

import org.junit.Test;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.CoreseDecimal;
import fr.inria.corese.sparql.datatype.CoreseDouble;
import fr.inria.corese.sparql.datatype.CoreseFloat;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.datatype.XSD;
import fr.inria.corese.sparql.exceptions.CoreseDatatypeException;

public class CoreseDecimalTest {

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
    public void newInstanceIntTest() {
        String number = "5";
        int int_number = Integer.valueOf(number);

        IDatatype corese_decimal = DatatypeMap.newDecimal(int_number);

        assertEquals(int_number, corese_decimal.decimalValue().doubleValue(), 0);
        assertEquals(int_number, corese_decimal.doubleValue(), 0);
        assertEquals(int_number, corese_decimal.intValue());
    }

    @Test
    public void addDecimalDecimal() {
        String number = "1.22222222222222222222";

        Double double_number = Double.valueOf(number);
        BigDecimal bd_number = new BigDecimal(number);

        CoreseDecimal corese_decimal_1 = (CoreseDecimal) DatatypeMap.newDecimal(bd_number);
        CoreseDecimal corese_decimal_2 = (CoreseDecimal) DatatypeMap.newDecimal(bd_number);

        assertEquals(double_number + double_number, corese_decimal_1.doubleValue() + corese_decimal_2.doubleValue(), 0);
        assertEquals(bd_number.add(bd_number), corese_decimal_1.plus(corese_decimal_2).decimalValue());
        assertEquals(XSD.xsddecimal, corese_decimal_1.plus(corese_decimal_2).getDatatypeURI());
    }

    @Test
    public void addDoubleDecimal() {
        String number = "1.22222222222222222222";

        Double double_number = Double.valueOf(number);
        BigDecimal bd_number = new BigDecimal(number);

        CoreseDecimal corese_decimal = (CoreseDecimal) DatatypeMap.newDecimal(bd_number);
        CoreseDouble corese_double = (CoreseDouble) DatatypeMap.newDouble(double_number);

        assertEquals(double_number + bd_number.doubleValue(), corese_double.plus(corese_decimal).doubleValue(), 0);
        assertEquals(bd_number.doubleValue() + double_number, corese_decimal.plus(corese_double).doubleValue(), 0);
        assertEquals(XSD.xsddouble, corese_double.plus(corese_decimal).getDatatypeURI());
        assertEquals(XSD.xsddouble, corese_decimal.plus(corese_double).getDatatypeURI());
    }

    @Test
    public void addFloatDecimal() {
        String number = "1.22222222222222222222";

        Float float_number = Float.valueOf(number);
        BigDecimal bd_number = new BigDecimal(number);

        CoreseDecimal corese_decimal = (CoreseDecimal) DatatypeMap.newDecimal(bd_number);
        CoreseFloat corese_float = (CoreseFloat) DatatypeMap.newFloat(float_number);

        assertEquals(float_number + bd_number.floatValue(), corese_float.plus(corese_decimal).floatValue(), 0);
        assertEquals(bd_number.floatValue() + float_number, corese_decimal.plus(corese_float).floatValue(), 0);
        assertEquals(XSD.xsdfloat, corese_float.plus(corese_decimal).getDatatypeURI());
        assertEquals(XSD.xsdfloat, corese_decimal.plus(corese_float).getDatatypeURI());
    }

    @Test
    public void minusDecimalDecimal() {
        String number = "1.22222222222222222222";

        Double double_number = Double.valueOf(number);
        BigDecimal bd_number = new BigDecimal(number);

        CoreseDecimal corese_decimal_1 = (CoreseDecimal) DatatypeMap.newDecimal(bd_number);
        CoreseDecimal corese_decimal_2 = (CoreseDecimal) DatatypeMap.newDecimal(bd_number);

        assertEquals(double_number - double_number, corese_decimal_1.doubleValue() - corese_decimal_2.doubleValue(), 0);
        assertEquals(bd_number.subtract(bd_number), corese_decimal_1.minus(corese_decimal_2).decimalValue());
        assertEquals(XSD.xsddecimal, corese_decimal_1.minus(corese_decimal_2).getDatatypeURI());
    }

    @Test
    public void minusDoubleDecimal() {
        String number = "1.22222222222222222222";

        Double double_number = Double.valueOf(number);
        BigDecimal bd_number = new BigDecimal(number);

        CoreseDecimal corese_decimal = (CoreseDecimal) DatatypeMap.newDecimal(bd_number);
        CoreseDouble corese_double = (CoreseDouble) DatatypeMap.newDouble(double_number);

        assertEquals(double_number - bd_number.doubleValue(), corese_double.minus(corese_decimal).doubleValue(), 0);
        assertEquals(bd_number.doubleValue() - double_number, corese_decimal.minus(corese_double).doubleValue(), 0);
        assertEquals(XSD.xsddouble, corese_double.minus(corese_decimal).getDatatypeURI());
        assertEquals(XSD.xsddouble, corese_decimal.minus(corese_double).getDatatypeURI());
    }

    @Test
    public void minusFloatDecimal() {
        String number = "1.22222222222222222222";

        Float float_number = Float.valueOf(number);
        BigDecimal bd_number = new BigDecimal(number);

        CoreseDecimal corese_decimal = (CoreseDecimal) DatatypeMap.newDecimal(bd_number);
        CoreseFloat corese_float = (CoreseFloat) DatatypeMap.newFloat(float_number);

        assertEquals(float_number - bd_number.floatValue(), corese_float.minus(corese_decimal).floatValue(), 0);
        assertEquals(bd_number.floatValue() - float_number, corese_decimal.minus(corese_float).floatValue(), 0);
        assertEquals(XSD.xsdfloat, corese_float.plus(corese_decimal).getDatatypeURI());
        assertEquals(XSD.xsdfloat, corese_decimal.plus(corese_float).getDatatypeURI());
    }

    // Fixme: (decimal value / decimal value) return a double and not a decimal
    @Test
    public void divideDecimalDecimal() {
        String number1 = "1.22222222222222222222";

        Double double_number = Double.valueOf(number1);
        BigDecimal bd_number = new BigDecimal(number1);

        CoreseDecimal corese_decimal_1 = (CoreseDecimal) DatatypeMap.newDecimal(bd_number);
        CoreseDecimal corese_decimal_2 = (CoreseDecimal) DatatypeMap.newDecimal(bd_number);

        assertEquals(double_number / double_number, corese_decimal_1.doubleValue() / corese_decimal_2.doubleValue(), 0);
        assertEquals(bd_number.divide(bd_number).doubleValue(), corese_decimal_1.div(corese_decimal_2).doubleValue(), 0);
        // assertEquals(bd_number.divide(bd_number), corese_decimal_1.div(corese_decimal_2).decimalValue());
        assertEquals(XSD.xsddecimal, corese_decimal_1.div(corese_decimal_2).getDatatypeURI());
    }

    @Test
    public void divideDoubleDecimal() {
        String number = "1.22222222222222222222";

        Double double_number = Double.valueOf(number); // 1.2222222222222223
        BigDecimal bd_number = new BigDecimal(number); // 1.22222222222222222222

        CoreseDecimal corese_decimal = (CoreseDecimal) DatatypeMap.newDecimal(bd_number);
        CoreseDouble corese_double = (CoreseDouble) DatatypeMap.newDouble(double_number);

        assertEquals(double_number / bd_number.doubleValue(), corese_double.div(corese_decimal).doubleValue(), 0);
        assertEquals(bd_number.doubleValue() / double_number, corese_decimal.div(corese_double).doubleValue(), 0);
        assertEquals(XSD.xsddouble, corese_double.div(corese_decimal).getDatatypeURI());
        assertEquals(XSD.xsddouble, corese_decimal.div(corese_double).getDatatypeURI());
    }

    @Test
    public void divideDoubleDecimal_2() {
        String number = "1.22222222222222222222";
        String divisor = "2";

        Double double_number = Double.valueOf(number);
        BigDecimal bd_number = new BigDecimal(number);

        Double double_divisor = Double.valueOf(divisor);
        BigDecimal bd_divisor = new BigDecimal(divisor);

        CoreseDecimal corese_decimal = (CoreseDecimal) DatatypeMap.newDecimal(bd_number);
        CoreseDouble corese_double = (CoreseDouble) DatatypeMap.newDouble(double_number);

        CoreseDecimal corese_divisor_decimal = (CoreseDecimal) DatatypeMap.newDecimal(bd_divisor);
        CoreseDouble corese_divisor_double = (CoreseDouble) DatatypeMap.newDouble(double_divisor);

        assertEquals(double_number / bd_divisor.doubleValue(), corese_double.div(corese_divisor_decimal).doubleValue(),
                0);
        assertEquals(bd_number.doubleValue() / double_divisor, corese_decimal.div(corese_divisor_double).doubleValue(),
                0);
        assertEquals(XSD.xsddouble, corese_double.div(corese_divisor_decimal).getDatatypeURI());
        assertEquals(XSD.xsddouble, corese_decimal.div(corese_divisor_double).getDatatypeURI());
    }

    @Test
    public void divideFloatDecimal() {
        String number = "1.22222222222222222222";

        Float float_number = Float.valueOf(number);
        BigDecimal bd_number = new BigDecimal(number);

        CoreseDecimal corese_decimal = (CoreseDecimal) DatatypeMap.newDecimal(bd_number);
        CoreseFloat corese_float = (CoreseFloat) DatatypeMap.newFloat(float_number);

        assertEquals(float_number / bd_number.floatValue(), corese_float.div(corese_decimal).floatValue(), 0);
        assertEquals(bd_number.floatValue() / float_number, corese_decimal.div(corese_float).floatValue(), 0);
        assertEquals(XSD.xsdfloat, corese_float.div(corese_decimal).getDatatypeURI());
        assertEquals(XSD.xsdfloat, corese_decimal.div(corese_float).getDatatypeURI());
    }

    @Test
    public void multDecimalDecimal() {
        String number = "1.22222222222222222222";

        Double double_number = Double.valueOf(number);
        BigDecimal bd_number = new BigDecimal(number);

        CoreseDecimal corese_decimal_1 = (CoreseDecimal) DatatypeMap.newDecimal(bd_number);
        CoreseDecimal corese_decimal_2 = (CoreseDecimal) DatatypeMap.newDecimal(bd_number);

        assertEquals(double_number * double_number, corese_decimal_1.doubleValue() * corese_decimal_2.doubleValue(), 0);
        assertEquals(bd_number.multiply(bd_number), corese_decimal_1.mult(corese_decimal_2).decimalValue());
        assertEquals(XSD.xsddecimal, corese_decimal_1.mult(corese_decimal_2).getDatatypeURI());
    }

    @Test
    public void multDoubleDecimal() {
        String number = "1.22222222222222222222";

        Double double_number = Double.valueOf(number);
        BigDecimal bd_number = new BigDecimal(number);

        CoreseDecimal corese_decimal = (CoreseDecimal) DatatypeMap.newDecimal(bd_number);
        CoreseDouble corese_double = (CoreseDouble) DatatypeMap.newDouble(double_number);

        assertEquals(double_number * bd_number.doubleValue(), corese_double.mult(corese_decimal).doubleValue(), 0);
        assertEquals(bd_number.doubleValue() * double_number, corese_decimal.mult(corese_double).doubleValue(), 0);
        assertEquals(XSD.xsddouble, corese_double.mult(corese_decimal).getDatatypeURI());
        assertEquals(XSD.xsddouble, corese_decimal.mult(corese_double).getDatatypeURI());
    }

    @Test
    public void multFloatDecimal() {
        String number = "1.22222222222222222222";

        Float float_number = Float.valueOf(number);
        BigDecimal bd_number = new BigDecimal(number);

        CoreseDecimal corese_decimal = (CoreseDecimal) DatatypeMap.newDecimal(bd_number);
        CoreseFloat corese_float = (CoreseFloat) DatatypeMap.newFloat(float_number);

        assertEquals(float_number * bd_number.floatValue(), corese_float.mult(corese_decimal).floatValue(), 0);
        assertEquals(bd_number.floatValue() * float_number, corese_decimal.mult(corese_float).floatValue(), 0);
        assertEquals(XSD.xsdfloat, corese_float.mult(corese_decimal).getDatatypeURI());
        assertEquals(XSD.xsdfloat, corese_decimal.mult(corese_float).getDatatypeURI());
    }

    @Test
    public void decimalLabel() throws CoreseDatatypeException {
        String number_string = "1.22222222222222222222";
        Double number_double = Double.valueOf(number_string);
        BigDecimal number_decimal = new BigDecimal(number_string);

        // From Constructors
        CoreseDecimal cd_string = new CoreseDecimal(number_string);
        CoreseDecimal cd_double = new CoreseDecimal(number_double);
        CoreseDecimal cd_decimal = new CoreseDecimal(number_decimal);

        // Tests
        assertEquals(XSD.xsddecimal, cd_string.getDatatypeURI());
        assertEquals(XSD.xsddecimal, cd_double.getDatatypeURI());
        assertEquals(XSD.xsddecimal, cd_decimal.getDatatypeURI());
        
        assertEquals(number_string, cd_string.getLabel());
        assertEquals(number_decimal, cd_string.decimalValue());
        
        assertEquals(number_double.toString(), cd_double.getLabel());
        assertEquals(BigDecimal.valueOf(number_double), cd_double.decimalValue());

        assertEquals(number_string, cd_decimal.getLabel());
        assertEquals(number_decimal, cd_decimal.decimalValue());
    }

    @Test
    public void decimalValueNumbers() {
        String number_string = "1.22222222222222222222";
        Double number_double = Double.valueOf(number_string);
        Float number_float = Float.valueOf(number_string);
        BigDecimal number_decimal = new BigDecimal(number_string);
        int number_integer = 1;

        IDatatype corese_double = DatatypeMap.newDouble(number_double);
        IDatatype corese_float = DatatypeMap.newFloat(number_float);
        IDatatype corese_decimal = DatatypeMap.newDecimal(number_decimal);
        IDatatype corese_integer = DatatypeMap.newInteger(number_integer);

        assertEquals(BigDecimal.valueOf(number_double), corese_double.decimalValue());
        assertEquals(BigDecimal.valueOf(number_float), corese_float.decimalValue());
        assertEquals(number_decimal, corese_decimal.decimalValue());
        assertEquals(BigDecimal.valueOf(number_integer), corese_integer.decimalValue());
    }

}