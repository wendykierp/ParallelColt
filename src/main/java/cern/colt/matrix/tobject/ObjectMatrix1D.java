/*
Copyright (C) 1999 CERN - European Organization for Nuclear Research.
Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose 
is hereby granted without fee, provided that the above copyright notice appear in all copies and 
that both that copyright notice and this permission notice appear in supporting documentation. 
CERN makes no representations about the suitability of this software for any purpose. 
It is provided "as is" without expressed or implied warranty.
 */
package cern.colt.matrix.tobject;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import cern.colt.list.tint.IntArrayList;
import cern.colt.list.tobject.ObjectArrayList;
import cern.colt.matrix.AbstractMatrix1D;
import edu.emory.mathcs.utils.ConcurrencyUtils;

/**
 * Abstract base class for 1-d matrices (aka <i>vectors</i>) holding
 * <code>Object</code> elements. First see the <a
 * href="package-summary.html">package summary</a> and javadoc <a
 * href="package-tree.html">tree view</a> to get the broad picture.
 * <p>
 * A matrix has a number of cells (its <i>size</i>), which are assigned upon
 * instance construction. Elements are accessed via zero based indexes. Legal
 * indexes are of the form <code>[0..size()-1]</code>. Any attempt to access an
 * element at a coordinate <code>index&lt;0 || index&gt;=size()</code> will throw an
 * <code>IndexOutOfBoundsException</code>.
 * 
 * @author wolfgang.hoschek@cern.ch
 * @version 1.0, 09/24/99
 */
public abstract class ObjectMatrix1D extends AbstractMatrix1D {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * Makes this class non instantiable, but still let's others inherit from
     * it.
     */
    protected ObjectMatrix1D() {
    }

    /**
     * Applies a function to each cell and aggregates the results. Returns a
     * value <code>v</code> such that <code>v==a(size())</code> where
     * <code>a(i) == aggr( a(i-1), f(get(i)) )</code> and terminators are
     * <code>a(1) == f(get(0)), a(0)==null</code>. 
     * 
     * @param aggr
     *            an aggregation function taking as first argument the current
     *            aggregation and as second argument the transformed current
     *            cell value.
     * @param f
     *            a function transforming the current cell value.
     * @return the aggregated measure.
     */
    public Object aggregate(final cern.colt.function.tobject.ObjectObjectFunction aggr,
            final cern.colt.function.tobject.ObjectFunction f) {
        if (size == 0)
            return null;
        Object a = f.apply(getQuick(0));
        int nthreads = ConcurrencyUtils.getNumberOfThreads();
        if ((nthreads > 1) && (size >= ConcurrencyUtils.getThreadsBeginN_1D())) {
            nthreads = Math.min(nthreads, size);
            Future<?>[] futures = new Future[nthreads];
            int k = size / nthreads;
            for (int j = 0; j < nthreads; j++) {
                final int firstIdx = j * k;
                final int lastIdx;
                if (j == nthreads - 1) {
                    lastIdx = size;
                } else {
                    lastIdx = firstIdx + k;
                }
                futures[j] = ConcurrencyUtils.submit(new Callable<Object>() {
                    public Object call() throws Exception {
                        Object a = f.apply(getQuick(firstIdx));
                        for (int i = firstIdx + 1; i < lastIdx; i++) {
                            a = aggr.apply(a, f.apply(getQuick(i)));
                        }
                        return a;
                    }
                });
            }
            a = ConcurrencyUtils.waitForCompletion(futures, aggr);
        } else {
            for (int i = 1; i < size; i++) {
                a = aggr.apply(a, f.apply(getQuick(i)));
            }
        }
        return a;
    }
    
