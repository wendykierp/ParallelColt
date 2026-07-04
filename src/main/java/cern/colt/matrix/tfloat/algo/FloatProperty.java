/*
Copyright (C) 1999 CERN - European Organization for Nuclear Research.
Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose 
is hereby granted without fee, provided that the above copyright notice appear in all copies and 
that both that copyright notice and this permission notice appear in supporting documentation. 
CERN makes no representations about the suitability of this software for any purpose. 
It is provided "as is" without expressed or implied warranty.
 */
package cern.colt.matrix.tfloat.algo;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import cern.colt.matrix.AbstractFormatter;
import cern.colt.matrix.tfloat.FloatMatrix1D;
import cern.colt.matrix.tfloat.FloatMatrix2D;
import cern.colt.matrix.tfloat.FloatMatrix3D;
import cern.colt.matrix.tfloat.impl.DenseColumnFloatMatrix2D;
import cern.colt.matrix.tfloat.impl.DenseFloatMatrix1D;
import cern.colt.matrix.tfloat.impl.DenseFloatMatrix2D;
import cern.colt.matrix.tfloat.impl.SparseCCFloatMatrix2D;
import cern.colt.matrix.tfloat.impl.SparseFloatMatrix1D;
import cern.colt.matrix.tfloat.impl.SparseRCFloatMatrix2D;
import cern.jet.math.tfloat.FloatFunctions;
import edu.emory.mathcs.utils.pc.ConcurrencyUtils;

/**
 * Tests matrices for linear algebraic properties (equality, tridiagonality,
 * symmetry, singularity, etc).
 * <p>
 * Except where explicitly indicated, all methods involving equality tests (
 * <code>==</code>) allow for numerical instability, to a degree specified upon
 * instance construction and returned by method {@link #tolerance()}. The public
 * static final variable <code>DEFAULT</code> represents a default Property object
 * with a tolerance of <code>1.0E-5</code>. The public static final variable
 * <code>ZERO</code> represents a Property object with a tolerance of <code>0.0</code>.
 * The public static final variable <code>SEVEN</code> represents a Property object
 * with a tolerance of <code>1.0E-7</code>. As long as you are happy with these
 * tolerances, there is no need to construct Property objects. Simply use idioms
 * like <code>Property.DEFAULT.equals(A,B)</code>,
 * <code>Property.ZERO.equals(A,B)</code>, <code>Property.SEVEN.equals(A,B)</code>.
 * <p>
 * To work with a different tolerance (e.g. <code>1.0E-2</code>) use the constructor
 * and/or method {@link #setTolerance(float)}. Note that the public static final
 * Property objects are immutable: Is is not possible to alter their tolerance.
 * Any attempt to do so will throw an Exception.
 * <p>
 * Note that this implementation is not synchronized.
 * <p>
 * Example: <code>equals(FloatMatrix2D A, FloatMatrix2D B)</code> is defined as
 * follows
 * <table>
 * <td class="PRE">
 * 
 * <pre>
 *  { some other tests not related to tolerance go here }
 *  float epsilon = tolerance();
 *  for (int row=rows; --row &gt;= 0;) {
 *     for (int column=columns; --column &gt;= 0;) {
 *        //if (!(A.getQuick(row,column) == B.getQuick(row,column))) return false;
 *        if (Math.abs(A.getQuick(row,column) - B.getQuick(row,column)) &gt; epsilon) return false;
 *     }
 *  }
 *  return true;
 * </pre>
 * 
 * </td>
 * </table>
 * Here are some example properties
 * <table border="1" cellspacing="0">
 * <tr align="left" valign="top">
 * <td valign="middle" align="left"><code>matrix</code></td>
 * <td> <code>4&nbsp;x&nbsp;4&nbsp;<br>
 0&nbsp;0&nbsp;0&nbsp;0<br>
 0&nbsp;0&nbsp;0&nbsp;0<br>
 0&nbsp;0&nbsp;0&nbsp;0<br>
 0&nbsp;0&nbsp;0&nbsp;0 </code></td>
 * <td><code>4&nbsp;x&nbsp;4<br>
 1&nbsp;0&nbsp;0&nbsp;0<br>
 0&nbsp;0&nbsp;0&nbsp;0<br>
 0&nbsp;0&nbsp;0&nbsp;0<br>
 0&nbsp;0&nbsp;0&nbsp;1 </code></td>
 * <td><code>4&nbsp;x&nbsp;4<br>
 1&nbsp;1&nbsp;0&nbsp;0<br>
 1&nbsp;1&nbsp;1&nbsp;0<br>
 0&nbsp;1&nbsp;1&nbsp;1<br>
 0&nbsp;0&nbsp;1&nbsp;1 </code></td>
 * <td><code> 4&nbsp;x&nbsp;4<br>
 0&nbsp;1&nbsp;1&nbsp;1<br>
 0&nbsp;1&nbsp;1&nbsp;1<br>
 0&nbsp;0&nbsp;0&nbsp;1<br>
 0&nbsp;0&nbsp;0&nbsp;1 </code></td>
 * <td><code> 4&nbsp;x&nbsp;4<br>
 0&nbsp;0&nbsp;0&nbsp;0<br>
 1&nbsp;1&nbsp;0&nbsp;0<br>
 1&nbsp;1&nbsp;0&nbsp;0<br>
 1&nbsp;1&nbsp;1&nbsp;1 </code></td>
 * <td><code>4&nbsp;x&nbsp;4<br>
 1&nbsp;1&nbsp;0&nbsp;0<br>
 0&nbsp;1&nbsp;1&nbsp;0<br>
 0&nbsp;1&nbsp;0&nbsp;1<br>
 1&nbsp;0&nbsp;1&nbsp;1 </code><code> </code></td>
 * <td><code>4&nbsp;x&nbsp;4<br>
 1&nbsp;1&nbsp;1&nbsp;0<br>
 0&nbsp;1&nbsp;0&nbsp;0<br>
 1&nbsp;1&nbsp;0&nbsp;1<br>
 0&nbsp;0&nbsp;1&nbsp;1 </code></td>
 * </tr>
 * <tr align="center" valign="middle">
 * <td><code>upperBandwidth</code></td>
 * <td><div align="center"><code>0</code></div></td>
 * <td><div align="center"><code>0</code></div></td>
 * <td><div align="center"><code>1</code></div></td>
 * <td><code>3</code></td>
 * <td align="center" valign="middle"><code>0</code></td>
 * <td align="center" valign="middle"><div align="center"><code>1</code></div></td>
 * <td align="center" valign="middle"><div align="center"><code>2</code></div></td>
 * </tr>
 * <tr align="center" valign="middle">
 * <td><code>lowerBandwidth</code></td>
 * <td><div align="center"><code>0</code></div></td>
 * <td><div align="center"><code>0</code></div></td>
 * <td><div align="center"><code>1</code></div></td>
 * <td><code>0</code></td>
 * <td align="center" valign="middle"><code>3</code></td>
 * <td align="center" valign="middle"><div align="center"><code>3</code></div></td>
 * <td align="center" valign="middle"><div align="center"><code>2</code></div></td>
 * </tr>
 * <tr align="center" valign="middle">
 * <td><code>semiBandwidth</code></td>
 * <td><div align="center"><code>1</code></div></td>
 * <td><div align="center"><code>1</code></div></td>
 * <td><div align="center"><code>2</code></div></td>
 * <td><code>4</code></td>
 * <td align="center" valign="middle"><code>4</code></td>
 * <td align="center" valign="middle"><div align="center"><code>4</code></div></td>
 * <td align="center" valign="middle"><div align="center"><code>3</code></div></td>
 * </tr>
 * <tr align="center" valign="middle">
 * <td><code>description</code></td>
 * <td><div align="center"><code>zero</code></div></td>
 * <td><div align="center"><code>diagonal</code></div></td>
 * <td><div align="center"><code>tridiagonal</code></div></td>
 * <td><code>upper triangular</code></td>
 * <td align="center" valign="middle"><code>lower triangular</code></td>
 * <td align="center" valign="middle"><div align="center"><code>unstructured</code>
 * </div></td>
 * <td align="center" valign="middle"><div align="center"><code>unstructured</code>
 * </div></td>
 * </tr>
 * </table>
 * 
 * @author wolfgang.hoschek@cern.ch
 * @version 1.1, 28/May/2000 (fixed strange bugs involving NaN, -inf, inf)
 * 
 * @author Piotr Wendykier (piotr.wendykier@gmail.com)
 */
