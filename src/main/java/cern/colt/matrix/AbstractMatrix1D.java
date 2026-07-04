/*
Copyright (C) 1999 CERN - European Organization for Nuclear Research.
Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose 
is hereby granted without fee, provided that the above copyright notice appear in all copies and 
that both that copyright notice and this permission notice appear in supporting documentation. 
CERN makes no representations about the suitability of this software for any purpose. 
It is provided "as is" without expressed or implied warranty.
 */
package cern.colt.matrix;

/**
 * Abstract base class for 1-d matrices (aka <i>vectors</i>) holding objects or
 * primitive data types such as <code>int</code>, <code>double</code>, etc.
 * First see the <a href="package-summary.html">package summary</a> and javadoc
 * <a href="package-tree.html">tree view</a> to get the broad picture.
 * <p>
 * <b>Note that this implementation is not synchronized.</b>
 * 
 * @author wolfgang.hoschek@cern.ch
 * @version 1.0, 09/24/99
 */
public abstract class AbstractMatrix1D extends AbstractMatrix {
    private static final long serialVersionUID = 1L;

    /** the number of cells this matrix (view) has */
    protected int size;

    /** the index of the first element */
    protected int zero;

    /**
     * the number of indexes between any two elements, i.e.
     * <code>index(i+1) - index(i)</code>.
     */
    protected int stride;

    /**
     * Makes this class non instantiable, but still let's others inherit from
     * it.
     */
    protected AbstractMatrix1D() {
    }

    /**
     * Returns the position of the given absolute rank within the (virtual or
     * non-virtual) internal 1-dimensional array. Default implementation.
     * Override, if necessary.
     * 
     * @param absRank
     * @return the position.
     */
    protected int _offset(int absRank) {
        return absRank;
    }

    /**
     * Returns the absolute rank of the given relative rank.
     * 
     * @param rank
     *            the relative rank of the element.
     * @return the absolute rank of the element.
     */
    protected int _rank(int rank) {
        return zero + rank * stride;
    }

    /**
     * Sanity check for operations requiring an index to be within bounds.
     * 
     * @param index
     * @throws IndexOutOfBoundsException
     *             if <code>index < 0 || index >= size()</code>.
     */
    protected void checkIndex(int index) {
        if (index < 0 || index >= size)
            throw new IndexOutOfBoundsException("Attempted to access " + toStringShort() + " at index=" + index);
    }

    /**
     * Checks whether indexes are legal and throws an exception, if necessary.
     * 
     * @param indexes
     * @throws IndexOutOfBoundsException
     *             if <code>! (0 <= indexes[i] < size())</code> for any
     *             i=0..indexes.length()-1.
     */
    protected void checkIndexes(int[] indexes) {
        for (int i = indexes.length; --i >= 0;) {
            int index = indexes[i];
            if (index < 0 || index >= size)
                checkIndex(index);
        }
    }

    /**
     * Checks whether the receiver contains the given range and throws an
     * exception, if necessary.
     * 
     * @param index
     * @param width
     * @throws IndexOutOfBoundsException
     *             if <code>index<0 || index+width>size()</code>.
     */
    protected void checkRange(int index, int width) {
        if (index < 0 || index + width > size)
            throw new IndexOutOfBoundsException("index: " + index + ", width: " + width + ", size=" + size);
    }

    /**
     * Sanity check for operations requiring two matrices with the same size.
     * 
     * @param B
     * @throws IllegalArgumentException
     *             if <code>size() != B.size()</code>.
     */
    public void checkSize(AbstractMatrix1D B) {
        if (size != B.size)
            throw new IllegalArgumentException("Incompatible sizes: " + toStringShort() + " and " + B.toStringShort());
    }

    /**
     * Returns the position of the element with the given relative rank within
     * the (virtual or non-virtual) internal 1-dimensional array. You may want
     * to override this method for performance.
     * 
     * @param rank
     *            the rank of the element.
     * @return 
     */
    public long index(int rank) {
        return _offset(_rank(rank));
    }

    /**
     * Sets up a matrix with a given number of cells.
     * 
     * @param size
     *            the number of cells the matrix shall have.
     * @throws IllegalArgumentException
     *             if <code>size &lt; 0</code>.
     */
    protected void setUp(int size) {
        setUp(size, 0, 1);
    }

    /**
     * Sets up a matrix with the given parameters.
     * 
     * @param size
     *            the number of elements the matrix shall have.
     * @param zero
     *            the index of the first element.
     * @param stride
     *            the number of indexes between any two elements, i.e.
     *            <code>index(i+1)-index(i)</code>.
     * @throws IllegalArgumentException
     *             if <code>size &lt; 0</code>.
     */
    protected void setUp(int size, int zero, int stride) {
        if (size < 0)
            throw new IllegalArgumentException("negative size");

        this.size = size;
        this.zero = zero;
        this.stride = stride;
        this.isNoView = true;
    }

    /**
     * Returns the number of cells.
     * @return 
     */

    public long size() {
        return size;
    }

    /**
     * Returns the stride.
     * @return 
     */
    public int stride() {
        return stride;
    }

    /**
     * Returns the stride of the given dimension (axis, rank).
     * 
     * @param dimension
     * @dimension the index of the dimension.
     * @return the stride in the given dimension.
     * @throws IllegalArgumentException
     *             if <code>dimension != 0</code>.
     */
    protected int stride(int dimension) {
        if (dimension != 0)
            throw new IllegalArgumentException("invalid dimension: " + dimension + "used to access" + toStringShort());
        return this.stride;
    }

    /**
     * Returns a string representation of the receiver's shape.
     * @return 
     */
    public String toStringShort() {
        return AbstractFormatter.shape(this);
    }

    /**
     * Self modifying version of viewFlip(). What used to be index <code>0</code> is
     * now index <code>size()-1</code>, ..., what used to be index <code>size()-1</code>
     * is now index <code>0</code>.
     * @return 
     */
    protected AbstractMatrix1D vFlip() {
        if (size > 0) {
            this.zero += (this.size() - 1) * this.stride;
            this.stride = -this.stride;
            this.isNoView = false;
        }
        return this;
    }

    /**
     * Self modifying version of viewPart().
     * 
     * @param index
     * @param width
     * @return 
     * @throws IndexOutOfBoundsException
     *             if <code>index<0 || index+width>size()</code>.
     */
    protected AbstractMatrix1D vPart(int index, int width) {
        checkRange(index, width);
        this.zero += this.stride * index;
        this.size = width;
        this.isNoView = false;
        return this;
    }

    /**
     * Self modifying version of viewStrides().
     * 
     * @param stride
     * @return 
     * @throws IndexOutOfBoundsException
     *             if <code>stride <= 0</code>.
     */
    protected AbstractMatrix1D vStrides(int stride) {
        if (stride <= 0)
            throw new IndexOutOfBoundsException("illegal stride: " + stride);
        this.stride *= stride;
        if (this.size != 0)
            this.size = (this.size - 1) / stride + 1;
        this.isNoView = false;
        return this;
    }
}
