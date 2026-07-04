/*
Copyright (C) 1999 CERN - European Organization for Nuclear Research.
Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose 
is hereby granted without fee, provided that the above copyright notice appear in all copies and 
that both that copyright notice and this permission notice appear in supporting documentation. 
CERN makes no representations about the suitability of this software for any purpose. 
It is provided "as is" without expressed or implied warranty.
 */
package cern.colt.matrix.tfloat.algo;

import cern.colt.matrix.tfloat.FloatMatrix1D;
import cern.colt.matrix.tfloat.FloatMatrix2D;

/**
 * <p>
 * Subset of the <A HREF="http://netlib2.cs.utk.edu/blas/faq.html">BLAS</A>
 * (Basic Linear Algebra System); High quality "building block" routines for
 * performing basic vector and matrix operations. Because the BLAS are
 * efficient, portable, and widely available, they're commonly used in the
 * development of high quality linear algebra software.
 * <p>
 * Mostly for compatibility with legacy notations. Most operations actually just
 * delegate to the appropriate methods directly defined on matrices and vectors.
 * </p>
 * <p>
 * This class implements the BLAS functions for operations on matrices from the
 * matrix package. It follows the spirit of the <A
 * HREF="http://math.nist.gov/javanumerics/blas.html">Draft Proposal for Java
 * BLAS Interface</A>, by Roldan Pozo of the National Institute of Standards and
 * Technology. Interface definitions are also identical to the Ninja interface.
 * Because the matrix package supports sections, the interface is actually
 * simpler.
 * </p>
 * <p>
 * Currently, the following operations are supported:
 * </p>
 * <ol>
 * <li>BLAS Level 1: Vector-Vector operations</li>
 * <ul>
 * <li>ddot : dot product of two vectors</li>
 * <li>daxpy : scalar times a vector plus a vector</li>
 * <li>drotg : construct a Givens plane rotation</li>
 * <li>drot : apply a plane rotation</li>
 * <li>dcopy : copy vector X into vector Y</li>
 * <li>dswap : interchange vectors X and Y</li>
 * <li>dnrm2 : Euclidean norm of a vector</li>
 * <li>dasum : sum of absolute values of vector components</li>
 * <li>dscal : scale a vector by a scalar</li>
 * <li>idamax: index of element with maximum absolute value</li>
 * </ul>
 * <li>2.BLAS Level 2: Matrix-Vector operations</li>
 * <ul>
 * <li>dgemv : matrix-vector multiply with general matrix</li>
 * <li>dger : rank-1 update on general matrix</li>
 * <li>dsymv : matrix-vector multiply with symmetric matrix</li>
 * <li>dtrmv : matrix-vector multiply with triangular matrix</li>
 * </ul>
 * <li>3.BLAS Level 3: Matrix-Matrix operations
 * <ul>
 * <li>dgemm : matrix-matrix multiply with general matrices</li>
 * </ul>
 * </li>
 * </ol>
 * 
 * @author wolfgang.hoschek@cern.ch
 * @version 0.9, 16/04/2000
 */
public interface FloatBlas {
    /**
     * Assigns the result of a function to each cell;
     * <code>x[row,col] = function(x[row,col])</code>.
     * 
     * @param A
     *            the matrix to modify.
     * @param function
     *            a function object taking as argument the current cell's value.
     * @see cern.jet.math.tfloat.FloatFunctions
     */
    public void assign(FloatMatrix2D A, cern.colt.function.tfloat.FloatFunction function);

    /**
     * Assigns the result of a function to each cell;
     * <code>x[row,col] = function(x[row,col],y[row,col])</code>.
     * 
     * @param x
     *            the matrix to modify.
     * @param y
     *            the secondary matrix to operate on.
     * @param function
     *            a function object taking as first argument the current cell's
     *            value of <code>this</code>, and as second argument the current
     *            cell's value of <code>y</code>,
     * @throws IllegalArgumentException
     *             if
     *             <code>x.columns() != y.columns() || x.rows() != y.rows()</code>
     * @see cern.jet.math.tfloat.FloatFunctions
     */
    public void assign(FloatMatrix2D x, FloatMatrix2D y, cern.colt.function.tfloat.FloatFloatFunction function);

    /**
     * Returns the sum of absolute values; <code>|x[0]| + |x[1]| + ... </code>. In
     * fact equivalent to
     * <code>x.aggregate(cern.jet.math.Functions.plus, cern.jet.math.Functions.abs)</code>
     * .
     * 
     * @param x
     *            the first vector.
     * @return 
     */
    public float dasum(FloatMatrix1D x);

    /**
     * Combined vector scaling; <code>y = y + alpha*x</code>. In fact equivalent to
     * <code>y.assign(x,cern.jet.math.Functions.plusMult(alpha))</code>.
     * 
     * @param alpha
     *            a scale factor.
     * @param x
     *            the first source vector.
     * @param y
     *            the second source vector, this is also the vector where
     *            results are stored.
     * 
     * @throws IllegalArgumentException
     *             <code>x.size() != y.size()</code>..
     */
    public void daxpy(float alpha, FloatMatrix1D x, FloatMatrix1D y);

