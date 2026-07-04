package cern.colt.matrix.tdcomplex.algo.decomposition;

import cern.colt.matrix.tdcomplex.DComplexMatrix1D;
import cern.colt.matrix.tdcomplex.DComplexMatrix2D;
import cern.colt.matrix.tdcomplex.algo.DComplexProperty;
import cern.colt.matrix.tdcomplex.impl.SparseCCDComplexMatrix2D;
import cern.colt.matrix.tdcomplex.impl.SparseRCDComplexMatrix2D;
import cern.jet.math.tdcomplex.DComplexFunctions;
import com.github.wendykierp.csparsej.tdcomplex.DZcs_common.DZcsa;
import com.github.wendykierp.csparsej.tdcomplex.DZcs_dmperm;
import com.github.wendykierp.csparsej.tdcomplex.DZcs_ipvec;
import com.github.wendykierp.csparsej.tdcomplex.DZcs_lsolve;
import com.github.wendykierp.csparsej.tdcomplex.DZcs_lu;
import com.github.wendykierp.csparsej.tdcomplex.DZcs_sqr;
import com.github.wendykierp.csparsej.tdcomplex.DZcs_usolve;
import com.github.wendykierp.csparsej.tdcomplex.DZcs_common.DZcs;
import com.github.wendykierp.csparsej.tdcomplex.DZcs_common.DZcsd;
import com.github.wendykierp.csparsej.tdcomplex.DZcs_common.DZcsn;
import com.github.wendykierp.csparsej.tdcomplex.DZcs_common.DZcss;

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
public class SparseDComplexLUDecomposition {
    private DZcss S;
    private DZcsn N;
    private DComplexMatrix2D L;
    private DComplexMatrix2D U;
    private boolean rcMatrix = false;
    private boolean isNonSingular = true;
    /**
     * Row and column dimension (square matrix).
     */
    private int n;

    /**
     * Constructs and returns a new LU Decomposition object; The decomposed
     * matrices can be retrieved via instance methods of the returned
     * decomposition object.
     * 
     * @param A
     *            Square matrix
     * @param order
     *            ordering option (0 to 3); 0: natural ordering, 1: amd(A+A'),
     *            2: amd(S'*S), 3: amd(A'*A)
     * @param checkIfSingular
     *            if true, then the singularity test (based on
     *            Dulmage-Mendelsohn decomposition) is performed.
     * @throws IllegalArgumentException
     *             if <code>A</code> is not square or is not sparse.
     * @throws IllegalArgumentException
     *             if <code>order</code> is not in [0,3]
     */
    public SparseDComplexLUDecomposition(DComplexMatrix2D A, int order, boolean checkIfSingular) {
        DComplexProperty.DEFAULT.checkSquare(A);
        DComplexProperty.DEFAULT.checkSparse(A);

        if (order < 0 || order > 3) {
            throw new IllegalArgumentException("order must be a number between 0 and 3");
        }
        DZcs dcs;
        if (A instanceof SparseRCDComplexMatrix2D) {
            rcMatrix = true;
            dcs = ((SparseRCDComplexMatrix2D) A).getColumnCompressed().elements();
        } else {
            dcs = (DZcs) A.elements();
        }
        n = A.rows();

        S = DZcs_sqr.cs_sqr(order, dcs, false);
        if (S == null) {
            throw new IllegalArgumentException("Exception occured in cs_sqr()");
        }
        N = DZcs_lu.cs_lu(dcs, S, 1);
        if (N == null) {
            throw new IllegalArgumentException("Exception occured in cs_lu()");
        }
        if (checkIfSingular) {
            DZcsd D = DZcs_dmperm.cs_dmperm(dcs, 1); /* check if matrix is singular */
            if (D != null && D.rr[3] < n) {
                isNonSingular = false;
            }
        }
    }

