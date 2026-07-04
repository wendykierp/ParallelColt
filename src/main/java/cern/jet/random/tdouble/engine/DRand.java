/*
Copyright (C) 1999 CERN - European Organization for Nuclear Research.
Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose 
is hereby granted without fee, provided that the above copyright notice appear in all copies and 
that both that copyright notice and this permission notice appear in supporting documentation. 
CERN makes no representations about the suitability of this software for any purpose. 
It is provided "as is" without expressed or implied warranty.
 */
package cern.jet.random.tdouble.engine;

import java.util.Date;

/**
 * Quick medium quality uniform pseudo-random number generator.
 * 
 * Produces uniformly distributed <code>int</code>'s and <code>long</code>'s in the
 * closed intervals <code>[Integer.MIN_VALUE,Integer.MAX_VALUE]</code> and
 * <code>[Long.MIN_VALUE,Long.MAX_VALUE]</code>, respectively, as well as
 * <code>float</code>'s and <code>double</code>'s in the open unit intervals
 * <code>(0.0f,1.0f)</code> and <code>(0.0,1.0)</code>, respectively.
 * <p>
 * The seed can be any integer satisfying
 * <code>0 &lt; 4*seed+1 &lt; 2<sup>32</sup></code>. In other words, there must hold
 * <code>seed &gt;= 0 &amp;&amp; seed &lt; 1073741823</code>.
 * <p>
 * <b>Quality:</b> This generator follows the multiplicative congruential method
 * of the form
 * <dt> <code>z(i+1) = a * z(i) (mod m)</code> with
 * <code>a=663608941 (=0X278DDE6DL), m=2<sup>32</sup></code>.
 * <dt> <code>z(i)</code> assumes all different values
 * <code>0 &lt; 4*seed+1 &lt; m</code> during a full period of 2<sup>30</sup>.
 * 
 * <p>
 * <b>Performance:</b> TO_DO
 * <p>
 * <b>Implementation:</b> TO_DO
 * <p>
 * Note that this implementation is <b>not synchronized</b>.
 * <p>
 * 
 * @author wolfgang.hoschek@cern.ch
 * @version 1.0, 09/24/99
 * @see DoubleMersenneTwister
 * @see java.util.Random
 */
public class DRand extends DoubleRandomEngine {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private int current;

    public static final int DEFAULT_SEED = 1;

    /**
     * Constructs and returns a random number generator with a default seed,
     * which is a <b>constant</b>.
     */
    public DRand() {
        this(DEFAULT_SEED);
    }

    /**
     * Constructs and returns a random number generator with the given seed.
     * 
     * @param seed
     *            should not be 0, in such a case <code>DRand.DEFAULT_SEED</code> is
     *            substituted.
     */
    public DRand(int seed) {
        setSeed(seed);
    }

    /**
     * Constructs and returns a random number generator seeded with the given
     * date.
     * 
     * @param d
     *            typically <code>new java.util.Date()</code>
     */
    public DRand(Date d) {
        this((int) d.getTime());
    }

    /**
     * Returns a 32 bit uniformly distributed random number in the closed
     * interval <code>[Integer.MIN_VALUE,Integer.MAX_VALUE]</code> (including
     * <code>Integer.MIN_VALUE</code> and <code>Integer.MAX_VALUE</code>).
     * @return 
     */

    public int nextInt() {
        current *= 0x278DDE6D; /* z(i+1)=a*z(i) (mod 2**32) */
        // a == 0x278DDE6D == 663608941

        return current;
    }

    /**
     * Sets the receiver's seed. This method resets the receiver's entire
     * internal state. The following condition must hold:
     * <code>seed &gt;= 0 &amp;&amp; seed &lt; (2<sup>32</sup>-1) / 4</code>.
     * 
     * @param seed
     *            if the above condition does not hold, a modified seed that
     *            meets the condition is silently substituted.
     */
    protected void setSeed(int seed) {
        if (seed < 0)
            seed = -seed;
        int limit = (int) ((Math.pow(2, 32) - 1) / 4); // --> 536870911
        if (seed >= limit)
            seed = seed >> 3;

        this.current = 4 * seed + 1;
    }
}
