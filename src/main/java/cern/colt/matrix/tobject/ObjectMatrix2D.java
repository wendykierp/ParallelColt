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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import cern.colt.list.tint.IntArrayList;
import cern.colt.list.tobject.ObjectArrayList;
import cern.colt.matrix.AbstractMatrix2D;
import edu.emory.mathcs.utils.pc.ConcurrencyUtils;

/**
 * Abstract base class for 2-d matrices holding <code>Object</code> elements. First
 * see the <a href="package-summary.html">package summary</a> and javadoc <a
 * href="package-tree.html">tree view</a> to get the broad picture.
 * <p>
 * A matrix has a number of rows and columns, which are assigned upon instance
 * construction - The matrix's size is then <code>rows()*columns()</code>. Elements
 * are accessed via <code>[row,column]</code> coordinates. Legal coordinates range
 * from <code>[0,0]</code> to <code>[rows()-1,columns()-1]</code>. Any attempt to access
 * an element at a coordinate
 * <code>column&lt;0 || column&gt;=columns() || row&lt;0 || row&gt;=rows()</code>
 * will throw an <code>IndexOutOfBoundsException</code>.
 * <p>
 * <b>Note</b> that this implementation is not synchronized.
 * 
 * @author wolfgang.hoschek@cern.ch
 * @version 1.0, 09/24/99
 */
public abstract class ObjectMatrix2D extends AbstractMatrix2D {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * Makes this class non instantiable, but still let's others inherit from
     * it.
     */
    protected ObjectMatrix2D() {
    }

    /**
     * Applies a function to each cell and aggregates the results. Returns a
     * value <code>v</code> such that <code>v==a(size())</code> where
     * <code>a(i) == aggr( a(i-1), f(get(row,column)) )</code> and terminators are
     * <code>a(1) == f(get(0,0)), a(0)==null</code>.
     * <p>
     * <b>Example:</b>
     * 
     * <pre>
     * 	 cern.jet.math.Functions F = cern.jet.math.Functions.functions;
     * 	 2 x 2 matrix
     * 	 0 1
     * 	 2 3
     * 
     * 	 // Sum( x[row,col]*x[row,col] ) 
     * 	 matrix.aggregate(F.plus,F.square);
     * 	 --&gt; 14
     * 
     * </pre>
     * 
     * For further examples, see the <a
     * href="package-summary.html#FunctionObjects">package doc</a>.
     * 
     * @param aggr
     *            an aggregation function taking as first argument the current
     *            aggregation and as second argument the transformed current
     *            cell value.
     * @param f
     *            a function transforming the current cell value.
     * @return the aggregated measure.
     * @see cern.jet.math.tdouble.DoubleFunctions
     */
    public Object aggregate(final cern.colt.function.tobject.ObjectObjectFunction aggr,
            final cern.colt.function.tobject.ObjectFunction f) {
        if (size() == 0)
            return null;
        Object a = 0;
        int nthreads = ConcurrencyUtils.getNumberOfThreads();
        if ((nthreads > 1) && (rows * columns >= ConcurrencyUtils.getThreadsBeginN_2D())) {
            nthreads = Math.min(nthreads, rows);
            Future<?>[] futures = new Future[nthreads];
            int k = rows / nthreads;
            for (int j = 0; j < nthreads; j++) {
                final int firstRow = j * k;
                final int lastRow = (j == nthreads - 1) ? rows : firstRow + k;
                futures[j] = ConcurrencyUtils.submit(new Callable<Object>() {

                    public Object call() throws Exception {
                        Object a = f.apply(getQuick(firstRow, 0));
                        int d = 1;
                        for (int r = firstRow; r < lastRow; r++) {
                            for (int c = d; c < columns; c++) {
                                a = aggr.apply(a, f.apply(getQuick(r, c)));
                            }
                            d = 0;
                        }
                        return a;
                    }
                });
            }
            a = ConcurrencyUtils.waitForCompletion(futures, aggr);
        } else {
            a = f.apply(getQuick(0, 0));
            int d = 1; // first cell already done
            for (int r = 0; r < rows; r++) {
                for (int c = d; c < columns; c++) {
                    a = aggr.apply(a, f.apply(getQuick(r, c)));
                }
                d = 0;
            }
        }
        return a;
    }
    
