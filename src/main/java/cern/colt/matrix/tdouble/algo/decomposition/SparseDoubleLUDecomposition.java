package cern.colt.matrix.tdouble.algo.decomposition;

import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import com.github.wendykierp.csparsej.tdouble.Dcs_common.Dcss;

/**
 * For a square matrix <code>A</code>, the LU decomposition is an unit lower
 * triangular matrix <code>L</code>, an upper triangular matrix <code>U</code>, and a
 * permutation vector <code>piv</code> so that <code>A(piv,:) = L*U</code>
 * <P>
 * The LU decomposition with pivoting always exists, even if the matrix is
 * singular. The primary use of the LU decomposition is in the solution of
 * square systems of simultaneous linear equations. This will fail if
 * <code>isNonsingular()</code> returns false.
 * 
 * @author Piotr Wendykier (piotr.wendykier@gmail.com)
 */
public interface SparseDoubleLUDecomposition {

	/**
	 * Returns the determinant, <code>det(A)</code>.
	 * 
     * @return 
	 */
	public abstract double det();

	/**
	 * Returns the lower triangular factor, <code>L</code>.
	 * 
	 * @return <code>L</code>
	 */
	public abstract DoubleMatrix2D getL();

	/**
	 * Returns a copy of the pivot permutation vector.
	 * 
	 * @return piv
	 */
	public abstract int[] getPivot();

	/**
	 * Returns the upper triangular factor, <code>U</code>.
	 * 
	 * @return <code>U</code>
	 */
	public abstract DoubleMatrix2D getU();

	/**
	 * Returns a copy of the symbolic LU analysis object
	 * 
	 * @return symbolic LU analysis
	 */
	public abstract Object getSymbolicAnalysis();

	/**
	 * Returns whether the matrix is nonsingular (has an inverse).
	 * 
	 * @return true if <code>U</code>, and hence <code>A</code>, is nonsingular; false
	 *         otherwise.
	 */
	public abstract boolean isNonsingular();

	/**
	 * Solves <code>A*x = b</code>(in-place). Upon return <code>b</code> is overridden
	 * with the result <code>x</code>.
	 * 
	 * @param b
	 *            A vector with of size A.rows();
	 * @exception IllegalArgumentException
	 *                if <code>b.size() != A.rows()</code> or if A is singular.
	 */
	public abstract void solve(DoubleMatrix1D b);

}