    /**
     * Combined matrix scaling; <code>B = B + alpha*A</code>. In fact equivalent to
     * <code>B.assign(A,cern.jet.math.Functions.plusMult(alpha))</code>.
     * 
     * @param alpha
     *            a scale factor.
     * @param A
     *            the first source matrix.
     * @param B
     *            the second source matrix, this is also the matrix where
     *            results are stored.
     * 
     * @throws IllegalArgumentException
     *             if
     *             <code>A.columns() != B.columns() || A.rows() != B.rows()</code>.
     */
    public void daxpy(float alpha, FloatMatrix2D A, FloatMatrix2D B);

    /**
     * Vector assignment (copying); <code>y = x</code>. In fact equivalent to
     * <code>y.assign(x)</code>.
     * 
     * @param x
     *            the source vector.
     * @param y
     *            the destination vector.
     * 
     * @throws IllegalArgumentException
     *             <code>x.size() != y.size()</code>.
     */
    public void dcopy(FloatMatrix1D x, FloatMatrix1D y);

    /**
     * Matrix assignment (copying); <code>B = A</code>. In fact equivalent to
     * <code>B.assign(A)</code>.
     * 
     * @param A
     *            the source matrix.
     * @param B
     *            the destination matrix.
     * 
     * @throws IllegalArgumentException
     *             if
     *             <code>A.columns() != B.columns() || A.rows() != B.rows()</code>.
     */
    public void dcopy(FloatMatrix2D A, FloatMatrix2D B);

    /**
     * Returns the dot product of two vectors x and y, which is
     * <code>Sum(x[i]*y[i])</code>. In fact equivalent to <code>x.zDotProduct(y)</code>.
     * 
     * @param x
     *            the first vector.
     * @param y
     *            the second vector.
     * @return the sum of products.
     * 
     * @throws IllegalArgumentException
     *             if <code>x.size() != y.size()</code>.
     */
    public float ddot(FloatMatrix1D x, FloatMatrix1D y);

    /**
     * Generalized linear algebraic matrix-matrix multiply;
     * <code>C = alpha*A*B + beta*C</code>. In fact equivalent to
     * <code>A.zMult(B,C,alpha,beta,transposeA,transposeB)</code>. Note: Matrix
     * shape conformance is checked <i>after</i> potential transpositions.
     * 
     * @param transposeA
     *            set this flag to indicate that the multiplication shall be
     *            performed on A'.
     * @param transposeB
     *            set this flag to indicate that the multiplication shall be
     *            performed on B'.
     * @param alpha
     *            a scale factor.
     * @param A
     *            the first source matrix.
     * @param B
     *            the second source matrix.
     * @param beta
     *            a scale factor.
     * @param C
     *            the third source matrix, this is also the matrix where results
     *            are stored.
     * 
     * @throws IllegalArgumentException
     *             if <code>B.rows() != A.columns()</code>.
     * @throws IllegalArgumentException
     *             if
     *             <code>C.rows() != A.rows() || C.columns() != B.columns()</code>.
     * @throws IllegalArgumentException
     *             if <code>A == C || B == C</code>.
     */
    public void dgemm(boolean transposeA, boolean transposeB, float alpha, FloatMatrix2D A, FloatMatrix2D B,
            float beta, FloatMatrix2D C);

    /**
     * Generalized linear algebraic matrix-vector multiply;
     * <code>y = alpha*A*x + beta*y</code>. In fact equivalent to
     * <code>A.zMult(x,y,alpha,beta,transposeA)</code>. Note: Matrix shape
     * conformance is checked <i>after</i> potential transpositions.
     * 
     * @param transposeA
     *            set this flag to indicate that the multiplication shall be
     *            performed on A'.
     * @param alpha
     *            a scale factor.
     * @param A
     *            the source matrix.
     * @param x
     *            the first source vector.
     * @param beta
     *            a scale factor.
     * @param y
     *            the second source vector, this is also the vector where
     *            results are stored.
     * 
     * @throws IllegalArgumentException
     *             <code>A.columns() != x.size() || A.rows() != y.size())</code>..
     */
    public void dgemv(boolean transposeA, float alpha, FloatMatrix2D A, FloatMatrix1D x, float beta, FloatMatrix1D y);

    /**
     * Performs a rank 1 update; <code>A = A + alpha*x*y'</code>. Example:
     * 
     * <pre>
     * 	 A = { {6,5}, {7,6} }, x = {1,2}, y = {3,4}, alpha = 1 --&gt;
     * 	 A = { {9,9}, {13,14} }
     * 
     * </pre>
     * 
     * @param alpha
     *            a scalar.
     * @param x
     *            an m element vector.
     * @param y
     *            an n element vector.
     * @param A
     *            an m by n matrix.
     */
    public void dger(float alpha, FloatMatrix1D x, FloatMatrix1D y, FloatMatrix2D A);