public class FloatProperty extends cern.colt.PersistentObject {
    private static final long serialVersionUID = 1L;

    /**
     * The default Property object; currently has <code>tolerance()==1.0E-5</code>.
     */
    public static final FloatProperty DEFAULT = new FloatProperty(1.0E-5f);

    /**
     * A Property object with <code>tolerance()==0.0</code>.
     */
    public static final FloatProperty ZERO = new FloatProperty(0.0f);

    /**
     * A Property object with <code>tolerance()==1.0E-7</code>.
     */
    public static final FloatProperty SEVEN = new FloatProperty(1.0E-7f);

    protected float tolerance;

    /**
     * Not instantiable by no-arg constructor.
     */
    private FloatProperty() {
        this(1.0E-5f); // just to be on the safe side
    }

    /**
     * Constructs an instance with a tolerance of
     * <code>Math.abs(newTolerance)</code>.
     * @param newTolerance
     */
    public FloatProperty(float newTolerance) {
        tolerance = Math.abs(newTolerance);
    }

    /**
     * Returns a String with <code>length</code> blanks.
     * @param length
     * @return 
     */
    protected static String blanks(int length) {
        if (length < 0)
            length = 0;
        StringBuffer buf = new StringBuffer(length);
        for (int k = 0; k < length; k++) {
            buf.append(' ');
        }
        return buf.toString();
    }

    /**
     * Checks whether the given matrix <code>A</code> is <i>rectangular</i>.
     * 
     * @param A
     * @throws IllegalArgumentException
     *             if <code>A.rows() &lt; A.columns()</code>.
     */
    public void checkRectangular(FloatMatrix2D A) {
        if (A.rows() < A.columns()) {
            throw new IllegalArgumentException("Matrix must be rectangular: " + AbstractFormatter.shape(A));
        }
    }

    /**
     * Checks whether the given matrix <code>A</code> is <i>square</i>.
     * 
     * @param A
     * @throws IllegalArgumentException
     *             if <code>A.rows() != A.columns()</code>.
     */
    public void checkSquare(FloatMatrix2D A) {
        if (A.rows() != A.columns())
            throw new IllegalArgumentException("Matrix must be square: " + AbstractFormatter.shape(A));
    }

    public void checkDense(FloatMatrix2D A) {
        if (!(A instanceof DenseFloatMatrix2D) && !(A instanceof DenseColumnFloatMatrix2D))
            throw new IllegalArgumentException("Matrix must be dense");
    }

    public void checkDense(FloatMatrix1D A) {
        if (!(A instanceof DenseFloatMatrix1D))
            throw new IllegalArgumentException("Matrix must be dense");
    }

    public void checkSparse(FloatMatrix1D A) {
        if (!(A instanceof SparseFloatMatrix1D))
            throw new IllegalArgumentException("Matrix must be sparse");
    }

    public void checkSparse(FloatMatrix2D A) {
        //        if (!(A instanceof SparseFloatMatrix2D) && !(A instanceof RCFloatMatrix2D) && !(A instanceof RCMFloatMatrix2D)
        //                && !(A instanceof CCFloatMatrix2D) && !(A instanceof CCMFloatMatrix2D))
        if (!(A instanceof SparseCCFloatMatrix2D) && !(A instanceof SparseRCFloatMatrix2D))
            throw new IllegalArgumentException("Matrix must be sparse");
    }

    /**
     * Returns the matrix's fraction of non-zero cells;
     * <code>A.cardinality() / A.size()</code>.
     * @param A
     * @return 
     */
    public float density(FloatMatrix2D A) {
        return A.cardinality() / (float) A.size();
    }

