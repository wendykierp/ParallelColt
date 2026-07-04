/*
Copyright (C) 1999 CERN - European Organization for Nuclear Research.
Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose 
is hereby granted without fee, provided that the above copyright notice appear in all copies and 
that both that copyright notice and this permission notice appear in supporting documentation. 
CERN makes no representations about the suitability of this software for any purpose. 
It is provided "as is" without expressed or implied warranty.
 */
package cern.jet.math.tint;

/**
 * Only for performance tuning of compute intensive linear algebraic
 * computations. Constructs functions that return one of
 * <ul>
 * <li><code>a * constant</code>
 * <li><code>a / constant</code>
 * </ul>
 * <code>a</code> is variable, <code>constant</code> is fixed, but for performance
 * reasons publicly accessible. Intended to be passed to
 * <code>matrix.assign(function)</code> methods.
 */
public final class IntMult implements cern.colt.function.tint.IntFunction {
    /**
     * Public read/write access to avoid frequent object construction.
     */
    public int multiplicator;

    /**
     * Insert the method's description here. Creation date: (8/10/99 19:12:09)
     * @param multiplicator
     */
    protected IntMult(final int multiplicator) {
        this.multiplicator = multiplicator;
    }

    /**
     * Returns the result of the function evaluation.
     * @param a
     */
    public final int apply(int a) {
        return a * multiplicator;
    }

    /**
     * <code>a / constant</code>.
     * @param constant
     * @return 
     */
    public static IntMult div(final int constant) {
        return mult(1 / constant);
    }

    /**
     * <code>a * constant</code>.
     * @param constant
     * @return 
     */
    public static IntMult mult(final int constant) {
        return new IntMult(constant);
    }
}
