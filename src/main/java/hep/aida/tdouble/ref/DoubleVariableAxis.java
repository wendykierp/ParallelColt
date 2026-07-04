package hep.aida.tdouble.ref;

import hep.aida.tdouble.DoubleIAxis;
import hep.aida.tdouble.DoubleIHistogram;

/**
 * Variable-width axis; A reference implementation of hep.aida.IAxis.
 * 
 * @author Wolfgang Hoschek, Tony Johnson, and others.
 * @version 1.0, 23/03/2000
 */
public class DoubleVariableAxis implements DoubleIAxis {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    protected double min;

    protected int bins;

    protected double[] edges;

    /**
     * Constructs and returns an axis with the given bin edges. Example:
     * <code>edges = (0.2, 1.0, 5.0)</code> yields an axis with 2 in-range bins
     * <code>[0.2,1.0), [1.0,5.0)</code> and 2 extra bins
     * <code>[-inf,0.2), [5.0,inf]</code>.
     * 
     * @param edges
     *            the bin boundaries the partition shall have; must be sorted
     *            ascending and must not contain multiple identical elements.
     * @throws IllegalArgumentException
     *             if <code>edges.length < 1</code>.
     */
    public DoubleVariableAxis(double[] edges) {
        if (edges.length < 1)
            throw new IllegalArgumentException();

        // check if really sorted and has no multiple identical elements
        for (int i = 0; i < edges.length - 1; i++) {
            if (edges[i + 1] <= edges[i]) {
                throw new IllegalArgumentException(
                        "edges must be sorted ascending and must not contain multiple identical values");
            }
        }

        this.min = edges[0];
        this.bins = edges.length - 1;
        this.edges = edges.clone();
    }

    public double binCentre(int index) {
        return (binLowerEdge(index) + binUpperEdge(index)) / 2;
    }

    public double binLowerEdge(int index) {
        if (index == DoubleIHistogram.UNDERFLOW)
            return Double.NEGATIVE_INFINITY;
        if (index == DoubleIHistogram.OVERFLOW)
            return upperEdge();
        return edges[index];
    }

    public int bins() {
        return bins;
    }

    public double binUpperEdge(int index) {
        if (index == DoubleIHistogram.UNDERFLOW)
            return lowerEdge();
        if (index == DoubleIHistogram.OVERFLOW)
            return Double.POSITIVE_INFINITY;
        return edges[index + 1];
    }

    public double binWidth(int index) {
        return binUpperEdge(index) - binLowerEdge(index);
    }

    public int coordToIndex(double coord) {
        if (coord < min)
            return DoubleIHistogram.UNDERFLOW;

        int index = java.util.Arrays.binarySearch(this.edges, coord);
        // int index = new DoubleArrayList(this.edges).binarySearch(coord); //
        // just for debugging
        if (index < 0)
            index = -index - 1 - 1; // not found
        // else index++; // found

        if (index >= bins)
            return DoubleIHistogram.OVERFLOW;

        return index;
    }

    public double lowerEdge() {
        return min;
    }

    /**
     * Returns a string representation of the specified array. The string
     * representation consists of a list of the arrays's elements, enclosed in
     * square brackets (<code>"[]"</code>). Adjacent elements are separated by the
     * characters <code>", "</code> (comma and space).
     * 
     * @param array
     * @return a string representation of the specified array.
     */
    protected static String toString(double[] array) {
        StringBuffer buf = new StringBuffer();
        buf.append("[");
        int maxIndex = array.length - 1;
        for (int i = 0; i <= maxIndex; i++) {
            buf.append(array[i]);
            if (i < maxIndex)
                buf.append(", ");
        }
        buf.append("]");
        return buf.toString();
    }

    public double upperEdge() {
        return edges[edges.length - 1];
    }
}
