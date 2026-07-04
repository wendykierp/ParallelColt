package hep.aida.tdouble.bin;

import cern.colt.list.tdouble.DoubleArrayList;
import cern.jet.stat.tdouble.DoubleDescriptive;

/**
 * Static and the same as its superclass, except that it can do more:
 * Additionally computes moments of arbitrary integer order, harmonic mean,
 * geometric mean, etc.
 * 
 * Constructors need to be told what functionality is required for the given use
 * case. Only maintains aggregate measures (incrementally) - the added elements
 * themselves are not kept.
 * 
 * @author wolfgang.hoschek@cern.ch
 * @version 0.9, 03-Jul-99
 */
public class MightyStaticDoubleBin1D extends StaticDoubleBin1D {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    protected boolean hasSumOfLogarithms = false;

    protected double sumOfLogarithms = 0.0; // Sum( Log(x[i]) )

    protected boolean hasSumOfInversions = false;

    protected double sumOfInversions = 0.0; // Sum( 1/x[i] )

    protected double[] sumOfPowers = null; // Sum( x[i]^3 ) .. Sum( x[i]^max_k

    // )

    /**
     * Constructs and returns an empty bin with limited functionality but good
     * performance; equivalent to <code>MightyStaticBin1D(false,false,4)</code>.
     */
    public MightyStaticDoubleBin1D() {
        this(false, false, 4);
    }

    /**
     * Constructs and returns an empty bin with the given capabilities.
     * 
     * @param hasSumOfLogarithms
     *            Tells whether {@link #sumOfLogarithms()} can return meaningful
     *            results. Set this parameter to <code>false</code> if measures of
     *            sum of logarithms, geometric mean and product are not
     *            required.
     *            <p>
     * @param hasSumOfInversions
     *            Tells whether {@link #sumOfInversions()} can return meaningful
     *            results. Set this parameter to <code>false</code> if measures of
     *            sum of inversions, harmonic mean and sumOfPowers(-1) are not
     *            required.
     *            <p>
     * @param maxOrderForSumOfPowers
     *            The maximum order <code>k</code> for which
     *            {@link #sumOfPowers(int)} can return meaningful results. Set
     *            this parameter to at least 3 if the skew is required, to at
     *            least 4 if the kurtosis is required. In general, if moments
     *            are required set this parameter at least as large as the
     *            largest required moment. This method always substitutes
     *            <code>Math.max(2,maxOrderForSumOfPowers)</code> for the parameter
     *            passed in. Thus, <code>sumOfPowers(0..2)</code> always returns
     *            meaningful results.
     * 
     * @see #hasSumOfPowers(int)
     * @see #moment(int,double)
     */
    public MightyStaticDoubleBin1D(boolean hasSumOfLogarithms, boolean hasSumOfInversions, int maxOrderForSumOfPowers) {
        setMaxOrderForSumOfPowers(maxOrderForSumOfPowers);
        this.hasSumOfLogarithms = hasSumOfLogarithms;
        this.hasSumOfInversions = hasSumOfInversions;
        this.clear();
    }

    /**
     * Adds the part of the specified list between indexes <code>from</code>
     * (inclusive) and <code>to</code> (inclusive) to the receiver.
     * 
     * @param list
     *            the list of which elements shall be added.
     * @param from
     *            the index of the first element to be added (inclusive).
     * @param to
     *            the index of the last element to be added (inclusive).
     * @throws IndexOutOfBoundsException
     *             if
     *             <code>list.size()&gt;0 &amp;&amp; (from&lt;0 || from&gt;to || to&gt;=list.size())</code>
     *             .
     */

    public synchronized void addAllOfFromTo(DoubleArrayList list, int from, int to) {
        super.addAllOfFromTo(list, from, to);

        if (this.sumOfPowers != null) {
            // int max_k = this.min_k + this.sumOfPowers.length-1;
            DoubleDescriptive.incrementalUpdateSumsOfPowers(list, from, to, 3, getMaxOrderForSumOfPowers(),
                    this.sumOfPowers);
        }

        if (this.hasSumOfInversions) {
            this.sumOfInversions += DoubleDescriptive.sumOfInversions(list, from, to);
        }

        if (this.hasSumOfLogarithms) {
            this.sumOfLogarithms += DoubleDescriptive.sumOfLogarithms(list, from, to);
        }
    }

    /**
     * Resets the values of all measures.
     */

    protected void clearAllMeasures() {
        super.clearAllMeasures();

        this.sumOfLogarithms = 0.0;
        this.sumOfInversions = 0.0;

        if (this.sumOfPowers != null) {
            for (int i = this.sumOfPowers.length; --i >= 0;) {
                this.sumOfPowers[i] = 0.0;
            }
        }
    }

    /**
     * Returns a deep copy of the receiver.
     * 
     * @return a deep copy of the receiver.
     */

