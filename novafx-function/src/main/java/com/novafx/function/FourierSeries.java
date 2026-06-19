package com.novafx.function;

/**
 * Utility for computing and generating Fourier series expressions.
 * <p>
 * Evaluates the Fourier series:
 * {@code f(t) = a * Σ[n=1..N] sin(n * ω * t) / n^d}
 * <p>
 * Where {@code N} is the number of harmonics, {@code ω} is the base
 * frequency, {@code d} is the decay rate, and {@code a} is the amplitude.
 * <p>
 * Higher decay ({@code d}) produces smoother wave forms. Common values:
 * {@code d=0} (square-like), {@code d=1} (sawtooth), {@code d=2} (smooth).
 */
public final class FourierSeries {

    private FourierSeries() {
    }

    /**
     * Evaluates the Fourier series at a given parameter value.
     *
     * @param t          the parameter value
     * @param harmonics  number of harmonic terms (N), must be >= 1
     * @param freq       base angular frequency (ω)
     * @param decay      harmonic amplitude decay exponent (d)
     * @param amp        overall amplitude scale factor (a)
     * @return the series sum at t
     */
    public static double evaluate(double t, int harmonics, double freq, double decay, double amp) {
        if (harmonics < 1) throw new IllegalArgumentException("harmonics must be >= 1");
        double sum = 0.0;
        for (int n = 1; n <= harmonics; n++) {
            sum += Math.sin(n * freq * t) / Math.pow(n, decay);
        }
        return amp * sum;
    }

    /**
     * Generates an expression string representing this Fourier series
     * using the built-in {@code fourier} function.
     * <p>
     * Example: {@code fourier(t, 10, 1, 0.5, 1)}
     *
     * @param harmonics number of harmonic terms
     * @param freq      base angular frequency
     * @param decay     harmonic amplitude decay exponent
     * @param amp       overall amplitude scale factor
     * @return expression string usable in the AST pipeline
     */
    public static String toExpression(int harmonics, double freq, double decay, double amp) {
        return "fourier(t," + harmonics + "," + format(freq) + "," + format(decay) + "," + format(amp) + ")";
    }

    private static String format(double v) {
        if (v == Math.floor(v) && !Double.isInfinite(v) && v <= Integer.MAX_VALUE) {
            return String.valueOf((int) v);
        }
        return String.valueOf(v);
    }
}
