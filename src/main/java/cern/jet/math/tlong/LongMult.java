/*
Copyright (C) 1999 CERN - European Organization for Nuclear Research.
Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose 
is hereby granted without fee, provided that the above copyright notice appear in all copies and 
that both that copyright notice and this permission notice appear in supporting documentation. 
CERN makes no representations about the suitability of this software for any purpose. 
It is provided "as is" without expressed or implied warranty.
 */
package cern.jet.math.tlong;

/**
 * Only for performance tuning of compute longensive linear algebraic
 * computations. Constructs functions that return one of
 * <ul>
 * <li><code>a * constant</code>
 * <li><code>a / constant</code>
 * </ul>
 * <code>a</code> is variable, <code>constant</code> is fixed, but for performance
 * reasons publicly accessible. Longended to be passed to
 * <code>matrix.assign(function)</code> methods.
 */
public final class LongMult implements cern.colt.function.tlong.LongFunction {
    /**
     * Public read/write access to avoid frequent object construction.
     */
    public long multiplicator;

    /**
     * Insert the method's description here. Creation date: (8/10/99 19:12:09)
     * @param multiplicator
     */
    protected LongMult(final long multiplicator) {
        this.multiplicator = multiplicator;
    }

    /**
     * Returns the result of the function evaluation.
     * @param a
     */
    public final long apply(long a) {
        return a * multiplicator;
    }

    /**
     * <code>a / constant</code>.
     * @param constant
     * @return 
     */
    public static LongMult div(final long constant) {
        return mult(1 / constant);
    }

    /**
     * <code>a * constant</code>.
     * @param constant
     * @return 
     */
    public static LongMult mult(final long constant) {
        return new LongMult(constant);
    }
}