    public synchronized Object clone() {
        MightyStaticDoubleBin1D clone = (MightyStaticDoubleBin1D) super.clone();
        if (this.sumOfPowers != null)
            clone.sumOfPowers = clone.sumOfPowers.clone();
        return clone;
    }

    /**
     * Computes the deviations from the receiver's measures to another bin's
     * measures.
     * 
     * @param other
     *            the other bin to compare with
     * @return a summary of the deviations.
     */

    public String compareWith(AbstractDoubleBin1D other) {
        StringBuffer buf = new StringBuffer(super.compareWith(other));
        if (other instanceof MightyStaticDoubleBin1D) {
            MightyStaticDoubleBin1D m = (MightyStaticDoubleBin1D) other;
            if (hasSumOfLogarithms() && m.hasSumOfLogarithms())
                buf.append("geometric mean: " + relError(geometricMean(), m.geometricMean()) + " %\n");
            if (hasSumOfInversions() && m.hasSumOfInversions())
                buf.append("harmonic mean: " + relError(harmonicMean(), m.harmonicMean()) + " %\n");
            if (hasSumOfPowers(3) && m.hasSumOfPowers(3))
                buf.append("skew: " + relError(skew(), m.skew()) + " %\n");
            if (hasSumOfPowers(4) && m.hasSumOfPowers(4))
                buf.append("kurtosis: " + relError(kurtosis(), m.kurtosis()) + " %\n");
            buf.append("\n");
        }
        return buf.toString();
    }

    /**
     * Returns the geometric mean, which is
     * <code>Product( x[i] )<sup>1.0/size()</sup></code>.
     * 
     * This method tries to avoid overflows at the expense of an equivalent but
     * somewhat inefficient definition:
     * <code>geoMean = exp( Sum( Log(x[i]) ) / size())</code>. Note that for a
     * geometric mean to be meaningful, the minimum of the data sequence must
     * not be less or equal to zero.
     * 
     * @return the geometric mean; <code>Double.NaN</code> if
     *         <code>!hasSumOfLogarithms()</code>.
     */
    public synchronized double geometricMean() {
        return DoubleDescriptive.geometricMean(size(), sumOfLogarithms());
    }

    /**
     * Returns the maximum order <code>k</code> for which sums of powers are
     * retrievable, as specified upon instance construction.
     * 
     * @return 
     * @see #hasSumOfPowers(int)
     * @see #sumOfPowers(int)
     */
    public synchronized int getMaxOrderForSumOfPowers() {
        /*
         * order 0..2 is always recorded. order 0 is size() order 1 is sum()
         * order 2 is sum_xx()
         */
        if (this.sumOfPowers == null)
            return 2;

        return 2 + this.sumOfPowers.length;
    }

    /**
     * Returns the minimum order <code>k</code> for which sums of powers are
     * retrievable, as specified upon instance construction.
     * 
     * @return 
     * @see #hasSumOfPowers(int)
     * @see #sumOfPowers(int)
     */
    public synchronized int getMinOrderForSumOfPowers() {
        int minOrder = 0;
        if (hasSumOfInversions())
            minOrder = -1;
        return minOrder;
    }

    /**
     * Returns the harmonic mean, which is <code>size() / Sum( 1/x[i] )</code>.
     * Remember: If the receiver contains at least one element of <code>0.0</code>,
     * the harmonic mean is <code>0.0</code>.
     * 
     * @return the harmonic mean; <code>Double.NaN</code> if
     *         <code>!hasSumOfInversions()</code>.
     * @see #hasSumOfInversions()
     */
    public synchronized double harmonicMean() {
        return DoubleDescriptive.harmonicMean(size(), sumOfInversions());
    }

    /**
     * Returns whether <code>sumOfInversions()</code> can return meaningful results.
     * 
     * @return <code>false</code> if the bin was constructed with insufficient
     *         parametrization, <code>true</code> otherwise. See the constructors
     *         for proper parametrization.
     */
    public boolean hasSumOfInversions() {
        return this.hasSumOfInversions;
    }

    /**
     * Tells whether <code>sumOfLogarithms()</code> can return meaningful results.
     * 
     * @return <code>false</code> if the bin was constructed with insufficient
     *         parametrization, <code>true</code> otherwise. See the constructors
     *         for proper parametrization.
     */
    public boolean hasSumOfLogarithms() {
        return this.hasSumOfLogarithms;
    }

