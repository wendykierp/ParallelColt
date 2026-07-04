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
 * Sparse hashed 3-d matrix holding <code>Object</code> elements. First see the <a
 * href="package-summary.html">package summary</a> and javadoc <a
 * href="package-tree.html">tree view</a> to get the broad picture.
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
 * Thus, a 100 x 100 x 100 matrix with minLoadFactor=0.25 and maxLoadFactor=0.5
 * and 1000000 non-zero cells consumes between 25 MB and 50 MB. The same 100 x
 * 100 x 100 matrix with 1000 non-zero cells consumes between 25 and 50 KB.
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
 * {@link DenseObjectMatrix2D}. However, constant factors are considerably
 * larger.
 * <p>
 * Cells are internally addressed in (in decreasing order of significance):
 * slice major, row major, column major. Applications demanding utmost speed can
 * exploit this fact. Setting/getting values in a loop slice-by-slice,
 * row-by-row, column-by-column is quicker than, for example, column-by-column,
 * row-by-row, slice-by-slice. Thus
 * 
 * <pre>
 * for (int slice = 0; slice &lt; slices; slice++) {
 *     for (int row = 0; row &lt; rows; row++) {
 *         for (int column = 0; column &lt; columns; column++) {
 *             matrix.setQuick(slice, row, column, someValue);
 *         }
 *     }
 * }
 * </pre>
 * 
 * is quicker than
 * 
 * <pre>
 * for (int column = 0; column &lt; columns; column++) {
 *     for (int row = 0; row &lt; rows; row++) {
 *         for (int slice = 0; slice &lt; slices; slice++) {
 *             matrix.setQuick(slice, row, column, someValue);
 *         }
 *     }
 * }
 * </pre>
 * 
 * @see cern.colt.map
 * @see cern.colt.map.tobject.OpenLongObjectHashMap
 * @author wolfgang.hoschek@cern.ch
 * @version 1.0, 09/24/99
 */
public class SparseObjectMatrix3D extends ObjectMatrix3D {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    /*
     * The elements of the matrix.
     */
    protected AbstractLongObjectMap elements;

    /**
     * Constructs a matrix with a copy of the given values. <code>values</code> is
     * required to have the form <code>values[slice][row][column]</code> and have
     * exactly the same number of rows in in every slice and exactly the same
     * number of columns in in every row.
     * <p>
     * The values are copied. So subsequent changes in <code>values</code> are not
     * reflected in the matrix, and vice-versa.
     * 
     * @param values
     *            The values to be filled into the new matrix.
     * @throws IllegalArgumentException
     *             if
     *             <code>for any 1 &lt;= slice &lt; values.length: values[slice].length != values[slice-1].length</code>
     *             .
     * @throws IllegalArgumentException
     *             if
     *             <code>for any 1 &lt;= row &lt; values[0].length: values[slice][row].length != values[slice][row-1].length</code>
     *             .
     */
    public SparseObjectMatrix3D(Object[][][] values) {
        this(values.length, (values.length == 0 ? 0 : values[0].length), (values.length == 0 ? 0
                : values[0].length == 0 ? 0 : values[0][0].length));
        assign(values);
    }

    /**
     * Constructs a matrix with a given number of slices, rows and columns and
     * default memory usage. All entries are initially <code>null</code>.
     * 
     * @param slices
     *            the number of slices the matrix shall have.
     * @param rows
     *            the number of rows the matrix shall have.
     * @param columns
     *            the number of columns the matrix shall have.
     * @throws IllegalArgumentException
     *             if <code>(double)slices*columns*rows &gt; Integer.MAX_VALUE</code>.
     * @throws IllegalArgumentException
     *             if <code>slices &lt; 0 || rows &lt; 0 || columns &lt; 0</code>.
     */
    public SparseObjectMatrix3D(int slices, int rows, int columns) {
        this(slices, rows, columns, slices * rows * (columns / 1000), 0.2, 0.5);
    }

    /**
     * Constructs a matrix with a given number of slices, rows and columns using
     * memory as specified. All entries are initially <code>null</code>. For details
     * related to memory usage see
     * {@link cern.colt.map.tobject.OpenLongObjectHashMap}.
     * 
     * @param slices
     *            the number of slices the matrix shall have.
     * @param rows
     *            the number of rows the matrix shall have.
     * @param columns
     *            the number of columns the matrix shall have.
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
     *             <code>initialCapacity &lt; 0 || (minLoadFactor &lt; 0.0 || minLoadFactor &gt;= 1.0) || (maxLoadFactor &lt;= 0.0 || maxLoadFactor &gt;= 1.0) || (minLoadFactor &gt;= maxLoadFactor)</code>
     *             .
     * @throws IllegalArgumentException
     *             if <code>(double)slices*columns*rows &gt; Integer.MAX_VALUE</code>.
     * @throws IllegalArgumentException
     *             if <code>slices &lt; 0 || rows &lt; 0 || columns &lt; 0</code>.
     */
    public SparseObjectMatrix3D(int slices, int rows, int columns, int initialCapacity, double minLoadFactor,
            double maxLoadFactor) {
        setUp(slices, rows, columns);
        this.elements = new OpenLongObjectHashMap(initialCapacity, minLoadFactor, maxLoadFactor);
    }

