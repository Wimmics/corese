package fr.inria.corese.core.approximate.algorithm;

import org.junit.Test;

import static org.junit.Assert.*;

public class UtilsTest {

    @Test
    public void format1() {
        assertEquals( "3.1416", Utils.format(Math.PI));
        assertEquals( "314.1593", Utils.format(Math.PI* 100));
    }
}