    /**
     * Returns the determinant, <code>det(A)</code>.
     * 
     * @return 
     */
    public double[] det() {
        if (!isNonsingular())
            return new double[] {0, 0}; // avoid rounding errors
        int pivsign = 1;
        for (int i = 0; i < n; i++) {
            if (N.pinv[i] != i) {
                pivsign = -pivsign;
            }
        }
        if (U == null) {
            U = new SparseCCDComplexMatrix2D(N.U);
            if (rcMatrix) {
                U = ((SparseCCDComplexMatrix2D) U).getRowCompressed();
            }
        }
        double[] det = new double[] {pivsign, 0};
        for (int j = 0; j < n; j++) {
            det = DComplexFunctions.mult(det).apply(U.getQuick(j, j));
        }
        return det;
    }

    /**
     * Returns the lower triangular factor, <code>L</code>.
     * 
     * @return <code>L</code>
     */
    public DComplexMatrix2D getL() {
        if (L == null) {
            L = new SparseCCDComplexMatrix2D(N.L);
            if (rcMatrix) {
                L = ((SparseCCDComplexMatrix2D) L).getRowCompressed();
            }
        }
        return L.copy();
    }

    /**
     * Returns a copy of the pivot permutation vector.
     * 
     * @return piv
     */
    public int[] getPivot() {
        if (N.pinv == null)
            return null;
        int[] pinv = new int[N.pinv.length];
        System.arraycopy(N.pinv, 0, pinv, 0, pinv.length);
        return pinv;
    }

    /**
     * Returns the upper triangular factor, <code>U</code>.
     * 
     * @return <code>U</code>
     */
    public DComplexMatrix2D getU() {
        if (U == null) {
            U = new SparseCCDComplexMatrix2D(N.U);
            if (rcMatrix) {
                U = ((SparseCCDComplexMatrix2D) U).getRowCompressed();
            }
        }
        return U.copy();
    }

    /**
     * Returns a copy of the symbolic LU analysis object
     * 
     * @return symbolic LU analysis
     */
    public DZcss getSymbolicAnalysis() {
        DZcss S2 = new DZcss();
        S2.cp = S.cp != null ? S.cp.clone() : null;
        S2.leftmost = S.leftmost != null ? S.leftmost.clone() : null;
        S2.lnz = S.lnz;
        S2.m2 = S.m2;
        S2.parent = S.parent != null ? S.parent.clone() : null;
        S2.pinv = S.pinv != null ? S.pinv.clone() : null;
        S2.q = S.q != null ? S.q.clone() : null;
        S2.unz = S.unz;
        return S2;
    }

    /**
     * Returns whether the matrix is nonsingular (has an inverse).
     * 
     * @return true if <code>U</code>, and hence <code>A</code>, is nonsingular; false
     *         otherwise.
     */
    public boolean isNonsingular() {
        return isNonSingular;
    }

    /**
     * Solves <code>A*x = b</code>(in-place). Upon return <code>b</code> is overridden
     * with the result <code>x</code>.
     * 
     * @param b
     *            A vector with of size A.rows();
     * @exception IllegalArgumentException
     *                if <code>b.size() != A.rows()</code> or if A is singular.
     */
    public void solve(DComplexMatrix1D b) {
        if (b.size() != n) {
            throw new IllegalArgumentException("b.size() != A.rows()");
        }
        if (!isNonsingular()) {
            throw new IllegalArgumentException("A is singular");
        }
        DComplexProperty.DEFAULT.checkDense(b);
        DZcsa y = new DZcsa(n);
        DZcsa x;
        if (b.isView()) {
            x = new DZcsa((double[]) b.copy().elements());
        } else {
            x = new DZcsa((double[]) b.elements());
        }
        DZcs_ipvec.cs_ipvec(N.pinv, x, y, n); /* y = b(p) */
        DZcs_lsolve.cs_lsolve(N.L, y); /* y = L\y */
        DZcs_usolve.cs_usolve(N.U, y); /* y = U\y */
        DZcs_ipvec.cs_ipvec(S.q, y, x, n); /* b(q) = x */

        if (b.isView()) {
            b.assign(x.x);
        }
    }
}
