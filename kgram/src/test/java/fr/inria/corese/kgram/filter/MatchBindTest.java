package fr.inria.corese.kgram.filter;

import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class MatchBindTest {

    @org.junit.jupiter.api.Test
    void clean()
    {
        MatchBind m = new MatchBind();
        IntStream.range( 0, 10 ).forEach(
                n -> {
                    m.setValue( Pattern.variable( "variable" + n ), Pattern.variable( "variable" + n ) );
                    assertEquals( n + 1, m.size() );
                }
        );
        m.clean(5);
        assertEquals(5, m.size());
    }
}