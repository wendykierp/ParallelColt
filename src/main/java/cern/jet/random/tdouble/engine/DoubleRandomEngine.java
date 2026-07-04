/*
Copyright (C) 1999 CERN - European Organization for Nuclear Research.
Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose 
is hereby granted without fee, provided that the above copyright notice appear in all copies and 
that both that copyright notice and this permission notice appear in supporting documentation. 
CERN makes no representations about the suitability of this software for any purpose. 
It is provided "as is" without expressed or implied warranty.
 */
package cern.jet.random.tdouble.engine;

/**
 * Abstract base class for uniform pseudo-random number generating engines.
 * <p>
 * Most probability distributions are obtained by using a <b>uniform</b>
 * pseudo-random number generation engine followed by a transformation to the
 * desired distribution. Thus, subclasses of this class are at the core of
 * computational statistics, simulations, Monte Carlo methods, etc.
 * <p>
 * Subclasses produce uniformly distributed <code>int</code>'s and <code>long</code>'s
 * in the closed intervals <code>[Integer.MIN_VALUE,Integer.MAX_VALUE]</code> and
 * <code>[Long.MIN_VALUE,Long.MAX_VALUE]</code>, respectively, as well as
 * <code>float</code>'s and <code>double</code>'s in the open unit intervals
 * <code>(0.0f,1.0f)</code> and <code>(0.0,1.0)</code>, respectively.
 * <p>
 * Subclasses need to override one single method only: <code>nextInt()</code>. All
 * other methods generating different data types or ranges are usually layered
 * upon <code>nextInt()</code>. <code>long</code>'s are formed by concatenating two 32
 * bit <code>int</code>'s. <code>float</code>'s are formed by dividing the interval
 * <code>[0.0f,1.0f]</code> into 2<sup>32</sup> sub intervals, then randomly
 * choosing one subinterval. <code>double</code>'s are formed by dividing the
 * interval <code>[0.0,1.0]</code> into 2<sup>64</sup> sub intervals, then randomly
 * choosing one subinterval.
 * <p>
 * Note that this implementation is <b>not synchronized</b>.
 * 
 * @author wolfgang.hoschek@cern.ch
 * @version 1.0, 09/24/99
 * @see DoubleMersenneTwister
 * @see MersenneTwister64
 * @see java.util.Random
 */
