package cern.jet.math.tdcomplex;

/**
 * Only for performance tuning of compute intensive linear algebraic
 * computations. Constructs functions that return one of
 * <ul>
 * <li><code>a + b*constant</code>
 * <li><code>a - b*constant</code>
 * <li><code>a + b/constant</code>
 * <li><code>a - b/constant</code>
 * </ul>
 * <code>a</code> and <code>b</code> are variables, <code>constant</code> is fixed, but for
 * performance reasons publicly accessible. Intended to be passed to
 * <code>matrix.assign(otherMatrix,function)</code> methods.
 */

public class DComplexPlusMultSecond implements cern.colt.function.tdcomplex.DComplexDComplexDComplexFunction {
    /**
     * Public read/write access to avoid frequent object construction.
     */
    public double[] multiplicator;

    /**
     * Insert the method's description here. Creation date: (8/10/99 19:12:09)
     * @param multiplicator
     */
    protected DComplexPlusMultSecond(final double[] multiplicator) {
        this.multiplicator = multiplicator;
    }

    /**
     * Returns the result of the function evaluation.
     * @param a
     * @param b
     */
    public final double[] apply(double[] a, double[] b) {
        double[] z = new double[2];
        z[0] = b[0] * multiplicator[0] - b[1] * multiplicator[1];
        z[1] = b[1] * multiplicator[0] + b[0] * multiplicator[1];
        z[0] += a[0];
        z[1] += a[1];
        return z;
    }

    /**
     * <code>a - b/constant</code>.
     * @param constant
     * @return 
     */
    public static DComplexPlusMultSecond minusDiv(final double[] constant) {
        return new DComplexPlusMultSecond(DComplex.neg(DComplex.inv(constant)));
    }

    /**
     * <code>a - b*constant</code>.
     * @param constant
     * @return 
     */
    public static DComplexPlusMultSecond minusMult(final double[] constant) {
        return new DComplexPlusMultSecond(DComplex.neg(constant));
    }

    /**
     * <code>a + b/constant</code>.
     * @param constant
     * @return 
     */
    public static DComplexPlusMultSecond plusDiv(final double[] constant) {
        return new DComplexPlusMultSecond(DComplex.inv(constant));
    }

    /**
     * <code>a + b*constant</code>.
     * @param constant
     * @return 
     */
    public static DComplexPlusMultSecond plusMult(final double[] constant) {
        return new DComplexPlusMultSecond(constant);
    }
}
