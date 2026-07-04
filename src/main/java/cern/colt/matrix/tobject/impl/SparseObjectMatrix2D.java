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

/**
 * Sparse hashed 2-d matrix holding <code>Object</code> elements. First see the <a
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
 * Thus, a 1000 x 1000 matrix with minLoadFactor=0.25 and maxLoadFactor=0.5 and
 * 1000000 non-zero cells consumes between 25 MB and 50 MB. The same 1000 x 1000
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
 * {@link DenseObjectMatrix2D}. However, constant factors are considerably
 * larger.
 * <p>
 * Cells are internally addressed in row-major. Performance sensitive
 * applications can exploit this fact. Setting values in a loop row-by-row is
 * quicker than column-by-column, because fewer hash collisions occur. Thus
 * 
 * <pre>
 * for (int row = 0; row &lt; rows; row++) {
 *     for (int column = 0; column &lt; columns; column++) {
 *         matrix.setQuick(row, column, someValue);
 *     }
 * }
 * </pre>
 * 
 * is quicker than
 * 
 * <pre>
 * for (int column = 0; column &lt; columns; column++) {
 *     for (int row = 0; row &lt; rows; row++) {
 *         matrix.setQuick(row, column, someValue);
 *     }
 * }
 * </pre>
 * 
 * @see cern.colt.map
 * @see cern.colt.map.tobject.OpenLongObjectHashMap
 * @author wolfgang.hoschek@cern.ch
 * @version 1.0, 09/24/99
 */
public class SparseObjectMatrix2D extends ObjectMatrix2D {
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
     * required to have the form <code>values[row][column]</code> and have exactly
     * the same number of columns in every row.
     * <p>
     * The values are copied. So subsequent changes in <code>values</code> are not
     * reflected in the matrix, and vice-versa.
     * 
     * @param values
     *            The values to be filled into the new matrix.
     * @throws IllegalArgumentException
     *             if
     *             <code>for any 1 &lt;= row &lt; values.length: values[row].length != values[row-1].length</code>
     *             .
     */
    public SparseObjectMatrix2D(Object[][] values) {
        this(values.length, values.length == 0 ? 0 : values[0].length);
        assign(values);
    }

    /**
     * Constructs a matrix with a given number of rows and columns and default
     * memory usage. All entries are initially <code>null</code>.
     * 
     * @param rows
     *            the number of rows the matrix shall have.
     * @param columns
     *            the number of columns the matrix shall have.
     * @throws IllegalArgumentException
     *             if
     *             <code>rows &lt; 0 || columns &lt; 0 || (double)columns*rows > Integer.MAX_VALUE</code>
     *             .
     */
    public SparseObjectMatrix2D(int rows, int columns) {
        this(rows, columns, rows * (columns / 1000), 0.2, 0.5);
    }

    /**
     * Constructs a matrix with a given number of rows and columns using memory
     * as specified. All entries are initially <code>null</code>. For details
     * related to memory usage see
     * {@link cern.colt.map.tobject.OpenLongObjectHashMap}.
     * 
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
     *             <code>initialCapacity < 0 || (minLoadFactor < 0.0 || minLoadFactor >= 1.0) || (maxLoadFactor <= 0.0 || maxLoadFactor >= 1.0) || (minLoadFactor >= maxLoadFactor)</code>
     *             .
     * @throws IllegalArgumentException
     *             if
     *             <code>rows &lt; 0 || columns &lt; 0 || (double)columns*rows > Integer.MAX_VALUE</code>
     *             .
     */
    public SparseObjectMatrix2D(int rows, int columns, int initialCapacity, double minLoadFactor, double maxLoadFactor) {
        setUp(rows, columns);
        this.elements = new OpenLongObjectHashMap(initialCapacity, minLoadFactor, maxLoadFactor);
    }