    /**
     * 
     * Applies a function to all cells with a given indexes and aggregates the
     * results.
     * 
     * @param aggr
     *            an aggregation function taking as first argument the current
     *            aggregation and as second argument the transformed current
     *            cell value.
     * @param f
     *            a function transforming the current cell value.
     * @param indexList
     *            indexes.
     * 
     * @return the aggregated measure.
     */
    public Object aggregate(final cern.colt.function.tobject.ObjectObjectFunction aggr,
            final cern.colt.function.tobject.ObjectFunction f, final IntArrayList indexList) {
        if (size() == 0)
            throw new IllegalArgumentException("size == 0");
        final int size = indexList.size();
        final int[] indexElements = indexList.elements();
        Object a = null;
        int nthreads = ConcurrencyUtils.getNumberOfThreads();
        if ((nthreads > 1) && (size >= ConcurrencyUtils.getThreadsBeginN_1D())) {
            nthreads = Math.min(nthreads, size);
            Future<?>[] futures = new Future[nthreads];
            int k = size / nthreads;
            for (int j = 0; j < nthreads; j++) {
                final int firstIdx = j * k;
                final int lastIdx = (j == nthreads - 1) ? size : firstIdx + k;
                futures[j] = ConcurrencyUtils.submit(new Callable<Object>() {

                    public Object call() throws Exception {
                        Object a = f.apply(getQuick(indexElements[firstIdx]));
                        Object elem;
                        for (int i = firstIdx + 1; i < lastIdx; i++) {
                            elem = getQuick(indexElements[i]);
                            a = aggr.apply(a, f.apply(elem));
                        }
                        return a;
                    }
                });
            }
            a = ConcurrencyUtils.waitForCompletion(futures, aggr);
        } else {
            Object elem;
            a = f.apply(getQuick(indexElements[0]));
            for (int i = 1; i < size; i++) {
                elem = getQuick(indexElements[i]);
                a = aggr.apply(a, f.apply(elem));
            }
        }
        return a;
    }

    /**
     * Applies a function to each corresponding cell of two matrices and
     * aggregates the results. Returns a value <code>v</code> such that
     * <code>v==a(size())</code> where
     * <code>a(i) == aggr( a(i-1), f(get(i),other.get(i)) )</code> and terminators
     * are <code>a(1) == f(get(0),other.get(0)), a(0)==null</code>.
     * <p>
     * <b>Example:</b>
     * 
     * <pre>
     * 	 cern.jet.math.Functions F = cern.jet.math.Functions.functions;
     * 	 x = 0 1 2 3 
     * 	 y = 0 1 2 3 
     * 
     * 	 // Sum( x[i]*y[i] )
     * 	 x.aggregate(y, F.plus, F.mult);
     * 	 --&gt; 14
     * 
     * 	 // Sum( (x[i]+y[i])&circ;2 )
     * 	 x.aggregate(y, F.plus, F.chain(F.square,F.plus));
     * 	 --&gt; 56
     * 
     * </pre>
     * 
     * For further examples, see the <a
     * href="package-summary.html#FunctionObjects">package doc</a>.
     * 
     * @param other
     * @param aggr
     *            an aggregation function taking as first argument the current
     *            aggregation and as second argument the transformed current
     *            cell values.
     * @param f
     *            a function transforming the current cell values.
     * @return the aggregated measure.
     * @throws IllegalArgumentException
     *             if <code>size() != other.size()</code>.
     */
    public Object aggregate(final ObjectMatrix1D other, final cern.colt.function.tobject.ObjectObjectFunction aggr,
            final cern.colt.function.tobject.ObjectObjectFunction f) {
        checkSize(other);
        if (size == 0)
            return null;
        Object a = f.apply(getQuick(0), other.getQuick(0));
        int nthreads = ConcurrencyUtils.getNumberOfThreads();
        if ((nthreads > 1) && (size >= ConcurrencyUtils.getThreadsBeginN_1D())) {
            nthreads = Math.min(nthreads, size);
            Future<?>[] futures = new Future[nthreads];
            int k = size / nthreads;
            for (int j = 0; j < nthreads; j++) {
                final int firstIdx = j * k;
                final int lastIdx = (j == nthreads - 1) ? size : firstIdx + k;
                futures[j] = ConcurrencyUtils.submit(new Callable<Object>() {
                    public Object call() throws Exception {
                        Object a = f.apply(getQuick(firstIdx), other.getQuick(firstIdx));
                        for (int i = firstIdx + 1; i < lastIdx; i++) {
                            a = aggr.apply(a, f.apply(getQuick(i), other.getQuick(i)));
                        }
                        return a;
                    }
                });
            }
            a = ConcurrencyUtils.waitForCompletion(futures, aggr);
        } else {
            for (int i = 1; i < size; i++) {
                a = aggr.apply(a, f.apply(getQuick(i), other.getQuick(i)));
            }
        }
        return a;
    }