    /**
     * Tells whether <code>sumOfPowers(k)</code> can return meaningful results.
     * Defined as
     * <code>hasSumOfPowers(k) &lt;==&gt; getMinOrderForSumOfPowers() &lt;= k &amp;&amp; k &lt;= getMaxOrderForSumOfPowers()</code>
     * . A return value of <code>true</code> implies that
     * <code>hasSumOfPowers(k-1) .. hasSumOfPowers(0)</code> will also return
     * <code>true</code>. See the constructors for proper parametrization.
     * <p>
     * <b>Details</b>: <code>hasSumOfPowers(0..2)</code> will always yield
     * <code>true</code>. <code>hasSumOfPowers(-1) &lt;==&gt; hasSumOfInversions()</code>.
     * 
     * @param k
     * @return <code>false</code> if the bin was constructed with insufficient
     *         parametrization, <code>true</code> otherwise.
     * @see #getMinOrderForSumOfPowers()
     * @see #getMaxOrderForSumOfPowers()
     */
    public boolean hasSumOfPowers(int k) {
        return getMinOrderForSumOfPowers() <= k && k <= getMaxOrderForSumOfPowers();
    }

    /**
     * Returns the kurtosis (aka excess), which is
     * <code>-3 + moment(4,mean()) / standardDeviation()<sup>4</sup></code>.
     * 
     * @return the kurtosis; <code>Double.NaN</code> if <code>!hasSumOfPowers(4)</code>.
     * @see #hasSumOfPowers(int)
     */
    public synchronized double kurtosis() {
        return DoubleDescriptive.kurtosis(moment(4, mean()), standardDeviation());
    }

    /**
     * Returns the moment of <code>k</code>-th order with value <code>c</code>, which is
     * <code>Sum( (x[i]-c)<sup>k</sup> ) / size()</code>.
     * 
     * @param k
     *            the order; must be greater than or equal to zero.
     * @param c
     *            any number.
     * @throws IllegalArgumentException
     *             if <code>k &lt; 0</code>.
     * @return <code>Double.NaN</code> if <code>!hasSumOfPower(k)</code>.
     */
    public synchronized double moment(int k, double c) {
        if (k < 0)
            throw new IllegalArgumentException("k must be >= 0");
        // checkOrder(k);
        if (!hasSumOfPowers(k))
            return Double.NaN;

        int maxOrder = Math.min(k, getMaxOrderForSumOfPowers());
        DoubleArrayList sumOfPows = new DoubleArrayList(maxOrder + 1);
        sumOfPows.add(size());
        sumOfPows.add(sum());
        sumOfPows.add(sumOfSquares());
        for (int i = 3; i <= maxOrder; i++)
            sumOfPows.add(sumOfPowers(i));

        return DoubleDescriptive.moment(k, c, size(), sumOfPows.elements());
    }

    /**
     * Returns the product, which is <code>Prod( x[i] )</code>. In other words:
     * <code>x[0]*x[1]*...*x[size()-1]</code>.
     * 
     * @return the product; <code>Double.NaN</code> if
     *         <code>!hasSumOfLogarithms()</code>.
     * @see #hasSumOfLogarithms()
     */
    public double product() {
        return DoubleDescriptive.product(size(), sumOfLogarithms());
    }

    /**
     * Sets the range of orders in which sums of powers are to be computed. In
     * other words, <code>sumOfPower(k)</code> will return <code>Sum( x[i]^k )</code> if
     * <code>min_k &lt;= k &lt;= max_k || 0 &lt;= k &lt;= 2</code> and throw an exception
     * otherwise.
     * 
     * @param max_k
     * @see #isLegalOrder(int)
     * @see #sumOfPowers(int)
     * @see #getRangeForSumOfPowers()
     */
    protected void setMaxOrderForSumOfPowers(int max_k) {
        // if (max_k < ) throw new IllegalArgumentException();

        if (max_k <= 2) {
            this.sumOfPowers = null;
        } else {
            this.sumOfPowers = new double[max_k - 2];
        }
    }

    /**
     * Returns the skew, which is
     * <code>moment(3,mean()) / standardDeviation()<sup>3</sup></code>.
     * 
     * @return the skew; <code>Double.NaN</code> if <code>!hasSumOfPowers(3)</code>.
     * @see #hasSumOfPowers(int)
     */
    public synchronized double skew() {
        return DoubleDescriptive.skew(moment(3, mean()), standardDeviation());
    }

    /**
     * Returns the sum of inversions, which is <code>Sum( 1 / x[i] )</code>.
     * 
     * @return the sum of inversions; <code>Double.NaN</code> if
     *         <code>!hasSumOfInversions()</code>.
     * @see #hasSumOfInversions()
     */
    public double sumOfInversions() {
        if (!this.hasSumOfInversions)
            return Double.NaN;
        // if (! this.hasSumOfInversions) throw new
        // IllegalOperationException("You must specify upon instance
        // construction that the sum of inversions shall be computed.");
        return this.sumOfInversions;
    }

