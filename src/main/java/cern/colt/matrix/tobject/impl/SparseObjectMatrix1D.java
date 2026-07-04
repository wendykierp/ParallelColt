/*
Copyright (C) 1999 CERN - European Organization for Nuclear Research.
Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose 
is hereby granted without fee, provided that the above copyright notice appear in all copies and 
that both that copyright notice and this permission notice appear in supporting documentation. 
CERN makes no representations about the suitability of this software for any purpose. 
It is provided "as is" without expressed or implied warranty.
 */
package cern.colt.matrix.tobject.impl;

import cern.colt.map.tobject.AbstractLongObjectMap;
import cern.colt.map.tobject.OpenLongObjectHashMap;
import cern.colt.matrix.tobject.ObjectMatrix1D;
import cern.colt.matrix.tobject.ObjectMatrix2D;
import cern.colt.matrix.tobject.ObjectMatrix3D;

/**
 * Sparse hashed 1-d matrix (aka <i>vector</i>) holding <code>Object</code>
 * elements. First see the <a href="package-summary.html">package summary</a>
 * and javadoc <a href="package-tree.html">tree view</a> to get the broad
 * picture.
 * <p>
 * <b>Implementation:</b>
 * <p>
 * Note that this implementation is not synchronized. Uses a
 * {@link cern.colt.map.tobject.OpenLongObjectHashMap}, which is a compact and
 * performant hashing technique.
 * <p>
 * <b>Memory requirements:</b>
 * <p>
 * Cells that
 * <ul>
 * <li>are never set to non-zero values do not use any memory.
 * <li>switch from zero to non-zero state do use memory.
 * <li>switch back from non-zero to zero state also do use memory. However,
 * their memory is automatically reclaimed from time to time. It can also
 * manually be reclaimed by calling {@link #trimToSize()}.
 * </ul>
 * <p>
 * worst case: <code>memory [bytes] = (1/minLoadFactor) * nonZeros * 13</code>. <br>
 * best case: <code>memory [bytes] = (1/maxLoadFactor) * nonZeros * 13</code>. <br>
 * Where <code>nonZeros = cardinality()</code> is the number of non-zero cells.
 * Thus, a 1000000 matrix with minLoadFactor=0.25 and maxLoadFactor=0.5 and
 * 1000000 non-zero cells consumes between 25 MB and 50 MB. The same 1000000
 * matrix with 1000 non-zero cells consumes between 25 and 50 KB.
 * <p>
 * <b>Time complexity:</b>
 * <p>
 * This class offers <i>expected</i> time complexity <code>O(1)</code> (i.e.
 * constant time) for the basic operations <code>get</code>, <code>getQuick</code>,
 * <code>set</code>, <code>setQuick</code> and <code>size</code> assuming the hash function
 * disperses the elements properly among the buckets. Otherwise, pathological
 * cases, although highly improbable, can occur, degrading performance to
 * <code>O(N)</code> in the worst case. As such this sparse class is expected to
 * have no worse time complexity than its dense counterpart
 * {@link DenseObjectMatrix1D}. However, constant factors are considerably
 * larger.
 * 
 * @author wolfgang.hoschek@cern.ch
 * @version 1.0, 09/24/99
 */
public class SparseObjectMatrix1D extends ObjectMatrix1D {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    /*
     * The elements of the matrix.
     */
    protected AbstractLongObjectMap elements;

    /**
     * Constructs a matrix with a copy of the given values. The values are
     * copied. So subsequent changes in <code>values</code> are not reflected in the
     * matrix, and vice-versa.
     * 
     * @param values
     *            The values to be filled into the new matrix.
     */
    public SparseObjectMatrix1D(Object[] values) {
        this(values.length);
        assign(values);
    }

    /**
     * Constructs a matrix with a given number of cells. All entries are
     * initially <code>null</code>.
     * 
     * @param size
     *            the number of cells the matrix shall have.
     * @throws IllegalArgumentException
     *             if <code>size &lt; 0</code>.
     */
    public SparseObjectMatrix1D(int size) {
        this(size, size / 1000, 0.2, 0.5);
    }