    /**
     * Sets all cells to the state specified by <code>values</code>. <code>values</code>
     * is required to have the same number of cells as the receiver.
     * <p>
     * The values are copied. So subsequent changes in <code>values</code> are not
     * reflected in the matrix, and vice-versa.
     * 
     * @param values
     *            the values to be filled into the cells.
     * @return <code>this</code> (for convenience only).
     * @throws IllegalArgumentException
     *             if <code>values.length != size()</code>.
     */
    public ObjectMatrix1D assign(final Object[] values) {
        if (values.length != size)
            throw new IllegalArgumentException("Must have same number of cells: length=" + values.length + ", size()="
                    + size());
        int nthreads = ConcurrencyUtils.getNumberOfThreads();
        if ((nthreads > 1) && (size >= ConcurrencyUtils.getThreadsBeginN_1D())) {
            nthreads = Math.min(nthreads, size);
            Future<?>[] futures = new Future[nthreads];
            int k = size / nthreads;
            for (int j = 0; j < nthreads; j++) {
                final int firstIdx = j * k;
                final int lastIdx = (j == nthreads - 1) ? size : firstIdx + k;

                futures[j] = ConcurrencyUtils.submit(new Runnable() {

                    public void run() {
                        for (int i = firstIdx; i < lastIdx; i++) {
                            setQuick(i, values[i]);
                        }
                    }
                });
            }
            ConcurrencyUtils.waitForCompletion(futures);
        } else {
            for (int i = 0; i < size; i++) {
                setQuick(i, values[i]);
            }
        }
        return this;
    }

    /**
     * Assigns the result of a function to each cell;
     * <code>x[i] = function(x[i])</code>. (Iterates downwards from
     * <code>[size()-1]</code> to <code>[0]</code>).
     * <p>
     * <b>Example:</b>
     * 
     * <pre>
     * 	 // change each cell to its sine
     * 	 matrix =   0.5      1.5      2.5       3.5 
     * 	 matrix.assign(cern.jet.math.Functions.sin);
     * 	 --&gt;
     * 	 matrix ==  0.479426 0.997495 0.598472 -0.350783
     * 
     * </pre>
     * 
     * For further examples, see the <a
     * href="package-summary.html#FunctionObjects">package doc</a>.
     * 
     * @param function
     *            a function object taking as argument the current cell's value.
     * @return <code>this</code> (for convenience only).
     * @see cern.jet.math.tdouble.DoubleFunctions
     */
    public ObjectMatrix1D assign(final cern.colt.function.tobject.ObjectFunction function) {
        int nthreads = ConcurrencyUtils.getNumberOfThreads();
        if ((nthreads > 1) && (size >= ConcurrencyUtils.getThreadsBeginN_1D())) {
            nthreads = Math.min(nthreads, size);
            Future<?>[] futures = new Future[nthreads];
            int k = size / nthreads;
            for (int j = 0; j < nthreads; j++) {
                final int firstIdx = j * k;
                final int lastIdx = (j == nthreads - 1) ? size : firstIdx + k;

                futures[j] = ConcurrencyUtils.submit(new Runnable() {

                    public void run() {
                        for (int i = firstIdx; i < lastIdx; i++) {
                            setQuick(i, function.apply(getQuick(i)));
                        }
                    }
                });
            }
            ConcurrencyUtils.waitForCompletion(futures);
        } else {
            for (int i = 0; i < size; i++) {
                setQuick(i, function.apply(getQuick(i)));
            }
        }
        return this;
    }

    /**
     * Replaces all cell values of the receiver with the values of another
     * matrix. Both matrices must have the same size. If both matrices share the
     * same cells (as is the case if they are views derived from the same
     * matrix) and intersect in an ambiguous way, then replaces <i>as if</i>
     * using an intermediate auxiliary deep copy of <code>other</code>.
     * 
     * @param other
     *            the source matrix to copy from (may be identical to the
     *            receiver).
     * @return <code>this</code> (for convenience only).
     * @throws IllegalArgumentException
     *             if <code>size() != other.size()</code>.
     */
    public ObjectMatrix1D assign(ObjectMatrix1D other) {
        if (other == this)
            return this;
        checkSize(other);
        final ObjectMatrix1D other_loc;
        if (haveSharedCells(other)) {
            other_loc = other.copy();
        } else {
            other_loc = other;
        }
        int nthreads = ConcurrencyUtils.getNumberOfThreads();
        if ((nthreads > 1) && (size >= ConcurrencyUtils.getThreadsBeginN_1D())) {
            nthreads = Math.min(nthreads, size);
            Future<?>[] futures = new Future[nthreads];
            int k = size / nthreads;
            for (int j = 0; j < nthreads; j++) {
                final int firstIdx = j * k;
                final int lastIdx = (j == nthreads - 1) ? size : firstIdx + k;

                futures[j] = ConcurrencyUtils.submit(new Runnable() {

                    public void run() {
                        for (int i = firstIdx; i < lastIdx; i++) {
                            setQuick(i, other_loc.getQuick(i));
                        }
                    }
                });
            }
            ConcurrencyUtils.waitForCompletion(futures);
        } else {
            for (int i = 0; i < size; i++) {
                setQuick(i, other_loc.getQuick(i));
            }
        }
        return this;
    }