    /**
     * Constructs a view with the given parameters.
     * 
     * @param slices
     *            the number of slices the matrix shall have.
     * @param rows
     *            the number of rows the matrix shall have.
     * @param columns
     *            the number of columns the matrix shall have.
     * @param elements
     *            the cells.
     * @param sliceZero
     *            the position of the first element.
     * @param rowZero
     *            the position of the first element.
     * @param columnZero
     *            the position of the first element.
     * @param sliceStride
     *            the number of elements between two slices, i.e.
     *            <code>index(k+1,i,j)-index(k,i,j)</code>.
     * @param rowStride
     *            the number of elements between two rows, i.e.
     *            <code>index(k,i+1,j)-index(k,i,j)</code>.
     * @param columnStride
     * @throws IllegalArgumentException
     *             if <code>(Object)slices*columns*rows &gt; Integer.MAX_VALUE</code>.
     * @throws IllegalArgumentException
     *             if <code>slices &lt; 0 || rows &lt; 0 || columns &lt; 0</code>.
     */
    protected SparseObjectMatrix3D(int slices, int rows, int columns, AbstractLongObjectMap elements, int sliceZero,
            int rowZero, int columnZero, int sliceStride, int rowStride, int columnStride) {
        setUp(slices, rows, columns, sliceZero, rowZero, columnZero, sliceStride, rowStride, columnStride);
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
     * Calling this method before tt&gt;set()</code>ing a large number of non-zero
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
     * Returns the matrix cell value at coordinate <code>[slice,row,column]</code>.
     * 
     * <p>
     * Provided with invalid parameters this method may return invalid objects
     * without throwing any exception. <b>You should only use this method when
     * you are absolutely sure that the coordinate is within bounds.</b>
     * Precondition (unchecked):
     * <code>slice&lt;0 || slice&gt;=slices() || row&lt;0 || row&gt;=rows() || column&lt;0 || column&gt;=column()</code>.
     * 
     * @param slice
     *            the index of the slice-coordinate.
     * @param row
     *            the index of the row-coordinate.
     * @param column
     *            the index of the column-coordinate.
     * @return the value at the specified coordinate.
     */

    public synchronized Object getQuick(int slice, int row, int column) {
        // if (debug) if (slice<0 || slice>=slices || row<0 || row>=rows ||
        // column<0 || column>=columns) throw new
        // IndexOutOfBoundsException("slice:"+slice+", row:"+row+",
        // column:"+column);
        // return elements.get(index(slice,row,column));
        // manually inlined:
        return elements.get((long) sliceZero + (long) slice * (long) sliceStride + (long) rowZero + (long) row
                * (long) rowStride + (long) columnZero + (long) column * (long) columnStride);
    }

    /**
     * Returns <code>true</code> if both matrices share at least one identical cell.
     * @param other
     * @return 
     */

    protected boolean haveSharedCellsRaw(ObjectMatrix3D other) {
        if (other instanceof SelectedSparseObjectMatrix3D) {
            SelectedSparseObjectMatrix3D otherMatrix = (SelectedSparseObjectMatrix3D) other;
            return this.elements == otherMatrix.elements;
        } else if (other instanceof SparseObjectMatrix3D) {
            SparseObjectMatrix3D otherMatrix = (SparseObjectMatrix3D) other;
            return this.elements == otherMatrix.elements;
        }
        return false;
    }

    /**
     * Returns the position of the given coordinate within the (virtual or
     * non-virtual) internal 1-dimensional array.
     * 
     * @param slice
     *            the index of the slice-coordinate.
     * @param row
     *            the index of the row-coordinate.
     * @param column
     *            the index of the third-coordinate.
     * @return 
     */

    public long index(int slice, int row, int column) {
        // return _sliceOffset(_sliceRank(slice)) + _rowOffset(_rowRank(row)) +
        // _columnOffset(_columnRank(column));
        // manually inlined:
        return (long) sliceZero + (long) slice * (long) sliceStride + (long) rowZero + (long) row * (long) rowStride
                + (long) columnZero + (long) column * (long) columnStride;
    }

    /**
     * Construct and returns a new empty matrix <i>of the same dynamic type</i>
     * as the receiver, having the specified number of slices, rows and columns.
     * For example, if the receiver is an instance of type
     * <code>DenseObjectMatrix3D</code> the new matrix must also be of type
     * <code>DenseObjectMatrix3D</code>, if the receiver is an instance of type
     * <code>SparseObjectMatrix3D</code> the new matrix must also be of type
     * <code>SparseObjectMatrix3D</code>, etc. In general, the new matrix should
     * have internal parametrization as similar as possible.
     * 
     * @param slices
     *            the number of slices the matrix shall have.
     * @param rows
     *            the number of rows the matrix shall have.
     * @param columns
     *            the number of columns the matrix shall have.
     * @return a new empty matrix of the same dynamic type.
     */

    public ObjectMatrix3D like(int slices, int rows, int columns) {
        return new SparseObjectMatrix3D(slices, rows, columns);
    }

    /**
     * Construct and returns a new 2-d matrix <i>of the corresponding dynamic
     * type</i>, sharing the same cells. For example, if the receiver is an
     * instance of type <code>DenseObjectMatrix3D</code> the new matrix must also be
     * of type <code>DenseObjectMatrix2D</code>, if the receiver is an instance of
     * type <code>SparseObjectMatrix3D</code> the new matrix must also be of type
     * <code>SparseObjectMatrix2D</code>, etc.
     * 
     * @param rows
     *            the number of rows the matrix shall have.
     * @param columns
     *            the number of columns the matrix shall have.
     * @param rowZero
     *            the position of the first element.
     * @param columnZero
     *            the position of the first element.
     * @param rowStride
     *            the number of elements between two rows, i.e.
     *            <code>index(i+1,j)-index(i,j)</code>.
     * @param columnStride
     *            the number of elements between two columns, i.e.
     *            <code>index(i,j+1)-index(i,j)</code>.
     * @return a new matrix of the corresponding dynamic type.
     */

    protected ObjectMatrix2D like2D(int rows, int columns, int rowZero, int columnZero, int rowStride, int columnStride) {
        return new SparseObjectMatrix2D(rows, columns, this.elements, rowZero, columnZero, rowStride, columnStride);
    }

    /**
     * Sets the matrix cell at coordinate <code>[slice,row,column]</code> to the
     * specified value.
     * 
     * <p>
     * Provided with invalid parameters this method may access illegal indexes
     * without throwing any exception. <b>You should only use this method when
     * you are absolutely sure that the coordinate is within bounds.</b>
     * Precondition (unchecked):
     * <code>slice&lt;0 || slice&gt;=slices() || row&lt;0 || row&gt;=rows() || column&lt;0 || column&gt;=column()</code>.
     * 
     * @param slice
     *            the index of the slice-coordinate.
     * @param row
     *            the index of the row-coordinate.
     * @param column
     *            the index of the column-coordinate.
     * @param value
     *            the value to be filled into the specified cell.
     */

    public synchronized void setQuick(int slice, int row, int column, Object value) {
        // if (debug) if (slice<0 || slice>=slices || row<0 || row>=rows ||
        // column<0 || column>=columns) throw new
        // IndexOutOfBoundsException("slice:"+slice+", row:"+row+",
        // column:"+column);
        // int index = index(slice,row,column);
        // manually inlined:
        long index = (long) sliceZero + (long) slice * (long) sliceStride + (long) rowZero + (long) row
                * (long) rowStride + (long) columnZero + (long) column * (long) columnStride;
        if (value == null)
            this.elements.removeKey(index);
        else
            this.elements.put(index, value);
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
     * A sequence like <code>set(s,r,c,5); set(s,r,c,0);</code> sets a cell to
     * non-zero state and later back to zero state. Such as sequence generates
     * obsolete memory that is automatically reclaimed from time to time or can
     * manually be reclaimed by calling <code>trimToSize()</code>. Putting zeros
     * into cells already containing zeros does not generate obsolete memory
     * since no memory was allocated to them in the first place.
     */

    public void trimToSize() {
        this.elements.trimToSize();
    }
    
    public ObjectMatrix1D vectorize() {
        ObjectMatrix1D v = new SparseObjectMatrix1D((int) size());
        int length = rows * columns;
        for (int s = 0; s < slices; s++) {
            v.viewPart(s * length, length).assign(viewSlice(s).vectorize());
        }
        return v;
    }

    /**
     * Construct and returns a new selection view.
     * 
     * @param sliceOffsets
     *            the offsets of the visible elements.
     * @param rowOffsets
     *            the offsets of the visible elements.
     * @param columnOffsets
     *            the offsets of the visible elements.
     * @return a new view.
     */

    protected ObjectMatrix3D viewSelectionLike(int[] sliceOffsets, int[] rowOffsets, int[] columnOffsets) {
        return new SelectedSparseObjectMatrix3D(this.elements, sliceOffsets, rowOffsets, columnOffsets, 0);
    }

    @Override
    public ObjectMatrix2D like2D(int rows, int columns) {
        return new SparseObjectMatrix2D(rows, columns);
    }
}
