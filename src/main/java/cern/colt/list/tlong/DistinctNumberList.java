/*
Copyright (C) 1999 CERN - European Organization for Nuclear Research.
Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose 
is hereby granted without fee, provided that the above copyright notice appear in all copies and 
that both that copyright notice and this permission notice appear in supporting documentation. 
CERN makes no representations about the suitability of this software for any purpose. 
It is provided "as is" without expressed or implied warranty.
 */
package cern.colt.list.tlong;

/**
 * Resizable compressed list holding numbers; based on the fact that a number
 * from a large list with few distinct values need not take more than
 * <code>log(distinctValues)</code> bits; implemented with a
 * <code>MinMaxNumberList</code>. First see the <a
 * href="package-summary.html">package summary</a> and javadoc <a
 * href="package-tree.html">tree view</a> to get the broad picture.
 * <p>
 * This class can, for example, be useful when making large lists of numbers
 * persistent. Also useful when very large lists would otherwise consume too
 * much main memory.
 * <p>
 * You can add, get and set elements quite similar to
 * <code>java.util.ArrayList</code>.
 * <p>
 * <b>Applicability:</b> Applicable if data is highly skewed and legal values
 * are known in advance. Robust in the presence of "outliers".
 * <p>
 * <b>Performance:</b> Operations <code>get()</code>, <code>size()</code> and
 * <code>clear()</code> are <code>O(1)</code>, i.e. run in constant time. Operations
 * like <code>add()</code> and <code>set()</code> are
 * <code>O(log(distinctValues.length))</code>.
 * <p>
 * Upon instantiation a contract is signed that defines the distinct values
 * allowed to be hold in this list. It is not legal to store elements other than
 * specified by the contract. Any attempt to violate the contract will throw an
 * <code>IllegalArgumentException</code>.
 * <p>
 * Although access methods are only defined on <code>long</code> values you can also
 * store all other primitive data types: <code>boolean</code>, <code>byte</code>,
 * <code>short</code>, <code>int</code>, <code>long</code>, <code>float</code>, <code>double</code>
 * and <code>char</code>. You can do this by explicitly representing them as
 * <code>long</code> values. Use casts for discrete data types. Use the methods of
 * <code>java.lang.Float</code> and <code>java.lang.Double</code> for floating point
 * data types: Recall that with those methods you can convert any floating point
 * value to a <code>long</code> value and back <b>without losing any precision</b>:
 * <p>
 * <b>Example usage:</b>
 * 
 * <pre>
 * DistinctNumberList list = ... instantiation goes here
 * double d1 = 1.234;
 * list.add(Double.doubleToLongBits(d1));
 * double d2 = Double.longBitsToDouble(list.get(0));
 * if (d1!=d2) System.out.println(&quot;This is impossible!&quot;);
 * 
 * DistinctNumberList list2 = ... instantiation goes here
 * float f1 = 1.234f;
 * list2.add((long) Float.floatToIntBits(f1));
 * float f2 = Float.intBitsToFloat((int)list2.get(0));
 * if (f1!=f2) System.out.println(&quot;This is impossible!&quot;);
 * </pre>
 * 
 * @see LongArrayList
 * @see MinMaxNumberList
 * @see java.lang.Float
 * @see java.lang.Double
 * @author wolfgang.hoschek@cern.ch
 * @version 1.0, 09/24/99
 */
public class DistinctNumberList extends cern.colt.list.tlong.AbstractLongList {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    protected long[] distinctValues;

    protected MinMaxNumberList elements;

    /**
     * Constructs an empty list with the specified initial capacity and the
     * specified distinct values allowed to be hold in this list.
     * 
     * @param distinctValues
     *            an array sorted ascending containing the distinct values
     *            allowed to be hold in this list.
     * @param initialCapacity
     *            the number of elements the receiver can hold without
     *            auto-expanding itself by allocating new internal memory.
     */
    public DistinctNumberList(long[] distinctValues, int initialCapacity) {
        setUp(distinctValues, initialCapacity);
    }