    /**
     * Assigns the result of a function to each cell;
     * <code>x[i] = function(x[i],y[i])</code>.
     * <p>
     * <b>Example:</b>
     * 
     * <pre>
     * 	 // assign x[i] = x[i]&lt;sup&gt;y[i]&lt;/sup&gt;
     * 	 m1 = 0 1 2 3;
     * 	 m2 = 0 2 4 6;
     * 	 m1.assign(m2, cern.jet.math.Functions.pow);
     * 	 --&gt;
     * 	 m1 == 1 1 16 729
     * 
     * </pre>
     * 
     * For further examples, see the <a
     * href="package-summary.html#FunctionObjects">package doc</a>.
     * 
     * @param y
     *            the secondary matrix to operate on.
     * @param function
     *            a function object taking as first argument the current cell's
     *            value of <code>this</code>, and as second argument the current
     *            cell's value of <code>y</code>,
     * @return <code>this</code> (for convenience only).
     * @throws IllegalArgumentException
     *             if <code>size() != y.size()</code>.
     * @see cern.jet.math.tdouble.DoubleFunctions
     */
    public ObjectMatrix1D assign(final ObjectMatrix1D y, final cern.colt.function.tobject.ObjectObjectFunction function) {
        checkSize(y);
        int nthreads = ConcurrencyUtils.getNumberOfThreads();
        if ((nthreads > 1) && (size >= ConcurrencyUtils.getThreadsBeginN_1D())) {
            nthreads = Math.min(nthreads, size);
            Future<?>[] futures = new Future[nthreads];
            int k = size / nthreads;
            for (int j = 0; j < nthreads; j++) {
                final int firstIdx = j * k;
                final int lastIdx = (j == nthreads - 1) ? size : firstIdx + k;

                futures[j] = ConcurrencyUtils.submit(new Runnable() {

                    public void run() {
                        for (int i = firstIdx; i < lastIdx; i++) {
                            setQuick(i, function.apply(getQuick(i), y.getQuick(i)));
                        }
                    }
                });
            }
            ConcurrencyUtils.waitForCompletion(futures);
        } else {
            for (int i = 0; i < size; i++) {
                setQuick(i, function.apply(getQuick(i), y.getQuick(i)));
            }
        }
        return this;
    }

    /**
     * Sets all cells to the state specified by <code>value</code>.
     * 
     * @param value
     *            the value to be filled into the cells.
     * @return <code>this</code> (for convenience only).
     */
    public ObjectMatrix1D assign(final Object value) {
        int nthreads = ConcurrencyUtils.getNumberOfThreads();
        if ((nthreads > 1) && (size >= ConcurrencyUtils.getThreadsBeginN_1D())) {
            nthreads = Math.min(nthreads, size);
            Future<?>[] futures = new Future[nthreads];
            int k = size / nthreads;
            for (int j = 0; j < nthreads; j++) {
                final int firstIdx = j * k;
                final int lastIdx = (j == nthreads - 1) ? size : firstIdx + k;

                futures[j] = ConcurrencyUtils.submit(new Runnable() {

                    public void run() {
                        for (int i = firstIdx; i < lastIdx; i++) {
                            setQuick(i, value);
                        }
                    }
                });
            }
            ConcurrencyUtils.waitForCompletion(futures);
        } else {
            for (int i = 0; i < size; i++) {
                setQuick(i, value);
            }
        }
        return this;
    }

    /**
     * Returns the number of cells having non-zero values; ignores tolerance.
     * @return 
     */
    public int cardinality() {
        int cardinality = 0;
        for (int i = size; --i >= 0;) {
            if (getQuick(i) != null)
                cardinality++;
        }
        return cardinality;
    }

