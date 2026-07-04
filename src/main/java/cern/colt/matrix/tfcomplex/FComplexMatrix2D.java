/*
Copyright (C) 1999 CERN - European Organization for Nuclear Research.
Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose 
is hereby granted without fee, provided that the above copyright notice appear in all copies and 
that both that copyright notice and this permission notice appear in supporting documentation. 
CERN makes no representations about the suitability of this software for any purpose. 
It is provided "as is" without expressed or implied warranty.
 */
package cern.colt.matrix.tfcomplex;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import cern.colt.list.tint.IntArrayList;
import cern.colt.matrix.AbstractMatrix2D;
import cern.colt.matrix.tfloat.FloatMatrix2D;
import cern.jet.math.tfcomplex.FComplex;
import edu.emory.mathcs.utils.pc.ConcurrencyUtils;

/**
 * Abstract base class for 2-d matrices holding <code>complex</code> elements.
 * 
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
 * @author Piotr Wendykier (piotr.wendykier@gmail.com)
 * 
 */
public abstract class FComplexMatrix2D extends AbstractMatrix2D {
    private static final long serialVersionUID = 1L;

    /**
     * Makes this class non instantiable, but still let's others inherit from
     * it.
     */
    protected FComplexMatrix2D() {
    }

    /**
     * Applies a function to each cell and aggregates the results.
     * 
     * @param aggr
     *            an aggregation function taking as first argument the current
     *            aggregation and as second argument the transformed current
     *            cell value.
     * @param f
     *            a function transforming the current cell value.
     * @return the aggregated measure.
     * @see cern.jet.math.tfcomplex.FComplexFunctions
     */
    public float[] aggregate(final cern.colt.function.tfcomplex.FComplexFComplexFComplexFunction aggr,
            final cern.colt.function.tfcomplex.FComplexFComplexFunction f) {
        float[] b = new float[2];
        if (size() == 0) {
            b[0] = Float.NaN;
            b[1] = Float.NaN;
            return b;
        }
        float[] a = null;
        int nthreads = ConcurrencyUtils.getNumberOfThreads();
        if ((nthreads > 1) && (size() >= ConcurrencyUtils.getThreadsBeginN_2D())) {
            nthreads = Math.min(nthreads, rows);
            Future<?>[] futures = new Future[nthreads];
            int k = rows / nthreads;
            for (int j = 0; j < nthreads; j++) {
                final int firstRow = j * k;
                final int lastRow = (j == nthreads - 1) ? rows : firstRow + k;
                futures[j] = ConcurrencyUtils.submit(new Callable<float[]>() {
                    public float[] call() throws Exception {
                        float[] a = f.apply(getQuick(firstRow, 0));
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
     * Applies a function to each corresponding cell of two matrices and
     * aggregates the results.
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
     * @see cern.jet.math.tfcomplex.FComplexFunctions
     */
    public float[] aggregate(final FComplexMatrix2D other,
            final cern.colt.function.tfcomplex.FComplexFComplexFComplexFunction aggr,
            final cern.colt.function.tfcomplex.FComplexFComplexFComplexFunction f) {
        checkShape(other);
        float[] b = new float[2];
        if (size() == 0) {
            b[0] = Float.NaN;
            b[1] = Float.NaN;
            return b;
        }
        float[] a = null;
        int nthreads = ConcurrencyUtils.getNumberOfThreads();
        if ((nthreads > 1) && (size() >= ConcurrencyUtils.getThreadsBeginN_2D())) {
            nthreads = Math.min(nthreads, rows);
            Future<?>[] futures = new Future[nthreads];
            int k = rows / nthreads;
            for (int j = 0; j < nthreads; j++) {
                final int firstRow = j * k;
                final int lastRow = (j == nthreads - 1) ? rows : firstRow + k;
                futures[j] = ConcurrencyUtils.submit(new Callable<float[]>() {

                    public float[] call() throws Exception {
                        float[] a = f.apply(getQuick(firstRow, 0), other.getQuick(firstRow, 0));
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
     * Assigns the result of a function to each cell;
     * 
     * @param f
     *            a function object taking as argument the current cell's value.
     * @return <code>this</code> (for convenience only).
     * @see cern.jet.math.tfcomplex.FComplexFunctions
     */
    public FComplexMatrix2D assign(final cern.colt.function.tfcomplex.FComplexFComplexFunction f) {
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
                                setQuick(r, c, f.apply(getQuick(r, c)));
                            }
                        }
                    }
                });
            }
            ConcurrencyUtils.waitForCompletion(futures);
        } else {

            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < columns; c++) {
                    setQuick(r, c, f.apply(getQuick(r, c)));
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
     * @see cern.jet.math.tfcomplex.FComplexFunctions
     */
    public FComplexMatrix2D assign(final cern.colt.function.tfcomplex.FComplexProcedure cond,
            final cern.colt.function.tfcomplex.FComplexFComplexFunction f) {
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
                        float[] elem;
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
            float[] elem;
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
     *            a value (re=value[0], im=value[1]).
     * @return <code>this</code> (for convenience only).
     * 
     */
    public FComplexMatrix2D assign(final cern.colt.function.tfcomplex.FComplexProcedure cond, final float[] value) {
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
                        float[] elem;
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
            float[] elem;
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
     * Assigns the result of a function to the real part of the receiver. The
     * imaginary part of the receiver is reset to zero.
     * 
     * @param f
     *            a function object taking as argument the current cell's value.
     * @return <code>this</code> (for convenience only).
     * @see cern.jet.math.tfcomplex.FComplexFunctions
     */
    public FComplexMatrix2D assign(final cern.colt.function.tfcomplex.FComplexRealFunction f) {
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
                                float re = f.apply(getQuick(r, c));
                                setQuick(r, c, re, 0);
                            }
                        }
                    }
                });
            }
            ConcurrencyUtils.waitForCompletion(futures);
        } else {
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < columns; c++) {
                    float re = f.apply(getQuick(r, c));
                    setQuick(r, c, re, 0);
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
    public FComplexMatrix2D assign(FComplexMatrix2D other) {
        if (other == this)
            return this;
        checkShape(other);
        final FComplexMatrix2D otherLoc;
        if (haveSharedCells(other)) {
            otherLoc = other.copy();
        } else {
            otherLoc = other;
        }
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
                                setQuick(r, c, otherLoc.getQuick(r, c));
                            }
                        }
                    }
                });
            }
            ConcurrencyUtils.waitForCompletion(futures);
        } else {
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < columns; c++) {
                    setQuick(r, c, otherLoc.getQuick(r, c));
                }
            }
        }
        return this;
    }

    /**
     * Assigns the result of a function to each cell.
     * 
     * @param y
     *            the secondary matrix to operate on.
     * @param f
     *            a function object taking as first argument the current cell's
     *            value of <code>this</code>, and as second argument the current
     *            cell's value of <code>y</code>,
     * @return <code>this</code> (for convenience only).
     * @throws IllegalArgumentException
     *             if
     *             <code>columns() != other.columns() || rows() != other.rows()</code>
     * @see cern.jet.math.tfcomplex.FComplexFunctions
     */
    public FComplexMatrix2D assign(final FComplexMatrix2D y,
            final cern.colt.function.tfcomplex.FComplexFComplexFComplexFunction f) {
        checkShape(y);
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
                                setQuick(r, c, f.apply(getQuick(r, c), y.getQuick(r, c)));
                            }
                        }
                    }
                });
            }
            ConcurrencyUtils.waitForCompletion(futures);
        } else {
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < columns; c++) {
                    setQuick(r, c, f.apply(getQuick(r, c), y.getQuick(r, c)));
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
     * @see cern.jet.math.tdouble.DoubleFunctions
     */
    public FComplexMatrix2D assign(final FComplexMatrix2D y,
            final cern.colt.function.tfcomplex.FComplexFComplexFComplexFunction function, IntArrayList rowList,
            IntArrayList columnList) {
        checkShape(y);
        final int size = rowList.size();
        final int[] rowElements = rowList.elements();
        final int[] columnElements = columnList.elements();
        int nthreads = ConcurrencyUtils.getNumberOfThreads();
        if ((nthreads > 1) && (size >= ConcurrencyUtils.getThreadsBeginN_2D())) {
            nthreads = Math.min(nthreads, rows);
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
     * Sets all cells to the state specified by <code>re</code> and <code>im</code>.
     * 
     * @param re
     *            the real part of the value to be filled into the cells.
     * @param im
     *            the imaginary part of the value to be filled into the cells.
     * 
     * @return <code>this</code> (for convenience only).
     */
    public FComplexMatrix2D assign(final float re, final float im) {
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
                                setQuick(r, c, re, im);
                            }
                        }
                    }
                });
            }
            ConcurrencyUtils.waitForCompletion(futures);
        } else {
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < columns; c++) {
                    setQuick(r, c, re, im);
                }
            }
        }
        return this;
    }

    /**
     * Sets all cells to the state specified by <code>values</code>. <code>values</code>
     * is required to have the form
     * <code>re = values[row*rowStride+column*columnStride]; im = values[row*rowStride+column*columnStride+1]</code>
     * and have exactly the same number of rows and columns as the receiver.
     * <p>
     * The values are copied. So subsequent changes in <code>values</code> are not
     * reflected in the matrix, and vice-versa.
     * 
     * @param values
     *            the values to be filled into the cells.
     * @return <code>this</code> (for convenience only).
     * @throws IllegalArgumentException
     *             if <code>values.length != rows()*2*columns()</code>.
     */
    public FComplexMatrix2D assign(final float[] values) {
        if (values.length != rows * 2 * columns)
            throw new IllegalArgumentException("Must have same length: length=" + values.length + "rows()*2*columns()="
                    + rows() * 2 * columns());
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
                        int idx = firstRow * columns * 2;
                        for (int r = firstRow; r < lastRow; r++) {
                            for (int c = 0; c < columns; c++) {
                                setQuick(r, c, values[idx], values[idx + 1]);
                                idx += 2;
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
                    setQuick(r, c, values[idx], values[idx + 1]);
                    idx += 2;
                }
            }
        }

        return this;
    }

    /**
     * Sets all cells to the state specified by <code>values</code>. <code>values</code>
     * is required to have the form
     * <code>re = values[row][2*column]; im = values[row][2*column+1]</code> and
     * have exactly the same number of rows and columns as the receiver.
     * <p>
     * The values are copied. So subsequent changes in <code>values</code> are not
     * reflected in the matrix, and vice-versa.
     * 
     * @param values
     *            the values to be filled into the cells.
     * @return <code>this</code> (for convenience only).
     * @throws IllegalArgumentException
     *             if
     *             <code>values.length != rows() || for any 0 &lt;= row &lt; rows(): values[row].length != 2*columns()</code>
     *             .
     */
    public FComplexMatrix2D assign(final float[][] values) {
        int nthreads = ConcurrencyUtils.getNumberOfThreads();
        if (values.length != rows)
            throw new IllegalArgumentException("Must have same number of rows: rows=" + values.length + "rows()="
                    + rows());
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
                            float[] currentRow = values[r];
                            if (currentRow.length != 2 * columns)
                                throw new IllegalArgumentException(
                                        "Must have same number of columns in every row: columns=" + currentRow.length
                                                + "2*columns()=" + 2 * columns());
                            for (int c = 0; c < columns; c++) {
                                setQuick(r, c, currentRow[2 * c], currentRow[2 * c + 1]);
                            }
                        }
                    }
                });
            }
            ConcurrencyUtils.waitForCompletion(futures);
        } else {
            for (int r = 0; r < rows; r++) {
                float[] currentRow = values[r];
                if (currentRow.length != 2 * columns)
                    throw new IllegalArgumentException("Must have same number of columns in every row: columns="
                            + currentRow.length + "2*columns()=" + 2 * columns());
                for (int c = 0; c < columns; c++) {
                    setQuick(r, c, currentRow[2 * c], currentRow[2 * c + 1]);
                }
            }
        }
        return this;
    }

    /**
     * Replaces imaginary part of the receiver with the values of another real
     * matrix. The real part of the receiver remains unchanged. Both matrices
     * must have the same size.
     * 
     * @param other
     *            the source matrix to copy from
     * @return <code>this</code> (for convenience only).
     * @throws IllegalArgumentException
     *             if <code>size() != other.size()</code>.
     */
    public FComplexMatrix2D assignImaginary(final FloatMatrix2D other) {
        checkShape(other);
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
                                float re = getQuick(r, c)[0];
                                float im = other.getQuick(r, c);
                                setQuick(r, c, re, im);
                            }
                        }
                    }
                });
            }
            ConcurrencyUtils.waitForCompletion(futures);
        } else {
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < columns; c++) {
                    float re = getQuick(r, c)[0];
                    float im = other.getQuick(r, c);
                    setQuick(r, c, re, im);
                }
            }
        }
        return this;
    }

    /**
     * Replaces real part of the receiver with the values of another real
     * matrix. The imaginary part of the receiver remains unchanged. Both
     * matrices must have the same size.
     * 
     * @param other
     *            the source matrix to copy from
     * @return <code>this</code> (for convenience only).
     * @throws IllegalArgumentException
     *             if <code>size() != other.size()</code>.
     */
    public FComplexMatrix2D assignReal(final FloatMatrix2D other) {
        checkShape(other);
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
                                float re = other.getQuick(r, c);
                                float im = getQuick(r, c)[1];
                                setQuick(r, c, re, im);
                            }
                        }
                    }
                });
            }
            ConcurrencyUtils.waitForCompletion(futures);
        } else {
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < columns; c++) {
                    float re = other.getQuick(r, c);
                    float im = getQuick(r, c)[1];
                    setQuick(r, c, re, im);
                }
            }
        }
        return this;
    }

    /**
     * Returns the number of cells having non-zero values; ignores tolerance.
     * 
     * @return the number of cells having non-zero values.
     */
    public int cardinality() {
        int cardinality = 0;
        int nthreads = ConcurrencyUtils.getNumberOfThreads();
        if ((nthreads > 1) && (size() >= ConcurrencyUtils.getThreadsBeginN_2D())) {
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
                        float[] tmp = new float[2];
                        for (int r = firstRow; r < lastRow; r++) {
                            for (int c = 0; c < columns; c++) {
                                tmp = getQuick(r, c);
                                if ((tmp[0] != 0.0) || (tmp[1] != 0.0))
                                    cardinality++;
                            }
                        }
                        return Integer.valueOf(cardinality);
                    }
                });
            }
            try {
                for (int j = 0; j < nthreads; j++) {
                    results[j] = (Integer) futures[j].get();
                }
                cardinality = results[0].intValue();
                for (int j = 1; j < nthreads; j++) {
                    cardinality += results[j].intValue();
                }
            } catch (ExecutionException ex) {
                ex.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            float[] tmp = new float[2];
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < columns; c++) {
                    tmp = getQuick(r, c);
                    if (tmp[0] != 0 || tmp[1] != 0)
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
    public FComplexMatrix2D copy() {
        return like().assign(this);
    }

    /**
     * Returns whether all cells are equal to the given value.
     * 
     * @param value
     *            the value to test against.
     * @return <code>true</code> if all cells are equal to the given value,
     *         <code>false</code> otherwise.
     */
    public boolean equals(float[] value) {
        return cern.colt.matrix.tfcomplex.algo.FComplexProperty.DEFAULT.equals(this, value);
    }

    /**
     * Compares this object against the specified object. The result is
     * <code>true</code> if and only if the argument is not <code>null</code>
     * and is at least a <code>FloatMatrix2D</code> object that has the same
     * number of columns and rows as the receiver and has exactly the same
     * values at the same coordinates.
     * 
     * @param obj
     *            the object to compare with.
     * @return <code>true</code> if the objects are the same; <code>false</code>
     *         otherwise.
     */

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof FComplexMatrix2D))
            return false;

        return cern.colt.matrix.tfcomplex.algo.FComplexProperty.DEFAULT.equals(this, (FComplexMatrix2D) obj);
    }

    /**
     * Assigns the result of a function to each <i>non-zero</i> cell. Use this
     * method for fast special-purpose iteration. If you want to modify another
     * matrix instead of <code>this</code> (i.e. work in read-only mode), simply
     * return the input value unchanged.
     * 
     * Parameters to function are as follows: <code>first==row</code>,
     * <code>second==column</code>, <code>third==nonZeroValue</code>.
     * 
     * @param function
     *            a function object taking as argument the current non-zero
     *            cell's row, column and value.
     * @return <code>this</code> (for convenience only).
     */
    public FComplexMatrix2D forEachNonZero(final cern.colt.function.tfcomplex.IntIntFComplexFunction function) {
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
                                float[] value = getQuick(r, c);
                                if (value[0] != 0 || value[1] != 0) {
                                    float[] v = function.apply(r, c, value);
                                    setQuick(r, c, v);
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
                    float[] value = getQuick(r, c);
                    if (value[0] != 0 || value[1] != 0) {
                        float[] v = function.apply(r, c, value);
                        setQuick(r, c, v);
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
    public float[] get(int row, int column) {
        if (column < 0 || column >= columns || row < 0 || row >= rows)
            throw new IndexOutOfBoundsException("row:" + row + ", column:" + column);
        return getQuick(row, column);
    }

    /**
     * Returns a new matrix that is a complex conjugate of this matrix. If
     * unconjugated complex transposition is needed, one should use viewDice()
     * method. This method creates a new object (not a view), so changes in the
     * returned matrix are NOT reflected in this matrix.
     * 
     * @return a complex conjugate matrix
     */
    public FComplexMatrix2D getConjugateTranspose() {
        final FComplexMatrix2D transpose = this.viewDice().copy();
        int nthreads = ConcurrencyUtils.getNumberOfThreads();
        if ((nthreads > 1) && (size() >= ConcurrencyUtils.getThreadsBeginN_2D())) {
            nthreads = Math.min(nthreads, columns);
            Future<?>[] futures = new Future[nthreads];
            int k = columns / nthreads;
            for (int j = 0; j < nthreads; j++) {
                final int firstRow = j * k;
                final int lastRow = (j == nthreads - 1) ? columns : firstRow + k;
                futures[j] = ConcurrencyUtils.submit(new Runnable() {
                    public void run() {
                        float[] tmp = new float[2];
                        for (int r = firstRow; r < lastRow; r++) {
                            for (int c = 0; c < rows; c++) {
                                tmp = transpose.getQuick(r, c);
                                tmp[1] = -tmp[1];
                                transpose.setQuick(r, c, tmp);
                            }
                        }
                    }
                });
            }
            ConcurrencyUtils.waitForCompletion(futures);
        } else {
            float[] tmp = new float[2];
            for (int r = 0; r < columns; r++) {
                for (int c = 0; c < rows; c++) {
                    tmp = transpose.getQuick(r, c);
                    tmp[1] = -tmp[1];
                    transpose.setQuick(r, c, tmp);
                }
            }
        }
        return transpose;
    }

    /**
     * Returns the elements of this matrix.
     * 
     * @return the elements
     */
    public abstract Object elements();

    /**
     * Returns the imaginary part of this matrix
     * 
     * @return the imaginary part
     */
    public abstract FloatMatrix2D getImaginaryPart();

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
     * 
     * @param rowList
     *            the list to be filled with row indexes, can have any size.
     * @param columnList
     *            the list to be filled with column indexes, can have any size.
     * @param valueList
     *            the list to be filled with values, can have any size.
     */
    public void getNonZeros(final IntArrayList rowList, final IntArrayList columnList,
            final ArrayList<float[]> valueList) {
        rowList.clear();
        columnList.clear();
        valueList.clear();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < columns; c++) {
                float[] value = getQuick(r, c);
                if (value[0] != 0 || value[1] != 0) {
                    rowList.add(r);
                    columnList.add(c);
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
    public abstract float[] getQuick(int row, int column);

    /**
     * Returns the real part of this matrix
     * 
     * @return the real part
     */
    public abstract FloatMatrix2D getRealPart();

    /**
     * Construct and returns a new empty matrix <i>of the same dynamic type</i>
     * as the receiver, having the same number of rows and columns. For example,
     * if the receiver is an instance of type <code>DenseComplexMatrix2D</code> the
     * new matrix must also be of type <code>DenseComplexMatrix2D</code>. In
     * general, the new matrix should have internal parametrization as similar
     * as possible.
     * 
     * @return a new empty matrix of the same dynamic type.
     */
    public FComplexMatrix2D like() {
        return like(rows, columns);
    }

    /**
     * Construct and returns a new empty matrix <i>of the same dynamic type</i>
     * as the receiver, having the specified number of rows and columns. For
     * example, if the receiver is an instance of type
     * <code>DenseComplexMatrix2D</code> the new matrix must also be of type
     * <code>DenseComplexMatrix2D</code>. In general, the new matrix should have
     * internal parametrization as similar as possible.
     * 
     * @param rows
     *            the number of rows the matrix shall have.
     * @param columns
     *            the number of columns the matrix shall have.
     * @return a new empty matrix of the same dynamic type.
     */
    public abstract FComplexMatrix2D like(int rows, int columns);

    /**
     * Construct and returns a new 1-d matrix <i>of the corresponding dynamic
     * type</i>, entirelly independent of the receiver. For example, if the
     * receiver is an instance of type <code>DenseComplexMatrix2D</code> the new
     * matrix must be of type <code>DenseComplexMatrix1D</code>.
     * 
     * @param size
     *            the number of cells the matrix shall have.
     * @return a new matrix of the corresponding dynamic type.
     */
    public abstract FComplexMatrix1D like1D(int size);

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
    public void set(int row, int column, float[] value) {
        if (column < 0 || column >= columns || row < 0 || row >= rows)
            throw new IndexOutOfBoundsException("row:" + row + ", column:" + column);
        setQuick(row, column, value);
    }

    /**
     * Sets the matrix cell at coordinate <code>[row,column]</code> to the specified
     * value.
     * 
     * @param row
     *            the index of the row-coordinate.
     * @param column
     *            the index of the column-coordinate.
     * @param re
     *            the real part of the value to be filled into the specified
     *            cell.
     * @param im
     *            the imaginary part of the value to be filled into the
     *            specified cell.
     * @throws IndexOutOfBoundsException
     *             if
     *             <code>column&lt;0 || column&gt;=columns() || row&lt;0 || row&gt;=rows()</code>
     */
    public void set(int row, int column, float re, float im) {
        if (column < 0 || column >= columns || row < 0 || row >= rows)
            throw new IndexOutOfBoundsException("row:" + row + ", column:" + column);
        setQuick(row, column, re, im);
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
     * @param re
     *            the real part of the value to be filled into the specified
     *            cell.
     * @param im
     *            the imaginary part of the value to be filled into the
     *            specified cell.
     * 
     */
    public abstract void setQuick(int row, int column, float re, float im);

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
    public abstract void setQuick(int row, int column, float[] value);

    /**
     * Constructs and returns a 2-dimensional array containing the cell values.
     * The returned array <code>values</code> has the form
     * <code>re = values[row][2*column]; im = values[row][2*column+1]</code> and has
     * the same number of rows and columns as the receiver.
     * <p>
     * The values are copied. So subsequent changes in <code>values</code> are not
     * reflected in the matrix, and vice-versa.
     * 
     * @return an array filled with the values of the cells.
     */
    public float[][] toArray() {
        final float[][] values = new float[rows][2 * columns];
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
                        float[] tmp;
                        for (int r = firstRow; r < lastRow; r++) {
                            for (int c = 0; c < columns; c++) {
                                tmp = getQuick(r, c);
                                values[r][2 * c] = tmp[0];
                                values[r][2 * c + 1] = tmp[1];
                            }
                        }
                    }
                });
            }
            ConcurrencyUtils.waitForCompletion(futures);
        } else {
            float[] tmp;
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < columns; c++) {
                    tmp = getQuick(r, c);
                    values[r][2 * c] = tmp[0];
                    values[r][2 * c + 1] = tmp[1];
                }
            }
        }
        return values;
    }

    /**
     * Returns a string representation using default formatting ("%.4f").
     * 
     * @return a string representation of the matrix.
     */

    public String toString() {
        return toString("%.4f");
    }

    /**
     * Returns a string representation using using given <code>format</code>
     * 
     * @param format
     * @return a string representation of the matrix.
     * 
     */
    public String toString(String format) {
        StringBuffer s = new StringBuffer(String.format("ComplexMatrix2D: %d rows, %d columns\n\n", rows, columns));
        float[] elem = new float[2];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < columns; c++) {
                elem = getQuick(r, c);
                if (elem[1] == 0) {
                    s.append(String.format(format + "\t", elem[0]));
                    continue;
                }
                if (elem[0] == 0) {
                    s.append(String.format(format + "i\t", elem[1]));
                    continue;
                }
                if (elem[1] < 0) {
                    s.append(String.format(format + " - " + format + "i\t", elem[0], -elem[1]));
                    continue;
                }
                s.append(String.format(format + " + " + format + "i\t", elem[0], elem[1]));
            }
            s.append("\n");
        }
        return s.toString();
    }

    /**
     * Returns a vector obtained by stacking the columns of this matrix on top
     * of one another.
     * 
     * @return a vector of columns of this matrix.
     */
    public abstract FComplexMatrix1D vectorize();

    /**
     * Constructs and returns a new <i>slice view</i> representing the rows of
     * the given column. The returned view is backed by this matrix, so changes
     * in the returned view are reflected in this matrix, and vice-versa. To
     * obtain a slice view on subranges, construct a sub-ranging view (
     * <code>viewPart(...)</code>), then apply this method to the sub-range view.
     * 
     * @param column
     *            the column to fix.
     * @return a new slice view.
     * @throws IndexOutOfBoundsException
     *             if <code>column &lt; 0 || column &gt;= columns()</code>.
     * @see #viewRow(int)
     */
    public FComplexMatrix1D viewColumn(int column) {
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
     * 
     * @return a new flip view.
     * @see #viewRowFlip()
     */
    public FComplexMatrix2D viewColumnFlip() {
        return (FComplexMatrix2D) (view().vColumnFlip());
    }

    /**
     * Constructs and returns a new <i>dice (transposition) view</i>; Swaps
     * axes; example: 3 x 4 matrix --&gt; 4 x 3 matrix. The view has both
     * dimensions exchanged; what used to be columns become rows, what used to
     * be rows become columns. This is a zero-copy transposition, taking O(1),
     * i.e. constant time. The returned view is backed by this matrix, so
     * changes in the returned view are reflected in this matrix, and
     * vice-versa. Use idioms like <code>result = viewDice(A).copy()</code> to
     * generate an independent transposed matrix.
     * 
     * @return a new dice view.
     */
    public FComplexMatrix2D viewDice() {
        return (FComplexMatrix2D) (view().vDice());
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
    public FComplexMatrix2D viewPart(int row, int column, int height, int width) {
        return (FComplexMatrix2D) (view().vPart(row, column, height, width));
    }

    /**
     * Constructs and returns a new <i>slice view</i> representing the columns
     * of the given row. The returned view is backed by this matrix, so changes
     * in the returned view are reflected in this matrix, and vice-versa. To
     * obtain a slice view on subranges, construct a sub-ranging view (
     * <code>viewPart(...)</code>), then apply this method to the sub-range view.
     * 
     * @param row
     *            the row to fix.
     * @return a new slice view.
     * @throws IndexOutOfBoundsException
     *             if <code>row &lt; 0 || row &gt;= rows()</code>.
     * @see #viewColumn(int)
     */
    public FComplexMatrix1D viewRow(int row) {
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
     * 
     * @return a new flip view.
     * @see #viewColumnFlip()
     */
    public FComplexMatrix2D viewRowFlip() {
        return (FComplexMatrix2D) (view().vRowFlip());
    }

    /**
     * Constructs and returns a new <i>selection view</i> that is a matrix
     * holding all <b>rows</b> matching the given condition. Applies the
     * condition to each row and takes only those row where
     * <code>condition.apply(viewRow(i))</code> yields <code>true</code>. To match
     * columns, use a dice view.
     * 
     * @param condition
     *            The condition to be matched.
     * @return the new view.
     */
    public FComplexMatrix2D viewSelection(FComplexMatrix1DProcedure condition) {
        IntArrayList matches = new IntArrayList();
        for (int i = 0; i < rows; i++) {
            if (condition.apply(viewRow(i)))
                matches.add(i);
        }
        matches.trimToSize();
        return viewSelection(matches.elements(), null); // take all columns
    }

    /**
     * Constructs and returns a new <i>selection view</i> that is a matrix
     * holding the indicated cells. There holds
     * <code>view.rows() == rowIndexes.length, view.columns() == columnIndexes.length</code>
     * and <code>view.get(i,j) == this.get(rowIndexes[i],columnIndexes[j])</code>.
     * Indexes can occur multiple times and can be in arbitrary order.
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
    public FComplexMatrix2D viewSelection(int[] rowIndexes, int[] columnIndexes) {
        // check for "all"
        if (rowIndexes == null) {
            rowIndexes = new int[rows];
            for (int i = 0; i < rows; i++)
                rowIndexes[i] = i;
        }
        if (columnIndexes == null) {
            columnIndexes = new int[columns];
            for (int i = 0; i < columns; i++)
                columnIndexes[i] = i;
        }

        checkRowIndexes(rowIndexes);
        checkColumnIndexes(columnIndexes);
        int[] rowOffsets = new int[rowIndexes.length];
        int[] columnOffsets = new int[columnIndexes.length];
        for (int i = 0; i < rowIndexes.length; i++) {
            rowOffsets[i] = _rowOffset(_rowRank(rowIndexes[i]));
        }
        for (int i = 0; i < columnIndexes.length; i++) {
            columnOffsets[i] = _columnOffset(_columnRank(columnIndexes[i]));
        }
        return viewSelectionLike(rowOffsets, columnOffsets);
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
    public FComplexMatrix2D viewStrides(int rowStride, int columnStride) {
        return (FComplexMatrix2D) (view().vStrides(rowStride, columnStride));
    }

    /**
     * Linear algebraic matrix-vector multiplication; <code>z = A * y</code>;
     * Equivalent to <code>return A.zMult(y,z,1,0);</code>
     * 
     * @param y
     *            the source vector.
     * @param z
     *            the vector where results are to be stored. Set this parameter
     *            to <code>null</code> to indicate that a new result vector shall be
     *            constructed.
     * @return z (for convenience only).
     */
    public FComplexMatrix1D zMult(FComplexMatrix1D y, FComplexMatrix1D z) {
        return zMult(y, z, new float[] { 1, 0 }, (z == null ? new float[] { 1, 0 } : new float[] { 0, 0 }), false);
    }

    /**
     * Linear algebraic matrix-vector multiplication;
     * <code>z = alpha * A * y + beta*z</code>. Where <code>A == this</code>. <br>
     * Note: Matrix shape conformance is checked <i>after</i> potential
     * transpositions.
     * 
     * @param y
     *            the source vector.
     * @param z
     *            the vector where results are to be stored. Set this parameter
     *            to <code>null</code> to indicate that a new result vector shall be
     *            constructed.
     * @param alpha
     * @param transposeA
     * @param beta
     * @return z (for convenience only).
     * 
     * @throws IllegalArgumentException
     *             if <code>A.columns() != y.size() || A.rows() &gt; z.size())</code>.
     */
    public FComplexMatrix1D zMult(final FComplexMatrix1D y, FComplexMatrix1D z, final float[] alpha,
            final float[] beta, boolean transposeA) {
        if (transposeA)
            return getConjugateTranspose().zMult(y, z, alpha, beta, false);
        final FComplexMatrix1D zz;
        if (z == null) {
            zz = y.like(this.rows);
        } else {
            zz = z;
        }
        if (columns != y.size() || rows > zz.size())
            throw new IllegalArgumentException("Incompatible args: " + toStringShort() + ", " + y.toStringShort()
                    + ", " + zz.toStringShort());
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
                        float[] s = new float[2];
                        for (int r = firstRow; r < lastRow; r++) {
                            s[0] = 0;
                            s[1] = 0;
                            for (int c = 0; c < columns; c++) {
                                s = FComplex.plus(s, FComplex.mult(getQuick(r, c), y.getQuick(c)));
                            }
                            zz.setQuick(r, FComplex.plus(FComplex.mult(s, alpha), FComplex.mult(zz.getQuick(r), beta)));
                        }
                    }
                });
            }
            ConcurrencyUtils.waitForCompletion(futures);
        } else {
            float[] s = new float[2];
            for (int r = 0; r < rows; r++) {
                s[0] = 0;
                s[1] = 0;
                for (int c = 0; c < columns; c++) {
                    s = FComplex.plus(s, FComplex.mult(getQuick(r, c), y.getQuick(c)));
                }
                zz.setQuick(r, FComplex.plus(FComplex.mult(s, alpha), FComplex.mult(zz.getQuick(r), beta)));
            }
        }
        return zz;
    }

    /**
     * Linear algebraic matrix-matrix multiplication; <code>C = A x B</code>;
     * Equivalent to <code>A.zMult(B,C,1,0,false,false)</code>.
     * 
     * @param B
     *            the second source matrix.
     * @param C
     *            the matrix where results are to be stored. Set this parameter
     *            to <code>null</code> to indicate that a new result matrix shall be
     *            constructed.
     * @return C (for convenience only).
     */
    public FComplexMatrix2D zMult(FComplexMatrix2D B, FComplexMatrix2D C) {
        return zMult(B, C, new float[] { 1, 0 }, (C == null ? new float[] { 1, 0 } : new float[] { 0, 0 }), false,
                false);
    }

    /**
     * Linear algebraic matrix-matrix multiplication;
     * <code>C = alpha * A x B + beta*C</code>. Matrix shapes:
     * <code>A(m x n), B(n x p), C(m x p)</code>. <br>
     * Note: Matrix shape conformance is checked <i>after</i> potential
     * transpositions.
     * 
     * @param B
     *            the second source matrix.
     * @param C
     *            the matrix where results are to be stored. Set this parameter
     *            to <code>null</code> to indicate that a new result matrix shall be
     *            constructed.
     * @param alpha
     * @param transposeB
     * @param beta
     * @param transposeA
     * @return C (for convenience only).
     * 
     * @throws IllegalArgumentException
     *             if <code>B.rows() != A.columns()</code>.
     * @throws IllegalArgumentException
     *             if
     *             <code>C.rows() != A.rows() || C.columns() != B.columns()</code>.
     * @throws IllegalArgumentException
     *             if <code>A == C || B == C</code>.
     */
    public FComplexMatrix2D zMult(final FComplexMatrix2D B, FComplexMatrix2D C, final float[] alpha,
            final float[] beta, boolean transposeA, boolean transposeB) {
        if (transposeA)
            return getConjugateTranspose().zMult(B, C, alpha, beta, false, transposeB);
        if (transposeB)
            return this.zMult(B.getConjugateTranspose(), C, alpha, beta, transposeA, false);
        final int m = rows;
        final int n = columns;
        final int p = B.columns;
        final FComplexMatrix2D CC;
        if (C == null) {
            CC = like(m, p);
        } else {
            CC = C;
        }
        if (B.rows != n)
            throw new IllegalArgumentException("Matrix2D inner dimensions must agree:" + toStringShort() + ", "
                    + B.toStringShort());
        if (CC.rows != m || CC.columns != p)
            throw new IllegalArgumentException("Incompatibe result matrix: " + toStringShort() + ", "
                    + B.toStringShort() + ", " + CC.toStringShort());
        if (this == CC || B == CC)
            throw new IllegalArgumentException("Matrices must not be identical");
        int nthreads = ConcurrencyUtils.getNumberOfThreads();
        if ((nthreads > 1) && (size() >= ConcurrencyUtils.getThreadsBeginN_2D())) {
            nthreads = Math.min(nthreads, p);
            Future<?>[] futures = new Future[nthreads];
            int k = p / nthreads;
            for (int j = 0; j < nthreads; j++) {
                final int firstIdx = j * k;
                final int lastIdx = (j == nthreads - 1) ? p : firstIdx + k;
                futures[j] = ConcurrencyUtils.submit(new Runnable() {
                    public void run() {
                        float[] s = new float[2];
                        for (int a = firstIdx; a < lastIdx; a++) {
                            for (int b = 0; b < m; b++) {
                                s[0] = 0;
                                s[1] = 0;
                                for (int c = 0; c < n; c++) {
                                    s = FComplex.plus(s, FComplex.mult(getQuick(b, c), B.getQuick(c, a)));
                                }
                                CC.setQuick(b, a, FComplex.plus(FComplex.mult(s, alpha), FComplex.mult(CC
                                        .getQuick(b, a), beta)));
                            }
                        }
                    }
                });
            }
            ConcurrencyUtils.waitForCompletion(futures);
        } else {
            float[] s = new float[2];
            for (int a = 0; a < p; a++) {
                for (int b = 0; b < m; b++) {
                    s[0] = 0;
                    s[1] = 0;
                    for (int c = 0; c < n; c++) {
                        s = FComplex.plus(s, FComplex.mult(getQuick(b, c), B.getQuick(c, a)));
                    }
                    CC.setQuick(b, a, FComplex.plus(FComplex.mult(s, alpha), FComplex.mult(CC.getQuick(b, a), beta)));
                }
            }
        }
        return CC;
    }

    /**
     * Returns the sum of all cells.
     * 
     * @return the sum.
     */
    public float[] zSum() {
        if (size() == 0)
            return new float[] { 0, 0 };
        return aggregate(cern.jet.math.tfcomplex.FComplexFunctions.plus,
                cern.jet.math.tfcomplex.FComplexFunctions.identity);
    }

    /**
     * Returns the content of this matrix if it is a wrapper; or <code>this</code>
     * otherwise. Override this method in wrappers.
     * 
     * @return <code>this</code>
     */
    protected FComplexMatrix2D getContent() {
        return this;
    }

    /**
     * Returns <code>true</code> if both matrices share at least one identical cell.
     * 
     * @param other
     *            matrix
     * @return <code>true</code> if both matrices share at least one identical cell.
     */
    protected boolean haveSharedCells(FComplexMatrix2D other) {
        if (other == null)
            return false;
        if (this == other)
            return true;
        return getContent().haveSharedCellsRaw(other.getContent());
    }

    /**
     * Always returns false
     * 
     * @param other
     *            matrix
     * @return false
     */
    protected boolean haveSharedCellsRaw(FComplexMatrix2D other) {
        return false;
    }

    /**
     * Construct and returns a new 1-d matrix <i>of the corresponding dynamic
     * type</i>, sharing the same cells. For example, if the receiver is an
     * instance of type <code>DenseComplexMatrix2D</code> the new matrix must be of
     * type <code>DenseComplexMatrix1D</code>.
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
    protected abstract FComplexMatrix1D like1D(int size, int zero, int stride);

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
    protected FComplexMatrix2D view() {
        return (FComplexMatrix2D) clone();
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
    protected abstract FComplexMatrix2D viewSelectionLike(int[] rowOffsets, int[] columnOffsets);
}