    /**
     * Appends the specified element to the end of this list.
     * 
     * @param element
     *            element to be appended to this list.
     */

    public void add(long element) {
        // overridden for performance only.
        elements.add(codeOf(element));
        size++;
    }

    /**
     * Returns the code that shall be stored for the given element.
     * @param element
     * @return 
     */
    protected int codeOf(long element) {
        int index = java.util.Arrays.binarySearch(distinctValues, element);
        if (index < 0)
            throw new IllegalArgumentException("Element=" + element + " not contained in distinct elements.");
        return index;
    }

    /**
     * Ensures that the receiver can hold at least the specified number of
     * elements without needing to allocate new internal memory. If necessary,
     * allocates new internal memory and increases the capacity of the receiver.
     * 
     * @param minCapacity
     *            the desired minimum capacity.
     */

    public void ensureCapacity(int minCapacity) {
        elements.ensureCapacity(minCapacity);
    }

    /**
     * Returns the element at the specified position in the receiver;
     * <b>WARNING:</b> Does not check preconditions. Provided with invalid
     * parameters this method may return invalid elements without throwing any
     * exception! <b>You should only use this method when you are absolutely
     * sure that the index is within bounds.</b> Precondition (unchecked):
     * <code>index &gt;= 0 &amp;&amp; index &lt; size()</code>.
     * 
     * @param index
     *            index of element to return.
     * @return 
     */

    public long getQuick(int index) {
        return distinctValues[(int) (elements.getQuick(index))];
    }

    /**
     * Removes from the receiver all elements whose index is between
     * <code>from</code>, inclusive and <code>to</code>, inclusive. Shifts any
     * succeeding elements to the left (reduces their index). This call shortens
     * the list by <code>(to - from + 1)</code> elements.
     * 
     * @param from
     *            index of first element to be removed.
     * @param to
     *            index of last element to be removed.
     * @exception IndexOutOfBoundsException
     *                index is out of range (
     *                <code>size()&gt;0 &amp;&amp; (from&lt;0 || from&gt;to || to&gt;=size())</code>
     *                ).
     */

    public void removeFromTo(int from, int to) {
        elements.removeFromTo(from, to);
        size -= to - from + 1;
    }

    /**
     * Replaces the element at the specified position in the receiver with the
     * specified element; <b>WARNING:</b> Does not check preconditions. Provided
     * with invalid parameters this method may access invalid indexes without
     * throwing any exception! <b>You should only use this method when you are
     * absolutely sure that the index is within bounds.</b> Precondition
     * (unchecked): <code>index &gt;= 0 &amp;&amp; index &lt; size()</code>.
     * 
     * @param index
     *            index of element to replace.
     * @param element
     *            element to be stored at the specified position.
     */

    public void setQuick(int index, long element) {
        elements.setQuick(index, codeOf(element));
    }

    /**
     * Sets the size of the receiver without modifying it otherwise. This method
     * should not release or allocate new memory but simply set some instance
     * variable like <code>size</code>.
     * @param newSize
     */

    public void setSizeRaw(int newSize) {
        super.setSizeRaw(newSize);
        elements.setSizeRaw(newSize);
    }

    /**
     * Sets the receiver to an empty list with the specified initial capacity
     * and the specified distinct values allowed to be hold in this list.
     * 
     * @param distinctValues
     *            an array sorted ascending containing the distinct values
     *            allowed to be hold in this list.
     * @param initialCapacity
     *            the number of elements the receiver can hold without
     *            auto-expanding itself by allocating new internal memory.
     */
    protected void setUp(long[] distinctValues, int initialCapacity) {
        this.distinctValues = distinctValues;
        // java.util.Arrays.sort(this.distinctElements);
        this.elements = new MinMaxNumberList(0, distinctValues.length - 1, initialCapacity);
    }

    /**
     * Trims the capacity of the receiver to be the receiver's current size. An
     * application can use this operation to minimize the storage of the
     * receiver.
     */

    public void trimToSize() {
        elements.trimToSize();
    }
}
