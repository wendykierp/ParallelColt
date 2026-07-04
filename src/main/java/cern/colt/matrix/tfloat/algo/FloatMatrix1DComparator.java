/*
Copyright (C) 1999 CERN - European Organization for Nuclear Research.
Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose 
is hereby granted without fee, provided that the above copyright notice appear in all copies and 
that both that copyright notice and this permission notice appear in supporting documentation. 
CERN makes no representations about the suitability of this software for any purpose. 
It is provided "as is" without expressed or implied warranty.
 */
package cern.colt.matrix.tfloat.algo;

import cern.colt.matrix.tfloat.FloatMatrix1D;

/**
 * A comparison function which imposes a <i>total ordering</i> on some
 * collection of elements. Comparators can be passed to a sort method (such as
 * <code>cern.colt.matrix.floatalgo.Sorting.quickSort</code>) to allow precise
 * control over the sort order.
 * <p>
 * 
 * Note: It is generally a good idea for comparators to implement
 * <code>java.io.Serializable</code>, as they may be used as ordering methods in
 * serializable data structures. In order for the data structure to serialize
 * successfully, the comparator (if provided) must implement
 * <code>Serializable</code>.
 * <p>
 * 
 * @author wolfgang.hoschek@cern.ch
 * @version 1.0, 09/24/99
 * @see java.util.Comparator
 * @see cern.colt
 * @see cern.colt.Sorting
 */
public interface FloatMatrix1DComparator {
    /**
     * Compares its two arguments for order. Returns a negative integer, zero,
     * or a positive integer as the first argument is less than, equal to, or
     * greater than the second.
     * <p>
     * 
     * The implementor must ensure that <code>sgn(compare(x, y)) ==
     * -sgn(compare(y, x))</code> for all <code>x</code> and <code>y</code>. (This implies
     * that <code>compare(x, y)</code> must throw an exception if and only if
     * <code>compare(y, x)</code> throws an exception.)
     * <p>
     * 
     * The implementor must also ensure that the relation is transitive:
     * <code>((compare(x, y)&gt;0) &amp;&amp; (compare(y, z)&gt;0))</code> implies
     * <code>compare(x, z)&gt;0</code>.
     * <p>
     * 
     * Finally, the implementer must ensure that <code>compare(x, y)==0</code>
     * implies that <code>sgn(compare(x, z))==sgn(compare(y, z))</code> for all
     * <code>z</code>.
     * <p>
     * 
     * 
     * @param o1
     * @param o2
     * @return a negative integer, zero, or a positive integer as the first
     *         argument is less than, equal to, or greater than the second.
     */
    int compare(FloatMatrix1D o1, FloatMatrix1D o2);

    /**
     * 
     * Indicates whether some other object is &quot;equal to&quot; this
     * Comparator. This method must obey the general contract of
     * <code>Object.equals(Object)</code>. Additionally, this method can return
     * <code>true</code> <i>only</i> if the specified Object is also a comparator
     * and it imposes the same ordering as this comparator. Thus,
     * <code>comp1.equals(comp2)</code> implies that <code>sgn(comp1.compare(o1,
     * o2))==sgn(comp2.compare(o1, o2))</code> for every element <code>o1</code> and
     * <code>o2</code>.
     * <p>
     * 
     * Note that it is <i>always</i> safe <i>not</i> to override
     * <code>Object.equals(Object)</code>. However, overriding this method may, in
     * some cases, improve performance by allowing programs to determine that
     * two distinct Comparators impose the same order.
     * 
     * @param obj
     *            the reference object with which to compare.
     * @return <code>true</code> only if the specified object is also a
     *         comparator and it imposes the same ordering as this comparator.
     * @see java.lang.Object#equals(java.lang.Object)
     * @see java.lang.Object#hashCode()
     */
    boolean equals(Object obj);
}
