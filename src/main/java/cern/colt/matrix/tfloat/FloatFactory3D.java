/*
Copyright (C) 1999 CERN - European Organization for Nuclear Research.
Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose 
is hereby granted without fee, provided that the above copyright notice appear in all copies and 
that both that copyright notice and this permission notice appear in supporting documentation. 
CERN makes no representations about the suitability of this software for any purpose. 
It is provided "as is" without expressed or implied warranty.
 */
package cern.colt.matrix.tfloat;

import cern.colt.matrix.tfloat.impl.DenseFloatMatrix3D;
import cern.colt.matrix.tfloat.impl.SparseFloatMatrix3D;
import cern.jet.math.tfloat.FloatFunctions;

/**
 * Factory for convenient construction of 3-d matrices holding <code>float</code>
 * cells. Use idioms like <code>FloatFactory3D.dense.make(4,4,4)</code> to construct
 * dense matrices, <code>FloatFactory3D.sparse.make(4,4,4)</code> to construct
 * sparse matrices.
 * 
 * If the factory is used frequently it might be useful to streamline the
 * notation. For example by aliasing:
 * <table>
 * <td class="PRE">
 * 
 * <pre>
 *  FloatFactory3D F = FloatFactory3D.dense;
 *  F.make(4,4,4);
 *  F.descending(10,20,5);
 *  F.random(4,4,5);
 *  ...
 * </pre>
 * 
 * </td>
 * </table>
 * 
 * @author wolfgang.hoschek@cern.ch
 * @version 1.0, 09/24/99
 */
public class FloatFactory3D extends cern.colt.PersistentObject {
    private static final long serialVersionUID = 1L;

    /**
     * A factory producing dense matrices.
     */
    public static final FloatFactory3D dense = new FloatFactory3D();

    /**
     * A factory producing sparse matrices.
     */
    public static final FloatFactory3D sparse = new FloatFactory3D();

    /**
     * Makes this class non instantiable, but still let's others inherit from
     * it.
     */
    protected FloatFactory3D() {
    }

    /**
     * Constructs a matrix with cells having ascending values. For debugging
     * purposes.
     * @param slices
     * @param columns
     * @param rows
     * @return 
     */
    public FloatMatrix3D ascending(int slices, int rows, int columns) {
        return descending(slices, rows, columns).assign(
                FloatFunctions.chain(FloatFunctions.neg, FloatFunctions.minus(slices * rows * columns)));
    }

    /**
     * Constructs a matrix with cells having descending values. For debugging
     * purposes.
     * @param slices
     * @param columns
     * @param rows
     * @return 
     */
    public FloatMatrix3D descending(int slices, int rows, int columns) {
        FloatMatrix3D matrix = make(slices, rows, columns);
        int v = 0;
        for (int slice = slices; --slice >= 0;) {
            for (int row = rows; --row >= 0;) {
                for (int column = columns; --column >= 0;) {
                    matrix.setQuick(slice, row, column, v++);
                }
            }
        }
        return matrix;
    }

    /**
     * Constructs a matrix with the given cell values. <code>values</code> is
     * required to have the form <code>values[slice][row][column]</code> and have
     * exactly the same number of slices, rows and columns as the receiver.
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
     *             <code>for any 0 &lt;= column &lt; columns(): values[slice][row].length != columns()</code>
     *             .
     */
    public FloatMatrix3D make(float[][][] values) {
        if (this == sparse)
            return new SparseFloatMatrix3D(values);
        return new DenseFloatMatrix3D(values);
    }

    /**
     * Constructs a matrix with the given shape, each cell initialized with
     * zero.
     * @param slices
     * @param columns
     * @param rows
     * @return 
     */
    public FloatMatrix3D make(int slices, int rows, int columns) {
        if (this == sparse)
            return new SparseFloatMatrix3D(slices, rows, columns);
        return new DenseFloatMatrix3D(slices, rows, columns);
    }

    /**
     * Constructs a matrix with the given shape, each cell initialized with the
     * given value.
     * @param slices
     * @param initialValue
     * @param rows
     * @param columns
     * @return 
     */
    public FloatMatrix3D make(int slices, int rows, int columns, float initialValue) {
        return make(slices, rows, columns).assign(initialValue);
    }

    /**
     * Constructs a matrix with uniformly distributed values in <code>(0,1)</code>
     * (exclusive).
     * @param slices
     * @param columns
     * @param rows
     * @return 
     */
    public FloatMatrix3D random(int slices, int rows, int columns) {
        return make(slices, rows, columns).assign(cern.jet.math.tfloat.FloatFunctions.random());
    }
}