    /**
     * Return the 2-norm; <code>sqrt(x[0]^2 + x[1]^2 + ...)</code>. In fact
     * equivalent to <code>(float)Math.sqrt(Algebra.DEFAULT.norm2(x))</code>.
     * 
     * @param x
     *            the vector.
     * @return 
     */
    public float dnrm2(FloatMatrix1D x);

    /**
     * Applies a givens plane rotation to (x,y);
     * <code>x = c*x + s*y; y = c*y - s*x</code>.
     * 
     * @param x
     *            the first vector.
     * @param y
     *            the second vector.
     * @param c
     *            the cosine of the angle of rotation.
     * @param s
     *            the sine of the angle of rotation.
     */
    public void drot(FloatMatrix1D x, FloatMatrix1D y, float c, float s);

    /**
     * Constructs a Givens plane rotation for <code>(a,b)</code>. Taken from the
     * LINPACK translation from FORTRAN to Java, interface slightly modified. In
     * the LINPACK listing DROTG is attributed to Jack Dongarra
     * 
     * @param a
     *            rotational elimination parameter a.
     * @param b
     *            rotational elimination parameter b.
     * @param rotvec
     *            Must be at least of length 4. On output contains the values
     *            <code>{a,b,c,s}</code>.
     */
    public void drotg(float a, float b, float[] rotvec);

    /**
     * Vector scaling; <code>x = alpha*x</code>. In fact equivalent to
     * <code>x.assign(cern.jet.math.Functions.mult(alpha))</code>.
     * 
     * @param alpha
     *            a scale factor.
     * @param x
     *            the first vector.
     */
    public void dscal(float alpha, FloatMatrix1D x);

    /**
     * Matrix scaling; <code>A = alpha*A</code>. In fact equivalent to
     * <code>A.assign(cern.jet.math.Functions.mult(alpha))</code>.
     * 
     * @param alpha
     *            a scale factor.
     * @param A
     *            the matrix.
     */
    public void dscal(float alpha, FloatMatrix2D A);

    /**
     * Swaps the elements of two vectors; <code>y <==> x</code>. In fact equivalent
     * to <code>y.swap(x)</code>.
     * 
     * @param x
     *            the first vector.
     * @param y
     *            the second vector.
     * 
     * @throws IllegalArgumentException
     *             <code>x.size() != y.size()</code>.
     */
    public void dswap(FloatMatrix1D x, FloatMatrix1D y);

    /**
     * Swaps the elements of two matrices; <code>B <==> A</code>.
     * 
     * @param x
     *            the first matrix.
     * @param y
     *            the second matrix.
     * 
     * @throws IllegalArgumentException
     *             if
     *             <code>A.columns() != B.columns() || A.rows() != B.rows()</code>.
     */
    public void dswap(FloatMatrix2D x, FloatMatrix2D y);

    /**
     * Symmetric matrix-vector multiplication; <code>y = alpha*A*x + beta*y</code>.
     * Where alpha and beta are scalars, x and y are n element vectors and A is
     * an n by n symmetric matrix. A can be in upper or lower triangular format.
     * 
     * @param isUpperTriangular
     *            is A upper triangular or lower triangular part to be used?
     * @param alpha
     *            scaling factor.
     * @param A
     *            the source matrix.
     * @param x
     *            the first source vector.
     * @param beta
     *            scaling factor.
     * @param y
     *            the second vector holding source and destination.
     */
    public void dsymv(boolean isUpperTriangular, float alpha, FloatMatrix2D A, FloatMatrix1D x, float beta,
            FloatMatrix1D y);

    /**
     * Triangular matrix-vector multiplication; <code>x = A*x</code> or <code>x = A'*x</code>.
     * Where x is an n element vector and A is an n by n unit, or non-unit,
     * upper or lower triangular matrix.
     * 
     * @param isUpperTriangular
     *            is A upper triangular or lower triangular?
     * @param transposeA
     *            set this flag to indicate that the multiplication shall be
     *            performed on A'.
     * @param isUnitTriangular
     *            true --> A is assumed to be unit triangular; false --> A is
     *            not assumed to be unit triangular
     * @param A
     *            the source matrix.
     * @param x
     *            the vector holding source and destination.
     */
    public void dtrmv(boolean isUpperTriangular, boolean transposeA, boolean isUnitTriangular, FloatMatrix2D A,
            FloatMatrix1D x);

    /**
     * Returns the index of largest absolute value;
     * <code>i such that |x[i]| == max(|x[0]|,|x[1]|,...).</code>.
     * 
     * @param x
     *            the vector to search through.
     * @return the index of largest absolute value (-1 if x is empty).
     */
    public int idamax(FloatMatrix1D x);

}