    /**
     * Constructs a matrix with a given number of parameters. All entries are
     * initially <code>null</code>. For details related to memory usage see
     * {@link cern.colt.map.tobject.OpenLongObjectHashMap}.
     * 
     * @param size
     *            the number of cells the matrix shall have.
     * @param initialCapacity
     *            the initial capacity of the hash map. If not known, set
     *            <code>initialCapacity=0</code> or small.
     * @param minLoadFactor
     *            the minimum load factor of the hash map.
     * @param maxLoadFactor
     *            the maximum load factor of the hash map.
     * @throws IllegalArgumentException
     *             if
     * 
     *             <code>initialCapacity < 0 || (minLoadFactor < 0.0 || minLoadFactor >= 1.0) || (maxLoadFactor <= 0.0 || maxLoadFactor >= 1.0) || (minLoadFactor >= maxLoadFactor)</code>
     *             .
     * @throws IllegalArgumentException
     *             if <code>size &lt; 0</code>.
     */
    public SparseObjectMatrix1D(int size, int initialCapacity, double minLoadFactor, double maxLoadFactor) {
        setUp(size);
        this.elements = new OpenLongObjectHashMap(initialCapacity, minLoadFactor, maxLoadFactor);
    }

    /**
     * Constructs a matrix view with a given number of parameters.
     * 
     * @param size
     *            the number of cells the matrix shall have.
     * @param elements
     *            the cells.
     * @param offset
     *            the index of the first element.
     * @param stride
     *            the number of indexes between any two elements, i.e.
     *            <code>index(i+1)-index(i)</code>.
     * @throws IllegalArgumentException
     *             if <code>size &lt; 0</code>.
     */
    protected SparseObjectMatrix1D(int size, AbstractLongObjectMap elements, int offset, int stride) {
        setUp(size, offset, stride);
        this.elements = elements;
        this.isNoView = false;
    }

    /**
     * Returns the number of cells having non-zero values.
     * @return 
     */

    public int cardinality() {
        if (this.isNoView)
            return this.elements.size();
        else
            return super.cardinality();
    }

    /**
     * Returns the elements of this matrix.
     * 
     * @return the elements
     */

    public AbstractLongObjectMap elements() {
        return elements;
    }

    /**
     * Ensures that the receiver can hold at least the specified number of
     * non-zero cells without needing to allocate new internal memory. If
     * necessary, allocates new internal memory and increases the capacity of
     * the receiver.
     * <p>
     * This method never need be called; it is for performance tuning only.
     * Calling this method before tt>set()</code>ing a large number of non-zero
     * values boosts performance, because the receiver will grow only once
     * instead of potentially many times and hash collisions get less probable.
     * 
     * @param minCapacity
     *            the desired minimum number of non-zero cells.
     */

    public void ensureCapacity(int minCapacity) {
        this.elements.ensureCapacity(minCapacity);
    }

    /**
     * Returns the matrix cell value at coordinate <code>index</code>.
     * 
     * <p>
     * Provided with invalid parameters this method may return invalid objects
     * without throwing any exception. <b>You should only use this method when
     * you are absolutely sure that the coordinate is within bounds.</b>
     * Precondition (unchecked): <code>index&lt;0 || index&gt;=size()</code>.
     * 
     * @param index
     *            the index of the cell.
     * @return the value of the specified cell.
     */

    public synchronized Object getQuick(int index) {
        // if (debug) if (index<0 || index>=size) checkIndex(index);
        // return this.elements.get(index(index));
        // manually inlined:
        return elements.get((long) zero + (long) index * (long) stride);
    }

    /**
     * Returns <code>true</code> if both matrices share at least one identical cell.
     * @param other
     * @return 
     */

    protected boolean haveSharedCellsRaw(ObjectMatrix1D other) {
        if (other instanceof SelectedSparseObjectMatrix1D) {
            SelectedSparseObjectMatrix1D otherMatrix = (SelectedSparseObjectMatrix1D) other;
            return this.elements == otherMatrix.elements;
        } else if (other instanceof SparseObjectMatrix1D) {
            SparseObjectMatrix1D otherMatrix = (SparseObjectMatrix1D) other;
            return this.elements == otherMatrix.elements;
        }
        return false;
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
        // overriden for manual inlining only
        // return _offset(_rank(rank));
        return (long) zero + (long) rank * (long) stride;
    }

    /**
     * Construct and returns a new empty matrix <i>of the same dynamic type</i>
     * as the receiver, having the specified size. For example, if the receiver
     * is an instance of type <code>DenseObjectMatrix1D</code> the new matrix must
     * also be of type <code>DenseObjectMatrix1D</code>, if the receiver is an
     * instance of type <code>SparseObjectMatrix1D</code> the new matrix must also
     * be of type <code>SparseObjectMatrix1D</code>, etc. In general, the new matrix
     * should have internal parametrization as similar as possible.
     * 
     * @param size
     *            the number of cell the matrix shall have.
     * @return a new empty matrix of the same dynamic type.
     */

