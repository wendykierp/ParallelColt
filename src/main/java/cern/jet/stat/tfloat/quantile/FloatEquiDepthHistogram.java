/*
Copyright (C) 1999 CERN - European Organization for Nuclear Research.
Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose 
is hereby granted without fee, provided that the above copyright notice appear in all copies and 
that both that copyright notice and this permission notice appear in supporting documentation. 
CERN makes no representations about the suitability of this software for any purpose. 
It is provided "as is" without expressed or implied warranty.
 */
package cern.jet.stat.tfloat.quantile;

/**
 * Read-only equi-depth histogram for selectivity estimation. Assume you have
 * collected statistics over a data set, among them a one-dimensional equi-depth
 * histogram (quantiles). Then an applications or DBMS might want to estimate
 * the <i>selectivity</i> of some range query <code>[from,to]</code>, i.e. the
 * percentage of data set elements contained in the query range. This class does
 * not collect equi-depth histograms but only space efficiently stores already
 * produced histograms and provides operations for selectivity estimation. Uses
 * linear interpolation.
 * <p>
 * This class stores a list <code>l</code> of <code>float</code> values for which holds:
 * <li>Let <code>v</code> be a list of values (sorted ascending) an equi-depth
 * histogram has been computed over.</li>
 * <li>Let <code>s=l.length</code>.</li>
 * <li>Let <code>p=(0, 1/s-1), 2/s-1,..., s-1/s-1=1.0)</code> be a list of the
 * <code>s</code> percentages.</li>
 * <li>Then for each
 * <code>i=0..s-1: l[i] = e : v.contains(e) &amp;&amp; v[0],..., v[p[i]*v.length] &lt;= e</code>
 * .</li>
 * <li>(In particular: <code>l[0]=min(v)=v[0]</code> and
 * <code>l[s-1]=max(v)=v[s-1]</code>.)</li>
 * 
 * @author wolfgang.hoschek@cern.ch
 * @version 1.0, 09/24/99
 */
public class FloatEquiDepthHistogram extends cern.colt.PersistentObject {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    protected float[] binBoundaries;

    /**
     * Constructs an equi-depth histogram with the given quantile elements.
     * Quantile elements must be sorted ascending and have the form specified in
     * the class documentation.
     * @param quantileElements
     */
    public FloatEquiDepthHistogram(float[] quantileElements) {
        this.binBoundaries = quantileElements;
    }

    /**
     * Returns the bin index of the given element. In other words, returns a
     * handle to the range the element falls into.
     * 
     * @param element
     *            the element to search for.
     * @return 
     * @throws java.lang.IllegalArgumentException
     *             if the element is not contained in any bin.
     */
    public int binOfElement(float element) {
        int index = java.util.Arrays.binarySearch(binBoundaries, element);
        if (index >= 0) {
            // element found.
            if (index == binBoundaries.length - 1)
                index--; // last bin is a closed interval.
        } else {
            // element not found.
            index -= -1; // index = -index-1; now index is the insertion
            // point.
            if (index == 0 || index == binBoundaries.length) {
                throw new IllegalArgumentException("Element=" + element + " not contained in any bin.");
            }
            index--;
        }
        return index;
    }

    /**
     * Returns the number of bins. In other words, returns the number of
     * subdomains partitioning the entire value domain.
     * @return 
     */
    public int bins() {
        return binBoundaries.length - 1;
    }

    /**
     * Returns the end of the range associated with the given bin.
     * 
     * @param binIndex
     * @return 
     * @throws ArrayIndexOutOfBoundsException
     *             if <code>binIndex &lt; 0 || binIndex &gt;= bins()</code>.
     */
    public float endOfBin(int binIndex) {
        return binBoundaries[binIndex + 1];
    }

    /**
     * Returns the percentage of elements in the range (from,to]. Does linear
     * interpolation.
     * 
     * @param from
     *            the start point (exclusive).
     * @param to
     *            the end point (inclusive).
     * @return a number in the closed interval <code>[0.0,1.0]</code>.
     */
    public double percentFromTo(float from, float to) {
        return phi(to) - phi(from);
    }

    /**
     * Returns how many percent of the elements contained in the receiver are
     * <code>&lt;= element</code>. Does linear interpolation.
     * 
     * @param element
     *            the element to search for.
     * @return a number in the closed interval <code>[0.0,1.0]</code>.
     */
    public double phi(float element) {
        int size = binBoundaries.length;
        if (element <= binBoundaries[0])
            return 0.0;
        if (element >= binBoundaries[size - 1])
            return 1.0;

        double binWidth = 1.0 / (size - 1);
        int index = java.util.Arrays.binarySearch(binBoundaries, element);
        // int index = new FloatArrayList(binBoundaries).binarySearch(element);
        if (index >= 0) { // found
            return binWidth * index;
        }

        // do linear interpolation
        int insertionPoint = -index - 1;
        double from = binBoundaries[insertionPoint - 1];
        double to = binBoundaries[insertionPoint] - from;
        double p = (element - from) / to;
        return binWidth * (p + (insertionPoint - 1));
    }

    /**
     * @return 
     * @deprecated Deprecated. Returns the number of bin boundaries.
     */
    @Deprecated
    public int size() {
        return binBoundaries.length;
    }

    /**
     * Returns the start of the range associated with the given bin.
     * 
     * @param binIndex
     * @return 
     * @throws ArrayIndexOutOfBoundsException
     *             if <code>binIndex &lt; 0 || binIndex &gt;= bins()</code>.
     */
    public float startOfBin(int binIndex) {
        return binBoundaries[binIndex];
    }

    /**
     * Not yet commented.
     * @param element
     */
    public static void test(float element) {
        float[] quantileElements = { 50.0f, 100.0f, 200.0f, 300.0f, 1400.0f, 1500.0f, 1600.0f, 1700.0f, 1800.0f,
                1900.0f, 2000.0f };
        FloatEquiDepthHistogram histo = new FloatEquiDepthHistogram(quantileElements);
        System.out.println("elem=" + element + ", phi=" + histo.phi(element));
    }
}