// public abstract class RandomEngine extends
// edu.cornell.lassp.houle.RngPack.RandomSeedable implements
// cern.colt.function.DoubleFunction, cern.colt.function.IntFunction {
public abstract class DoubleRandomEngine extends cern.colt.PersistentObject implements
        cern.colt.function.tdouble.DoubleFunction, cern.colt.function.tint.IntFunction,
        cern.colt.function.tlong.LongFunction {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * Makes this class non instantiable, but still let's others inherit from
     * it.
     */
    protected DoubleRandomEngine() {
    }

    /**
     * Equivalent to <code>raw()</code>. This has the effect that random engines can
     * now be used as function objects, returning a random number upon function
     * evaluation.
     * @param dummy
     */
    public double apply(double dummy) {
        return raw();
    }

    /**
     * Equivalent to <code>nextInt()</code>. This has the effect that random engines
     * can now be used as function objects, returning a random number upon
     * function evaluation.
     * @param dummy
     */
    public int apply(int dummy) {
        return nextInt();
    }

    /**
     * Equivalent to <code>nextLong()</code>. This has the effect that random
     * engines can now be used as function objects, returning a random number
     * upon function evaluation.
     * @param dummy
     */
    public long apply(long dummy) {
        return nextLong();
    }

    /**
     * Constructs and returns a new uniform random number engine seeded with the
     * current time. Currently this is
     * {@link cern.jet.random.tdouble.engine.DoubleMersenneTwister}.
     * @return 
     */
    public static DoubleRandomEngine makeDefault() {
        return new cern.jet.random.tdouble.engine.DoubleMersenneTwister((int) System.currentTimeMillis());
    }

    /**
     * Returns a 64 bit uniformly distributed random number in the open unit
     * interval <code>(0.0,1.0)</code> (excluding 0.0 and 1.0).
     * @return 
     */
    public double nextDouble() {
        double nextDouble;

        do {
            // -9.223372036854776E18 == (double) Long.MIN_VALUE
            // 5.421010862427522E-20 == 1 / Math.pow(2,64) == 1 / ((double)
            // Long.MAX_VALUE - (double) Long.MIN_VALUE);
            nextDouble = (nextLong() - -9.223372036854776E18) * 5.421010862427522E-20;
        }
        // catch loss of precision of long --> double conversion
        while (!(nextDouble > 0.0 && nextDouble < 1.0));

        // --> in (0.0,1.0)
        return nextDouble;

        /*
         * nextLong == Long.MAX_VALUE --> 1.0 nextLong == Long.MIN_VALUE --> 0.0
         * nextLong == Long.MAX_VALUE-1 --> 1.0 nextLong ==
         * Long.MAX_VALUE-100000L --> 0.9999999999999946 nextLong ==
         * Long.MIN_VALUE+1 --> 0.0 nextLong == Long.MIN_VALUE-100000L -->
         * 0.9999999999999946 nextLong == 1L --> 0.5 nextLong == -1L --> 0.5
         * nextLong == 2L --> 0.5 nextLong == -2L --> 0.5 nextLong == 2L+100000L
         * --> 0.5000000000000054 nextLong == -2L-100000L -->
         * 0.49999999999999456
         */
    }

    /**
     * Returns a 32 bit uniformly distributed random number in the open unit
     * interval <code>(0.0f,1.0f)</code> (excluding 0.0f and 1.0f).
     * @return 
     */
    public float nextFloat() {
        // catch loss of precision of double --> float conversion
        float nextFloat;
        do {
            nextFloat = (float) raw();
        } while (nextFloat >= 1.0f);

        // --> in (0.0f,1.0f)
        return nextFloat;
    }

    /**
     * Returns a 32 bit uniformly distributed random number in the closed
     * interval <code>[Integer.MIN_VALUE,Integer.MAX_VALUE]</code> (including
     * <code>Integer.MIN_VALUE</code> and <code>Integer.MAX_VALUE</code>);
     * @return 
     */
    public abstract int nextInt();

    /**
     * Returns a 64 bit uniformly distributed random number in the closed
     * interval <code>[Long.MIN_VALUE,Long.MAX_VALUE]</code> (including
     * <code>Long.MIN_VALUE</code> and <code>Long.MAX_VALUE</code>).
     * @return 
     */
    public long nextLong() {
        // concatenate two 32-bit strings into one 64-bit string
        return ((nextInt() & 0xFFFFFFFFL) << 32) | ((nextInt() & 0xFFFFFFFFL));
    }

    /**
     * Returns a 32 bit uniformly distributed random number in the open unit
     * interval <code>(0.0,1.0)</code> (excluding 0.0 and 1.0).
     * @return 
     */
    public double raw() {
        int nextInt;
        do { // accept anything but zero
            nextInt = nextInt(); // in
            // [Integer.MIN_VALUE,Integer.MAX_VALUE]-interval
        } while (nextInt == 0);

        // transform to (0.0,1.0)-interval
        // 2.3283064365386963E-10 == 1.0 / Math.pow(2,32)
        return (nextInt & 0xFFFFFFFFL) * 2.3283064365386963E-10;

        /*
         * nextInt == Integer.MAX_VALUE --> 0.49999999976716936 nextInt ==
         * Integer.MIN_VALUE --> 0.5 nextInt == Integer.MAX_VALUE-1 -->
         * 0.4999999995343387 nextInt == Integer.MIN_VALUE+1 -->
         * 0.5000000002328306 nextInt == 1 --> 2.3283064365386963E-10 nextInt ==
         * -1 --> 0.9999999997671694 nextInt == 2 --> 4.6566128730773926E-10
         * nextInt == -2 --> 0.9999999995343387
         */
    }
}
