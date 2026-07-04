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
import cern.colt.matrix.AbstractMatrix3D;
import cern.colt.matrix.tfloat.FloatMatrix3D;
import edu.emory.mathcs.utils.ConcurrencyUtils;

/**
 * Abstract base class for 3-d matrices holding <code>complex</code> elements.
 * <p>
 * A matrix has a number of slices, rows and columns, which are assigned upon
 * instance construction - The matrix's size is then
 * <code>slices()*rows()*columns()</code>. Elements are accessed via
 * <code>[slice,row,column]</code> coordinates. Legal coordinates range from
 * <code>[0,0,0]</code> to <code>[slices()-1,rows()-1,columns()-1]</code>. Any attempt
 * to access an element at a coordinate
 * <code>slice&lt;0 || slice&gt;=slices() || row&lt;0 || row&gt;=rows() || column&lt;0 || column&gt;=column()</code>
 * will throw an <code>IndexOutOfBoundsException</code>.
 * <p>
 * <b>Note</b> that this implementation is not synchronized.
 * 
 * 
 * @author Piotr Wendykier (piotr.wendykier@gmail.com)
 * 
 */
public abstract class FComplexMatrix3D extends AbstractMatrix3D {
    private static final long serialVersionUID = 1L;

    /**
     * Makes this class non instantiable, but still let's others inherit from
     * it.
     */
    protected FComplexMatrix3D() {
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
     * @see cern.jet.math.tfloat.FloatFunctions
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
        if ((nthreads > 1) && (size() >= ConcurrencyUtils.getThreadsBeginN_3D())) {
            nthreads = Math.min(nthreads, slices);
            Future<?>[] futures = new Future[nthreads];
            int k = slices / nthreads;
            for (int j = 0; j < nthreads; j++) {
                final int firstSlice = j * k;
                final int lastSlice = (j == nthreads - 1) ? slices : firstSlice + k;
                futures[j] = ConcurrencyUtils.submit(new Callable<float[]>() {

                    public float[] call() throws Exception {
                        float[] a = f.apply(getQuick(firstSlice, 0, 0));
                        int d = 1;
                        for (int s = firstSlice; s < lastSlice; s++) {
                            for (int r = 0; r < rows; r++) {
                                for (int c = d; c < columns; c++) {
                                    a = aggr.apply(a, f.apply(getQuick(s, r, c)));
                                }
                                d = 0;
                            }
                        }
                        return a;
                    }
                });
            }
            a = ConcurrencyUtils.waitForCompletion(futures, aggr);
        } else {
            a = f.apply(getQuick(0, 0, 0));
            int d = 1; // first cell already done
            for (int s = 0; s < slices; s++) {
                for (int r = 0; r < rows; r++) {
                    for (int c = d; c < columns; c++) {
                        a = aggr.apply(a, f.apply(getQuick(s, r, c)));
                    }
                    d = 0;
                }
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
     *             <code>slices() != other.slices() || rows() != other.rows() || columns() != other.columns()</code>
     * @see cern.jet.math.tfloat.FloatFunctions
     */
    public float[] aggregate(final FComplexMatrix3D other,
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
        if ((nthreads > 1) && (size() >= ConcurrencyUtils.getThreadsBeginN_3D())) {
            nthreads = Math.min(nthreads, slices);
            Future<?>[] futures = new Future[nthreads];
            int k = slices / nthreads;
            for (int j = 0; j < nthreads; j++) {
                final int firstSlice = j * k;
                final int lastSlice = (j == nthreads - 1) ? slices : firstSlice + k;
                futures[j] = ConcurrencyUtils.submit(new Callable<float[]>() {

                    public float[] call() throws Exception {
                        float[] a = f.apply(getQuick(firstSlice, 0, 0), other.getQuick(firstSlice, 0, 0));
                        int d = 1;
                        for (int s = firstSlice; s < lastSlice; s++) {
                            for (int r = 0; r < rows; r++) {
                                for (int c = d; c < columns; c++) {
                                    a = aggr.apply(a, f.apply(getQuick(s, r, c), other.getQuick(s, r, c)));
                                }
                                d = 0;
                            }
                        }
                        return a;
                    }
                });
            }
            a = ConcurrencyUtils.waitForCompletion(futures, aggr);
        } else {
            a = f.apply(getQuick(0, 0, 0), other.getQuick(0, 0, 0));
            int d = 1; // first cell already done
            for (int s = 0; s < slices; s++) {
                for (int r = 0; r < rows; r++) {
                    for (int c = d; c < columns; c++) {
                        a = aggr.apply(a, f.apply(getQuick(s, r, c), other.getQuick(s, r, c)));
                    }
                    d = 0;
                }
            }
        }
        return a;
    }

    /**
     * Assigns the result of a function to each cell.
     * 
     * @param function
     *            a function object taking as argument the current cell's value.
     * @return <code>this</code> (for convenience only).
     * @see cern.jet.math.tfcomplex.FComplexFunctions
     */
    public FComplexMatrix3D assign(final cern.colt.function.tfcomplex.FComplexFComplexFunction function) {
        int nthreads = ConcurrencyUtils.getNumberOfThreads();
        if ((nthreads > 1) && (size() >= ConcurrencyUtils.getThreadsBeginN_3D())) {
            nthreads = Math.min(nthreads, slices);
            Future<?>[] futures = new Future[nthreads];
            int k = slices / nthreads;
            for (int j = 0; j < nthreads; j++) {
                final int firstSlice = j * k;
                final int lastSlice = (j == nthreads - 1) ? slices : firstSlice + k;
                futures[j] = ConcurrencyUtils.submit(new Runnable() {
                    public void run() {
                        for (int s = firstSlice; s < lastSlice; s++) {
                            for (int r = 0; r < rows; r++) {
                                for (int c = 0; c < columns; c++) {
                                    setQuick(s, r, c, function.apply(getQuick(s, r, c)));
                                }
                            }
                        }
                    }
                });
            }
            ConcurrencyUtils.waitForCompletion(futures);

        } else {
            for (int s = 0; s < slices; s++) {
                for (int r = 0; r < rows; r++) {
                    for (int c = 0; c < columns; c++) {
                        setQuick(s, r, c, function.apply(getQuick(s, r, c)));
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
     * @param function
     *            a function object taking as argument the current cell's value.
     * @return <code>this</code> (for convenience only).
     * @see cern.jet.math.tfcomplex.FComplexFunctions
     */
    public FComplexMatrix3D assign(final cern.colt.function.tfcomplex.FComplexRealFunction function) {
        int nthreads = ConcurrencyUtils.getNumberOfThreads();
        if ((nthreads > 1) && (size() >= ConcurrencyUtils.getThreadsBeginN_3D())) {
            nthreads = Math.min(nthreads, slices);
            Future<?>[] futures = new Future[nthreads];
            int k = slices / nthreads;
            for (int j = 0; j < nthreads; j++) {
                final int firstSlice = j * k;
                final int lastSlice = (j == nthreads - 1) ? slices : firstSlice + k;
                futures[j] = ConcurrencyUtils.submit(new Runnable() {
                    public void run() {
                        float[] tmp = new float[2];
                        for (int s = firstSlice; s < lastSlice; s++) {
                            for (int r = 0; r < rows; r++) {
                                for (int c = 0; c < columns; c++) {
                                    tmp[0] = function.apply(getQuick(s, r, c));
                                    setQuick(s, r, c, tmp);
                                }
                            }
                        }
                    }
                });
            }
            ConcurrencyUtils.waitForCompletion(futures);

        } else {
            float[] tmp = new float[2];
            for (int s = 0; s < slices; s++) {
                for (int r = 0; r < rows; r++) {
                    for (int c = 0; c < columns; c++) {
                        tmp[0] = function.apply(getQuick(s, r, c));
                        setQuick(s, r, c, tmp);
                    }
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
    public FComplexMatrix3D assign(final cern.colt.function.tfcomplex.FComplexProcedure cond,
            final cern.colt.function.tfcomplex.FComplexFComplexFunction f) {
        int nthreads = ConcurrencyUtils.getNumberOfThreads();
        if ((nthreads > 1) && (size() >= ConcurrencyUtils.getThreadsBeginN_3D())) {
            nthreads = Math.min(nthreads, slices);
            Future<?>[] futures = new Future[nthreads];
            int k = slices / nthreads;
            for (int j = 0; j < nthreads; j++) {
                final int firstSlice = j * k;
                final int lastSlice = (j == nthreads - 1) ? slices : firstSlice + k;
                futures[j] = ConcurrencyUtils.submit(new Runnable() {

                    public void run() {
                        float[] elem;
                        for (int s = firstSlice; s < lastSlice; s++) {
                            for (int r = 0; r < rows; r++) {
                                for (int c = 0; c < columns; c++) {
                                    elem = getQuick(s, r, c);
                                    if (cond.apply(elem) == true) {
                                        setQuick(s, r, c, f.apply(elem));
                                    }
                                }
                            }
                        }
                    }
                });
            }
            ConcurrencyUtils.waitForCompletion(futures);
        } else {
            float[] elem;
            for (int s = 0; s < slices; s++) {
                for (int r = 0; r < rows; r++) {
                    for (int c = 0; c < columns; c++) {
                        elem = getQuick(s, r, c);
                        if (cond.apply(elem) == true) {
                            setQuick(s, r, c, f.apply(elem));
                        }
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
    public FComplexMatrix3D assign(final cern.colt.function.tfcomplex.FComplexProcedure cond, final float[] value) {
        int nthreads = ConcurrencyUtils.getNumberOfThreads();
        if ((nthreads > 1) && (size() >= ConcurrencyUtils.getThreadsBeginN_3D())) {
            nthreads = Math.min(nthreads, slices);
            Future<?>[] futures = new Future[nthreads];
            int k = slices / nthreads;
            for (int j = 0; j < nthreads; j++) {
                final int firstSlice = j * k;
                final int lastSlice = (j == nthreads - 1) ? slices : firstSlice + k;
                futures[j] = ConcurrencyUtils.submit(new Runnable() {

                    public void run() {
                        float[] elem;
                        for (int s = firstSlice; s < lastSlice; s++) {
                            for (int r = 0; r < rows; r++) {
                                for (int c = 0; c < columns; c++) {
                                    elem = getQuick(s, r, c);
                                    if (cond.apply(elem) == true) {
                                        setQuick(s, r, c, value);
                                    }
                                }
                            }
                        }
                    }
                });
            }
            ConcurrencyUtils.waitForCompletion(futures);
        } else {
            float[] elem;
            for (int s = 0; s < slices; s++) {
                for (int r = 0; r < rows; r++) {
                    for (int c = 0; c < columns; c++) {
                        elem = getQuick(s, r, c);
                        if (cond.apply(elem) == true) {
                            setQuick(s, r, c, value);
                        }
                    }
                }
            }
        }
        return this;
    }

    /**
     * Replaces all cell values of the receiver with the values of another
     * matrix. Both matrices must have the same number of slices, rows and
     * columns. If both matrices share the same cells (as is the case if they
     * are views derived from the same matrix) and intersect in an ambiguous
     * way, then replaces <i>as if</i> using an intermediate auxiliary deep copy
     * of <code>other</code>.
     * 
     * @param other
     *            the source matrix to copy from (may be identical to the
     *            receiver).
     * @return <code>this</code> (for convenience only).
     * @throws IllegalArgumentException
     *             if
     *             <code>slices() != other.slices() || rows() != other.rows() || columns() != other.columns()</code>
     */
    public FComplexMatrix3D assign(FComplexMatrix3D other) {
        if (other == this)
            return this;
        checkShape(other);
        final FComplexMatrix3D B;
        if (haveSharedCells(other)) {
            B = other.copy();
        } else {
            B = other;
        }
        int nthreads = ConcurrencyUtils.getNumberOfThreads();
        if ((nthreads > 1) && (size() >= ConcurrencyUtils.getThreadsBeginN_3D())) {
            nthreads = Math.min(nthreads, slices);
            Future<?>[] futures = new Future[nthreads];
            int k = slices / nthreads;
            for (int j = 0; j < nthreads; j++) {
                final int firstSlice = j * k;
                final int lastSlice = (j == nthreads - 1) ? slices : firstSlice + k;
                futures[j] = ConcurrencyUtils.submit(new Runnable() {
                    public void run() {
                        for (int s = firstSlice; s < lastSlice; s++) {
                            for (int r = 0; r < rows; r++) {
                                for (int c = 0; c < columns; c++) {
                                    setQuick(s, r, c, B.getQuick(s, r, c));
                                }
                            }
                        }
                    }
                });
            }
            ConcurrencyUtils.waitForCompletion(futures);
        } else {
            for (int s = 0; s < slices; s++) {
                for (int r = 0; r < rows; r++) {
                    for (int c = 0; c < columns; c++) {
                        setQuick(s, r, c, B.getQuick(s, r, c));
                    }
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
     * @param function
     *            a function object taking as first argument the current cell's
     *            value of <code>this</code>, and as second argument the current
     *            cell's value of <code>y</code>,
     * @return <code>this</code> (for convenience only).
     * @throws IllegalArgumentException
     *             if
     *             <code>slices() != other.slices() || rows() != other.rows() || columns() != other.columns()</code>
     * @see cern.jet.math.tfcomplex.FComplexFunctions
     */
    public FComplexMatrix3D assign(final FComplexMatrix3D y,
            final cern.colt.function.tfcomplex.FComplexFComplexFComplexFunction function) {
        checkShape(y);
        int nthreads = ConcurrencyUtils.getNumberOfThreads();
        if ((nthreads > 1) && (size() >= ConcurrencyUtils.getThreadsBeginN_3D())) {
            nthreads = Math.min(nthreads, slices);
            Future<?>[] futures = new Future[nthreads];
            int k = slices / nthreads;
            for (int j = 0; j < nthreads; j++) {
                final int firstSlice = j * k;
                final int lastSlice = (j == nthreads - 1) ? slices : firstSlice + k;
                futures[j] = ConcurrencyUtils.submit(new Runnable() {
                    public void run() {
                        for (int s = firstSlice; s < lastSlice; s++) {
                            for (int r = 0; r < rows; r++) {
                                for (int c = 0; c < columns; c++) {
                                    setQuick(s, r, c, function.apply(getQuick(s, r, c), y.getQuick(s, r, c)));
                                }
                            }
                        }
                    }
                });
            }
            ConcurrencyUtils.waitForCompletion(futures);
        } else {
            for (int s = 0; s < slices; s++) {
                for (int r = 0; r < rows; r++) {
                    for (int c = 0; c < columns; c++) {
                        setQuick(s, r, c, function.apply(getQuick(s, r, c), y.getQuick(s, r, c)));
                    }
                }
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
     *            the imagiary part of the value to be filled into the cells.
     * 
     * @return <code>this</code> (for convenience only).
     */
    public FComplexMatrix3D assign(final float re, final float im) {
        int nthreads = ConcurrencyUtils.getNumberOfThreads();
        if ((nthreads > 1) && (slices * rows * columns >= ConcurrencyUtils.getThreadsBeginN_3D())) {
            nthreads = Math.min(nthreads, slices);
            Future<?>[] futures = new Future[nthreads];
            int k = slices / nthreads;
            for (int j = 0; j < nthreads; j++) {
                final int firstSlice = j * k;
                final int lastSlice = (j == nthreads - 1) ? slices : firstSlice + k;
                futures[j] = ConcurrencyUtils.submit(new Runnable() {
                    public void run() {
                        for (int s = firstSlice; s < lastSlice; s++) {
                            for (int r = 0; r < rows; r++) {
                                for (int c = 0; c < columns; c++) {
                                    setQuick(s, r, c, re, im);
                                }
                            }
                        }
                    }
                });
            }
            ConcurrencyUtils.waitForCompletion(futures);

        } else {
            for (int s = 0; s < slices; s++) {
                for (int r = 0; r < rows; r++) {
                    for (int c = 0; c < columns; c++) {
                        setQuick(s, r, c, re, im);
                    }
                }
            }
        }
        return this;
    }

    /**
     * Sets all cells to the state specified by <code>values</code>. <code>values</code>
     * is required to have the form
     * <code>re = values[slice*silceStride+row*rowStride+2*column], 
     * im = values[slice*silceStride+row*rowStride+2*column+1]</code> and have
     * exactly the same number of slices, rows and columns as the receiver.
     * <p>
     * The values are copied. So subsequent changes in <code>values</code> are not
     * reflected in the matrix, and vice-versa.
     * 
     * @param values
     *            the values to be filled into the cells.
     * @return <code>this</code> (for convenience only).
     * @throws IllegalArgumentException
     *             if <code>values.length != slices()*rows()*2*columns()
     */
    public FComplexMatrix3D assign(final float[] values) {
        if (values.length != slices * rows * 2 * columns)
            throw new IllegalArgumentException("Must have same length: length=" + values.length
                    + "slices()*rows()*2*columns()=" + slices() * rows() * 2 * columns());
        int nthreads = ConcurrencyUtils.getNumberOfThreads();
        if ((nthreads > 1) && (slices * rows * columns >= ConcurrencyUtils.getThreadsBeginN_3D())) {
            nthreads = Math.min(nthreads, slices);
            Future<?>[] futures = new Future[nthreads];
            int k = slices / nthreads;
            for (int j = 0; j < nthreads; j++) {
                final int firstSlice = j * k;
                final int lastSlice = (j == nthreads - 1) ? slices : firstSlice + k;
                futures[j] = ConcurrencyUtils.submit(new Runnable() {
                    public void run() {
                        int idx = firstSlice * rows * columns * 2;
                        for (int s = firstSlice; s < lastSlice; s++) {
                            for (int r = 0; r < rows; r++) {
                                for (int c = 0; c < columns; c++) {
                                    setQuick(s, r, c, values[idx], values[idx + 1]);
                                    idx += 2;
                                }
                            }
                        }
                    }
                });
            }
            ConcurrencyUtils.waitForCompletion(futures);

        } else {
            int idx = 0;
            for (int s = 0; s < slices; s++) {
                for (int r = 0; r < rows; r++) {
                    for (int c = 0; c < columns; c++) {
                        setQuick(s, r, c, values[idx], values[idx + 1]);
                        idx += 2;
                    }
                }
            }
        }
        return this;
    }

    /**
     * Sets all cells to the state specified by <code>values</code>. <code>values</code>
     * is required to have the form
     * <code>re = values[slice][row][2*column], im = values[slice][row][2*column+1]</code>
     * and have exactly the same number of slices, rows and columns as the
     * receiver.
     * <p>
     * The values are copied. So subsequent changes in <code>values</code> are not
     * reflected in the matrix, and vice-versa.
     * 
     * @param values
     *            the values to be filled into the cells.
     * @return <code>this</code> (for convenience only).
     * @throws IllegalArgumentException
     *             if
     *             <code>values.length != slices() || for any 0 &lt;= slice &lt; slices(): values[slice].length != rows()</code>
     *             .
     * @throws IllegalArgumentException
     *             if
     *             <code>for any 0 &lt;= column &lt; columns(): values[slice][row].length != 2*columns()</code>
     *             .
     */
    public FComplexMatrix3D assign(final float[][][] values) {
        if (values.length != slices)
            throw new IllegalArgumentException("Must have same number of slices: slices=" + values.length + "slices()="
                    + slices());
        int nthreads = ConcurrencyUtils.getNumberOfThreads();
        if ((nthreads > 1) && (slices * rows * columns >= ConcurrencyUtils.getThreadsBeginN_3D())) {
            nthreads = Math.min(nthreads, slices);
            Future<?>[] futures = new Future[nthreads];
            int k = slices / nthreads;
            for (int j = 0; j < nthreads; j++) {
                final int firstSlice = j * k;
                final int lastSlice = (j == nthreads - 1) ? slices : firstSlice + k;
                futures[j] = ConcurrencyUtils.submit(new Runnable() {
                    public void run() {
                        for (int s = firstSlice; s < lastSlice; s++) {
                            float[][] currentSlice = values[s];
                            if (currentSlice.length != rows)
                                throw new IllegalArgumentException(
                                        "Must have same number of rows in every slice: rows=" + currentSlice.length
                                                + "rows()=" + rows());
                            for (int r = 0; r < rows; r++) {
                                float[] currentRow = currentSlice[r];
                                if (currentRow.length != 2 * columns)
                                    throw new IllegalArgumentException(
                                            "Must have same number of columns in every row: columns="
                                                    + currentRow.length + "2*columns()=" + 2 * columns());
                                for (int c = 0; c < columns; c++) {
                                    setQuick(s, r, c, currentRow[2 * c], currentRow[2 * c + 1]);
                                }
                            }
                        }
                    }
                });
            }
            ConcurrencyUtils.waitForCompletion(futures);

        } else {
            for (int s = 0; s < slices; s++) {
                float[][] currentSlice = values[s];
                if (currentSlice.length != rows)
                    throw new IllegalArgumentException("Must have same number of rows in every slice: rows="
                            + currentSlice.length + "rows()=" + rows());
                for (int r = 0; r < rows; r++) {
                    float[] currentRow = currentSlice[r];
                    if (currentRow.length != 2 * columns)
                        throw new IllegalArgumentException("Must have same number of columns in every row: columns="
                                + currentRow.length + "2*columns()=" + 2 * columns());
                    for (int c = 0; c < columns; c++) {
                        setQuick(s, r, c, currentRow[2 * c], currentRow[2 * c + 1]);
                    }
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
    public FComplexMatrix3D assignImaginary(final FloatMatrix3D other) {
        checkShape(other);
        int nthreads = ConcurrencyUtils.getNumberOfThreads();
        if ((nthreads > 1) && (size() >= ConcurrencyUtils.getThreadsBeginN_3D())) {
            nthreads = Math.min(nthreads, slices);
            Future<?>[] futures = new Future[nthreads];
            int k = slices / nthreads;
            for (int j = 0; j < nthreads; j++) {
                final int firstSlice = j * k;
                final int lastSlice = (j == nthreads - 1) ? slices : firstSlice + k;
                futures[j] = ConcurrencyUtils.submit(new Runnable() {
                    public void run() {
                        for (int s = firstSlice; s < lastSlice; s++) {
                            for (int r = 0; r < rows; r++) {
                                for (int c = 0; c < columns; c++) {
                                    float re = getQuick(s, r, c)[0];
                                    float im = other.getQuick(s, r, c);
                                    setQuick(s, r, c, re, im);
                                }
                            }
                        }
                    }
                });
            }
            ConcurrencyUtils.waitForCompletion(futures);
        } else {
            for (int s = 0; s < slices; s++) {
                for (int r = 0; r < rows; r++) {
                    for (int c = 0; c < columns; c++) {
                        float re = getQuick(s, r, c)[0];
                        float im = other.getQuick(s, r, c);
                        setQuick(s, r, c, re, im);
                    }
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
    public FComplexMatrix3D assignReal(final FloatMatrix3D other) {
        checkShape(other);
        int nthreads = ConcurrencyUtils.getNumberOfThreads();
        if ((nthreads > 1) && (size() >= ConcurrencyUtils.getThreadsBeginN_3D())) {
            nthreads = Math.min(nthreads, slices);
            Future<?>[] futures = new Future[nthreads];
            int k = slices / nthreads;
            for (int j = 0; j < nthreads; j++) {
                final int firstSlice = j * k;
                final int lastSlice = (j == nthreads - 1) ? slices : firstSlice + k;
                futures[j] = ConcurrencyUtils.submit(new Runnable() {
                    public void run() {
                        for (int s = firstSlice; s < lastSlice; s++) {
                            for (int r = 0; r < rows; r++) {
                                for (int c = 0; c < columns; c++) {
                                    float re = other.getQuick(s, r, c);
                                    float im = getQuick(s, r, c)[1];
                                    setQuick(s, r, c, re, im);
                                }
                            }
                        }
                    }
                });
            }
            ConcurrencyUtils.waitForCompletion(futures);
        } else {
            for (int s = 0; s < slices; s++) {
                for (int r = 0; r < rows; r++) {
                    for (int c = 0; c < columns; c++) {
                        float re = other.getQuick(s, r, c);
                        float im = getQuick(s, r, c)[1];
                        setQuick(s, r, c, re, im);
                    }
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
        if ((nthreads > 1) && (size() >= ConcurrencyUtils.getThreadsBeginN_3D())) {
            nthreads = Math.min(nthreads, slices);
            Future<?>[] futures = new Future[nthreads];
            Integer[] results = new Integer[nthreads];
            int k = slices / nthreads;
            for (int j = 0; j < nthreads; j++) {
                final int firstSlice = j * k;
                final int lastSlice = (j == nthreads - 1) ? slices : firstSlice + k;
                futures[j] = ConcurrencyUtils.submit(new Callable<Integer>() {
                    public Integer call() throws Exception {
                        int cardinality = 0;
                        float[] tmp = new float[2];
                        for (int s = firstSlice; s < lastSlice; s++) {
                            for (int r = 0; r < rows; r++) {
                                for (int c = 0; c < columns; c++) {
                                    tmp = getQuick(s, r, c);
                                    if ((tmp[0] != 0.0) || (tmp[1] != 0.0))
                                        cardinality++;
                                }
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
            for (int s = 0; s < slices; s++) {
                for (int r = 0; r < rows; r++) {
                    for (int c = 0; c < columns; c++) {
                        tmp = getQuick(s, r, c);
                        if (tmp[0] != 0 || tmp[1] != 0)
                            cardinality++;
                    }
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
    public FComplexMatrix3D copy() {
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
     * and is at least a <code>FloatMatrix3D</code> object that has the same
     * number of slices, rows and columns as the receiver and has exactly the
     * same values at the same coordinates.
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
        if (!(obj instanceof FComplexMatrix3D))
            return false;

        return cern.colt.matrix.tfcomplex.algo.FComplexProperty.DEFAULT.equals(this, (FComplexMatrix3D) obj);
    }

    /**
     * Returns the matrix cell value at coordinate <code>[slice,row,column]</code>.
     * 
     * @param slice
     *            the index of the slice-coordinate.
     * @param row
     *            the index of the row-coordinate.
     * @param column
     *            the index of the column-coordinate.
     * @return the value of the specified cell.
     * @throws IndexOutOfBoundsException
     *             if
     *             <code>slice&lt;0 || slice&gt;=slices() || row&lt;0 || row&gt;=rows() || column&lt;0 || column&gt;=column()</code>
     *             .
     */
    public float[] get(int slice, int row, int column) {
        if (slice < 0 || slice >= slices || row < 0 || row >= rows || column < 0 || column >= columns)
            throw new IndexOutOfBoundsException("slice:" + slice + ", row:" + row + ", column:" + column);
        return getQuick(slice, row, column);
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
    public abstract FloatMatrix3D getImaginaryPart();

    /**
     * Fills the coordinates and values of cells having non-zero values into the
     * specified lists. Fills into the lists, starting at index 0. After this
     * call returns the specified lists all have a new size, the number of
     * non-zero values.
     * <p>
     * In general, fill order is <i>unspecified</i>. This implementation fill
     * like:
     * <code>for (slice = 0..slices-1) for (row = 0..rows-1) for (column = 0..colums-1) do ... </code>
     * . However, subclasses are free to us any other order, even an order that
     * may change over time as cell values are changed. (Of course, result lists
     * indexes are guaranteed to correspond to the same cell).
     * 
     * @param sliceList
     *            the list to be filled with slice indexes, can have any size.
     * @param rowList
     *            the list to be filled with row indexes, can have any size.
     * @param columnList
     *            the list to be filled with column indexes, can have any size.
     * @param valueList
     *            the list to be filled with values, can have any size.
     */
    public void getNonZeros(final IntArrayList sliceList, final IntArrayList rowList, final IntArrayList columnList,
            final ArrayList<float[]> valueList) {
        sliceList.clear();
        rowList.clear();
        columnList.clear();
        valueList.clear();

        for (int s = 0; s < slices; s++) {
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < columns; c++) {
                    float[] value = getQuick(s, r, c);
                    if (value[0] != 0 || value[1] != 0) {
                        sliceList.add(s);
                        rowList.add(r);
                        columnList.add(c);
                        valueList.add(value);
                    }
                }
            }
        }

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
    public abstract float[] getQuick(int slice, int row, int column);

    /**
     * Returns the real part of this matrix
     * 
     * @return the real part
     */
    public abstract FloatMatrix3D getRealPart();

    /**
     * Construct and returns a new empty matrix <i>of the same dynamic type</i>
     * as the receiver, having the same number of slices, rows and columns. For
     * example, if the receiver is an instance of type
     * <code>DenseComplexMatrix3D</code> the new matrix must also be of type
     * <code>DenseComplexMatrix3D</code>. In general, the new matrix should have
     * internal parametrization as similar as possible.
     * 
     * @return a new empty matrix of the same dynamic type.
     */
    public FComplexMatrix3D like() {
        return like(slices, rows, columns);
    }

    /**
     * Construct and returns a new empty matrix <i>of the same dynamic type</i>
     * as the receiver, having the specified number of slices, rows and columns.
     * For example, if the receiver is an instance of type
     * <code>DenseComplexMatrix3D</code> the new matrix must also be of type
     * <code>DenseComplexMatrix3D</code>. In general, the new matrix should have
     * internal parametrization as similar as possible.
     * 
     * @param slices
     *            the number of slices the matrix shall have.
     * @param rows
     *            the number of rows the matrix shall have.
     * @param columns
     *            the number of columns the matrix shall have.
     * @return a new empty matrix of the same dynamic type.
     */
    public abstract FComplexMatrix3D like(int slices, int rows, int columns);

    /**
     * Sets the matrix cell at coordinate <code>[slice,row,column]</code> to the
     * specified value.
     * 
     * @param slice
     *            the index of the slice-coordinate.
     * @param row
     *            the index of the row-coordinate.
     * @param column
     *            the index of the column-coordinate.
     * @param value
     *            the value to be filled into the specified cell.
     * @throws IndexOutOfBoundsException
     *             if
     *             <code>row&lt;0 || row&gt;=rows() || slice&lt;0 || slice&gt;=slices() || column&lt;0 || column&gt;=column()</code>
     *             .
     */
    public void set(int slice, int row, int column, float[] value) {
        if (slice < 0 || slice >= slices || row < 0 || row >= rows || column < 0 || column >= columns)
            throw new IndexOutOfBoundsException("slice:" + slice + ", row:" + row + ", column:" + column);
        setQuick(slice, row, column, value);
    }

    /**
     * Sets the matrix cell at coordinate <code>[slice,row,column]</code> to the
     * specified value.
     * 
     * @param slice
     *            the index of the slice-coordinate.
     * @param row
     *            the index of the row-coordinate.
     * @param column
     *            the index of the column-coordinate. the index of the
     *            column-coordinate.
     * @param re
     *            the real part of the value to be filled into the specified
     *            cell.
     * @param im
     *            the imaginary part of the value to be filled into the
     *            specified cell.
     * 
     * @throws IndexOutOfBoundsException
     *             if
     *             <code>row&lt;0 || row&gt;=rows() || slice&lt;0 || slice&gt;=slices() || column&lt;0 || column&gt;=column()</code>
     *             .
     */
    public void set(int slice, int row, int column, float re, float im) {
        if (slice < 0 || slice >= slices || row < 0 || row >= rows || column < 0 || column >= columns)
            throw new IndexOutOfBoundsException("slice:" + slice + ", row:" + row + ", column:" + column);
        setQuick(slice, row, column, re, im);
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
     * @param re
     *            the real part of the value to be filled into the specified
     *            cell.
     * @param im
     *            the imaginary part of the value to be filled into the
     *            specified cell.
     * 
     */
    public abstract void setQuick(int slice, int row, int column, float re, float im);

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
    public abstract void setQuick(int slice, int row, int column, float[] value);

    /**
     * Constructs and returns a 3-dimensional array containing the cell values.
     * The returned array <code>values</code> has the form
     * <code>values[slice][row][column]</code> and has the same number of slices,
     * rows and columns as the receiver.
     * <p>
     * The values are copied. So subsequent changes in <code>values</code> are not
     * reflected in the matrix, and vice-versa.
     * 
     * @return an array filled with the values of the cells.
     */
    public float[][][] toArray() {
        final float[][][] values = new float[slices][rows][2 * columns];
        int nthreads = ConcurrencyUtils.getNumberOfThreads();
        if ((nthreads > 1) && (size() >= ConcurrencyUtils.getThreadsBeginN_3D())) {
            nthreads = Math.min(nthreads, slices);
            Future<?>[] futures = new Future[nthreads];
            int k = slices / nthreads;
            for (int j = 0; j < nthreads; j++) {
                final int firstSlice = j * k;
                final int lastSlice = (j == nthreads - 1) ? slices : firstSlice + k;
                futures[j] = ConcurrencyUtils.submit(new Runnable() {
                    public void run() {
                        float[] tmp = new float[2];
                        for (int s = firstSlice; s < lastSlice; s++) {
                            float[][] currentSlice = values[s];
                            for (int r = 0; r < rows; r++) {
                                float[] currentRow = currentSlice[r];
                                for (int c = 0; c < columns; c++) {
                                    tmp = getQuick(s, r, c);
                                    currentRow[2 * c] = tmp[0];
                                    currentRow[2 * c + 1] = tmp[1];
                                }
                            }
                        }
                    }
                });
            }
            ConcurrencyUtils.waitForCompletion(futures);
        } else {
            float[] tmp = new float[2];
            for (int s = 0; s < slices; s++) {
                float[][] currentSlice = values[s];
                for (int r = 0; r < rows; r++) {
                    float[] currentRow = currentSlice[r];
                    for (int c = 0; c < columns; c++) {
                        tmp = getQuick(s, r, c);
                        currentRow[2 * c] = tmp[0];
                        currentRow[2 * c + 1] = tmp[1];
                    }
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
        StringBuffer sb = new StringBuffer(String.format("ComplexMatrix3D: %d slices, %d rows, %d columns\n\n", slices,
                rows, columns));
        float[] elem = new float[2];
        for (int s = 0; s < slices; s++) {
            sb.append(String.format("(:,:,%d)=\n", s));
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < columns; c++) {
                    elem = getQuick(s, r, c);
                    if (elem[1] == 0) {
                        sb.append(String.format(format + "\t", elem[0]));
                        continue;
                    }
                    if (elem[0] == 0) {
                        sb.append(String.format(format + "i\t", elem[1]));
                        continue;
                    }
                    if (elem[1] < 0) {
                        sb.append(String.format(format + " - " + format + "i\t", elem[0], -elem[1]));
                        continue;
                    }
                    sb.append(String.format(format + " + " + format + "i\t", elem[0], elem[1]));
                }
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    /**
     * Returns a vector obtained by stacking the columns of each slice of the
     * matrix on top of one another.
     * 
     * @return a vector obtained by stacking the columns of each slice of the
     *         matrix on top of one another.
     */
    public abstract FComplexMatrix1D vectorize();

    /**
     * Constructs and returns a new 2-dimensional <i>slice view</i> representing
     * the slices and rows of the given column. The returned view is backed by
     * this matrix, so changes in the returned view are reflected in this
     * matrix, and vice-versa.
     * <p>
     * To obtain a slice view on subranges, construct a sub-ranging view (
     * <code>view().part(...)</code>), then apply this method to the sub-range view.
     * To obtain 1-dimensional views, apply this method, then apply another
     * slice view (methods <code>viewColumn</code>, <code>viewRow</code>) on the
     * intermediate 2-dimensional view. To obtain 1-dimensional views on
     * subranges, apply both steps.
     * 
     * @param column
     *            the index of the column to fix.
     * @return a new 2-dimensional slice view.
     * @throws IndexOutOfBoundsException
     *             if <code>column &lt; 0 || column &gt;= columns()</code>.
     * @see #viewSlice(int)
     * @see #viewRow(int)
     */
    public FComplexMatrix2D viewColumn(int column) {
        checkColumn(column);
        int sliceRows = this.slices;
        int sliceColumns = this.rows;

        int sliceRowZero = sliceZero;
        int sliceColumnZero = rowZero + _columnOffset(_columnRank(column));

        int sliceRowStride = this.sliceStride;
        int sliceColumnStride = this.rowStride;
        return like2D(sliceRows, sliceColumns, sliceRowZero, sliceColumnZero, sliceRowStride, sliceColumnStride);
    }

    /**
     * Constructs and returns a new <i>flip view</i> along the column axis. What
     * used to be column <code>0</code> is now column <code>columns()-1</code>, ...,
     * what used to be column <code>columns()-1</code> is now column <code>0</code>. The
     * returned view is backed by this matrix, so changes in the returned view
     * are reflected in this matrix, and vice-versa.
     * 
     * @return a new flip view.
     * @see #viewSliceFlip()
     * @see #viewRowFlip()
     */
    public FComplexMatrix3D viewColumnFlip() {
        return (FComplexMatrix3D) (view().vColumnFlip());
    }

    /**
     * Constructs and returns a new <i>dice view</i>; Swaps dimensions (axes);
     * Example: 3 x 4 x 5 matrix --&gt; 4 x 3 x 5 matrix. The view has dimensions
     * exchanged; what used to be one axis is now another, in all desired
     * permutations. The returned view is backed by this matrix, so changes in
     * the returned view are reflected in this matrix, and vice-versa.
     * 
     * @param axis0
     *            the axis that shall become axis 0 (legal values 0..2).
     * @param axis1
     *            the axis that shall become axis 1 (legal values 0..2).
     * @param axis2
     *            the axis that shall become axis 2 (legal values 0..2).
     * @return a new dice view.
     * @throws IllegalArgumentException
     *             if some of the parameters are equal or not in range 0..2.
     */
    public FComplexMatrix3D viewDice(int axis0, int axis1, int axis2) {
        return (FComplexMatrix3D) (view().vDice(axis0, axis1, axis2));
    }

    /**
     * Constructs and returns a new <i>sub-range view</i> that is a
     * <code>depth x height x width</code> sub matrix starting at
     * <code>[slice,row,column]</code>; Equivalent to
     * <code>view().part(slice,row,column,depth,height,width)</code>; Provided for
     * convenience only. The returned view is backed by this matrix, so changes
     * in the returned view are reflected in this matrix, and vice-versa.
     * 
     * @param slice
     *            The index of the slice-coordinate.
     * @param row
     *            The index of the row-coordinate.
     * @param column
     *            The index of the column-coordinate.
     * @param depth
     *            The depth of the box.
     * @param height
     *            The height of the box.
     * @param width
     *            The width of the box.
     * @throws IndexOutOfBoundsException
     *             if
     * 
     *             <code>slice&lt;0 || depth&lt;0 || slice+depth&gt;slices() || row&lt;0 || height&lt;0 || row+height&gt;rows() || column&lt;0 || width&lt;0 || column+width&gt;columns()</code>
     * @return the new view.
     * 
     */
    public FComplexMatrix3D viewPart(int slice, int row, int column, int depth, int height, int width) {
        return (FComplexMatrix3D) (view().vPart(slice, row, column, depth, height, width));
    }

    /**
     * Constructs and returns a new 2-dimensional <i>slice view</i> representing
     * the slices and columns of the given row. The returned view is backed by
     * this matrix, so changes in the returned view are reflected in this
     * matrix, and vice-versa.
     * <p>
     * To obtain a slice view on subranges, construct a sub-ranging view (
     * <code>view().part(...)</code>), then apply this method to the sub-range view.
     * To obtain 1-dimensional views, apply this method, then apply another
     * slice view (methods <code>viewColumn</code>, <code>viewRow</code>) on the
     * intermediate 2-dimensional view. To obtain 1-dimensional views on
     * subranges, apply both steps.
     * 
     * @param row
     *            the index of the row to fix.
     * @return a new 2-dimensional slice view.
     * @throws IndexOutOfBoundsException
     *             if <code>row &lt; 0 || row &gt;= row()</code>.
     * @see #viewSlice(int)
     * @see #viewColumn(int)
     */
    public FComplexMatrix2D viewRow(int row) {
        checkRow(row);
        int sliceRows = this.slices;
        int sliceColumns = this.columns;

        int sliceRowZero = sliceZero;
        int sliceColumnZero = columnZero + _rowOffset(_rowRank(row));

        int sliceRowStride = this.sliceStride;
        int sliceColumnStride = this.columnStride;
        return like2D(sliceRows, sliceColumns, sliceRowZero, sliceColumnZero, sliceRowStride, sliceColumnStride);
    }

    /**
     * Constructs and returns a new <i>flip view</i> along the row axis. What
     * used to be row <code>0</code> is now row <code>rows()-1</code>, ..., what used to
     * be row <code>rows()-1</code> is now row <code>0</code>. The returned view is
     * backed by this matrix, so changes in the returned view are reflected in
     * this matrix, and vice-versa.
     * 
     * @return a new flip view.
     * @see #viewSliceFlip()
     * @see #viewColumnFlip()
     */
    public FComplexMatrix3D viewRowFlip() {
        return (FComplexMatrix3D) (view().vRowFlip());
    }

    /**
     * Constructs and returns a new <i>selection view</i> that is a matrix
     * holding all <b>slices</b> matching the given condition. Applies the
     * condition to each slice and takes only those where
     * <code>condition.apply(viewSlice(i))</code> yields <code>true</code>. To match
     * rows or columns, use a dice view. The returned view is backed by this
     * matrix, so changes in the returned view are reflected in this matrix, and
     * vice-versa.
     * 
     * @param condition
     *            The condition to be matched.
     * @return the new view.
     */
    public FComplexMatrix3D viewSelection(FComplexMatrix2DProcedure condition) {
        IntArrayList matches = new IntArrayList();
        for (int i = 0; i < slices; i++) {
            if (condition.apply(viewSlice(i)))
                matches.add(i);
        }

        matches.trimToSize();
        return viewSelection(matches.elements(), null, null); // take all rows
        // and columns
    }

    /**
     * Constructs and returns a new <i>selection view</i> that is a matrix
     * holding the indicated cells. There holds
     * 
     * <code>view.slices() == sliceIndexes.length, view.rows() == rowIndexes.length, view.columns() == columnIndexes.length</code>
     * and
     * <code>view.get(k,i,j) == this.get(sliceIndexes[k],rowIndexes[i],columnIndexes[j])</code>
     * . Indexes can occur multiple times and can be in arbitrary order.
     * <p>
     * Note that modifying the index arguments after this call has returned has
     * no effect on the view. The returned view is backed by this matrix, so
     * changes in the returned view are reflected in this matrix, and
     * vice-versa.
     * 
     * @param sliceIndexes
     *            The slices of the cells that shall be visible in the new view.
     *            To indicate that <i>all</i> slices shall be visible, simply
     *            set this parameter to <code>null</code>.
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
     *             if <code>!(0 &lt;= sliceIndexes[i] &lt; slices())</code> for any
     *             <code>i=0..sliceIndexes.length()-1</code>.
     * @throws IndexOutOfBoundsException
     *             if <code>!(0 &lt;= rowIndexes[i] &lt; rows())</code> for any
     *             <code>i=0..rowIndexes.length()-1</code>.
     * @throws IndexOutOfBoundsException
     *             if <code>!(0 &lt;= columnIndexes[i] &lt; columns())</code> for any
     *             <code>i=0..columnIndexes.length()-1</code>.
     */
    public FComplexMatrix3D viewSelection(int[] sliceIndexes, int[] rowIndexes, int[] columnIndexes) {
        // check for "all"
        if (sliceIndexes == null) {
            sliceIndexes = new int[slices];
            for (int i = slices; --i >= 0;)
                sliceIndexes[i] = i;
        }
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

        checkSliceIndexes(sliceIndexes);
        checkRowIndexes(rowIndexes);
        checkColumnIndexes(columnIndexes);

        int[] sliceOffsets = new int[sliceIndexes.length];
        int[] rowOffsets = new int[rowIndexes.length];
        int[] columnOffsets = new int[columnIndexes.length];

        for (int i = 0; i < sliceIndexes.length; i++) {
            sliceOffsets[i] = _sliceOffset(_sliceRank(sliceIndexes[i]));
        }
        for (int i = 0; i < rowIndexes.length; i++) {
            rowOffsets[i] = _rowOffset(_rowRank(rowIndexes[i]));
        }
        for (int i = 0; i < columnIndexes.length; i++) {
            columnOffsets[i] = _columnOffset(_columnRank(columnIndexes[i]));
        }
        return viewSelectionLike(sliceOffsets, rowOffsets, columnOffsets);
    }

    /**
     * Constructs and returns a new 2-dimensional <i>slice view</i> representing
     * the rows and columns of the given slice. The returned view is backed by
     * this matrix, so changes in the returned view are reflected in this
     * matrix, and vice-versa.
     * <p>
     * To obtain a slice view on subranges, construct a sub-ranging view (
     * <code>view().part(...)</code>), then apply this method to the sub-range view.
     * To obtain 1-dimensional views, apply this method, then apply another
     * slice view (methods <code>viewColumn</code>, <code>viewRow</code>) on the
     * intermediate 2-dimensional view. To obtain 1-dimensional views on
     * subranges, apply both steps.
     * 
     * @param slice
     *            the index of the slice to fix.
     * @return a new 2-dimensional slice view.
     * @throws IndexOutOfBoundsException
     *             if <code>slice &lt; 0 || slice &gt;= slices()</code>.
     * @see #viewRow(int)
     * @see #viewColumn(int)
     */
    public FComplexMatrix2D viewSlice(int slice) {
        checkSlice(slice);
        int sliceRows = this.rows;
        int sliceColumns = this.columns;

        int sliceRowZero = rowZero;
        int sliceColumnZero = columnZero + _sliceOffset(_sliceRank(slice));

        int sliceRowStride = this.rowStride;
        int sliceColumnStride = this.columnStride;
        return like2D(sliceRows, sliceColumns, sliceRowZero, sliceColumnZero, sliceRowStride, sliceColumnStride);
    }

    /**
     * Constructs and returns a new <i>flip view</i> along the slice axis. What
     * used to be slice <code>0</code> is now slice <code>slices()-1</code>, ..., what
     * used to be slice <code>slices()-1</code> is now slice <code>0</code>. The
     * returned view is backed by this matrix, so changes in the returned view
     * are reflected in this matrix, and vice-versa.
     * 
     * @return a new flip view.
     * @see #viewRowFlip()
     * @see #viewColumnFlip()
     */
    public FComplexMatrix3D viewSliceFlip() {
        return (FComplexMatrix3D) (view().vSliceFlip());
    }

    /**
     * Constructs and returns a new <i>stride view</i> which is a sub matrix
     * consisting of every i-th cell. More specifically, the view has
     * <code>this.slices()/sliceStride</code> slices and
     * <code>this.rows()/rowStride</code> rows and
     * <code>this.columns()/columnStride</code> columns holding cells
     * <code>this.get(k*sliceStride,i*rowStride,j*columnStride)</code> for all
     * 
     * <code>k = 0..slices()/sliceStride - 1, i = 0..rows()/rowStride - 1, j = 0..columns()/columnStride - 1</code>
     * . The returned view is backed by this matrix, so changes in the returned
     * view are reflected in this matrix, and vice-versa.
     * 
     * @param sliceStride
     *            the slice step factor.
     * @param rowStride
     *            the row step factor.
     * @param columnStride
     *            the column step factor.
     * @return a new view.
     * @throws IndexOutOfBoundsException
     *             if <code>sliceStride $lt;=0 || rowStride $lt;=0 || columnStride $lt;=0</code>
     *             .
     */
    public FComplexMatrix3D viewStrides(int sliceStride, int rowStride, int columnStride) {
        return (FComplexMatrix3D) (view().vStrides(sliceStride, rowStride, columnStride));
    }

    /**
     * Returns the sum of all cells; <code>Sum( x[i,j,k] )</code>.
     * 
     * @return the sum.
     */
    public float[] zSum() {
        if (size() == 0)
            return new float[2];
        return aggregate(cern.jet.math.tfcomplex.FComplexFunctions.plus,
                cern.jet.math.tfcomplex.FComplexFunctions.identity);
    }

    /**
     * Returns the content of this matrix if it is a wrapper; or <code>this</code>
     * otherwise. Override this method in wrappers.
     * 
     * @return this
     */
    protected FComplexMatrix3D getContent() {
        return this;
    }

    /**
     * Returns <code>true</code> if both matrices share at least one identical cell.
     * 
     * @param other
     *            matrix
     * @return <code>true</code> if both matrices share at least one identical cell.
     */
    protected boolean haveSharedCells(FComplexMatrix3D other) {
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
    protected boolean haveSharedCellsRaw(FComplexMatrix3D other) {
        return false;
    }

    /**
     * Construct and returns a new 2-d matrix <i>of the corresponding dynamic
     * type</i>, sharing the same cells. For example, if the receiver is an
     * instance of type <code>DenseComplexMatrix3D</code> the new matrix must also
     * be of type <code>DenseComplexMatrix2D</code>.
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
    protected abstract FComplexMatrix2D like2D(int rows, int columns, int rowZero, int columnZero, int rowStride,
            int columnStride);

    /**
     * Constructs and returns a new view equal to the receiver. The view is a
     * shallow clone. Calls <code>clone()</code> and casts the result.
     * <p>
     * <b>Note that the view is not a deep copy.</b> The returned matrix is
     * backed by this matrix, so changes in the returned matrix are reflected in
     * this matrix, and vice-versa.
     * <p>
     * Use {@link #copy()} if you want to construct an independent deep copy
     * rather than a new view.
     * 
     * @return a new view of the receiver.
     */
    protected FComplexMatrix3D view() {
        return (FComplexMatrix3D) clone();
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
    protected abstract FComplexMatrix3D viewSelectionLike(int[] sliceOffsets, int[] rowOffsets, int[] columnOffsets);

    /**
     * Construct and returns a new 2-d matrix <i>of the corresponding dynamic
     * type</i>, sharing the same cells. For example, if the receiver is an
     * instance of type <code>DenseFComplexMatrix3D</code> the new matrix must also
     * be of type <code>DenseFComplexMatrix2D</code>, if the receiver is an instance
     * of type <code>SparseFComplexMatrix3D</code> the new matrix must also be of
     * type <code>SparseFComplexMatrix2D</code>, etc.
     * 
     * @param rows
     *            the number of rows the matrix shall have.
     * @param columns
     *            the number of columns the matrix shall have.
     * @return a new matrix of the corresponding dynamic type.
     */
    public abstract FComplexMatrix2D like2D(int rows, int columns);
}
