package cern.jet.math.tfcomplex;

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

public class FComplexPlusMultFirst implements cern.colt.function.tfcomplex.FComplexFComplexFComplexFunction {
    /**
     * Public read/write access to avoid frequent object construction.
     */
    public float[] multiplicator;

    /**
     * Insert the method's description here. Creation date: (8/10/99 19:12:09)
     * @param multiplicator
     */
    protected FComplexPlusMultFirst(final float[] multiplicator) {
        this.multiplicator = multiplicator;
    }

    /**
     * Returns the result of the function evaluation.
     * @param a
     * @param b
     */
    public final float[] apply(float[] a, float[] b) {
        float[] z = new float[2];
        z[0] = a[0] * multiplicator[0] - a[1] * multiplicator[1];
        z[1] = a[1] * multiplicator[0] + a[0] * multiplicator[1];
        z[0] += b[0];
        z[1] += b[1];
        return z;
    }

    /**
     * <code>a - b/constant</code>.
     * @param constant
     * @return 
     */
    public static FComplexPlusMultFirst minusDiv(final float[] constant) {
        return new FComplexPlusMultFirst(FComplex.neg(FComplex.inv(constant)));
    }

    /**
     * <code>a - b*constant</code>.
     * @param constant
     * @return 
     */
    public static FComplexPlusMultFirst minusMult(final float[] constant) {
        return new FComplexPlusMultFirst(FComplex.neg(constant));
    }

    /**
     * <code>a + b/constant</code>.
     * @param constant
     * @return 
     */
    public static FComplexPlusMultFirst plusDiv(final float[] constant) {
        return new FComplexPlusMultFirst(FComplex.inv(constant));
    }

    /**
     * <code>a + b*constant</code>.
     * @param constant
     * @return 
     */
    public static FComplexPlusMultFirst plusMult(final float[] constant) {
        return new FComplexPlusMultFirst(constant);
    }
}