    /**
     * Applies a function to each cell that satisfies a condition and aggregates
     * the results.
     * 
     * @param aggr
     *            an aggregation function taking as first argument the current
     *            aggregation and as second argument the transformed current
     *            cell value.
     * @param f
     *            a function transforming the current cell value.
     * @param cond
     *            a condition.
     * @return the aggregated measure.
     */
    public Object aggregate(final cern.colt.function.tobject.ObjectObjectFunction aggr,
            final cern.colt.function.tobject.ObjectFunction f, final cern.colt.function.tobject.ObjectProcedure cond) {
        if (size() == 0)
            throw new IllegalArgumentException("size == 0");
        Object a = null;
        int nthreads = ConcurrencyUtils.getNumberOfThreads();
        if ((nthreads > 1) && (rows * columns >= ConcurrencyUtils.getThreadsBeginN_2D())) {
            nthreads = Math.min(nthreads, rows);
            Future<?>[] futures = new Future[nthreads];
            int k = rows / nthreads;
            for (int j = 0; j < nthreads; j++) {
                final int firstRow = j * k;
                final int lastRow = (j == nthreads - 1) ? rows : firstRow + k;
                futures[j] = ConcurrencyUtils.submit(new Callable<Object>() {

                    public Object call() throws Exception {
                        Object elem = getQuick(firstRow, 0);
                        Object a = 0;
                        if (cond.apply(elem) == true) {
                            a = aggr.apply(a, f.apply(elem));
                        }
                        int d = 1;
                        for (int r = firstRow; r < lastRow; r++) {
                            for (int c = d; c < columns; c++) {
                                elem = getQuick(r, c);
                                if (cond.apply(elem) == true) {
                                    a = aggr.apply(a, f.apply(elem));
                                }
                            }
                            d = 0;
                        }
                        return a;
                    }
                });
            }
            a = ConcurrencyUtils.waitForCompletion(futures, aggr);
        } else {
            Object elem = getQuick(0, 0);
            if (cond.apply(elem) == true) {
                a = aggr.apply(a, f.apply(elem));
            }
            int d = 1; // first cell already done
            for (int r = 0; r < rows; r++) {
                for (int c = d; c < columns; c++) {
                    elem = getQuick(r, c);
                    if (cond.apply(elem) == true) {
                        a = aggr.apply(a, f.apply(elem));
                    }
                }
                d = 0;
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
     * @param rowList
     *            row indexes.
     * @param columnList
     *            column indexes.
     * 
     * @return the aggregated measure.
     */
    public Object aggregate(final cern.colt.function.tobject.ObjectObjectFunction aggr,
            final cern.colt.function.tobject.ObjectFunction f, final IntArrayList rowList, final IntArrayList columnList) {
        if (size() == 0)
            throw new IllegalArgumentException("size == 0");
        final int size = rowList.size();
        final int[] rowElements = rowList.elements();
        final int[] columnElements = columnList.elements();
        Object a = 0;
        int nthreads = ConcurrencyUtils.getNumberOfThreads();
        if ((nthreads > 1) && (size >= ConcurrencyUtils.getThreadsBeginN_2D())) {
            nthreads = Math.min(nthreads, size);
            Future<?>[] futures = new Future[nthreads];
            int k = size / nthreads;
            for (int j = 0; j < nthreads; j++) {
                final int firstIdx = j * k;
                final int lastIdx = (j == nthreads - 1) ? size : firstIdx + k;
                futures[j] = ConcurrencyUtils.submit(new Callable<Object>() {

                    public Object call() throws Exception {
                        Object a = f.apply(getQuick(rowElements[firstIdx], columnElements[firstIdx]));
                        Object elem;
                        for (int i = firstIdx + 1; i < lastIdx; i++) {
                            elem = getQuick(rowElements[i], columnElements[i]);
                            a = aggr.apply(a, f.apply(elem));
                        }
                        return a;
                    }
                });
            }
            a = ConcurrencyUtils.waitForCompletion(futures, aggr);
        } else {
            Object elem;
            a = f.apply(getQuick(rowElements[0], columnElements[0]));
            for (int i = 1; i < size; i++) {
                elem = getQuick(rowElements[i], columnElements[i]);
                a = aggr.apply(a, f.apply(elem));
            }
        }
        return a;
    }


    /**
     * Applies a function to each corresponding cell of two matrices and
     * aggregates the results. Returns a value <code>v</code> such that
     * <code>v==a(size())</code> where
     * <code>a(i) == aggr( a(i-1), f(get(row,column),other.get(row,column)) )</code>
     * and terminators are
     * <code>a(1) == f(get(0,0),other.get(0,0)), a(0)==null</code>.
     * <p>
     * <b>Example:</b>
     * 
     * <pre>
     * 	 cern.jet.math.Functions F = cern.jet.math.Functions.functions;
     * 	 x == 2 x 2 matrix
     * 	 0 1
     * 	 2 3
     * 
     * 	 y == 2 x 2 matrix
     * 	 0 1
     * 	 2 3
     * 
     * 	 // Sum( x[row,col] * y[row,col] ) 
     * 	 x.aggregate(y, F.plus, F.mult);
     * 	 --&gt; 14
     * 
     * 	 // Sum( (x[row,col] + y[row,col])&circ;2 )
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
     *             if
     *             <code>columns() != other.columns() || rows() != other.rows()</code>
     * @see cern.jet.math.tdouble.DoubleFunctions
     */
    public Object aggregate(final ObjectMatrix2D other, final cern.colt.function.tobject.ObjectObjectFunction aggr,
            final cern.colt.function.tobject.ObjectObjectFunction f) {
        checkShape(other);
        if (size() == 0)
            return null;
        Object a = null;
        int nthreads = ConcurrencyUtils.getNumberOfThreads();
        if ((nthreads > 1) && (rows * columns >= ConcurrencyUtils.getThreadsBeginN_2D())) {
            nthreads = Math.min(nthreads, rows);
            Future<?>[] futures = new Future[nthreads];
            int k = rows / nthreads;
            for (int j = 0; j < nthreads; j++) {
                final int firstRow = j * k;
                final int lastRow = (j == nthreads - 1) ? rows : firstRow + k;
                futures[j] = ConcurrencyUtils.submit(new Callable<Object>() {

                    public Object call() throws Exception {
                        Object a = f.apply(getQuick(firstRow, 0), other.getQuick(firstRow, 0));
                        int d = 1;
                        for (int r = firstRow; r < lastRow; r++) {
                            for (int c = d; c < columns; c++) {
                                a = aggr.apply(a, f.apply(getQuick(r, c), other.getQuick(r, c)));
                            }
                            d = 0;
                        }
                        return a;
                    }
                });
            }
            a = ConcurrencyUtils.waitForCompletion(futures, aggr);
        } else {
            a = f.apply(getQuick(0, 0), other.getQuick(0, 0));
            int d = 1; // first cell already done
            for (int r = 0; r < rows; r++) {
                for (int c = d; c < columns; c++) {
                    a = aggr.apply(a, f.apply(getQuick(r, c), other.getQuick(r, c)));
                }
                d = 0;
            }
        }
        return a;
    }

    /**
     * Sets all cells to the state specified by <code>values</code>. <code>values</code>
     * is required to have the form <code>values[row][column]</code> and have
     * exactly the same number of rows and columns as the receiver.
     * <p>
     * The values are copied. So subsequent changes in <code>values</code> are not
     * reflected in the matrix, and vice-versa.
     * 
     * @param values
     *            the values to be filled into the cells.
     * @return <code>this</code> (for convenience only).
     * @throws IllegalArgumentException
     *             if
     *             <code>values.length != rows() || for any 0 &lt;= row &lt; rows(): values[row].length != columns()</code>
     *             .
     */
    public ObjectMatrix2D assign(final Object[][] values) {
        if (values.length != rows)
            throw new IllegalArgumentException("Must have same number of rows: rows=" + values.length + "rows()="
                    + rows());
        int nthreads = ConcurrencyUtils.getNumberOfThreads();
        if ((nthreads > 1) && (rows * columns >= ConcurrencyUtils.getThreadsBeginN_2D())) {
            nthreads = Math.min(nthreads, rows);
            Future<?>[] futures = new Future[nthreads];
            int k = rows / nthreads;
            for (int j = 0; j < nthreads; j++) {
                final int firstRow = j * k;
                final int lastRow = (j == nthreads - 1) ? rows : firstRow + k;

                futures[j] = ConcurrencyUtils.submit(new Runnable() {

                    public void run() {
                        for (int r = firstRow; r < lastRow; r++) {
                            Object[] currentRow = values[r];
                            if (currentRow.length != columns)
                                throw new IllegalArgumentException(
                                        "Must have same number of columns in every row: columns=" + currentRow.length
                                                + "columns()=" + columns());
                            for (int c = 0; c < columns; c++) {
                                setQuick(r, c, currentRow[c]);
                            }
                        }
                    }
                });
            }
            ConcurrencyUtils.waitForCompletion(futures);
        } else {
            for (int r = 0; r < rows; r++) {
                Object[] currentRow = values[r];
                if (currentRow.length != columns)
                    throw new IllegalArgumentException("Must have same number of columns in every row: columns="
                            + currentRow.length + "columns()=" + columns());
                for (int c = 0; c < columns; c++) {
                    setQuick(r, c, currentRow[c]);
                }
            }
        }
        return this;
    }

    /**
     * Assigns the result of a function to each cell;
     * <code>x[row,col] = function(x[row,col])</code>.
     * <p>
     * <b>Example:</b>
     * 
     * <pre>
     * 	 matrix = 2 x 2 matrix 
     * 	 0.5 1.5      
     * 	 2.5 3.5
     * 
     * 	 // change each cell to its sine
     * 	 matrix.assign(cern.jet.math.Functions.sin);
     * 	 --&gt;
     * 	 2 x 2 matrix
     * 	 0.479426  0.997495 
     * 	 0.598472 -0.350783
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
    public ObjectMatrix2D assign(final cern.colt.function.tobject.ObjectFunction function) {
        int nthreads = ConcurrencyUtils.getNumberOfThreads();
        if ((nthreads > 1) && (rows * columns >= ConcurrencyUtils.getThreadsBeginN_2D())) {
            nthreads = Math.min(nthreads, rows);
            Future<?>[] futures = new Future[nthreads];
            int k = rows / nthreads;
            for (int j = 0; j < nthreads; j++) {
                final int firstRow = j * k;
                final int lastRow = (j == nthreads - 1) ? rows : firstRow + k;

                futures[j] = ConcurrencyUtils.submit(new Runnable() {

                    public void run() {
                        for (int r = firstRow; r < lastRow; r++) {
                            for (int c = 0; c < columns; c++) {
                                setQuick(r, c, function.apply(getQuick(r, c)));
                            }
                        }
                    }
                });
            }
            ConcurrencyUtils.waitForCompletion(futures);
        } else {
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < columns; c++) {
                    setQuick(r, c, function.apply(getQuick(r, c)));
                }
            }
        }
        return this;
    }
    
    /**
     * Assigns the result of a function to all cells that satisfy a condition.
     * 
     * @param cond
     *            a condition.
     * 
     * @param f
     *            a function object.
     * @return <code>this</code> (for convenience only).
     */
    public ObjectMatrix2D assign(final cern.colt.function.tobject.ObjectProcedure cond,
            final cern.colt.function.tobject.ObjectFunction f) {
        int nthreads = ConcurrencyUtils.getNumberOfThreads();
        if ((nthreads > 1) && (rows * columns >= ConcurrencyUtils.getThreadsBeginN_2D())) {
            nthreads = Math.min(nthreads, rows);
            Future<?>[] futures = new Future[nthreads];
            int k = rows / nthreads;
            for (int j = 0; j < nthreads; j++) {
                final int firstRow = j * k;
                final int lastRow = (j == nthreads - 1) ? rows : firstRow + k;

                futures[j] = ConcurrencyUtils.submit(new Runnable() {

                    public void run() {
                        Object elem;
                        for (int r = firstRow; r < lastRow; r++) {
                            for (int c = 0; c < columns; c++) {
                                elem = getQuick(r, c);
                                if (cond.apply(elem) == true) {
                                    setQuick(r, c, f.apply(elem));
                                }
                            }
                        }
                    }
                });
            }
            ConcurrencyUtils.waitForCompletion(futures);
        } else {
            Object elem;
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < columns; c++) {
                    elem = getQuick(r, c);
                    if (cond.apply(elem) == true) {
                        setQuick(r, c, f.apply(elem));
                    }
                }
            }
        }
        return this;
    }

    /**
     * Assigns a value to all cells that satisfy a condition.
     * 
     * @param cond
     *            a condition.
     * 
     * @param value
     *            a value.
     * @return <code>this</code> (for convenience only).
     * 
     */
    public ObjectMatrix2D assign(final cern.colt.function.tobject.ObjectProcedure cond, final Object value) {
        int nthreads = ConcurrencyUtils.getNumberOfThreads();
        if ((nthreads > 1) && (rows * columns >= ConcurrencyUtils.getThreadsBeginN_2D())) {
            nthreads = Math.min(nthreads, rows);
            Future<?>[] futures = new Future[nthreads];
            int k = rows / nthreads;
            for (int j = 0; j < nthreads; j++) {
                final int firstRow = j * k;
                final int lastRow = (j == nthreads - 1) ? rows : firstRow + k;

                futures[j] = ConcurrencyUtils.submit(new Runnable() {

                    public void run() {
                        Object elem;
                        for (int r = firstRow; r < lastRow; r++) {
                            for (int c = 0; c < columns; c++) {
                                elem = getQuick(r, c);
                                if (cond.apply(elem) == true) {
                                    setQuick(r, c, value);
                                }
                            }
                        }
                    }
                });
            }
            ConcurrencyUtils.waitForCompletion(futures);
        } else {
            Object elem;
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < columns; c++) {
                    elem = getQuick(r, c);
                    if (cond.apply(elem) == true) {
                        setQuick(r, c, value);
                    }
                }
            }
        }
        return this;
    }
    
    /**
     * Sets all cells to the state specified by <code>values</code>. <code>values</code>
     * is required to have the form <code>values[row*column]</code> and elements
     * have to be stored in a row-wise order.
     * <p>
     * The values are copied. So subsequent changes in <code>values</code> are not
     * reflected in the matrix, and vice-versa.
     * 
     * @param values
     *            the values to be filled into the cells.
     * @return <code>this</code> (for convenience only).
     * @throws IllegalArgumentException
     *             if <code>values.length != rows()*columns()</code>.
     */
    public ObjectMatrix2D assign(final Object[] values) {
        if (values.length != rows * columns)
            throw new IllegalArgumentException("Must have same length: length=" + values.length + "rows()*columns()="
                    + rows() * columns());
        int nthreads = ConcurrencyUtils.getNumberOfThreads();
        if ((nthreads > 1) && (rows * columns >= ConcurrencyUtils.getThreadsBeginN_2D())) {
            nthreads = Math.min(nthreads, rows);
            Future<?>[] futures = new Future[nthreads];
            int k = rows / nthreads;
            for (int j = 0; j < nthreads; j++) {
                final int firstRow = j * k;
                final int lastRow = (j == nthreads - 1) ? rows : firstRow + k;

                futures[j] = ConcurrencyUtils.submit(new Runnable() {

                    public void run() {
                        int idx = firstRow * columns;
                        for (int r = firstRow; r < lastRow; r++) {
                            for (int c = 0; c < columns; c++) {
                                setQuick(r, c, values[idx++]);
                            }
                        }
                    }
                });
            }
            ConcurrencyUtils.waitForCompletion(futures);
        } else {

            int idx = 0;
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < columns; c++) {
                    setQuick(r, c, values[idx++]);
                }
            }
        }

        return this;
    }

    /**
     * Replaces all cell values of the receiver with the values of another
     * matrix. Both matrices must have the same number of rows and columns. If
     * both matrices share the same cells (as is the case if they are views
     * derived from the same matrix) and intersect in an ambiguous way, then
     * replaces <i>as if</i> using an intermediate auxiliary deep copy of
     * <code>other</code>.
     * 
     * @param other
     *            the source matrix to copy from (may be identical to the
     *            receiver).
     * @return <code>this</code> (for convenience only).
     * @throws IllegalArgumentException
     *             if
     *             <code>columns() != other.columns() || rows() != other.rows()</code>
     */
    public ObjectMatrix2D assign(ObjectMatrix2D other) {
        if (other == this)
            return this;
        checkShape(other);
        final ObjectMatrix2D other_loc;
        if (haveSharedCells(other)) {
            other_loc = other.copy();
        } else {
            other_loc = other;
        }
        int nthreads = ConcurrencyUtils.getNumberOfThreads();
        if ((nthreads > 1) && (rows * columns >= ConcurrencyUtils.getThreadsBeginN_2D())) {
            nthreads = Math.min(nthreads, rows);
            Future<?>[] futures = new Future[nthreads];
            int k = rows / nthreads;
            for (int j = 0; j < nthreads; j++) {
                final int firstRow = j * k;
                final int lastRow = (j == nthreads - 1) ? rows : firstRow + k;

                futures[j] = ConcurrencyUtils.submit(new Runnable() {

                    public void run() {
                        for (int r = firstRow; r < lastRow; r++) {
                            for (int c = 0; c < columns; c++) {
                                setQuick(r, c, other_loc.getQuick(r, c));
                            }
                        }
                    }
                });
            }
            ConcurrencyUtils.waitForCompletion(futures);
        } else {
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < columns; c++) {
                    setQuick(r, c, other_loc.getQuick(r, c));
                }
            }
        }
        return this;
    }

    /**
     * Assigns the result of a function to each cell;
     * <code>x[row,col] = function(x[row,col],y[row,col])</code>.
     * 
     * 
     * @param y
     *            the secondary matrix to operate on.
     * @param function
     *            a function object taking as first argument the current cell's
     *            value of <code>this</code>, and as second argument the current
     *            cell's value of <code>y</code>,
     * @return <code>this</code> (for convenience only).
     * @throws IllegalArgumentException
     *             if
     *             <code>columns() != other.columns() || rows() != other.rows()</code>
     * @see cern.jet.math.tdouble.DoubleFunctions
     */
    public ObjectMatrix2D assign(final ObjectMatrix2D y, final cern.colt.function.tobject.ObjectObjectFunction function) {
        checkShape(y);
        int nthreads = ConcurrencyUtils.getNumberOfThreads();
        if ((nthreads > 1) && (rows * columns >= ConcurrencyUtils.getThreadsBeginN_2D())) {
            nthreads = Math.min(nthreads, rows);
            Future<?>[] futures = new Future[nthreads];
            int k = rows / nthreads;
            for (int j = 0; j < nthreads; j++) {
                final int firstRow = j * k;
                final int lastRow = (j == nthreads - 1) ? rows : firstRow + k;

                futures[j] = ConcurrencyUtils.submit(new Runnable() {

                    public void run() {
                        for (int r = firstRow; r < lastRow; r++) {
                            for (int c = 0; c < columns; c++) {
                                setQuick(r, c, function.apply(getQuick(r, c), y.getQuick(r, c)));
                            }
                        }
                    }
                });
            }
            ConcurrencyUtils.waitForCompletion(futures);
        } else {
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < columns; c++) {
                    setQuick(r, c, function.apply(getQuick(r, c), y.getQuick(r, c)));
                }
            }
        }
        return this;
    }
    
    /**
     * Assigns the result of a function to all cells with a given indexes
     * 
     * @param y
     *            the secondary matrix to operate on.
     * @param function
     *            a function object taking as first argument the current cell's
     *            value of <code>this</code>, and as second argument the current
     *            cell's value of <code>y</code>,
     * @param rowList
     *            row indexes.
     * @param columnList
     *            column indexes.
     * 
     * @return <code>this</code> (for convenience only).
     * @throws IllegalArgumentException
     *             if
     *             <code>columns() != other.columns() || rows() != other.rows()</code>
     */
    public ObjectMatrix2D assign(final ObjectMatrix2D y, final cern.colt.function.tobject.ObjectObjectFunction function,
            IntArrayList rowList, IntArrayList columnList) {
        checkShape(y);
        final int size = rowList.size();
        final int[] rowElements = rowList.elements();
        final int[] columnElements = columnList.elements();
        int nthreads = ConcurrencyUtils.getNumberOfThreads();
        if ((nthreads > 1) && (size >= ConcurrencyUtils.getThreadsBeginN_2D())) {
            nthreads = Math.min(nthreads, size);
            Future<?>[] futures = new Future[nthreads];
            int k = size / nthreads;
            for (int j = 0; j < nthreads; j++) {
                final int firstIdx = j * k;
                final int lastIdx = (j == nthreads - 1) ? size : firstIdx + k;
                futures[j] = ConcurrencyUtils.submit(new Runnable() {

                    public void run() {
                        for (int i = firstIdx; i < lastIdx; i++) {
                            setQuick(rowElements[i], columnElements[i], function.apply(getQuick(rowElements[i],
                                    columnElements[i]), y.getQuick(rowElements[i], columnElements[i])));
                        }
                    }

                });
            }
            ConcurrencyUtils.waitForCompletion(futures);
        } else {
            for (int i = 0; i < size; i++) {
                setQuick(rowElements[i], columnElements[i], function.apply(getQuick(rowElements[i], columnElements[i]),
                        y.getQuick(rowElements[i], columnElements[i])));
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
    public ObjectMatrix2D assign(final Object value) {
        int nthreads = ConcurrencyUtils.getNumberOfThreads();
        if ((nthreads > 1) && (rows * columns >= ConcurrencyUtils.getThreadsBeginN_2D())) {
            nthreads = Math.min(nthreads, rows);
            Future<?>[] futures = new Future[nthreads];
            int k = rows / nthreads;
            for (int j = 0; j < nthreads; j++) {
                final int firstRow = j * k;
                final int lastRow = (j == nthreads - 1) ? rows : firstRow + k;

                futures[j] = ConcurrencyUtils.submit(new Runnable() {

                    public void run() {
                        for (int r = firstRow; r < lastRow; r++) {
                            for (int c = 0; c < columns; c++) {
                                setQuick(r, c, value);
                            }
                        }
                    }
                });
            }
            ConcurrencyUtils.waitForCompletion(futures);
        } else {
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < columns; c++) {
                    setQuick(r, c, value);
                }
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
        int nthreads = ConcurrencyUtils.getNumberOfThreads();
        if ((nthreads > 1) && (rows * columns >= ConcurrencyUtils.getThreadsBeginN_2D())) {
            nthreads = Math.min(nthreads, rows);
            Future<?>[] futures = new Future[nthreads];
            Integer[] results = new Integer[nthreads];
            int k = rows / nthreads;
            for (int j = 0; j < nthreads; j++) {
                final int firstRow = j * k;
                final int lastRow = (j == nthreads - 1) ? rows : firstRow + k;

                futures[j] = ConcurrencyUtils.submit(new Callable<Integer>() {
                    public Integer call() throws Exception {
                        int cardinality = 0;
                        for (int r = firstRow; r < lastRow; r++) {
                            for (int c = 0; c < columns; c++) {
                                if (getQuick(r, c) != null)
                                    cardinality++;
                            }
                        }
                        return cardinality;
                    }
                });
            }
            try {
                for (int j = 0; j < nthreads; j++) {
                    results[j] = (Integer) futures[j].get();
                }
                cardinality = results[0].intValue();
                for (int j = 1; j < nthreads; j++) {
                    cardinality += results[j];
                }
            } catch (ExecutionException ex) {
                ex.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < columns; c++) {
                    if (getQuick(r, c) != null)
                        cardinality++;
                }
            }
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
    public ObjectMatrix2D copy() {
        return like().assign(this);
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
     * ObjectMatrix2D, both matrices have the same size, and all corresponding
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
        if (!(otherObj instanceof ObjectMatrix2D)) {
            return false;
        }
        if (this == otherObj)
            return true;
        if (otherObj == null)
            return false;
        ObjectMatrix2D other = (ObjectMatrix2D) otherObj;
        if (rows != other.rows())
            return false;
        if (columns != other.columns())
            return false;

        if (!testForEquality) {
            for (int row = rows; --row >= 0;) {
                for (int column = columns; --column >= 0;) {
                    if (getQuick(row, column) != other.getQuick(row, column))
                        return false;
                }
            }
        } else {
            for (int row = rows; --row >= 0;) {
                for (int column = columns; --column >= 0;) {
                    if (!(getQuick(row, column) == null ? other.getQuick(row, column) == null : getQuick(row, column)
                            .equals(other.getQuick(row, column))))
                        return false;
                }
            }
        }

        return true;

    }
    
    /**
     * Assigns the result of a function to each <i>non-zero</i> cell;
     * <code>x[row,col] = function(x[row,col])</code>. Use this method for fast
     * special-purpose iteration. If you want to modify another matrix instead
     * of <code>this</code> (i.e. work in read-only mode), simply return the input
     * value unchanged.
     * 
     * Parameters to function are as follows: <code>first==row</code>,
     * <code>second==column</code>, <code>third==nonZeroValue</code>.
     * 
     * @param function
     *            a function object taking as argument the current non-zero
     *            cell's row, column and value.
     * @return <code>this</code> (for convenience only).
     */
    public ObjectMatrix2D forEachNonZero(final cern.colt.function.tobject.IntIntObjectFunction function) {
        int nthreads = ConcurrencyUtils.getNumberOfThreads();
        if ((nthreads > 1) && (size() >= ConcurrencyUtils.getThreadsBeginN_2D())) {
            nthreads = Math.min(nthreads, rows);
            Future<?>[] futures = new Future[nthreads];
            int k = rows / nthreads;
            for (int j = 0; j < nthreads; j++) {
                final int firstRow = j * k;
                final int lastRow = (j == nthreads - 1) ? rows : firstRow + k;

                futures[j] = ConcurrencyUtils.submit(new Runnable() {
                    public void run() {
                        for (int r = firstRow; r < lastRow; r++) {
                            for (int c = 0; c < columns; c++) {
                                Object value = getQuick(r, c);
                                if (value != null) {
                                    Object a = function.apply(r, c, value);
                                    if (a != value)
                                        setQuick(r, c, a);
                                }
                            }
                        }
                    }
                });
            }
            ConcurrencyUtils.waitForCompletion(futures);
        } else {
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < columns; c++) {
                    Object value = getQuick(r, c);
                    if (value != null) {
                        Object a = function.apply(r, c, value);
                        if (a != value)
                            setQuick(r, c, a);
                    }
                }
            }
        }
        return this;
    }

    /**
     * Returns the matrix cell value at coordinate <code>[row,column]</code>.
     * 
     * @param row
     *            the index of the row-coordinate.
     * @param column
     *            the index of the column-coordinate.
     * @return the value of the specified cell.
     * @throws IndexOutOfBoundsException
     *             if
     *             <code>column&lt;0 || column&gt;=columns() || row&lt;0 || row&gt;=rows()</code>
     */
    public Object get(int row, int column) {
        if (column < 0 || column >= columns || row < 0 || row >= rows)
            throw new IndexOutOfBoundsException("row:" + row + ", column:" + column);
        return getQuick(row, column);
    }

    /**
     * Returns the content of this matrix if it is a wrapper; or <code>this</code>
     * otherwise. Override this method in wrappers.
     * @return 
     */
    protected ObjectMatrix2D getContent() {
        return this;
    }

    /**
     * Fills the coordinates and values of cells having non-zero values into the
     * specified lists. Fills into the lists, starting at index 0. After this
     * call returns the specified lists all have a new size, the number of
     * non-zero values.
     * <p>
     * In general, fill order is <i>unspecified</i>. This implementation fills
     * like <code>for (row = 0..rows-1) for (column = 0..columns-1) do ... </code>.
     * However, subclasses are free to us any other order, even an order that
     * may change over time as cell values are changed. (Of course, result lists
     * indexes are guaranteed to correspond to the same cell).
     * <p>
     * <b>Example:</b> <br>
     * 
     * <pre>
     * 	 2 x 3 matrix:
     * 	 0, 0, 8
     * 	 0, 7, 0
     * 	 --&gt;
     * 	 rowList    = (0,1)
     * 	 columnList = (2,1)
     * 	 valueList  = (8,7)
     * 
     * </pre>
     * 
     * In other words, <code>get(0,2)==8, get(1,1)==7</code>.
     * 
     * @param rowList
     *            the list to be filled with row indexes, can have any size.
     * @param columnList
     *            the list to be filled with column indexes, can have any size.
     * @param valueList
     *            the list to be filled with values, can have any size.
     */
    public void getNonZeros(IntArrayList rowList, IntArrayList columnList, ObjectArrayList valueList) {
        rowList.clear();
        columnList.clear();
        valueList.clear();
        int r = rows;
        int c = columns;
        for (int row = 0; row < r; row++) {
            for (int column = 0; column < c; column++) {
                Object value = getQuick(row, column);
                if (value != null) {
                    rowList.add(row);
                    columnList.add(column);
                    valueList.add(value);
                }
            }
        }
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
    public abstract Object getQuick(int row, int column);

    /**
     * Returns <code>true</code> if both matrices share at least one identical cell.
     * @param other
     * @return 
     */
    protected boolean haveSharedCells(ObjectMatrix2D other) {
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
    protected boolean haveSharedCellsRaw(ObjectMatrix2D other) {
        return false;
    }

    /**
     * Construct and returns a new empty matrix <i>of the same dynamic type</i>
     * as the receiver, having the same number of rows and columns. For example,
     * if the receiver is an instance of type <code>DenseObjectMatrix2D</code> the
     * new matrix must also be of type <code>DenseObjectMatrix2D</code>, if the
     * receiver is an instance of type <code>SparseObjectMatrix2D</code> the new
     * matrix must also be of type <code>SparseObjectMatrix2D</code>, etc. In
     * general, the new matrix should have internal parametrization as similar
     * as possible.
     * 
     * @return a new empty matrix of the same dynamic type.
     */
    public ObjectMatrix2D like() {
        return like(rows, columns);
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
    public abstract ObjectMatrix2D like(int rows, int columns);

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
    public abstract ObjectMatrix1D like1D(int size);

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
     * @param zero
     *            the index of the first element.
     * @param stride
     *            the number of indexes between any two elements, i.e.
     *            <code>index(i+1)-index(i)</code>.
     * @return a new matrix of the corresponding dynamic type.
     */
    protected abstract ObjectMatrix1D like1D(int size, int zero, int stride);

    /**
     * Sets the matrix cell at coordinate <code>[row,column]</code> to the specified
     * value.
     * 
     * @param row
     *            the index of the row-coordinate.
     * @param column
     *            the index of the column-coordinate.
     * @param value
     *            the value to be filled into the specified cell.
     * @throws IndexOutOfBoundsException
     *             if
     *             <code>column&lt;0 || column&gt;=columns() || row&lt;0 || row&gt;=rows()</code>
     */
    public void set(int row, int column, Object value) {
        if (column < 0 || column >= columns || row < 0 || row >= rows)
            throw new IndexOutOfBoundsException("row:" + row + ", column:" + column);
        setQuick(row, column, value);
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
    public abstract void setQuick(int row, int column, Object value);

    /**
     * Constructs and returns a 2-dimensional array containing the cell values.
     * The returned array <code>values</code> has the form
     * <code>values[row][column]</code> and has the same number of rows and columns
     * as the receiver.
     * <p>
     * The values are copied. So subsequent changes in <code>values</code> are not
     * reflected in the matrix, and vice-versa.
     * 
     * @return an array filled with the values of the cells.
     */
    public Object[][] toArray() {
        final Object[][] values = new Object[rows][columns];
        int nthreads = ConcurrencyUtils.getNumberOfThreads();
        if ((nthreads > 1) && (size() >= ConcurrencyUtils.getThreadsBeginN_2D())) {
            nthreads = Math.min(nthreads, rows);
            Future<?>[] futures = new Future[nthreads];
            int k = rows / nthreads;
            for (int j = 0; j < nthreads; j++) {
                final int firstRow = j * k;
                final int lastRow = (j == nthreads - 1) ? rows : firstRow + k;

                futures[j] = ConcurrencyUtils.submit(new Runnable() {
                    public void run() {
                        for (int r = firstRow; r < lastRow; r++) {
                            Object[] currentRow = values[r];
                            for (int c = 0; c < columns; c++) {
                                currentRow[c] = getQuick(r, c);
                            }
                        }
                    }
                });
            }
            ConcurrencyUtils.waitForCompletion(futures);
        } else {
            for (int r = 0; r < rows; r++) {
                Object[] currentRow = values[r];
                for (int c = 0; c < columns; c++) {
                    currentRow[c] = getQuick(r, c);
                }
            }
        }
        return values;
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
     * Returns a vector obtained by stacking the columns of the matrix on top of
     * one another.
     * 
     * @return a vector of columns of this matrix.
     */
    public abstract ObjectMatrix1D vectorize();

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
    protected ObjectMatrix2D view() {
        return (ObjectMatrix2D) clone();
    }

    /**
     * Constructs and returns a new <i>slice view</i> representing the rows of
     * the given column. The returned view is backed by this matrix, so changes
     * in the returned view are reflected in this matrix, and vice-versa. To
     * obtain a slice view on subranges, construct a sub-ranging view (
     * <code>viewPart(...)</code>), then apply this method to the sub-range view.
     * <p>
     * <b>Example:</b>
     * <table border="0">
     * <tr nowrap>
     * <td valign="top">2 x 3 matrix: <br>
     * 1, 2, 3<br>
     * 4, 5, 6</td>
     * <td>viewColumn(0) ==&gt;</td>
     * <td valign="top">Matrix1D of size 2:<br>
     * 1, 4</td>
     * </tr>
     * </table>
     * 
     * @param column
     *            the column to fix.
     * @return a new slice view.
     * @throws IndexOutOfBoundsException
     *             if <code>column &lt; 0 || column &gt;= columns()</code>.
     * @see #viewRow(int)
     */
    public ObjectMatrix1D viewColumn(int column) {
        checkColumn(column);
        int viewSize = this.rows;
        int viewZero = (int) index(0, column);
        int viewStride = this.rowStride;
        return like1D(viewSize, viewZero, viewStride);
    }

    /**
     * Constructs and returns a new <i>flip view</i> along the column axis. What
     * used to be column <code>0</code> is now column <code>columns()-1</code>, ...,
     * what used to be column <code>columns()-1</code> is now column <code>0</code>. The
     * returned view is backed by this matrix, so changes in the returned view
     * are reflected in this matrix, and vice-versa.
     * <p>
     * <b>Example:</b>
     * <table border="0">
     * <tr nowrap>
     * <td valign="top">2 x 3 matrix: <br>
     * 1, 2, 3<br>
     * 4, 5, 6</td>
     * <td>columnFlip ==&gt;</td>
     * <td valign="top">2 x 3 matrix:<br>
     * 3, 2, 1 <br>
     * 6, 5, 4</td>
     * <td>columnFlip ==&gt;</td>
     * <td valign="top">2 x 3 matrix: <br>
     * 1, 2, 3<br>
     * 4, 5, 6</td>
     * </tr>
     * </table>
     * 
     * @return a new flip view.
     * @see #viewRowFlip()
     */
    public ObjectMatrix2D viewColumnFlip() {
        return (ObjectMatrix2D) (view().vColumnFlip());
    }

    /**
     * Constructs and returns a new <i>dice (transposition) view</i>; Swaps
     * axes; example: 3 x 4 matrix --&gt; 4 x 3 matrix. The view has both
     * dimensions exchanged; what used to be columns become rows, what used to
     * be rows become columns. In other words:
     * <code>view.get(row,column)==this.get(column,row)</code>. This is a zero-copy
     * transposition, taking O(1), i.e. constant time. The returned view is
     * backed by this matrix, so changes in the returned view are reflected in
     * this matrix, and vice-versa. Use idioms like
     * <code>result = viewDice(A).copy()</code> to generate an independent
     * transposed matrix.
     * <p>
     * <b>Example:</b>
     * <table border="0">
     * <tr nowrap>
     * <td valign="top">2 x 3 matrix: <br>
     * 1, 2, 3<br>
     * 4, 5, 6</td>
     * <td>transpose ==&gt;</td>
     * <td valign="top">3 x 2 matrix:<br>
     * 1, 4 <br>
     * 2, 5 <br>
     * 3, 6</td>
     * <td>transpose ==&gt;</td>
     * <td valign="top">2 x 3 matrix: <br>
     * 1, 2, 3<br>
     * 4, 5, 6</td>
     * </tr>
     * </table>
     * 
     * @return a new dice view.
     */
    public ObjectMatrix2D viewDice() {
        return (ObjectMatrix2D) (view().vDice());
    }

    /**
     * Constructs and returns a new <i>sub-range view</i> that is a
     * <code>height x width</code> sub matrix starting at <code>[row,column]</code>.
     * 
     * Operations on the returned view can only be applied to the restricted
     * range. Any attempt to access coordinates not contained in the view will
     * throw an <code>IndexOutOfBoundsException</code>.
     * <p>
     * <b>Note that the view is really just a range restriction:</b> The
     * returned matrix is backed by this matrix, so changes in the returned
     * matrix are reflected in this matrix, and vice-versa.
     * <p>
     * The view contains the cells from <code>[row,column]</code> to
     * <code>[row+height-1,column+width-1]</code>, all inclusive. and has
     * <code>view.rows() == height; view.columns() == width;</code>. A view's legal
     * coordinates are again zero based, as usual. In other words, legal
     * coordinates of the view range from <code>[0,0]</code> to
     * <code>[view.rows()-1==height-1,view.columns()-1==width-1]</code>. As usual,
     * any attempt to access a cell at a coordinate
     * <code>column&lt;0 || column&gt;=view.columns() || row&lt;0 || row&gt;=view.rows()</code>
     * will throw an <code>IndexOutOfBoundsException</code>.
     * 
     * @param row
     *            The index of the row-coordinate.
     * @param column
     *            The index of the column-coordinate.
     * @param height
     *            The height of the box.
     * @param width
     *            The width of the box.
     * @throws IndexOutOfBoundsException
     *             if
     *             <code>column&lt;0 || width&lt;0 || column+width&gt;columns() || row&lt;0 || height&lt;0 || row+height&gt;rows()</code>
     * @return the new view.
     * 
     */
    public ObjectMatrix2D viewPart(int row, int column, int height, int width) {
        return (ObjectMatrix2D) (view().vPart(row, column, height, width));
    }

    /**
     * Constructs and returns a new <i>slice view</i> representing the columns
     * of the given row. The returned view is backed by this matrix, so changes
     * in the returned view are reflected in this matrix, and vice-versa. To
     * obtain a slice view on subranges, construct a sub-ranging view (
     * <code>viewPart(...)</code>), then apply this method to the sub-range view.
     * <p>
     * <b>Example:</b>
     * <table border="0">
     * <tr nowrap>
     * <td valign="top">2 x 3 matrix: <br>
     * 1, 2, 3<br>
     * 4, 5, 6</td>
     * <td>viewRow(0) ==&gt;</td>
     * <td valign="top">Matrix1D of size 3:<br>
     * 1, 2, 3</td>
     * </tr>
     * </table>
     * 
     * @param row
     *            the row to fix.
     * @return a new slice view.
     * @throws IndexOutOfBoundsException
     *             if <code>row &lt; 0 || row &gt;= rows()</code>.
     * @see #viewColumn(int)
     */
    public ObjectMatrix1D viewRow(int row) {
        checkRow(row);
        int viewSize = this.columns;
        int viewZero = (int) index(row, 0);
        int viewStride = this.columnStride;
        return like1D(viewSize, viewZero, viewStride);
    }

    /**
     * Constructs and returns a new <i>flip view</i> along the row axis. What
     * used to be row <code>0</code> is now row <code>rows()-1</code>, ..., what used to
     * be row <code>rows()-1</code> is now row <code>0</code>. The returned view is
     * backed by this matrix, so changes in the returned view are reflected in
     * this matrix, and vice-versa.
     * <p>
     * <b>Example:</b>
     * <table border="0">
     * <tr nowrap>
     * <td valign="top">2 x 3 matrix: <br>
     * 1, 2, 3<br>
     * 4, 5, 6</td>
     * <td>rowFlip ==&gt;</td>
     * <td valign="top">2 x 3 matrix:<br>
     * 4, 5, 6 <br>
     * 1, 2, 3</td>
     * <td>rowFlip ==&gt;</td>
     * <td valign="top">2 x 3 matrix: <br>
     * 1, 2, 3<br>
     * 4, 5, 6</td>
     * </tr>
     * </table>
     * 
     * @return a new flip view.
     * @see #viewColumnFlip()
     */
    public ObjectMatrix2D viewRowFlip() {
        return (ObjectMatrix2D) (view().vRowFlip());
    }

    /**
     * Constructs and returns a new <i>selection view</i> that is a matrix
     * holding the indicated cells. There holds
     * <code>view.rows() == rowIndexes.length, view.columns() == columnIndexes.length</code>
     * and <code>view.get(i,j) == this.get(rowIndexes[i],columnIndexes[j])</code>.
     * Indexes can occur multiple times and can be in arbitrary order.
     * <p>
     * <b>Example:</b>
     * 
     * <pre>
     * 	 this = 2 x 3 matrix:
     * 	 1, 2, 3
     * 	 4, 5, 6
     * 	 rowIndexes     = (0,1)
     * 	 columnIndexes  = (1,0,1,0)
     * 	 --&gt;
     * 	 view = 2 x 4 matrix:
     * 	 2, 1, 2, 1
     * 	 5, 4, 5, 4
     * 
     * </pre>
     * 
     * Note that modifying the index arguments after this call has returned has
     * no effect on the view. The returned view is backed by this matrix, so
     * changes in the returned view are reflected in this matrix, and
     * vice-versa.
     * <p>
     * To indicate "all" rows or "all columns", simply set the respective
     * parameter
     * 
     * @param rowIndexes
     *            The rows of the cells that shall be visible in the new view.
     *            To indicate that <i>all</i> rows shall be visible, simply set
     *            this parameter to <code>null</code>.
     * @param columnIndexes
     *            The columns of the cells that shall be visible in the new
     *            view. To indicate that <i>all</i> columns shall be visible,
     *            simply set this parameter to <code>null</code>.
     * @return the new view.
     * @throws IndexOutOfBoundsException
     *             if <code>!(0 &lt;= rowIndexes[i] &lt; rows())</code> for any
     *             <code>i=0..rowIndexes.length()-1</code>.
     * @throws IndexOutOfBoundsException
     *             if <code>!(0 &lt;= columnIndexes[i] &lt; columns())</code> for any
     *             <code>i=0..columnIndexes.length()-1</code>.
     */
    public ObjectMatrix2D viewSelection(int[] rowIndexes, int[] columnIndexes) {
        // check for "all"
        if (rowIndexes == null) {
            rowIndexes = new int[rows];
            for (int i = rows; --i >= 0;)
                rowIndexes[i] = i;
        }
        if (columnIndexes == null) {
            columnIndexes = new int[columns];
            for (int i = columns; --i >= 0;)
                columnIndexes[i] = i;
        }

        checkRowIndexes(rowIndexes);
        checkColumnIndexes(columnIndexes);
        int[] rowOffsets = new int[rowIndexes.length];
        int[] columnOffsets = new int[columnIndexes.length];
        for (int i = rowIndexes.length; --i >= 0;) {
            rowOffsets[i] = _rowOffset(_rowRank(rowIndexes[i]));
        }
        for (int i = columnIndexes.length; --i >= 0;) {
            columnOffsets[i] = _columnOffset(_columnRank(columnIndexes[i]));
        }
        return viewSelectionLike(rowOffsets, columnOffsets);
    }

    /**
     * Constructs and returns a new <i>selection view</i> that is a matrix
     * holding all <b>rows</b> matching the given condition. Applies the
     * condition to each row and takes only those row where
     * <code>condition.apply(viewRow(i))</code> yields <code>true</code>. To match
     * columns, use a dice view.
     * <p>
     * <b>Example:</b> <br>
     * 
     * <pre>
     * 	 // extract and view all rows which have a value &lt; threshold in the first column (representing &quot;age&quot;)
     * 	 final Object threshold = 16;
     * 	 matrix.viewSelection( 
     * 	    new ObjectMatrix1DProcedure() {
     * 	       public final boolean apply(ObjectMatrix1D m) { return m.get(0) &lt; threshold; }
     * 	    }
     * 	 );
     * 
     * 	 // extract and view all rows with RMS &lt; threshold
     * 	 // The RMS (Root-Mean-Square) is a measure of the average &quot;size&quot; of the elements of a data sequence.
     * 	 matrix = 0 1 2 3
     * 	 final Object threshold = 0.5;
     * 	 matrix.viewSelection( 
     * 	    new ObjectMatrix1DProcedure() {
     * 	       public final boolean apply(ObjectMatrix1D m) { return Math.sqrt(m.aggregate(F.plus,F.square) / m.size()) &lt; threshold; }
     * 	    }
     * 	 );
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
    public ObjectMatrix2D viewSelection(ObjectMatrix1DProcedure condition) {
        IntArrayList matches = new IntArrayList();
        for (int i = 0; i < rows; i++) {
            if (condition.apply(viewRow(i)))
                matches.add(i);
        }

        matches.trimToSize();
        return viewSelection(matches.elements(), null); // take all columns
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
    protected abstract ObjectMatrix2D viewSelectionLike(int[] rowOffsets, int[] columnOffsets);

    /**
     * Sorts the matrix rows into ascending order, according to the <i>natural
     * ordering</i> of the matrix values in the given column. This sort is
     * guaranteed to be <i>stable</i>. For further information, see
     * {@link cern.colt.matrix.tobject.algo.ObjectSorting#sort(ObjectMatrix2D,int)}
     * . For more advanced sorting functionality, see
     * {@link cern.colt.matrix.tobject.algo.ObjectSorting}.
     * 
     * @param column
     * @return a new sorted vector (matrix) view.
     * @throws IndexOutOfBoundsException
     *             if <code>column &lt; 0 || column &gt;= columns()</code>.
     */
    public ObjectMatrix2D viewSorted(int column) {
        return cern.colt.matrix.tobject.algo.ObjectSorting.mergeSort.sort(this, column);
    }

    /**
     * Constructs and returns a new <i>stride view</i> which is a sub matrix
     * consisting of every i-th cell. More specifically, the view has
     * <code>this.rows()/rowStride</code> rows and
     * <code>this.columns()/columnStride</code> columns holding cells
     * <code>this.get(i*rowStride,j*columnStride)</code> for all
     * <code>i = 0..rows()/rowStride - 1, j = 0..columns()/columnStride - 1</code>.
     * The returned view is backed by this matrix, so changes in the returned
     * view are reflected in this matrix, and vice-versa.
     * 
     * @param rowStride
     *            the row step factor.
     * @param columnStride
     *            the column step factor.
     * @return a new view.
     * @throws IndexOutOfBoundsException
     *             if <code>rowStride&lt;=0 || columnStride&lt;=0</code>.
     */
    public ObjectMatrix2D viewStrides(int rowStride, int columnStride) {
        return (ObjectMatrix2D) (view().vStrides(rowStride, columnStride));
    }
}