    /**
     * Constructs and returns a deep copy of the receiver.
     * <p>
     * <b>Note that the returned matrix is an independent deep copy.</b> The
     * returned matrix is not backed by this matrix, so changes in the returned
     * matrix are not reflected in this matrix, and vice-versa.
     * 
     * @return a deep copy of the receiver.
     */
    public ObjectMatrix1D copy() {
        ObjectMatrix1D copy = like();
        copy.assign(this);
        return copy;
    }

    /**
     * Returns the elements of this matrix.
     * 
     * @return the elements
     */
    public abstract Object elements();

    /**
     * Compares the specified Object with the receiver for equality. Equivalent
     * to <code>equals(otherObj,true)</code>.
     * 
     * @param otherObj
     *            the Object to be compared for equality with the receiver.
     * @return true if the specified Object is equal to the receiver.
     */

    public boolean equals(Object otherObj) { // delta
        return equals(otherObj, true);
    }

    /**
     * Compares the specified Object with the receiver for equality. Returns
     * true if and only if the specified Object is also at least an
     * ObjectMatrix1D, both matrices have the same size, and all corresponding
     * pairs of cells in the two matrices are the same. In other words, two
     * matrices are defined to be equal if they contain the same cell values in
     * the same order. Tests elements for equality or identity as specified by
     * <code>testForEquality</code>. When testing for equality, two elements
     * <code>e1</code> and <code>e2</code> are <i>equal</i> if
     * <code>(e1==null ? e2==null :
     * e1.equals(e2))</code>.)
     * 
     * @param otherObj
     *            the Object to be compared for equality with the receiver.
     * @param testForEquality
     *            if true -&gt; tests for equality, otherwise for identity.
     * @return true if the specified Object is equal to the receiver.
     */
    public boolean equals(Object otherObj, boolean testForEquality) { // delta
        if (!(otherObj instanceof ObjectMatrix1D)) {
            return false;
        }
        if (this == otherObj)
            return true;
        if (otherObj == null)
            return false;
        ObjectMatrix1D other = (ObjectMatrix1D) otherObj;
        if (size != other.size())
            return false;

        if (!testForEquality) {
            for (int i = size; --i >= 0;) {
                if (getQuick(i) != other.getQuick(i))
                    return false;
            }
        } else {
            for (int i = size; --i >= 0;) {
                if (!(getQuick(i) == null ? other.getQuick(i) == null : getQuick(i).equals(other.getQuick(i))))
                    return false;
            }
        }

        return true;

    }

    /**
     * Returns the matrix cell value at coordinate <code>index</code>.
     * 
     * @param index
     *            the index of the cell.
     * @return the value of the specified cell.
     * @throws IndexOutOfBoundsException
     *             if <code>index&lt;0 || index&gt;=size()</code>.
     */
    public Object get(int index) {
        if (index < 0 || index >= size)
            checkIndex(index);
        return getQuick(index);
    }

    /**
     * Returns the content of this matrix if it is a wrapper; or <code>this</code>
     * otherwise. Override this method in wrappers.
     * @return 
     */
    protected ObjectMatrix1D getContent() {
        return this;
    }