    /**
     * Constructs a view with the given parameters.
     * 
     * @param rows
     *            the number of rows the matrix shall have.
     * @param columns
     *            the number of columns the matrix shall have.
     * @param elements
     *            the cells.
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
     * @throws IllegalArgumentException
     *             if
     *             <code>rows &lt; 0 || columns &lt; 0 || (double)columns*rows > Integer.MAX_VALUE</code>
     *             or flip's are illegal.
     */
    protected SparseObjectMatrix2D(int rows, int columns, AbstractLongObjectMap elements, int rowZero, int columnZero,
            int rowStride, int columnStride) {
        setUp(rows, columns, rowZero, columnZero, rowStride, columnStride);
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
     * Returns a new matrix that has the same elements as this matrix, but is in
     * a column-compressed form. This method creates a new object (not a view),
     * so changes in the returned matrix are NOT reflected in this matrix.
     * 
     * @param sortRowIndexes
     *            if true, then row indexes in column compressed matrix are
     *            sorted
     * 
     * @return this matrix in a column-compressed form
     */
    public SparseCCObjectMatrix2D getColumnCompressed(boolean sortRowIndexes) {
        int nnz = cardinality();
        long[] keys = elements.keys().elements();
        Object[] values = elements.values().elements();
        int[] rowIndexes = new int[nnz];
        int[] columnIndexes = new int[nnz];

        for (int k = 0; k < nnz; k++) {
            long key = keys[k];
            rowIndexes[k] = (int) (key / columns);
            columnIndexes[k] = (int) (key % columns);
        }
        return new SparseCCObjectMatrix2D(rows, columns, rowIndexes, columnIndexes, values, false, sortRowIndexes);
    }

    /**
     * Returns a new matrix that has the same elements as this matrix, but is in
     * a column-compressed modified form. This method creates a new object (not
     * a view), so changes in the returned matrix are NOT reflected in this
     * matrix.
     * 
     * @return this matrix in a column-compressed modified form
     */
    public SparseCCMObjectMatrix2D getColumnCompressedModified() {
        SparseCCMObjectMatrix2D A = new SparseCCMObjectMatrix2D(rows, columns);
        int nnz = cardinality();
        long[] keys = elements.keys().elements();
        Object[] values = elements.values().elements();
        for (int i = 0; i < nnz; i++) {
            int row = (int) (keys[i] / columns);
            int column = (int) (keys[i] % columns);
            A.setQuick(row, column, values[i]);
        }
        return A;
    }

    /**
     * Returns a new matrix that has the same elements as this matrix, but is in
     * a row-compressed form. This method creates a new object (not a view), so
     * changes in the returned matrix are NOT reflected in this matrix.
     * 
     * @param sortColumnIndexes
     *            if true, then column indexes in row compressed matrix are
     *            sorted
     * 
     * @return this matrix in a row-compressed form
     */
    public SparseRCObjectMatrix2D getRowCompressed(boolean sortColumnIndexes) {
        int nnz = cardinality();
        long[] keys = elements.keys().elements();
        Object[] values = elements.values().elements();
        final int[] rowIndexes = new int[nnz];
        final int[] columnIndexes = new int[nnz];
        for (int k = 0; k < nnz; k++) {
            long key = keys[k];
            rowIndexes[k] = (int) (key / columns);
            columnIndexes[k] = (int) (key % columns);
        }
        return new SparseRCObjectMatrix2D(rows, columns, rowIndexes, columnIndexes, values, false, 
                sortColumnIndexes);
    }

    /**
     * Returns a new matrix that has the same elements as this matrix, but is in
     * a row-compressed modified form. This method creates a new object (not a
     * view), so changes in the returned matrix are NOT reflected in this
     * matrix.
     * 
     * @return this matrix in a row-compressed modified form
     */
    public SparseRCMObjectMatrix2D getRowCompressedModified() {
        SparseRCMObjectMatrix2D A = new SparseRCMObjectMatrix2D(rows, columns);
        int nnz = cardinality();
        long[] keys = elements.keys().elements();
        Object[] values = elements.values().elements();
        for (int i = 0; i < nnz; i++) {
            int row = (int) (keys[i] / columns);
            int column = (int) (keys[i] % columns);
            A.setQuick(row, column, values[i]);
        }
        return A;
    }

    /**
     * Returns the matrix cell value at coordinate <code>[row,column]</code>.
     * 
     * <p>
     * Provided with invalid parameters this method may return invalid objects
     * without throwing any exception. <b>You should only use this method when
     * you are absolutely sure that the coordinate is within bounds.</b>
     * Precondition (unchecked):
     * <code>0 &lt;= column &lt; columns() &amp;&amp; 0 &lt;= row &lt; rows()</code>.
     * 
     * @param row
     *            the index of the row-coordinate.
     * @param column
     *            the index of the column-coordinate.
     * @return the value at the specified coordinate.
     */

    public synchronized Object getQuick(int row, int column) {
        // if (debug) if (column<0 || column>=columns || row<0 || row>=rows)
        // throw new IndexOutOfBoundsException("row:"+row+", column:"+column);
        // return this.elements.get(index(row,column));
        // manually inlined:
        return this.elements.get((long) rowZero + (long) row * (long) rowStride + (long) columnZero + (long) column
                * (long) columnStride);
    }

    /**
     * Returns <code>true</code> if both matrices share common cells. More formally,
     * returns <code>true</code> if at least one of the following conditions is met
     * <ul>
     * <li>the receiver is a view of the other matrix
     * <li>the other matrix is a view of the receiver
     * <li><code>this == other</code>
     * </ul>
     * @param other
     * @return 
     */

    protected boolean haveSharedCellsRaw(ObjectMatrix2D other) {
        if (other instanceof SelectedSparseObjectMatrix2D) {
            SelectedSparseObjectMatrix2D otherMatrix = (SelectedSparseObjectMatrix2D) other;
            return this.elements == otherMatrix.elements;
        } else if (other instanceof SparseObjectMatrix2D) {
            SparseObjectMatrix2D otherMatrix = (SparseObjectMatrix2D) other;
            return this.elements == otherMatrix.elements;
        }
        return false;
    }

    /**
     * Returns the position of the given coordinate within the (virtual or
     * non-virtual) internal 1-dimensional array.
     * 
     * @param row
     *            the index of the row-coordinate.
     * @param column
     *            the index of the column-coordinate.
     * @return 
     */

    public long index(int row, int column) {
        // return super.index(row,column);
        // manually inlined for speed:
        return (long) rowZero + (long) row * (long) rowStride + (long) columnZero + (long) column * (long) columnStride;
    }

    /**
     * Construct and returns a new empty matrix <i>of the same dynamic type</i>
     * as the receiver, having the specified number of rows and columns. For
     * example, if the receiver is an instance of type
     * <code>DenseObjectMatrix2D</code> the new matrix must also be of type
     * <code>DenseObjectMatrix2D</code>, if the receiver is an instance of type
     * <code>SparseObjectMatrix2D</code> the new matrix must also be of type
     * <code>SparseObjectMatrix2D</code>, etc. In general, the new matrix should
     * have internal parametrization as similar as possible.
     * 
     * @param rows
     *            the number of rows the matrix shall have.
     * @param columns
     *            the number of columns the matrix shall have.
     * @return a new empty matrix of the same dynamic type.
     */

    public ObjectMatrix2D like(int rows, int columns) {
        return new SparseObjectMatrix2D(rows, columns);
    }

    /**
     * Construct and returns a new 1-d matrix <i>of the corresponding dynamic
     * type</i>, entirelly independent of the receiver. For example, if the
     * receiver is an instance of type <code>DenseObjectMatrix2D</code> the new
     * matrix must be of type <code>DenseObjectMatrix1D</code>, if the receiver is
     * an instance of type <code>SparseObjectMatrix2D</code> the new matrix must be
     * of type <code>SparseObjectMatrix1D</code>, etc.
     * 
     * @param size
     *            the number of cells the matrix shall have.
     * @return a new matrix of the corresponding dynamic type.
     */

    public ObjectMatrix1D like1D(int size) {
        return new SparseObjectMatrix1D(size);
    }

    /**
     * Construct and returns a new 1-d matrix <i>of the corresponding dynamic
     * type</i>, sharing the same cells. For example, if the receiver is an
     * instance of type <code>DenseObjectMatrix2D</code> the new matrix must be of
     * type <code>DenseObjectMatrix1D</code>, if the receiver is an instance of type
     * <code>SparseObjectMatrix2D</code> the new matrix must be of type
     * <code>SparseObjectMatrix1D</code>, etc.
     * 
     * @param size
     *            the number of cells the matrix shall have.
     * @param offset
     *            the index of the first element.
     * @param stride
     *            the number of indexes between any two elements, i.e.
     *            <code>index(i+1)-index(i)</code>.
     * @return a new matrix of the corresponding dynamic type.
     */

    protected ObjectMatrix1D like1D(int size, int offset, int stride) {
        return new SparseObjectMatrix1D(size, this.elements, offset, stride);
    }

    /**
     * Sets the matrix cell at coordinate <code>[row,column]</code> to the specified
     * value.
     * 
     * <p>
     * Provided with invalid parameters this method may access illegal indexes
     * without throwing any exception. <b>You should only use this method when
     * you are absolutely sure that the coordinate is within bounds.</b>
     * Precondition (unchecked):
     * <code>0 &lt;= column &lt; columns() &amp;&amp; 0 &lt;= row &lt; rows()</code>.
     * 
     * @param row
     *            the index of the row-coordinate.
     * @param column
     *            the index of the column-coordinate.
     * @param value
     *            the value to be filled into the specified cell.
     */

    public synchronized void setQuick(int row, int column, Object value) {
        // if (debug) if (column<0 || column>=columns || row<0 || row>=rows)
        // throw new IndexOutOfBoundsException("row:"+row+", column:"+column);
        // int index = index(row,column);
        // manually inlined:
        long index = (long) rowZero + (long) row * (long) rowStride + (long) columnZero + (long) column
                * (long) columnStride;

        if (value == null)
            this.elements.removeKey(index);
        else
            this.elements.put(index, value);
    }
    
    public ObjectMatrix1D vectorize() {
        SparseObjectMatrix1D v = new SparseObjectMatrix1D((int) size());
        int idx = 0;
        for (int c = 0; c < columns; c++) {
            for (int r = 0; r < rows; r++) {
                Object elem = getQuick(r, c);
                v.setQuick(idx++, elem);
            }
        }
        return v;
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
     * A sequence like <code>set(r,c,5); set(r,c,0);</code> sets a cell to non-zero
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
     * @param rowOffsets
     *            the offsets of the visible elements.
     * @param columnOffsets
     *            the offsets of the visible elements.
     * @return a new view.
     */

    protected ObjectMatrix2D viewSelectionLike(int[] rowOffsets, int[] columnOffsets) {
        return new SelectedSparseObjectMatrix2D(this.elements, rowOffsets, columnOffsets, 0);
    }
}