    /**
     * Returns whether all cells of the given matrix <code>A</code> are equal to the
     * given value. The result is <code>true</code> if and only if
     * <code>A != null</code> and <code>! (Math.abs(value - A[i]) > tolerance())</code>
     * holds for all coordinates.
     * 
     * @param A
     *            the first matrix to compare.
     * @param value
     *            the value to compare against.
     * @return <code>true</code> if the matrix is equal to the value; <code>false</code>
     *         otherwise.
     */
    public boolean equals(final FloatMatrix1D A, final float value) {
        if (A == null)
            return false;
        int size = (int) A.size();
        final float epsilon = tolerance();
        boolean result = false;
        int nthreads = ConcurrencyUtils.getNumberOfThreads();
        if ((nthreads > 1) && (size >= ConcurrencyUtils.getThreadsBeginN_1D())) {
            nthreads = Math.min(nthreads, size);
            Future<?>[] futures = new Future[nthreads];
            Boolean[] results = new Boolean[nthreads];
            int k = size / nthreads;
            for (int j = 0; j < nthreads; j++) {
                final int firstIdx = j * k;
                final int lastIdx = (j == nthreads - 1) ? size : firstIdx + k;
                futures[j] = ConcurrencyUtils.submit(new Callable<Boolean>() {
                    public Boolean call() throws Exception {
                        for (int i = firstIdx; i < lastIdx; i++) {
                            float x = A.getQuick(i);
                            float diff = Math.abs(value - x);
                            if ((diff != diff) && ((value != value && x != x) || value == x))
                                diff = 0;
                            if (!(diff <= epsilon)) {
                                return false;
                            }
                        }
                        return true;
                    }
                });
            }
            try {
                for (int j = 0; j < nthreads; j++) {
                    results[j] = (Boolean) futures[j].get();
                }
                result = results[0].booleanValue();
                for (int j = 1; j < nthreads; j++) {
                    result = result && results[j].booleanValue();
                }
            } catch (ExecutionException ex) {
                ex.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return result;
        } else {
            for (int i = 0; i < size; i++) {
                float x = A.getQuick(i);
                float diff = Math.abs(value - x);
                if ((diff != diff) && ((value != value && x != x) || value == x))
                    diff = 0;
                if (!(diff <= epsilon)) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * Returns whether both given matrices <code>A</code> and <code>B</code> are equal.
     * The result is <code>true</code> if <code>A==B</code>. Otherwise, the result is
     * <code>true</code> if and only if both arguments are <code>!= null</code>, have
     * the same size and <code>! (Math.abs(A[i] - B[i]) > tolerance())</code> holds
     * for all indexes.
     * 
     * @param A
     *            the first matrix to compare.
     * @param B
     *            the second matrix to compare.
     * @return <code>true</code> if both matrices are equal; <code>false</code>
     *         otherwise.
     */
    public boolean equals(final FloatMatrix1D A, final FloatMatrix1D B) {
        if (A == B)
            return true;
        if (!(A != null && B != null))
            return false;
        int size = (int) A.size();
        if (size != B.size())
            return false;

        final float epsilon = tolerance();
        boolean result = false;
        int nthreads = ConcurrencyUtils.getNumberOfThreads();
        if ((nthreads > 1) && (size >= ConcurrencyUtils.getThreadsBeginN_1D())) {
            nthreads = Math.min(nthreads, size);
            Future<?>[] futures = new Future[nthreads];
            Boolean[] results = new Boolean[nthreads];
            int k = size / nthreads;
            for (int j = 0; j < nthreads; j++) {
                final int firstIdx = j * k;
                final int lastIdx = (j == nthreads - 1) ? size : firstIdx + k;
                futures[j] = ConcurrencyUtils.submit(new Callable<Boolean>() {
                    public Boolean call() throws Exception {
                        for (int i = firstIdx; i < lastIdx; i++) {
                            float x = A.getQuick(i);
                            float value = B.getQuick(i);
                            float diff = Math.abs(value - x);
                            if ((diff != diff) && ((value != value && x != x) || value == x))
                                diff = 0;
                            if (!(diff <= epsilon)) {
                                return false;
                            }
                        }
                        return true;
                    }
                });
            }
            try {
                for (int j = 0; j < nthreads; j++) {
                    results[j] = (Boolean) futures[j].get();
                }
                result = results[0].booleanValue();
                for (int j = 1; j < nthreads; j++) {
                    result = result && results[j].booleanValue();
                }
            } catch (ExecutionException ex) {
                ex.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return result;
        } else {
            for (int i = 0; i < size; i++) {
                float x = A.getQuick(i);
                float value = B.getQuick(i);
                float diff = Math.abs(value - x);
                if ((diff != diff) && ((value != value && x != x) || value == x))
                    diff = 0;
                if (!(diff <= epsilon)) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * Returns whether all cells of the given matrix <code>A</code> are equal to the
     * given value. The result is <code>true</code> if and only if
     * <code>A != null</code> and
     * <code>! (Math.abs(value - A[row,col]) > tolerance())</code> holds for all
     * coordinates.
     * 
     * @param A
     *            the first matrix to compare.
     * @param value
     *            the value to compare against.
     * @return <code>true</code> if the matrix is equal to the value; <code>false</code>
     *         otherwise.
     */
    public boolean equals(final FloatMatrix2D A, final float value) {
        if (A == null)
            return false;
        final int rows = A.rows();
        final int columns = A.columns();
        boolean result = false;
        final float epsilon = tolerance();
        int nthreads = ConcurrencyUtils.getNumberOfThreads();
        if ((nthreads > 1) && (A.size() >= ConcurrencyUtils.getThreadsBeginN_2D())) {
            nthreads = Math.min(nthreads, A.rows());
            Future<?>[] futures = new Future[nthreads];
            Boolean[] results = new Boolean[nthreads];
            int k = A.rows() / nthreads;
            for (int j = 0; j < nthreads; j++) {
                final int firstRow = j * k;
                final int lastRow = (j == nthreads - 1) ? A.rows() : firstRow + k;
                futures[j] = ConcurrencyUtils.submit(new Callable<Boolean>() {
                    public Boolean call() throws Exception {
                        for (int r = firstRow; r < lastRow; r++) {
                            for (int c = 0; c < columns; c++) {
                                float x = A.getQuick(r, c);
                                float diff = Math.abs(value - x);
                                if ((diff != diff) && ((value != value && x != x) || value == x))
                                    diff = 0;
                                if (!(diff <= epsilon)) {
                                    return false;
                                }
                            }
                        }
                        return true;
                    }
                });
            }
            try {
                for (int j = 0; j < nthreads; j++) {
                    results[j] = (Boolean) futures[j].get();
                }
                result = results[0].booleanValue();
                for (int j = 1; j < nthreads; j++) {
                    result = result && results[j].booleanValue();
                }
            } catch (ExecutionException ex) {
                ex.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return result;
        } else {
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < columns; c++) {
                    float x = A.getQuick(r, c);
                    float diff = Math.abs(value - x);
                    if ((diff != diff) && ((value != value && x != x) || value == x))
                        diff = 0;
                    if (!(diff <= epsilon)) {
                        return false;
                    }
                }
            }
            return true;
        }
    }

    /**
     * Returns whether both given matrices <code>A</code> and <code>B</code> are equal.
     * The result is <code>true</code> if <code>A==B</code>. Otherwise, the result is
     * <code>true</code> if and only if both arguments are <code>!= null</code>, have
     * the same number of columns and rows and
     * <code>! (Math.abs(A[row,col] - B[row,col]) > tolerance())</code> holds for
     * all coordinates.
     * 
     * @param A
     *            the first matrix to compare.
     * @param B
     *            the second matrix to compare.
     * @return <code>true</code> if both matrices are equal; <code>false</code>
     *         otherwise.
     */
    public boolean equals(final FloatMatrix2D A, final FloatMatrix2D B) {
        if (A == B)
            return true;
        if (!(A != null && B != null))
            return false;
        final int rows = A.rows();
        final int columns = A.columns();
        if (columns != B.columns() || rows != B.rows())
            return false;
        boolean result = false;
        final float epsilon = tolerance();
        int nthreads = ConcurrencyUtils.getNumberOfThreads();
        if ((nthreads > 1) && (A.size() >= ConcurrencyUtils.getThreadsBeginN_2D())) {
            nthreads = Math.min(nthreads, A.rows());
            Future<?>[] futures = new Future[nthreads];
            Boolean[] results = new Boolean[nthreads];
            int k = A.rows() / nthreads;
            for (int j = 0; j < nthreads; j++) {
                final int firstRow = j * k;
                final int lastRow = (j == nthreads - 1) ? A.rows() : firstRow + k;
                futures[j] = ConcurrencyUtils.submit(new Callable<Boolean>() {
                    public Boolean call() throws Exception {
                        for (int r = firstRow; r < lastRow; r++) {
                            for (int c = 0; c < columns; c++) {
                                float x = A.getQuick(r, c);
                                float value = B.getQuick(r, c);
                                float diff = Math.abs(value - x);
                                if ((diff != diff) && ((value != value && x != x) || value == x))
                                    diff = 0;
                                if (!(diff <= epsilon)) {
                                    return false;
                                }
                            }
                        }
                        return true;
                    }
                });
            }
            try {
                for (int j = 0; j < nthreads; j++) {
                    results[j] = (Boolean) futures[j].get();
                }
                result = results[0].booleanValue();
                for (int j = 1; j < nthreads; j++) {
                    result = result && results[j].booleanValue();
                }
            } catch (ExecutionException ex) {
                ex.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return result;
        } else {
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < columns; c++) {
                    float x = A.getQuick(r, c);
                    float value = B.getQuick(r, c);
                    float diff = Math.abs(value - x);
                    if ((diff != diff) && ((value != value && x != x) || value == x))
                        diff = 0;
                    if (!(diff <= epsilon)) {
                        return false;
                    }
                }
            }
            return true;
        }
    }

    /**
     * Returns whether all cells of the given matrix <code>A</code> are equal to the
     * given value. The result is <code>true</code> if and only if
     * <code>A != null</code> and
     * <code>! (Math.abs(value - A[slice,row,col]) > tolerance())</code> holds for
     * all coordinates.
     * 
     * @param A
     *            the first matrix to compare.
     * @param value
     *            the value to compare against.
     * @return <code>true</code> if the matrix is equal to the value; <code>false</code>
     *         otherwise.
     */
    public boolean equals(final FloatMatrix3D A, final float value) {
        if (A == null)
            return false;
        final int slices = A.slices();
        final int rows = A.rows();
        final int columns = A.columns();
        boolean result = false;
        final float epsilon = tolerance();
        int nthreads = ConcurrencyUtils.getNumberOfThreads();
        if ((nthreads > 1) && (A.size() >= ConcurrencyUtils.getThreadsBeginN_3D())) {
            nthreads = Math.min(nthreads, slices);
            Future<?>[] futures = new Future[nthreads];
            Boolean[] results = new Boolean[nthreads];
            int k = slices / nthreads;
            for (int j = 0; j < nthreads; j++) {
                final int firstSlice = j * k;
                final int lastSlice = (j == nthreads - 1) ? slices : firstSlice + k;
                futures[j] = ConcurrencyUtils.submit(new Callable<Boolean>() {
                    public Boolean call() throws Exception {
                        for (int s = firstSlice; s < lastSlice; s++) {
                            for (int r = 0; r < rows; r++) {
                                for (int c = 0; c < columns; c++) {
                                    float x = A.getQuick(s, r, c);
                                    float diff = Math.abs(value - x);
                                    if ((diff != diff) && ((value != value && x != x) || value == x))
                                        diff = 0;
                                    if (!(diff <= epsilon)) {
                                        return false;
                                    }
                                }
                            }
                        }
                        return true;
                    }
                });
            }
            try {
                for (int j = 0; j < nthreads; j++) {
                    results[j] = (Boolean) futures[j].get();
                }
                result = results[0].booleanValue();
                for (int j = 1; j < nthreads; j++) {
                    result = result && results[j].booleanValue();
                }
            } catch (ExecutionException ex) {
                ex.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return result;
        } else {
            for (int s = 0; s < slices; s++) {
                for (int r = 0; r < rows; r++) {
                    for (int c = 0; c < columns; c++) {
                        float x = A.getQuick(s, r, c);
                        float diff = Math.abs(value - x);
                        if ((diff != diff) && ((value != value && x != x) || value == x))
                            diff = 0;
                        if (!(diff <= epsilon)) {
                            return false;
                        }
                    }
                }
            }
            return true;
        }
    }

    /**
     * Returns whether both given matrices <code>A</code> and <code>B</code> are equal.
     * The result is <code>true</code> if <code>A==B</code>. Otherwise, the result is
     * <code>true</code> if and only if both arguments are <code>!= null</code>, have
     * the same number of columns, rows and slices, and
     * <code>! (Math.abs(A[slice,row,col] - B[slice,row,col]) > tolerance())</code>
     * holds for all coordinates.
     * 
     * @param A
     *            the first matrix to compare.
     * @param B
     *            the second matrix to compare.
     * @return <code>true</code> if both matrices are equal; <code>false</code>
     *         otherwise.
     */
    public boolean equals(final FloatMatrix3D A, final FloatMatrix3D B) {
        if (A == B)
            return true;
        if (!(A != null && B != null))
            return false;
        final int slices = A.slices();
        final int rows = A.rows();
        final int columns = A.columns();
        if (columns != B.columns() || rows != B.rows() || slices != B.slices())
            return false;
        boolean result = false;
        final float epsilon = tolerance();
        int nthreads = ConcurrencyUtils.getNumberOfThreads();
        if ((nthreads > 1) && (A.size() >= ConcurrencyUtils.getThreadsBeginN_3D())) {
            nthreads = Math.min(nthreads, slices);
            Future<?>[] futures = new Future[nthreads];
            Boolean[] results = new Boolean[nthreads];
            int k = slices / nthreads;
            for (int j = 0; j < nthreads; j++) {
                final int firstSlice = j * k;
                final int lastSlice = (j == nthreads - 1) ? slices : firstSlice + k;
                futures[j] = ConcurrencyUtils.submit(new Callable<Boolean>() {
                    public Boolean call() throws Exception {
                        for (int s = firstSlice; s < lastSlice; s++) {
                            for (int r = 0; r < rows; r++) {
                                for (int c = 0; c < columns; c++) {
                                    float x = A.getQuick(s, r, c);
                                    float value = B.getQuick(s, r, c);
                                    float diff = Math.abs(value - x);
                                    if ((diff != diff) && ((value != value && x != x) || value == x))
                                        diff = 0;
                                    if (!(diff <= epsilon)) {
                                        return false;
                                    }
                                }
                            }
                        }
                        return true;
                    }
                });
            }
            try {
                for (int j = 0; j < nthreads; j++) {
                    results[j] = (Boolean) futures[j].get();
                }
                result = results[0].booleanValue();
                for (int j = 1; j < nthreads; j++) {
                    result = result && results[j].booleanValue();
                }
            } catch (ExecutionException ex) {
                ex.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return result;
        } else {
            for (int s = 0; s < slices; s++) {
                for (int r = 0; r < rows; r++) {
                    for (int c = 0; c < columns; c++) {
                        float x = A.getQuick(s, r, c);
                        float value = B.getQuick(s, r, c);
                        float diff = Math.abs(value - x);
                        if ((diff != diff) && ((value != value && x != x) || value == x))
                            diff = 0;
                        if (!(diff <= epsilon)) {
                            return false;
                        }
                    }
                }
            }
            return true;
        }
    }

    /**
     * Modifies the given matrix square matrix <code>A</code> such that it is
     * diagonally dominant by row and column, hence non-singular, hence
     * invertible. For testing purposes only.
     * 
     * @param A
     *            the square matrix to modify.
     * @throws IllegalArgumentException
     *             if <code>!isSquare(A)</code>.
     */
    public void generateNonSingular(FloatMatrix2D A) {
        checkSquare(A);
        cern.jet.math.tfloat.FloatFunctions F = cern.jet.math.tfloat.FloatFunctions.functions;
        int min = Math.min(A.rows(), A.columns());
        for (int i = min; --i >= 0;) {
            A.setQuick(i, i, 0);
        }
        for (int i = min; --i >= 0;) {
            float rowSum = A.viewRow(i).aggregate(FloatFunctions.plus, FloatFunctions.abs);
            float colSum = A.viewColumn(i).aggregate(FloatFunctions.plus, FloatFunctions.abs);
            A.setQuick(i, i, Math.max(rowSum, colSum) + i + 1);
        }
    }

    /**
     * @param list
     * @param index
     * @return 
     */
    protected static String get(cern.colt.list.tobject.ObjectArrayList list, int index) {
        return ((String) list.get(index));
    }

    /**
     * A matrix <code>A</code> is <i>diagonal</i> if <code>A[i,j] == 0</code> whenever
     * <code>i != j</code>. Matrix may but need not be square.
     * @param A
     * @return 
     */
    public boolean isDiagonal(FloatMatrix2D A) {
        float epsilon = tolerance();
        int rows = A.rows();
        int columns = A.columns();
        for (int row = rows; --row >= 0;) {
            for (int column = columns; --column >= 0;) {
                if (row != column && !(Math.abs(A.getQuick(row, column)) <= epsilon))
                    return false;
            }
        }
        return true;
    }

    /**
     * A matrix <code>A</code> is <i>diagonally dominant by column</i> if the
     * absolute value of each diagonal element is larger than the sum of the
     * absolute values of the off-diagonal elements in the corresponding column.
     * 
     * <code>returns true if for all i: abs(A[i,i]) &gt; Sum(abs(A[j,i])); j != i.</code>
     * Matrix may but need not be square.
     * <p>
     * Note: Ignores tolerance.
     * @param A
     * @return 
     */
    public boolean isDiagonallyDominantByColumn(FloatMatrix2D A) {
        cern.jet.math.tfloat.FloatFunctions F = cern.jet.math.tfloat.FloatFunctions.functions;
        int min = Math.min(A.rows(), A.columns());
        for (int i = min; --i >= 0;) {
            float diag = Math.abs(A.getQuick(i, i));
            diag += diag;
            if (diag <= A.viewColumn(i).aggregate(FloatFunctions.plus, FloatFunctions.abs))
                return false;
        }
        return true;
    }

    /**
     * A matrix <code>A</code> is <i>diagonally dominant by row</i> if the absolute
     * value of each diagonal element is larger than the sum of the absolute
     * values of the off-diagonal elements in the corresponding row.
     * <code>returns true if for all i: abs(A[i,i]) &gt; Sum(abs(A[i,j])); j != i.</code>
     * Matrix may but need not be square.
     * <p>
     * Note: Ignores tolerance.
     * @param A
     * @return 
     */
    public boolean isDiagonallyDominantByRow(FloatMatrix2D A) {
        cern.jet.math.tfloat.FloatFunctions F = cern.jet.math.tfloat.FloatFunctions.functions;
        int min = Math.min(A.rows(), A.columns());
        for (int i = min; --i >= 0;) {
            float diag = Math.abs(A.getQuick(i, i));
            diag += diag;
            if (diag <= A.viewRow(i).aggregate(FloatFunctions.plus, FloatFunctions.abs))
                return false;
        }
        return true;
    }

    /**
     * A matrix <code>A</code> is an <i>identity</i> matrix if <code>A[i,i] == 1</code>
     * and all other cells are zero. Matrix may but need not be square.
     * @param A
     * @return 
     */
    public boolean isIdentity(FloatMatrix2D A) {
        float epsilon = tolerance();
        int rows = A.rows();
        int columns = A.columns();
        for (int row = rows; --row >= 0;) {
            for (int column = columns; --column >= 0;) {
                float v = A.getQuick(row, column);
                if (row == column) {
                    if (!(Math.abs(1 - v) < epsilon))
                        return false;
                } else if (!(Math.abs(v) <= epsilon))
                    return false;
            }
        }
        return true;
    }

    /**
     * A matrix <code>A</code> is <i>lower bidiagonal</i> if <code>A[i,j]==0</code>
     * unless <code>i==j || i==j+1</code>. Matrix may but need not be square.
     * @param A
     * @return 
     */
    public boolean isLowerBidiagonal(FloatMatrix2D A) {
        float epsilon = tolerance();
        int rows = A.rows();
        int columns = A.columns();
        for (int row = rows; --row >= 0;) {
            for (int column = columns; --column >= 0;) {
                if (!(row == column || row == column + 1)) {
                    if (!(Math.abs(A.getQuick(row, column)) <= epsilon))
                        return false;
                }
            }
        }
        return true;
    }

    /**
     * A matrix <code>A</code> is <i>lower triangular</i> if <code>A[i,j]==0</code>
     * whenever <code>i &lt; j</code>. Matrix may but need not be square.
     * @param A
     * @return 
     */
    public boolean isLowerTriangular(FloatMatrix2D A) {
        float epsilon = tolerance();
        int rows = A.rows();
        int columns = A.columns();
        for (int column = columns; --column >= 0;) {
            for (int row = Math.min(column, rows); --row >= 0;) {
                if (!(Math.abs(A.getQuick(row, column)) <= epsilon))
                    return false;
            }
        }
        return true;
    }

    /**
     * A matrix <code>A</code> is <i>non-negative</i> if <code>A[i,j] &gt;= 0</code>
     * holds for all cells.
     * <p>
     * Note: Ignores tolerance.
     * @param A
     * @return 
     */
    public boolean isNonNegative(FloatMatrix2D A) {
        int rows = A.rows();
        int columns = A.columns();
        for (int row = rows; --row >= 0;) {
            for (int column = columns; --column >= 0;) {
                if (!(A.getQuick(row, column) >= 0))
                    return false;
            }
        }
        return true;
    }

    /**
     * A square matrix <code>A</code> is <i>orthogonal</i> if
     * <code>A*transpose(A) = I</code>.
     * 
     * @param A
     * @return 
     * @throws IllegalArgumentException
     *             if <code>!isSquare(A)</code>.
     */
    public boolean isOrthogonal(FloatMatrix2D A) {
        checkSquare(A);
        return equals(A.zMult(A, null, 1, 0, false, true), cern.colt.matrix.tfloat.FloatFactory2D.dense.identity(A
                .rows()));
    }

    /**
     * A matrix <code>A</code> is <i>positive</i> if <code>A[i,j] &gt; 0</code> holds
     * for all cells.
     * <p>
     * Note: Ignores tolerance.
     * @param A
     * @return 
     */
    public boolean isPositive(FloatMatrix2D A) {
        int rows = A.rows();
        int columns = A.columns();
        for (int row = rows; --row >= 0;) {
            for (int column = columns; --column >= 0;) {
                if (!(A.getQuick(row, column) > 0))
                    return false;
            }
        }
        return true;
    }

    /**
     * A matrix <code>A</code> is <i>singular</i> if it has no inverse, that is, iff
     * <code>det(A)==0</code>.
     * @param A
     * @return 
     */
    public boolean isSingular(FloatMatrix2D A) {
        return !(Math.abs(DenseFloatAlgebra.DEFAULT.det(A)) >= tolerance());
    }

    /**
     * A square matrix <code>A</code> is <i>skew-symmetric</i> if
     * <code>A = -transpose(A)</code>, that is <code>A[i,j] == -A[j,i]</code>.
     * 
     * @param A
     * @return 
     * @throws IllegalArgumentException
     *             if <code>!isSquare(A)</code>.
     */
    public boolean isSkewSymmetric(FloatMatrix2D A) {
        checkSquare(A);
        float epsilon = tolerance();
        int rows = A.rows();
        for (int row = rows; --row >= 0;) {
            for (int column = rows; --column >= 0;) {
                if (!(Math.abs(A.getQuick(row, column) + A.getQuick(column, row)) <= epsilon))
                    return false;
            }
        }
        return true;
    }

    /**
     * A matrix <code>A</code> is <i>square</i> if it has the same number of rows
     * and columns.
     * @param A
     * @return 
     */
    public boolean isSquare(FloatMatrix2D A) {
        return A.rows() == A.columns();
    }

    /**
     * A matrix <code>A</code> is <i>strictly lower triangular</i> if
     * <code>A[i,j]==0</code> whenever <code>i &lt;= j</code>. Matrix may but need not
     * be square.
     * @param A
     * @return 
     */
    public boolean isStrictlyLowerTriangular(FloatMatrix2D A) {
        float epsilon = tolerance();
        int rows = A.rows();
        int columns = A.columns();
        for (int column = columns; --column >= 0;) {
            for (int row = Math.min(rows, column + 1); --row >= 0;) {
                if (!(Math.abs(A.getQuick(row, column)) <= epsilon))
                    return false;
            }
        }
        return true;
    }

    /**
     * A matrix <code>A</code> is <i>strictly triangular</i> if it is triangular and
     * its diagonal elements all equal 0. Matrix may but need not be square.
     * @param A
     * @return 
     */
    public boolean isStrictlyTriangular(FloatMatrix2D A) {
        if (!isTriangular(A))
            return false;

        float epsilon = tolerance();
        for (int i = Math.min(A.rows(), A.columns()); --i >= 0;) {
            if (!(Math.abs(A.getQuick(i, i)) <= epsilon))
                return false;
        }
        return true;
    }

    /**
     * A matrix <code>A</code> is <i>strictly upper triangular</i> if
     * <code>A[i,j]==0</code> whenever <code>i &gt;= j</code>. Matrix may but need not
     * be square.
     * @param A
     * @return 
     */
    public boolean isStrictlyUpperTriangular(FloatMatrix2D A) {
        float epsilon = tolerance();
        int rows = A.rows();
        int columns = A.columns();
        for (int column = columns; --column >= 0;) {
            for (int row = rows; --row >= column;) {
                if (!(Math.abs(A.getQuick(row, column)) <= epsilon))
                    return false;
            }
        }
        return true;
    }

    /**
     * A matrix <code>A</code> is <i>symmetric</i> if <code>A = tranpose(A)</code>, that
     * is <code>A[i,j] == A[j,i]</code>.
     * 
     * @param A
     * @return 
     * @throws IllegalArgumentException
     *             if <code>!isSquare(A)</code>.
     */
    public boolean isSymmetric(FloatMatrix2D A) {
        checkSquare(A);
        return equals(A, A.viewDice());
    }

    /**
     * A matrix <code>A</code> is <i>triangular</i> iff it is either upper or lower
     * triangular. Matrix may but need not be square.
     * @param A
     * @return 
     */
    public boolean isTriangular(FloatMatrix2D A) {
        return isLowerTriangular(A) || isUpperTriangular(A);
    }

    /**
     * A matrix <code>A</code> is <i>tridiagonal</i> if <code>A[i,j]==0</code> whenever
     * <code>Math.abs(i-j) > 1</code>. Matrix may but need not be square.
     * @param A
     * @return 
     */
    public boolean isTridiagonal(FloatMatrix2D A) {
        float epsilon = tolerance();
        int rows = A.rows();
        int columns = A.columns();
        for (int row = rows; --row >= 0;) {
            for (int column = columns; --column >= 0;) {
                if (Math.abs(row - column) > 1) {
                    if (!(Math.abs(A.getQuick(row, column)) <= epsilon))
                        return false;
                }
            }
        }
        return true;
    }

    /**
     * A matrix <code>A</code> is <i>unit triangular</i> if it is triangular and its
     * diagonal elements all equal 1. Matrix may but need not be square.
     * @param A
     * @return 
     */
    public boolean isUnitTriangular(FloatMatrix2D A) {
        if (!isTriangular(A))
            return false;

        float epsilon = tolerance();
        for (int i = Math.min(A.rows(), A.columns()); --i >= 0;) {
            if (!(Math.abs(1 - A.getQuick(i, i)) <= epsilon))
                return false;
        }
        return true;
    }

    /**
     * A matrix <code>A</code> is <i>upper bidiagonal</i> if <code>A[i,j]==0</code>
     * unless <code>i==j || i==j-1</code>. Matrix may but need not be square.
     * @param A
     * @return 
     */
    public boolean isUpperBidiagonal(FloatMatrix2D A) {
        float epsilon = tolerance();
        int rows = A.rows();
        int columns = A.columns();
        for (int row = rows; --row >= 0;) {
            for (int column = columns; --column >= 0;) {
                if (!(row == column || row == column - 1)) {
                    if (!(Math.abs(A.getQuick(row, column)) <= epsilon))
                        return false;
                }
            }
        }
        return true;
    }

    /**
     * A matrix <code>A</code> is <i>upper triangular</i> if <code>A[i,j]==0</code>
     * whenever <code>i &gt; j</code>. Matrix may but need not be square.
     * @param A
     * @return 
     */
    public boolean isUpperTriangular(FloatMatrix2D A) {
        float epsilon = tolerance();
        int rows = A.rows();
        int columns = A.columns();
        for (int column = columns; --column >= 0;) {
            for (int row = rows; --row > column;) {
                if (!(Math.abs(A.getQuick(row, column)) <= epsilon))
                    return false;
            }
        }
        return true;
    }

    /**
     * A matrix <code>A</code> is <i>zero</i> if all its cells are zero.
     * @param A
     * @return 
     */
    public boolean isZero(FloatMatrix2D A) {
        return equals(A, 0);
    }

    /**
     * The <i>lower bandwidth</i> of a square matrix <code>A</code> is the maximum
     * <code>i-j</code> for which <code>A[i,j]</code> is nonzero and <code>i &gt; j</code>.
     * A <i>banded</i> matrix has a "band" about the diagonal. Diagonal,
     * tridiagonal and triangular matrices are special cases.
     * 
     * @param A
     *            the square matrix to analyze.
     * @return the lower bandwith.
     * @throws IllegalArgumentException
     *             if <code>!isSquare(A)</code>.
     * @see #semiBandwidth(FloatMatrix2D)
     * @see #upperBandwidth(FloatMatrix2D)
     */
    public int lowerBandwidth(FloatMatrix2D A) {
        checkSquare(A);
        float epsilon = tolerance();
        int rows = A.rows();

        for (int k = rows; --k >= 0;) {
            for (int i = rows - k; --i >= 0;) {
                int j = i + k;
                if (!(Math.abs(A.getQuick(j, i)) <= epsilon))
                    return k;
            }
        }
        return 0;
    }

    /**
     * Returns the <i>semi-bandwidth</i> of the given square matrix <code>A</code>.
     * A <i>banded</i> matrix has a "band" about the diagonal. It is a matrix
     * with all cells equal to zero, with the possible exception of the cells
     * along the diagonal line, the <code>k</code> diagonal lines above the
     * diagonal, and the <code>k</code> diagonal lines below the diagonal. The
     * <i>semi-bandwith l</i> is the number <code>k+1</code>. The <i>bandwidth p</i>
     * is the number <code>2*k + 1</code>. For example, a tridiagonal matrix
     * corresponds to <code>k=1, l=2, p=3</code>, a diagonal or zero matrix
     * corresponds to <code>k=0, l=1, p=1</code>,
     * <p>
     * The <i>upper bandwidth</i> is the maximum <code>j-i</code> for which
     * <code>A[i,j]</code> is nonzero and <code>j &gt; i</code>. The <i>lower
     * bandwidth</i> is the maximum <code>i-j</code> for which <code>A[i,j]</code> is
     * nonzero and <code>i &gt; j</code>. Diagonal, tridiagonal and triangular
     * matrices are special cases.
     * <p>
     * Examples:
     * <table border="1" cellspacing="0">
     * <tr align="left" valign="top">
     * <td valign="middle" align="left"><code>matrix</code></td>
     * <td> <code>4&nbsp;x&nbsp;4&nbsp;<br>
     0&nbsp;0&nbsp;0&nbsp;0<br>
     0&nbsp;0&nbsp;0&nbsp;0<br>
     0&nbsp;0&nbsp;0&nbsp;0<br>
     0&nbsp;0&nbsp;0&nbsp;0 </code></td>
     * <td><code>4&nbsp;x&nbsp;4<br>
     1&nbsp;0&nbsp;0&nbsp;0<br>
     0&nbsp;0&nbsp;0&nbsp;0<br>
     0&nbsp;0&nbsp;0&nbsp;0<br>
     0&nbsp;0&nbsp;0&nbsp;1 </code></td>
     * <td><code>4&nbsp;x&nbsp;4<br>
     1&nbsp;1&nbsp;0&nbsp;0<br>
     1&nbsp;1&nbsp;1&nbsp;0<br>
     0&nbsp;1&nbsp;1&nbsp;1<br>
     0&nbsp;0&nbsp;1&nbsp;1 </code></td>
     * <td><code> 4&nbsp;x&nbsp;4<br>
     0&nbsp;1&nbsp;1&nbsp;1<br>
     0&nbsp;1&nbsp;1&nbsp;1<br>
     0&nbsp;0&nbsp;0&nbsp;1<br>
     0&nbsp;0&nbsp;0&nbsp;1 </code></td>
     * <td><code> 4&nbsp;x&nbsp;4<br>
     0&nbsp;0&nbsp;0&nbsp;0<br>
     1&nbsp;1&nbsp;0&nbsp;0<br>
     1&nbsp;1&nbsp;0&nbsp;0<br>
     1&nbsp;1&nbsp;1&nbsp;1 </code></td>
     * <td><code>4&nbsp;x&nbsp;4<br>
     1&nbsp;1&nbsp;0&nbsp;0<br>
     0&nbsp;1&nbsp;1&nbsp;0<br>
     0&nbsp;1&nbsp;0&nbsp;1<br>
     1&nbsp;0&nbsp;1&nbsp;1 </code><code> </code></td>
     * <td><code>4&nbsp;x&nbsp;4<br>
     1&nbsp;1&nbsp;1&nbsp;0<br>
     0&nbsp;1&nbsp;0&nbsp;0<br>
     1&nbsp;1&nbsp;0&nbsp;1<br>
     0&nbsp;0&nbsp;1&nbsp;1 </code></td>
     * </tr>
     * <tr align="center" valign="middle">
     * <td><code>upperBandwidth</code></td>
     * <td><div align="center"><code>0</code></div></td>
     * <td><div align="center"><code>0</code></div></td>
     * <td><div align="center"><code>1</code></div></td>
     * <td><code>3</code></td>
     * <td align="center" valign="middle"><code>0</code></td>
     * <td align="center" valign="middle"><div align="center"><code>1</code></div></td>
     * <td align="center" valign="middle"><div align="center"><code>2</code></div></td>
     * </tr>
     * <tr align="center" valign="middle">
     * <td><code>lowerBandwidth</code></td>
     * <td><div align="center"><code>0</code></div></td>
     * <td><div align="center"><code>0</code></div></td>
     * <td><div align="center"><code>1</code></div></td>
     * <td><code>0</code></td>
     * <td align="center" valign="middle"><code>3</code></td>
     * <td align="center" valign="middle"><div align="center"><code>3</code></div></td>
     * <td align="center" valign="middle"><div align="center"><code>2</code></div></td>
     * </tr>
     * <tr align="center" valign="middle">
     * <td><code>semiBandwidth</code></td>
     * <td><div align="center"><code>1</code></div></td>
     * <td><div align="center"><code>1</code></div></td>
     * <td><div align="center"><code>2</code></div></td>
     * <td><code>4</code></td>
     * <td align="center" valign="middle"><code>4</code></td>
     * <td align="center" valign="middle"><div align="center"><code>4</code></div></td>
     * <td align="center" valign="middle"><div align="center"><code>3</code></div></td>
     * </tr>
     * <tr align="center" valign="middle">
     * <td><code>description</code></td>
     * <td><div align="center"><code>zero</code></div></td>
     * <td><div align="center"><code>diagonal</code></div></td>
     * <td><div align="center"><code>tridiagonal</code></div></td>
     * <td><code>upper triangular</code></td>
     * <td align="center" valign="middle"><code>lower triangular</code></td>
     * <td align="center" valign="middle"><div align="center">
     * <code>unstructured</code></div></td>
     * <td align="center" valign="middle"><div align="center">
     * <code>unstructured</code></div></td>
     * </tr>
     * </table>
     * 
     * @param A
     *            the square matrix to analyze.
     * @return the semi-bandwith <code>l</code>.
     * @throws IllegalArgumentException
     *             if <code>!isSquare(A)</code>.
     * @see #lowerBandwidth(FloatMatrix2D)
     * @see #upperBandwidth(FloatMatrix2D)
     */
    public int semiBandwidth(FloatMatrix2D A) {
        checkSquare(A);
        float epsilon = tolerance();
        int rows = A.rows();

        for (int k = rows; --k >= 0;) {
            for (int i = rows - k; --i >= 0;) {
                int j = i + k;
                if (!(Math.abs(A.getQuick(j, i)) <= epsilon))
                    return k + 1;
                if (!(Math.abs(A.getQuick(i, j)) <= epsilon))
                    return k + 1;
            }
        }
        return 1;
    }

    /**
     * Sets the tolerance to <code>Math.abs(newTolerance)</code>.
     * 
     * @param newTolerance
     * @throws UnsupportedOperationException
     *             if <code>this==DEFAULT || this==ZERO || this==TWELVE</code>.
     */
    public void setTolerance(float newTolerance) {
        if (this == DEFAULT || this == ZERO || this == SEVEN) {
            throw new IllegalArgumentException("Attempted to modify immutable object.");
        }
        tolerance = Math.abs(newTolerance);
    }

    /**
     * Returns the current tolerance.
     * @return 
     */
    public float tolerance() {
        return tolerance;
    }

    /**
     * Returns summary information about the given matrix <code>A</code>. That is a
     * String with (propertyName, propertyValue) pairs. Useful for debugging or
     * to quickly get the rough picture of a matrix. For example,
     * 
     * <pre>
     * 	 density                      : 0.9
     * 	 isDiagonal                   : false
     * 	 isDiagonallyDominantByRow    : false
     * 	 isDiagonallyDominantByColumn : false
     * 	 isIdentity                   : false
     * 	 isLowerBidiagonal            : false
     * 	 isLowerTriangular            : false
     * 	 isNonNegative                : true
     * 	 isOrthogonal                 : Illegal operation or error: Matrix must be square.
     * 	 isPositive                   : true
     * 	 isSingular                   : Illegal operation or error: Matrix must be square.
     * 	 isSkewSymmetric              : Illegal operation or error: Matrix must be square.
     * 	 isSquare                     : false
     * 	 isStrictlyLowerTriangular    : false
     * 	 isStrictlyTriangular         : false
     * 	 isStrictlyUpperTriangular    : false
     * 	 isSymmetric                  : Illegal operation or error: Matrix must be square.
     * 	 isTriangular                 : false
     * 	 isTridiagonal                : false
     * 	 isUnitTriangular             : false
     * 	 isUpperBidiagonal            : false
     * 	 isUpperTriangular            : false
     * 	 isZero                       : false
     * 	 lowerBandwidth               : Illegal operation or error: Matrix must be square.
     * 	 semiBandwidth                : Illegal operation or error: Matrix must be square.
     * 	 upperBandwidth               : Illegal operation or error: Matrix must be square.
     * 
     * </pre>
     * @param A
     * @return 
     */
    public String toString(FloatMatrix2D A) {
        final cern.colt.list.tobject.ObjectArrayList names = new cern.colt.list.tobject.ObjectArrayList();
        final cern.colt.list.tobject.ObjectArrayList values = new cern.colt.list.tobject.ObjectArrayList();
        String unknown = "Illegal operation or error: ";

        // determine properties
        names.add("density");
        try {
            values.add(String.valueOf(density(A)));
        } catch (IllegalArgumentException exc) {
            values.add(unknown + exc.getMessage());
        }

        // determine properties
        names.add("isDiagonal");
        try {
            values.add(String.valueOf(isDiagonal(A)));
        } catch (IllegalArgumentException exc) {
            values.add(unknown + exc.getMessage());
        }

        // determine properties
        names.add("isDiagonallyDominantByRow");
        try {
            values.add(String.valueOf(isDiagonallyDominantByRow(A)));
        } catch (IllegalArgumentException exc) {
            values.add(unknown + exc.getMessage());
        }

        // determine properties
        names.add("isDiagonallyDominantByColumn");
        try {
            values.add(String.valueOf(isDiagonallyDominantByColumn(A)));
        } catch (IllegalArgumentException exc) {
            values.add(unknown + exc.getMessage());
        }

        names.add("isIdentity");
        try {
            values.add(String.valueOf(isIdentity(A)));
        } catch (IllegalArgumentException exc) {
            values.add(unknown + exc.getMessage());
        }

        names.add("isLowerBidiagonal");
        try {
            values.add(String.valueOf(isLowerBidiagonal(A)));
        } catch (IllegalArgumentException exc) {
            values.add(unknown + exc.getMessage());
        }

        names.add("isLowerTriangular");
        try {
            values.add(String.valueOf(isLowerTriangular(A)));
        } catch (IllegalArgumentException exc) {
            values.add(unknown + exc.getMessage());
        }

        names.add("isNonNegative");
        try {
            values.add(String.valueOf(isNonNegative(A)));
        } catch (IllegalArgumentException exc) {
            values.add(unknown + exc.getMessage());
        }

        names.add("isOrthogonal");
        try {
            values.add(String.valueOf(isOrthogonal(A)));
        } catch (IllegalArgumentException exc) {
            values.add(unknown + exc.getMessage());
        }

        names.add("isPositive");
        try {
            values.add(String.valueOf(isPositive(A)));
        } catch (IllegalArgumentException exc) {
            values.add(unknown + exc.getMessage());
        }

        names.add("isSingular");
        try {
            values.add(String.valueOf(isSingular(A)));
        } catch (IllegalArgumentException exc) {
            values.add(unknown + exc.getMessage());
        }

        names.add("isSkewSymmetric");
        try {
            values.add(String.valueOf(isSkewSymmetric(A)));
        } catch (IllegalArgumentException exc) {
            values.add(unknown + exc.getMessage());
        }

        names.add("isSquare");
        try {
            values.add(String.valueOf(isSquare(A)));
        } catch (IllegalArgumentException exc) {
            values.add(unknown + exc.getMessage());
        }

        names.add("isStrictlyLowerTriangular");
        try {
            values.add(String.valueOf(isStrictlyLowerTriangular(A)));
        } catch (IllegalArgumentException exc) {
            values.add(unknown + exc.getMessage());
        }

        names.add("isStrictlyTriangular");
        try {
            values.add(String.valueOf(isStrictlyTriangular(A)));
        } catch (IllegalArgumentException exc) {
            values.add(unknown + exc.getMessage());
        }

        names.add("isStrictlyUpperTriangular");
        try {
            values.add(String.valueOf(isStrictlyUpperTriangular(A)));
        } catch (IllegalArgumentException exc) {
            values.add(unknown + exc.getMessage());
        }

        names.add("isSymmetric");
        try {
            values.add(String.valueOf(isSymmetric(A)));
        } catch (IllegalArgumentException exc) {
            values.add(unknown + exc.getMessage());
        }

        names.add("isTriangular");
        try {
            values.add(String.valueOf(isTriangular(A)));
        } catch (IllegalArgumentException exc) {
            values.add(unknown + exc.getMessage());
        }

        names.add("isTridiagonal");
        try {
            values.add(String.valueOf(isTridiagonal(A)));
        } catch (IllegalArgumentException exc) {
            values.add(unknown + exc.getMessage());
        }

        names.add("isUnitTriangular");
        try {
            values.add(String.valueOf(isUnitTriangular(A)));
        } catch (IllegalArgumentException exc) {
            values.add(unknown + exc.getMessage());
        }

        names.add("isUpperBidiagonal");
        try {
            values.add(String.valueOf(isUpperBidiagonal(A)));
        } catch (IllegalArgumentException exc) {
            values.add(unknown + exc.getMessage());
        }

        names.add("isUpperTriangular");
        try {
            values.add(String.valueOf(isUpperTriangular(A)));
        } catch (IllegalArgumentException exc) {
            values.add(unknown + exc.getMessage());
        }

        names.add("isZero");
        try {
            values.add(String.valueOf(isZero(A)));
        } catch (IllegalArgumentException exc) {
            values.add(unknown + exc.getMessage());
        }

        names.add("lowerBandwidth");
        try {
            values.add(String.valueOf(lowerBandwidth(A)));
        } catch (IllegalArgumentException exc) {
            values.add(unknown + exc.getMessage());
        }

        names.add("semiBandwidth");
        try {
            values.add(String.valueOf(semiBandwidth(A)));
        } catch (IllegalArgumentException exc) {
            values.add(unknown + exc.getMessage());
        }

        names.add("upperBandwidth");
        try {
            values.add(String.valueOf(upperBandwidth(A)));
        } catch (IllegalArgumentException exc) {
            values.add(unknown + exc.getMessage());
        }

        // sort ascending by property name
        cern.colt.function.tint.IntComparator comp = new cern.colt.function.tint.IntComparator() {
            public int compare(int a, int b) {
                return get(names, a).compareTo(get(names, b));
            }
        };
        cern.colt.Swapper swapper = new cern.colt.Swapper() {
            public void swap(int a, int b) {
                Object tmp;
                tmp = names.get(a);
                names.set(a, names.get(b));
                names.set(b, tmp);
                tmp = values.get(a);
                values.set(a, values.get(b));
                values.set(b, tmp);
            }
        };
        cern.colt.GenericSorting.quickSort(0, names.size(), comp, swapper);

        // determine padding for nice formatting
        int maxLength = 0;
        for (int i = 0; i < names.size(); i++) {
            int length = ((String) names.get(i)).length();
            maxLength = Math.max(length, maxLength);
        }

        // finally, format properties
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < names.size(); i++) {
            String name = ((String) names.get(i));
            buf.append(name);
            buf.append(blanks(maxLength - name.length()));
            buf.append(" : ");
            buf.append(values.get(i));
            if (i < names.size() - 1)
                buf.append('\n');
        }

        return buf.toString();
    }

    /**
     * The <i>upper bandwidth</i> of a square matrix <code>A</code> is the maximum
     * <code>j-i</code> for which <code>A[i,j]</code> is nonzero and <code>j &gt; i</code>.
     * A <i>banded</i> matrix has a "band" about the diagonal. Diagonal,
     * tridiagonal and triangular matrices are special cases.
     * 
     * @param A
     *            the square matrix to analyze.
     * @return the upper bandwith.
     * @throws IllegalArgumentException
     *             if <code>!isSquare(A)</code>.
     * @see #semiBandwidth(FloatMatrix2D)
     * @see #lowerBandwidth(FloatMatrix2D)
     */
    public int upperBandwidth(FloatMatrix2D A) {
        checkSquare(A);
        float epsilon = tolerance();
        int rows = A.rows();

        for (int k = rows; --k >= 0;) {
            for (int i = rows - k; --i >= 0;) {
                int j = i + k;
                if (!(Math.abs(A.getQuick(i, j)) <= epsilon))
                    return k;
            }
        }
        return 0;
    }
}