    /**
     * Fills the coordinates and values of cells having non-zero values into the
     * specified lists. Fills into the lists, starting at index 0. After this
     * call returns the specified lists all have a new size, the number of
     * non-zero values.
     * <p>
     * In general, fill order is <i>unspecified</i>. This implementation fills
     * like: <code>for (index = 0..size()-1)  do ... </code>. However, subclasses
     * are free to us any other order, even an order that may change over time
     * as cell values are changed. (Of course, result lists indexes are
     * guaranteed to correspond to the same cell).
     * <p>
     * <b>Example:</b> <br>
     * 
     * <pre>
     * 	 0, 0, 8, 0, 7
     * 	 --&gt;
     * 	 indexList  = (2,4)
     * 	 valueList  = (8,7)
     * 
     * </pre>
     * 
     * In other words, <code>get(2)==8, get(4)==7</code>.
     * 
     * @param indexList
     *            the list to be filled with indexes, can have any size.
     * @param valueList
     *            the list to be filled with values, can have any size.
     */
    public void getNonZeros(IntArrayList indexList, ObjectArrayList valueList) {
        boolean fillIndexList = indexList != null;
        boolean fillValueList = valueList != null;
        if (fillIndexList)
            indexList.clear();
        if (fillValueList)
            valueList.clear();
        int s = size;
        for (int i = 0; i < s; i++) {
            Object value = getQuick(i);
            if (value != null) {
                if (fillIndexList)
                    indexList.add(i);
                if (fillValueList)
                    valueList.add(value);
            }
        }
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
    public abstract Object getQuick(int index);

    /**
     * Returns <code>true</code> if both matrices share at least one identical cell.
     * @param other
     * @return 
     */
    protected boolean haveSharedCells(ObjectMatrix1D other) {
        if (other == null)
            return false;
        if (this == other)
            return true;
        return getContent().haveSharedCellsRaw(other.getContent());
    }

    /**
     * Returns <code>true</code> if both matrices share at least one identical cell.
     * @param other
     * @return 
     */
    protected boolean haveSharedCellsRaw(ObjectMatrix1D other) {
        return false;
    }

    /**
     * Construct and returns a new empty matrix <i>of the same dynamic type</i>
     * as the receiver, having the same size. For example, if the receiver is an
     * instance of type <code>DenseObjectMatrix1D</code> the new matrix must also be
     * of type <code>DenseObjectMatrix1D</code>, if the receiver is an instance of
     * type <code>SparseObjectMatrix1D</code> the new matrix must also be of type
     * <code>SparseObjectMatrix1D</code>, etc. In general, the new matrix should
     * have internal parametrization as similar as possible.
     * 
     * @return a new empty matrix of the same dynamic type.
     */
    public ObjectMatrix1D like() {
        return like(size);
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
    public abstract ObjectMatrix1D like(int size);

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
    public abstract ObjectMatrix2D like2D(int rows, int columns);

    
    /**
     * Returns new ObjectMatrix2D of size rows x columns whose elements are taken
     * column-wise from this matrix.
     * 
     * @param rows
     *            number of rows
     * @param columns
     *            number of columns
     * @return new 2D matrix with columns being the elements of this matrix.
     */
    public abstract ObjectMatrix2D reshape(int rows, int columns);

    /**
     * Returns new ObjectMatrix3D of size slices x rows x columns, whose elements
     * are taken column-wise from this matrix.
     * 
     * @param slices
     * @param rows
     *            number of rows
     * @param columns
     *            number of columns
     * @return new 2D matrix with columns being the elements of this matrix.
     */
    public abstract ObjectMatrix3D reshape(int slices, int rows, int columns);
    
    /**
     * Sets the matrix cell at coordinate <code>index</code> to the specified value.
     * 
     * @param index
     *            the index of the cell.
     * @param value
     *            the value to be filled into the specified cell.
     * @throws IndexOutOfBoundsException
     *             if <code>index&lt;0 || index&gt;=size()</code>.
     */
    public void set(int index, Object value) {
        if (index < 0 || index >= size)
            checkIndex(index);
        setQuick(index, value);
    }
    
    /**
     * Sets the size of this matrix.
     * 
     * @param size
     */
    public void setSize(int size) {
        this.size = size;
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
    public abstract void setQuick(int index, Object value);

    /**
     * Swaps each element <code>this[i]</code> with <code>other[i]</code>.
     * 
     * @param other
     * @throws IllegalArgumentException
     *             if <code>size() != other.size()</code>.
     */
    public void swap(final ObjectMatrix1D other) {
        checkSize(other);
        int nthreads = ConcurrencyUtils.getNumberOfThreads();
        if ((nthreads > 1) && (size >= ConcurrencyUtils.getThreadsBeginN_1D())) {
            nthreads = Math.min(nthreads, size);
            Future<?>[] futures = new Future[nthreads];
            int k = size / nthreads;
            for (int j = 0; j < nthreads; j++) {
                final int firstIdx = j * k;
                final int lastIdx = (j == nthreads - 1) ? size : firstIdx + k;

                futures[j] = ConcurrencyUtils.submit(new Runnable() {
                    public void run() {
                        for (int i = firstIdx; i < lastIdx; i++) {
                            Object tmp = getQuick(i);
                            setQuick(i, other.getQuick(i));
                            other.setQuick(i, tmp);
                        }
                    }
                });
            }
            ConcurrencyUtils.waitForCompletion(futures);
        } else {
            for (int i = 0; i < size; i++) {
                Object tmp = getQuick(i);
                setQuick(i, other.getQuick(i));
                other.setQuick(i, tmp);
            }
        }
        return;
    }

    /**
     * Constructs and returns a 1-dimensional array containing the cell values.
     * The values are copied. So subsequent changes in <code>values</code> are not
     * reflected in the matrix, and vice-versa. The returned array
     * <code>values</code> has the form <br>
     * <code>for (int i=0; i &lt; size(); i++) values[i] = get(i);</code>
     * 
     * @return an array filled with the values of the cells.
     */
    public Object[] toArray() {
        Object[] values = new Object[size];
        toArray(values);
        return values;
    }

    /**
     * Fills the cell values into the specified 1-dimensional array. The values
     * are copied. So subsequent changes in <code>values</code> are not reflected in
     * the matrix, and vice-versa. After this call returns the array
     * <code>values</code> has the form <br>
     * <code>for (int i=0; i &lt; size(); i++) values[i] = get(i);</code>
     * 
     * @param values
     * @throws IllegalArgumentException
     *             if <code>values.length &lt; size()</code>.
     */
    public void toArray(final Object[] values) {
        if (values.length < size)
            throw new IllegalArgumentException("values too small");
        int nthreads = ConcurrencyUtils.getNumberOfThreads();
        if ((nthreads > 1) && (size >= ConcurrencyUtils.getThreadsBeginN_1D())) {
            nthreads = Math.min(nthreads, size);
            Future<?>[] futures = new Future[nthreads];
            int k = size / nthreads;
            for (int j = 0; j < nthreads; j++) {
                final int firstIdx = j * k;
                final int lastIdx = (j == nthreads - 1) ? size : firstIdx + k;

                futures[j] = ConcurrencyUtils.submit(new Runnable() {
                    public void run() {
                        for (int i = firstIdx; i < lastIdx; i++) {
                            values[i] = getQuick(i);
                        }
                    }
                });
            }
            ConcurrencyUtils.waitForCompletion(futures);
        } else {
            for (int i = 0; i < size; i++) {
                values[i] = getQuick(i);
            }
        }
    }

    /**
     * Returns a string representation using default formatting.
     * 
     * @return 
     * @see cern.colt.matrix.tobject.algo.ObjectFormatter
     */

    public String toString() {
        return new cern.colt.matrix.tobject.algo.ObjectFormatter().toString(this);
    }

    /**
     * Constructs and returns a new view equal to the receiver. The view is a
     * shallow clone. Calls <code>clone()</code> and casts the result.
     * <p>
     * <b>Note that the view is not a deep copy.</b> The returned matrix is
     * backed by this matrix, so changes in the returned matrix are reflected in
     * this matrix, and vice-versa.
     * <p>
     * Use {@link #copy()} to construct an independent deep copy rather than a
     * new view.
     * 
     * @return a new view of the receiver.
     */
    protected ObjectMatrix1D view() {
        return (ObjectMatrix1D) clone();
    }

    /**
     * Constructs and returns a new <i>flip view</i>. What used to be index
     * <code>0</code> is now index <code>size()-1</code>, ..., what used to be index
     * <code>size()-1</code> is now index <code>0</code>. The returned view is backed by
     * this matrix, so changes in the returned view are reflected in this
     * matrix, and vice-versa.
     * 
     * @return a new flip view.
     */
    public ObjectMatrix1D viewFlip() {
        return (ObjectMatrix1D) (view().vFlip());
    }

    /**
     * Constructs and returns a new <i>sub-range view</i> that is a
     * <code>width</code> sub matrix starting at <code>index</code>.
     * 
     * Operations on the returned view can only be applied to the restricted
     * range. Any attempt to access coordinates not contained in the view will
     * throw an <code>IndexOutOfBoundsException</code>.
     * <p>
     * <b>Note that the view is really just a range restriction:</b> The
     * returned matrix is backed by this matrix, so changes in the returned
     * matrix are reflected in this matrix, and vice-versa.
     * <p>
     * The view contains the cells from <code>index..index+width-1</code>. and has
     * <code>view.size() == width</code>. A view's legal coordinates are again zero
     * based, as usual. In other words, legal coordinates of the view are
     * <code>0 .. view.size()-1==width-1</code>. As usual, any attempt to access a
     * cell at other coordinates will throw an
     * <code>IndexOutOfBoundsException</code>.
     * 
     * @param index
     *            The index of the first cell.
     * @param width
     *            The width of the range.
     * @throws IndexOutOfBoundsException
     *             if <code>index&lt;0 || width&lt;0 || index+width&gt;size()</code>.
     * @return the new view.
     * 
     */
    public ObjectMatrix1D viewPart(int index, int width) {
        return (ObjectMatrix1D) (view().vPart(index, width));
    }

    /**
     * Constructs and returns a new <i>selection view</i> that is a matrix
     * holding the indicated cells. There holds
     * <code>view.size() == indexes.length</code> and
     * <code>view.get(i) == this.get(indexes[i])</code>. Indexes can occur multiple
     * times and can be in arbitrary order.
     * <p>
     * <b>Example:</b> <br>
     * 
     * <pre>
     * 	 this     = (0,0,8,0,7)
     * 	 indexes  = (0,2,4,2)
     * 	 --&gt;
     * 	 view     = (0,8,7,8)
     * 
     * </pre>
     * 
     * Note that modifying <code>indexes</code> after this call has returned has no
     * effect on the view. The returned view is backed by this matrix, so
     * changes in the returned view are reflected in this matrix, and
     * vice-versa.
     * 
     * @param indexes
     *            The indexes of the cells that shall be visible in the new
     *            view. To indicate that <i>all</i> cells shall be visible,
     *            simply set this parameter to <code>null</code>.
     * @return the new view.
     * @throws IndexOutOfBoundsException
     *             if <code>!(0 &lt;= indexes[i] &lt; size())</code> for any
     *             <code>i=0..indexes.length()-1</code>.
     */
    public ObjectMatrix1D viewSelection(int[] indexes) {
        // check for "all"
        if (indexes == null) {
            indexes = new int[size];
            for (int i = size; --i >= 0;)
                indexes[i] = i;
        }

        checkIndexes(indexes);
        int[] offsets = new int[indexes.length];
        for (int i = indexes.length; --i >= 0;) {
            offsets[i] = (int) index(indexes[i]);
        }
        return viewSelectionLike(offsets);
    }

    /**
     * Constructs and returns a new <i>selection view</i> that is a matrix
     * holding the cells matching the given condition. Applies the condition to
     * each cell and takes only those cells where
     * <code>condition.apply(get(i))</code> yields <code>true</code>.
     * <p>
     * <b>Example:</b> <br>
     * 
     * <pre>
     * 	 // extract and view all cells with even value
     * 	 matrix = 0 1 2 3 
     * 	 matrix.viewSelection( 
     * 	    new ObjectProcedure() {
     * 	       public final boolean apply(Object a) { return a % 2 == 0; }
     * 	    }
     * 	 );
     * 	 --&gt;
     * 	 matrix ==  0 2
     * 
     * </pre>
     * 
     * For further examples, see the <a
     * href="package-summary.html#FunctionObjects">package doc</a>. The returned
     * view is backed by this matrix, so changes in the returned view are
     * reflected in this matrix, and vice-versa.
     * 
     * @param condition
     *            The condition to be matched.
     * @return the new view.
     */
    public ObjectMatrix1D viewSelection(cern.colt.function.tobject.ObjectProcedure condition) {
        IntArrayList matches = new IntArrayList();
        for (int i = 0; i < size; i++) {
            if (condition.apply(getQuick(i)))
                matches.add(i);
        }
        matches.trimToSize();
        return viewSelection(matches.elements());
    }

    /**
     * Construct and returns a new selection view.
     * 
     * @param offsets
     *            the offsets of the visible elements.
     * @return a new view.
     */
    protected abstract ObjectMatrix1D viewSelectionLike(int[] offsets);

    /**
     * Sorts the vector into ascending order, according to the <i>natural
     * ordering</i>. This sort is guaranteed to be <i>stable</i>. For further
     * information, see
     * {@link cern.colt.matrix.tobject.algo.ObjectSorting#sort(ObjectMatrix1D)}.
     * For more advanced sorting functionality, see
     * {@link cern.colt.matrix.tobject.algo.ObjectSorting}.
     * 
     * @return a new sorted vector (matrix) view.
     */
    public ObjectMatrix1D viewSorted() {
        return cern.colt.matrix.tobject.algo.ObjectSorting.mergeSort.sort(this);
    }

    /**
     * Constructs and returns a new <i>stride view</i> which is a sub matrix
     * consisting of every i-th cell. More specifically, the view has size
     * <code>this.size()/stride</code> holding cells <code>this.get(i*stride)</code> for
     * all <code>i = 0..size()/stride - 1</code>.
     * 
     * @param stride
     *            the step factor.
     * @throws IndexOutOfBoundsException
     *             if <code>stride &lt;= 0</code>.
     * @return the new view.
     * 
     */
    public ObjectMatrix1D viewStrides(int stride) {
        return (ObjectMatrix1D) (view().vStrides(stride));
    }
}
