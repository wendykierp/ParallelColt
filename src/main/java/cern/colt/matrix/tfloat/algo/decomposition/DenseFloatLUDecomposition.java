/*
Copyright (C) 1999 CERN - European Organization for Nuclear Research.
Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose 
is hereby granted without fee, provided that the above copyright notice appear in all copies and 
that both that copyright notice and this permission notice appear in supporting documentation. 
CERN makes no representations about the suitability of this software for any purpose. 
It is provided "as is" without expressed or implied warranty.
 */
package cern.colt.matrix.tfloat.algo.decomposition;

import cern.colt.matrix.tfloat.FloatMatrix1D;
import cern.colt.matrix.tfloat.FloatMatrix2D;

/**
 * For an <code>m x n</code> matrix <code>A</code> with <code>m >= n</code>, the LU
 * decomposition is an <code>m x n</code> unit lower triangular matrix <code>L</code>,
 * an <code>n x n</code> upper triangular matrix <code>U</code>, and a permutation
 * vector <code>piv</code> of length <code>m</code> so that <code>A(piv,:) = L*U</code>; If
 * <code>m &lt; n</code>, then <code>L</code> is <code>m x m</code> and <code>U</code> is
 * <code>m x n</code>.
 * <P>
 * The LU decomposition with pivoting always exists, even if the matrix is
 * singular, so the constructor will never fail. The primary use of the LU
 * decomposition is in the solution of square systems of simultaneous linear
 * equations. This will fail if <code>isNonsingular()</code> returns false.
 */
public class DenseFloatLUDecomposition implements java.io.Serializable {
    static final long serialVersionUID = 1020;

    protected DenseFloatLUDecompositionQuick quick;

    /**
     * Constructs and returns a new LU Decomposition object; The decomposed
     * matrices can be retrieved via instance methods of the returned
     * decomposition object.
     * 
     * @param A
     *            Rectangular matrix
     */
    public DenseFloatLUDecomposition(FloatMatrix2D A) {
        quick = new DenseFloatLUDecompositionQuick(0); // zero tolerance for
        // compatibility with Jama
        quick.decompose(A.copy());
    }

    /**
     * Returns the determinant, <code>det(A)</code>.
     * 
     * @return 
     * @exception IllegalArgumentException
     *                Matrix must be square
     */
    public float det() {
        return quick.det();
    }

    /**
     * Returns the lower triangular factor, <code>L</code>.
     * 
     * @return <code>L</code>
     */
    public FloatMatrix2D getL() {
        return quick.getL();
    }

    /**
     * Returns a copy of the pivot permutation vector.
     * 
     * @return piv
     */
    public int[] getPivot() {
        return quick.getPivot().clone();
    }

    /**
     * Returns the upper triangular factor, <code>U</code>.
     * 
     * @return <code>U</code>
     */
    public FloatMatrix2D getU() {
        return quick.getU();
    }

    /**
     * Returns whether the matrix is nonsingular (has an inverse).
     * 
     * @return true if <code>U</code>, and hence <code>A</code>, is nonsingular; false
     *         otherwise.
     */
    public boolean isNonsingular() {
        return quick.isNonsingular();
    }

    /**
     * Solves <code>A*X = B</code>.
     * 
     * @param B
     *            A matrix with as many rows as <code>A</code> and any number of
     *            columns.
     * @return <code>X</code> so that <code>L*U*X = B(piv)</code>.
     * @exception IllegalArgumentException
     *                if </code>B.rows() != A.rows()</code>.
     * @exception IllegalArgumentException
     *                if A is singular, that is, if
     *                <code>!this.isNonsingular()</code>.
     * @exception IllegalArgumentException
     *                if <code>A.rows() &lt; A.columns()</code>.
     */

    public FloatMatrix2D solve(FloatMatrix2D B) {
        FloatMatrix2D X = B.copy();
        quick.solve(X);
        return X;
    }

    /**
     * Solves <code>A*x = b</code>.
     * 
     * @param b
     *            A vector of size <code>A.rows()</code>
     * @return <code>x</code> so that <code>L*U*x = b(piv)</code>.
     * @exception IllegalArgumentException
     *                if </code>b.size() != A.rows()</code>.
     * @exception IllegalArgumentException
     *                if A is singular, that is, if
     *                <code>!this.isNonsingular()</code>.
     * @exception IllegalArgumentException
     *                if <code>A.rows() &lt; A.columns()</code>.
     */

    public FloatMatrix1D solve(FloatMatrix1D b) {
        FloatMatrix1D x = b.copy();
        quick.solve(x);
        return x;
    }

    /**
     * Returns a String with (propertyName, propertyValue) pairs. Useful for
     * debugging or to quickly get the rough picture. For example,
     * 
     * <pre>
     * 	 rank          : 3
     * 	 trace         : 0
     * 
     * </pre>
     * @return 
     */

    public String toString() {
        return quick.toString();
    }
}
