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

import cern.colt.matrix.tfcomplex.impl.DenseFComplexMatrix1D;
import cern.colt.matrix.tfcomplex.impl.SparseFComplexMatrix1D;

/**
 * Factory for convenient construction of 1-d matrices holding <code>complex</code>
 * cells. Use idioms like <code>ComplexFactory1D.dense.make(1000)</code> to
 * construct dense matrices, <code>ComplexFactory1D.sparse.make(1000)</code> to
 * construct sparse matrices.
 * 
 * If the factory is used frequently it might be useful to streamline the
 * notation. For example by aliasing:
 * <table>
 * <td class="PRE">
 * 
 * <pre>
 *  ComplexFactory1D F = ComplexFactory1D.dense;
 *  F.make(1000);
 *  F.random(3);
 *  ...
 * </pre>
 * 
 * </td>
 * </table>
 * 
 * @author Piotr Wendykier (piotr.wendykier@gmail.com)
 */
public class FComplexFactory1D extends cern.colt.PersistentObject {
    private static final long serialVersionUID = 1L;

    /**
     * A factory producing dense matrices.
     */
    public static final FComplexFactory1D dense = new FComplexFactory1D();

    /**
     * A factory producing sparse matrices.
     */
    public static final FComplexFactory1D sparse = new FComplexFactory1D();

    /**
     * Makes this class non instantiable, but still let's others inherit from
     * it.
     */
    protected FComplexFactory1D() {
    }

    /**
     * C = A||B; Constructs a new matrix which is the concatenation of two other
     * matrices. Example: <code>0 1</code> append <code>3 4</code> --> <code>0 1 3 4</code>.
     * @param A
     * @param B
     * @return 
     */
    public FComplexMatrix1D append(FComplexMatrix1D A, FComplexMatrix1D B) {
        // concatenate
        FComplexMatrix1D matrix = make((int) (A.size() + B.size()));
        matrix.viewPart(0, (int) A.size()).assign(A);
        matrix.viewPart((int) A.size(), (int) B.size()).assign(B);
        return matrix;
    }

    /**
     * Constructs a matrix with the given cell values. The values are copied. So
     * subsequent changes in <code>values</code> are not reflected in the matrix,
     * and vice-versa.
     * 
     * @param values
     *            The values to be filled into the new matrix.
     * @return 
     */
    public FComplexMatrix1D make(float[] values) {
        if (this == sparse)
            return new SparseFComplexMatrix1D(values);
        else
            return new DenseFComplexMatrix1D(values);
    }

    /**
     * Constructs a matrix which is the concatenation of all given parts. Cells
     * are copied.
     * @param parts
     * @return 
     */
    public FComplexMatrix1D make(FComplexMatrix1D[] parts) {
        if (parts.length == 0)
            return make(0);

        int size = 0;
        for (int i = 0; i < parts.length; i++)
            size += parts[i].size();

        FComplexMatrix1D vector = make(size);
        size = 0;
        for (int i = 0; i < parts.length; i++) {
            vector.viewPart(size, (int) parts[i].size()).assign(parts[i]);
            size += parts[i].size();
        }

        return vector;
    }

    /**
     * Constructs a matrix with the given shape, each cell initialized with
     * zero.
     * @param size
     * @return 
     */
    public FComplexMatrix1D make(int size) {
        if (this == sparse) {
            return new SparseFComplexMatrix1D(size);
        } else {
            return new DenseFComplexMatrix1D(size);
        }
    }

    /**
     * Constructs a matrix with the given shape, each cell initialized with the
     * given value.
     * @param size
     * @param initialValue
     * @return 
     */
    public FComplexMatrix1D make(int size, float[] initialValue) {
        return make(size).assign(initialValue[0], initialValue[1]);
    }

    /**
     * Constructs a matrix from the values of the given list. The values are
     * copied. So subsequent changes in <code>values</code> are not reflected in the
     * matrix, and vice-versa.
     * 
     * @param values
     *            The values to be filled into the new matrix.
     * @return a new matrix.
     */
    public FComplexMatrix1D make(ArrayList<float[]> values) {
        int size = values.size();
        FComplexMatrix1D vector = make(size);
        for (int i = 0; i < size; i++)
            vector.setQuick(i, values.get(i));
        return vector;
    }

    /**
     * Constructs a matrix with uniformly distributed values in <code>(0,1)</code>
     * (exclusive).
     * @param size
     * @return 
     */
    public FComplexMatrix1D random(int size) {
        return make(size).assign(cern.jet.math.tfcomplex.FComplexFunctions.random());
    }

    /**
     * C = A||A||..||A; Constructs a new matrix which is concatenated
     * <code>repeat</code> times.
     * @param A
     * @param repeat
     * @return 
     */
    public FComplexMatrix1D repeat(FComplexMatrix1D A, int repeat) {
        int size = (int) A.size();
        FComplexMatrix1D matrix = make(repeat * size);
        for (int i = 0; i < repeat; i++) {
            matrix.viewPart(size * i, size).assign(A);
        }
        return matrix;
    }

    /**
     * Constructs a randomly sampled matrix with the given shape. Randomly picks
     * exactly <code>Math.round(size*nonZeroFraction)</code> cells and initializes
     * them to <code>value</code>, all the rest will be initialized to zero. Note
     * that this is not the same as setting each cell with probability
     * <code>nonZeroFraction</code> to <code>value</code>.
     * 
     * @param size
     * @param nonZeroFraction
     * @param value
     * @return 
     * @throws IllegalArgumentException
     *             if <code>nonZeroFraction < 0 || nonZeroFraction > 1</code>.
     * @see cern.jet.random.tfloat.sampling.FloatRandomSampler
     */
    public FComplexMatrix1D sample(int size, float[] value, float nonZeroFraction) {
        float epsilon = 1e-05f;
        if (nonZeroFraction < 0 - epsilon || nonZeroFraction > 1 + epsilon)
            throw new IllegalArgumentException();
        if (nonZeroFraction < 0)
            nonZeroFraction = 0;
        if (nonZeroFraction > 1)
            nonZeroFraction = 1;

        FComplexMatrix1D matrix = make(size);

        int n = Math.round(size * nonZeroFraction);
        if (n == 0)
            return matrix;

        cern.jet.random.tfloat.sampling.FloatRandomSamplingAssistant sampler = new cern.jet.random.tfloat.sampling.FloatRandomSamplingAssistant(
                n, size, new cern.jet.random.tfloat.engine.FloatMersenneTwister());
        for (int i = 0; i < size; i++) {
            if (sampler.sampleNextElement()) {
                matrix.setQuick(i, value);
            }
        }

        return matrix;
    }

    /**
     * Constructs a list from the given matrix. The values are copied. So
     * subsequent changes in <code>values</code> are not reflected in the list, and
     * vice-versa.
     * 
     * @param values
     *            The values to be filled into the new list.
     * @return a new list.
     */
    public ArrayList<float[]> toList(FComplexMatrix1D values) {
        int size = (int) values.size();
        ArrayList<float[]> list = new ArrayList<float[]>(size);
        for (int i = 0; i < size; i++)
            list.set(i, values.getQuick(i));
        return list;
    }
}