    public ObjectMatrix1D like(int size) {
        return new SparseObjectMatrix1D(size);
    }

    /**
     * Construct and returns a new 2-d matrix <i>of the corresponding dynamic
     * type</i>, entirelly independent of the receiver. For example, if the
     * receiver is an instance of type <code>DenseObjectMatrix1D</code> the new
     * matrix must be of type <code>DenseObjectMatrix2D</code>, if the receiver is
     * an instance of type <code>SparseObjectMatrix1D</code> the new matrix must be
     * of type <code>SparseObjectMatrix2D</code>, etc.
     * 
     * @param rows
     *            the number of rows the matrix shall have.
     * @param columns
     *            the number of columns the matrix shall have.
     * @return a new matrix of the corresponding dynamic type.
     */

    public ObjectMatrix2D like2D(int rows, int columns) {
        return new SparseObjectMatrix2D(rows, columns);
    }

    public ObjectMatrix2D reshape(int rows, int columns) {
        if (rows * columns != size) {
            throw new IllegalArgumentException("rows*columns != size");
        }
        ObjectMatrix2D M = new SparseObjectMatrix2D(rows, columns);
        int idx = 0;
        for (int c = 0; c < columns; c++) {
            for (int r = 0; r < rows; r++) {
                Object elem = getQuick(idx++);
                if (elem != null) {
                    M.setQuick(r, c, elem);
                }
            }
        }
        return M;
    }

    public ObjectMatrix3D reshape(int slices, int rows, int columns) {
        if (slices * rows * columns != size) {
            throw new IllegalArgumentException("slices*rows*columns != size");
        }
        ObjectMatrix3D M = new SparseObjectMatrix3D(slices, rows, columns);
        int idx = 0;
        for (int s = 0; s < slices; s++) {
            for (int c = 0; c < columns; c++) {
                for (int r = 0; r < rows; r++) {
                    Object elem = getQuick(idx++);
                    if (elem != null) {
                        M.setQuick(s, r, c, elem);
                    }
                }
            }
        }
        return M;
    }
    
    /**
     * Sets the matrix cell at coordinate <code>index</code> to the specified value.
     * 
     * <p>
     * Provided with invalid parameters this method may access illegal indexes
     * without throwing any exception. <b>You should only use this method when
     * you are absolutely sure that the coordinate is within bounds.</b>
     * Precondition (unchecked): <code>index&lt;0 || index&gt;=size()</code>.
     * 
     * @param index
     *            the index of the cell.
     * @param value
     *            the value to be filled into the specified cell.
     */

    public synchronized void setQuick(int index, Object value) {
        // if (debug) if (index<0 || index>=size) checkIndex(index);
        // int i = index(index);
        // manually inlined:
        long i = (long) zero + (long) index * (long) stride;
        if (value == null)
            this.elements.removeKey(i);
        else
            this.elements.put(i, value);
    }

    /**
     * Releases any superfluous memory created by explicitly putting zero values
     * into cells formerly having non-zero values; An application can use this
     * operation to minimize the storage of the receiver.
     * <p>
     * <b>Background:</b>
     * <p>
     * Cells that
     * <ul>
     * <li>are never set to non-zero values do not use any memory.
     * <li>switch from zero to non-zero state do use memory.
     * <li>switch back from non-zero to zero state also do use memory. However,
     * their memory can be reclaimed by calling <code>trimToSize()</code>.
     * </ul>
     * A sequence like <code>set(i,5); set(i,0);</code> sets a cell to non-zero
     * state and later back to zero state. Such as sequence generates obsolete
     * memory that is automatically reclaimed from time to time or can manually
     * be reclaimed by calling <code>trimToSize()</code>. Putting zeros into cells
     * already containing zeros does not generate obsolete memory since no
     * memory was allocated to them in the first place.
     */

    public void trimToSize() {
        this.elements.trimToSize();
    }

    /**
     * Construct and returns a new selection view.
     * 
     * @param offsets
     *            the offsets of the visible elements.
     * @return a new view.
     */

    protected ObjectMatrix1D viewSelectionLike(int[] offsets) {
        return new SelectedSparseObjectMatrix1D(this.elements, offsets);
    }
}
