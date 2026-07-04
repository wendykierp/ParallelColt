/*
Copyright (C) 1999 CERN - European Organization for Nuclear Research.
Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose 
is hereby granted without fee, provided that the above copyright notice appear in all copies and 
that both that copyright notice and this permission notice appear in supporting documentation. 
CERN makes no representations about the suitability of this software for any purpose. 
It is provided "as is" without expressed or implied warranty.
 */
package cern.jet.math.tdouble;

/**
 * Only for performance tuning of compute intensive linear algebraic
 * computations. Constructs functions that return one of
 * <ul>
 * <li><code>a*constant + b</code>
 * <li><code>a*constant - b</code>
 * <li><code>a/constant + b</code>
 * <li><code>a/constant - b</code>
 * </ul>
 * <code>a</code> and <code>b</code> are variables, <code>constant</code> is fixed, but for
 * performance reasons publicly accessible. Intended to be passed to
 * <code>matrix.assign(otherMatrix,function)</code> methods.
 */
public final class DoublePlusMultFirst implements cern.colt.function.tdouble.DoubleDoubleFunction {
    /**
     * Public read/write access to avoid frequent object construction.
     */
    public double multiplicator;

    protected DoublePlusMultFirst(final double multiplicator) {
        this.multiplicator = multiplicator;
    }

    /**
     * Returns the result of the function evaluation.
     * @param a
     * @param b
     */
    public final double apply(double a, double b) {
        return a * multiplicator + b;
    }

    /**
     * <code>a - b/constant</code>.
     * @param constant
     * @return 
     */
    public static DoublePlusMultFirst minusDiv(final double constant) {
        return new DoublePlusMultFirst(-1 / constant);
    }

    /**
     * <code>a - b*constant</code>.
     * @param constant
     * @return 
     */
    public static DoublePlusMultFirst minusMult(final double constant) {
        return new DoublePlusMultFirst(-constant);
    }

    /**
     * <code>a + b/constant</code>.
     * @param constant
     * @return 
     */
    public static DoublePlusMultFirst plusDiv(final double constant) {
        return new DoublePlusMultFirst(1 / constant);
    }

    /**
     * <code>a + b*constant</code>.
     * @param constant
     * @return 
     */
    public static DoublePlusMultFirst plusMult(final double constant) {
        return new DoublePlusMultFirst(constant);
    }
}