    /**
     * Returns the sum of logarithms, which is <code>Sum( Log(x[i]) )</code>.
     * 
     * @return the sum of logarithms; <code>Double.NaN</code> if
     *         <code>!hasSumOfLogarithms()</code>.
     * @see #hasSumOfLogarithms()
     */
    public synchronized double sumOfLogarithms() {
        if (!this.hasSumOfLogarithms)
            return Double.NaN;
        // if (! this.hasSumOfLogarithms) throw new
        // IllegalOperationException("You must specify upon instance
        // construction that the sum of logarithms shall be computed.");
        return this.sumOfLogarithms;
    }

    /**
     * Returns the <code>k-th</code> order sum of powers, which is
     * <code>Sum( x[i]<sup>k</sup> )</code>.
     * 
     * @param k
     *            the order of the powers.
     * @return the sum of powers; <code>Double.NaN</code> if
     *         <code>!hasSumOfPowers(k)</code>.
     * @see #hasSumOfPowers(int)
     */
    public synchronized double sumOfPowers(int k) {
        if (!hasSumOfPowers(k))
            return Double.NaN;
        // checkOrder(k);
        if (k == -1)
            return sumOfInversions();
        if (k == 0)
            return size();
        if (k == 1)
            return sum();
        if (k == 2)
            return sumOfSquares();

        return this.sumOfPowers[k - 3];
    }

    /**
     * Returns a String representation of the receiver.
     * @return 
     */

    public synchronized String toString() {
        StringBuffer buf = new StringBuffer(super.toString());

        if (hasSumOfLogarithms()) {
            buf.append("Geometric mean: " + geometricMean());
            buf.append("\nProduct: " + product() + "\n");
        }

        if (hasSumOfInversions()) {
            buf.append("Harmonic mean: " + harmonicMean());
            buf.append("\nSum of inversions: " + sumOfInversions() + "\n");
        }

        int maxOrder = getMaxOrderForSumOfPowers();
        int maxPrintOrder = Math.min(6, maxOrder); // don't print tons of
        // measures
        if (maxOrder > 2) {
            if (maxOrder >= 3) {
                buf.append("Skew: " + skew() + "\n");
            }
            if (maxOrder >= 4) {
                buf.append("Kurtosis: " + kurtosis() + "\n");
            }
            for (int i = 3; i <= maxPrintOrder; i++) {
                buf.append("Sum of powers(" + i + "): " + sumOfPowers(i) + "\n");
            }
            for (int k = 0; k <= maxPrintOrder; k++) {
                buf.append("Moment(" + k + ",0): " + moment(k, 0) + "\n");
            }
            for (int k = 0; k <= maxPrintOrder; k++) {
                buf.append("Moment(" + k + ",mean()): " + moment(k, mean()) + "\n");
            }
        }
        return buf.toString();
    }

    /**
     * @param k
     */
    protected void xcheckOrder(int k) {
        // if (! isLegalOrder(k)) return Double.NaN;
        // if (! xisLegalOrder(k)) throw new IllegalOperationException("Illegal
        // order of sum of powers: k="+k+". Upon instance construction legal
        // range was fixed to be "+getMinOrderForSumOfPowers()+" <= k <=
        // "+getMaxOrderForSumOfPowers());
    }

    /**
     * Returns whether two bins are equal; They are equal if the other object is
     * of the same class or a subclass of this class and both have the same
     * size, minimum, maximum, sum, sumOfSquares, sumOfInversions and
     * sumOfLogarithms.
     * @param object
     * @return 
     */
    protected boolean xequals(Object object) {
        if (!(object instanceof MightyStaticDoubleBin1D))
            return false;
        MightyStaticDoubleBin1D other = (MightyStaticDoubleBin1D) object;
        return super.equals(other) && sumOfInversions() == other.sumOfInversions()
                && sumOfLogarithms() == other.sumOfLogarithms();
    }

    /**
     * Tells whether <code>sumOfPowers(fromK) .. sumOfPowers(toK)</code> can return
     * meaningful results.
     * 
     * @param fromK
     * @param toK
     * @return <code>false</code> if the bin was constructed with insufficient
     *         parametrization, <code>true</code> otherwise. See the constructors
     *         for proper parametrization.
     * @throws IllegalArgumentException
     *             if <code>fromK &gt; toK</code>.
     */
    protected boolean xhasSumOfPowers(int fromK, int toK) {
        if (fromK > toK)
            throw new IllegalArgumentException("fromK must be less or equal to toK");
        return getMinOrderForSumOfPowers() <= fromK && toK <= getMaxOrderForSumOfPowers();
    }

    /**
     * Returns
     * <code>getMinOrderForSumOfPowers() &lt;= k &amp;&amp; k &lt;= getMaxOrderForSumOfPowers()</code>
     * .
     * @param k
     * @return 
     */
    protected synchronized boolean xisLegalOrder(int k) {
        return getMinOrderForSumOfPowers() <= k && k <= getMaxOrderForSumOfPowers();
    }
}
