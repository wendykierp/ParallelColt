/*
Copyright (C) 1999 CERN - European Organization for Nuclear Research.
Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose 
is hereby granted without fee, provided that the above copyright notice appear in all copies and 
that both that copyright notice and this permission notice appear in supporting documentation. 
CERN makes no representations about the suitability of this software for any purpose. 
It is provided "as is" without expressed or implied warranty.
 */
package cern.colt.matrix.tdouble.algo;

import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.DoubleMatrix2DProcedure;
import cern.colt.matrix.tdouble.DoubleMatrix3D;
import cern.colt.matrix.tdouble.DoubleMatrix3DProcedure;

/**
 * Stencil operations. For efficient finite difference operations. Applies a
 * function to a moving <code>3 x 3</code> or <code>3 x 3 x 3</code> window. Build on
 * top of <code>matrix.zAssignXXXNeighbors(...)</code>. You can specify how many
 * iterations shall at most be done, a convergence condition when iteration
 * shall be terminated, and how many iterations shall pass between convergence
 * checks. Always does two iterations at a time for efficiency. These class is
 * for convencience and efficiency.
 * 
 * @author wolfgang.hoschek@cern.ch
 * @version 1.0, 01/02/2000
 */
public class DoubleStencil extends Object {
    /**
     * Makes this class non instantiable, but still let's others inherit from
     * it.
     */
    protected DoubleStencil() {
    }

    /**
     * 27 point stencil operation. Applies a function to a moving
     * <code>3 x 3 x 3</code> window.
     * 
     * @param A
     *            the matrix to operate on.
     * @param function
     *            the function to be applied to each window.
     * @param maxIterations
     *            the maximum number of times the stencil shall be applied to
     *            the matrix. Should be a multiple of 2 because two iterations
     *            are always done in one atomic step.
     * @param hasConverged
     *            Convergence condition; will return before maxIterations are
     *            done when <code>hasConverged.apply(A)==true</code>. Set this
     *            parameter to <code>null</code> to indicate that no convergence
     *            checks shall be made.
     * @param convergenceIterations
     *            the number of iterations to pass between each convergence
     *            check. (Since a convergence may be expensive, you may want to
     *            do it only every 2,4 or 8 iterations.)
     * @return the number of iterations actually executed.
     */
    public static int stencil27(DoubleMatrix3D A, cern.colt.function.tdouble.Double27Function function,
            int maxIterations, DoubleMatrix3DProcedure hasConverged, int convergenceIterations) {
        DoubleMatrix3D B = A.copy();
        if (convergenceIterations <= 1)
            convergenceIterations = 2;
        if (convergenceIterations % 2 != 0)
            convergenceIterations++; // odd -> make it even

        int i = 0;
        while (i < maxIterations) { // do two steps at a time for efficiency
            A.zAssign27Neighbors(B, function);
            B.zAssign27Neighbors(A, function);
            i = i + 2;
            if (i % convergenceIterations == 0 && hasConverged != null) {
                if (hasConverged.apply(A))
                    return i;
            }
        }
        return i;
    }

    /**
     * 9 point stencil operation. Applies a function to a moving <code>3 x 3</code>
     * window.
     * 
     * @param A
     *            the matrix to operate on.
     * @param function
     *            the function to be applied to each window.
     * @param maxIterations
     *            the maximum number of times the stencil shall be applied to
     *            the matrix. Should be a multiple of 2 because two iterations
     *            are always done in one atomic step.
     * @param hasConverged
     *            Convergence condition; will return before maxIterations are
     *            done when <code>hasConverged.apply(A)==true</code>. Set this
     *            parameter to <code>null</code> to indicate that no convergence
     *            checks shall be made.
     * @param convergenceIterations
     *            the number of iterations to pass between each convergence
     *            check. (Since a convergence may be expensive, you may want to
     *            do it only every 2,4 or 8 iterations.)
     * @return the number of iterations actually executed.
     */
    public static int stencil9(DoubleMatrix2D A, cern.colt.function.tdouble.Double9Function function,
            int maxIterations, DoubleMatrix2DProcedure hasConverged, int convergenceIterations) {
        DoubleMatrix2D B = A.copy();
        if (convergenceIterations <= 1)
            convergenceIterations = 2;
        if (convergenceIterations % 2 != 0)
            convergenceIterations++; // odd -> make it even

        int i = 0;
        while (i < maxIterations) { // do two steps at a time for efficiency
            A.zAssign8Neighbors(B, function);
            B.zAssign8Neighbors(A, function);
            i = i + 2;
            if (i % convergenceIterations == 0 && hasConverged != null) {
                if (hasConverged.apply(A))
                    return i;
            }
        }
        return i;
    }
}
