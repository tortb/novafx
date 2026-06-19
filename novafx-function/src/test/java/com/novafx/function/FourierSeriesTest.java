package com.novafx.function;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class FourierSeriesTest {

    @Test
    void singleHarmonicEqualsSin() {
        // fourier(t, 1, 1, 0, 1) = sin(t)
        assertThat(FourierSeries.evaluate(0, 1, 1, 0, 1)).isEqualTo(0.0);
        assertThat(FourierSeries.evaluate(Math.PI / 2, 1, 1, 0, 1))
                .isCloseTo(1.0, within(1e-12));
    }

    @Test
    void twoHarmonicsWithDecay() {
        // N=2, decay=0 → sin(t) + sin(2t)
        double t = 0.5;
        double expected = Math.sin(t) + Math.sin(2 * t);
        assertThat(FourierSeries.evaluate(t, 2, 1, 0, 1))
                .isCloseTo(expected, within(1e-12));
    }

    @Test
    void amplitudeScalesResult() {
        // amp=3 should give 3× the result of amp=1
        double r1 = FourierSeries.evaluate(1.0, 5, 1, 0.5, 1);
        double r3 = FourierSeries.evaluate(1.0, 5, 1, 0.5, 3);
        assertThat(r3).isCloseTo(3 * r1, within(1e-12));
    }

    @Test
    void zeroHarmonicsShouldReject() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> FourierSeries.evaluate(0, 0, 1, 0, 1));
    }

    @Test
    void negativeHarmonicsShouldReject() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> FourierSeries.evaluate(0, -1, 1, 0, 1));
    }

    @Test
    void toExpressionGeneratesValidString() {
        String expr = FourierSeries.toExpression(10, 1, 0.5, 2);
        assertThat(expr).isEqualTo("fourier(t,10,1,0.5,2)");
    }

    @Test
    void toExpressionWithIntValues() {
        String expr = FourierSeries.toExpression(5, 1, 0, 1);
        assertThat(expr).isEqualTo("fourier(t,5,1,0,1)");
    }

    @Test
    void decayTwoProducesSmallerHarmonics() {
        // With decay=2, higher harmonics contribute much less
        double r1 = FourierSeries.evaluate(1.0, 10, 1, 0, 1); // no decay
        double r2 = FourierSeries.evaluate(1.0, 10, 1, 2, 1); // high decay
        assertThat(Math.abs(r2)).isLessThan(Math.abs(r1));
    }

    @Test
    void differentFrequencyChangesResult() {
        double r1 = FourierSeries.evaluate(1.0, 3, 1, 0, 1);
        double r2 = FourierSeries.evaluate(1.0, 3, 2, 0, 1);
        assertThat(r2).isNotCloseTo(r1, within(1e-6));
    }
}
