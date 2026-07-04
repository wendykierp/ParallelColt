/*
 * Copyright (C) 2003-2006 Bjørn-Ove Heimsund
 * 
 * This file is part of MTJ.
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or (at your
 * option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package cern.colt.matrix.io;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;

/**
 * Writes matrices and vectors
 */
public class MatrixVectorWriter extends PrintWriter {

    /**
     * Constructor for MatrixVectorWriter
     * 
     * @param out
     */
    public MatrixVectorWriter(OutputStream out) {
        super(out);
    }

    /**
     * Constructor for MatrixVectorWriter
     * 
     * @param out
     * @param autoFlush
     */
    public MatrixVectorWriter(OutputStream out, boolean autoFlush) {
        super(out, autoFlush);
    }

    /**
     * Constructor for MatrixVectorWriter
     * 
     * @param out
     */
    public MatrixVectorWriter(Writer out) {
        super(out);
    }

    /**
     * Constructor for MatrixVectorWriter
     * 
     * @param out
     * @param autoFlush
     */
    public MatrixVectorWriter(Writer out, boolean autoFlush) {
        super(out, autoFlush);
    }

    /**
     * Shifts the indexes. Useful for converting between 0- and 1-based
     * indicing.
     * 
     * @param num
     *            Added to every index
     * @param indexes
     *            indexes to shift
     */
    public void add(int num, int[] indexes) {
        for (int i = 0; i < indexes.length; ++i)
            indexes[i] += num;
    }

    /**
     * Prints the matrix info
     * @param info
     */
    public void printMatrixInfo(MatrixInfo info) {
        print(info.toString());
    }

    /**
     * Prints the vector info
     * @param info
     */
    public void printVectorInfo(VectorInfo info) {
        print(info.toString());
    }

    /**
     * Prints the matrix size
     * @param size
     * @param info
     */
    public void printMatrixSize(MatrixSize size, MatrixInfo info) {
        format("%10d %10d", size.numRows(), size.numColumns());
        if (info.isCoordinate())
            format(" %19d", size.numEntries());
        println();
    }

    /**
     * Prints the matrix size. Assumes coordinate format
     * @param size
     */
    public void printMatrixSize(MatrixSize size) {
        format("%10d %10d %19d\n", size.numRows(), size.numColumns(), size.numEntries());
    }

    /**
     * Prints the vector size
     * @param size
     * @param info
     */
    public void printVectorSize(VectorSize size, VectorInfo info) {
        format("%10d", size.size());
        if (info.isCoordinate())
            format(" %19d", size.numEntries());
        println();
    }

    /**
     * Prints the vector size. Assumes coordinate format
     * @param size
     */
    public void printVectorSize(VectorSize size) {
        format("%10d %19d\n", size.size(), size.numEntries());
    }

    /**
     * Prints an array to the underlying stream. One entry per line.
     * @param data
     */
    public void printArray(float[] data) {
        for (int i = 0; i < data.length; ++i)
            format("% .12e\n", data[i]);
    }

    /**
     * Prints an array to the underlying stream. One entry per line.
     * @param data
     */
    public void printArray(double[] data) {
        for (int i = 0; i < data.length; ++i)
            format("% .12e\n", data[i]);
    }

    /**
     * Prints an array to the underlying stream. One entry per line. The first
     * array specifies the real entries, and the second is the imaginary entries
     * @param dataR
     * @param dataI
     */
    public void printArray(float[] dataR, float[] dataI) {
        int size = dataR.length;
        if (size != dataI.length)
            throw new IllegalArgumentException("All arrays must be of the same size");
        for (int i = 0; i < size; ++i)
            format("% .12e % .12e\n", dataR[i], dataI[i]);
    }

    /**
     * Prints an array to the underlying stream. One entry per line. The first
     * array specifies the real entries, and the second is the imaginary entries
     * @param dataR
     * @param dataI
     */
    public void printArray(double[] dataR, double[] dataI) {
        int size = dataR.length;
        if (size != dataI.length)
            throw new IllegalArgumentException("All arrays must be of the same size");
        for (int i = 0; i < size; ++i)
            format("% .12e % .12e\n", dataR[i], dataI[i]);
    }

    /**
     * Prints an array to the underlying stream. One entry per line.
     * @param data
     */
    public void printArray(int[] data) {
        for (int i = 0; i < data.length; ++i)
            format("%10d\n", data[i]);
    }

    /**
     * Prints an array to the underlying stream. One entry per line.
     * @param data
     */
    public void printArray(long[] data) {
        for (int i = 0; i < data.length; ++i)
            format("%10d\n", data[i]);
    }

    /**
     * Prints the coordinate format to the underlying stream. One index and
     * entry on each line. The offset is added to the index, typically, this can
     * transform from a 0-based indicing to a 1-based.
     * @param index
     * @param offset
     * @param data
     */
    public void printCoordinate(int[] index, float[] data, int offset) {
        int size = index.length;
        if (size != data.length)
            throw new IllegalArgumentException("All arrays must be of the same size");
        for (int i = 0; i < size; ++i)
            format("%10d % .12e\n", index[i] + offset, data[i]);
    }

    /**
     * Prints the coordinate format to the underlying stream. One index and
     * entry on each line. The offset is added to the index, typically, this can
     * transform from a 0-based indicing to a 1-based.
     * @param index
     * @param offset
     * @param data
     */
    public void printCoordinate(int[] index, double[] data, int offset) {
        int size = index.length;
        if (size != data.length)
            throw new IllegalArgumentException("All arrays must be of the same size");
        for (int i = 0; i < size; ++i)
            format("%10d % .12e\n", index[i] + offset, data[i]);
    }

    /**
     * Prints the coordinate format to the underlying stream. One index and
     * entry on each line. The offset is added to the index, typically, this can
     * transform from a 0-based indicing to a 1-based.
     * @param index
     * @param offset
     * @param data
     */
    public void printCoordinate(int[] index, int[] data, int offset) {
        int size = index.length;
        if (size != data.length)
            throw new IllegalArgumentException("All arrays must be of the same size");
        for (int i = 0; i < size; ++i)
            format("%10d %10d\n", index[i] + offset, data[i]);
    }

    /**
     * Prints the coordinate format to the underlying stream. One index and
     * entry on each line. The offset is added to the index, typically, this can
     * transform from a 0-based indicing to a 1-based.
     * @param index
     * @param offset
     * @param data
     */
    public void printCoordinate(int[] index, long[] data, int offset) {
        int size = index.length;
        if (size != data.length)
            throw new IllegalArgumentException("All arrays must be of the same size");
        for (int i = 0; i < size; ++i)
            format("%10d %10d\n", index[i] + offset, data[i]);
    }

    /**
     * Prints the coordinate format to the underlying stream. One index pair and
     * entry on each line. The offset is added to each index, typically, this
     * can transform from a 0-based indicing to a 1-based.
     * @param row
     * @param offset
     * @param column
     * @param data
     */
    public void printCoordinate(int[] row, int[] column, float[] data, int offset) {
        int size = row.length;
        if (size != column.length || size != data.length)
            throw new IllegalArgumentException("All arrays must be of the same size");
        for (int i = 0; i < size; ++i)
            format("%10d %10d % .12e\n", row[i] + offset, column[i] + offset, data[i]);
    }

    /**
     * Prints the coordinate format to the underlying stream. One index pair and
     * entry on each line. The offset is added to each index, typically, this
     * can transform from a 0-based indicing to a 1-based.
     * @param row
     * @param offset
     * @param column
     * @param data
     */
    public void printCoordinate(int[] row, int[] column, double[] data, int offset) {
        int size = row.length;
        if (size != column.length || size != data.length)
            throw new IllegalArgumentException("All arrays must be of the same size");
        for (int i = 0; i < size; ++i)
            format("%10d %10d % .12e\n", row[i] + offset, column[i] + offset, data[i]);
    }

    /**
     * Prints the coordinate format to the underlying stream. One index and
     * entry on each line. The offset is added to each index, typically, this
     * can transform from a 0-based indicing to a 1-based. The first float array
     * specifies the real entries, and the second is the imaginary entries
     * @param index
     * @param offset
     * @param dataR
     * @param dataI
     */
    public void printCoordinate(int[] index, float[] dataR, float[] dataI, int offset) {
        int size = index.length;
        if (size != dataR.length || size != dataI.length)
            throw new IllegalArgumentException("All arrays must be of the same size");
        for (int i = 0; i < size; ++i)
            format("%10d % .12e % .12e\n", index[i] + offset, dataR[i], dataI[i]);
    }

    /**
     * Prints the coordinate format to the underlying stream. One index and
     * entry on each line. The offset is added to each index, typically, this
     * can transform from a 0-based indicing to a 1-based. The first double
     * array specifies the real entries, and the second is the imaginary entries
     * @param index
     * @param offset
     * @param dataR
     * @param dataI
     */
    public void printCoordinate(int[] index, double[] dataR, double[] dataI, int offset) {
        int size = index.length;
        if (size != dataR.length || size != dataI.length)
            throw new IllegalArgumentException("All arrays must be of the same size");
        for (int i = 0; i < size; ++i)
            format("%10d % .12e % .12e\n", index[i] + offset, dataR[i], dataI[i]);
    }

    /**
     * Prints the coordinate format to the underlying stream. One index pair and
     * entry on each line. The offset is added to each index, typically, this
     * can transform from a 0-based indicing to a 1-based. The first float array
     * specifies the real entries, and the second is the imaginary entries
     * @param row
     * @param offset
     * @param column
     * @param dataI
     * @param dataR
     */
    public void printCoordinate(int[] row, int[] column, float[] dataR, float[] dataI, int offset) {
        int size = row.length;
        if (size != column.length || size != dataR.length || size != dataI.length)
            throw new IllegalArgumentException("All arrays must be of the same size");
        for (int i = 0; i < size; ++i)
            format("%10d %10d % .12e % .12e\n", row[i] + offset, column[i] + offset, dataR[i], dataI[i]);
    }

    /**
     * Prints the coordinate format to the underlying stream. One index pair and
     * entry on each line. The offset is added to each index, typically, this
     * can transform from a 0-based indicing to a 1-based. The first double
     * array specifies the real entries, and the second is the imaginary entries
     * @param row
     * @param offset
     * @param column
     * @param dataI
     * @param dataR
     */
    public void printCoordinate(int[] row, int[] column, double[] dataR, double[] dataI, int offset) {
        int size = row.length;
        if (size != column.length || size != dataR.length || size != dataI.length)
            throw new IllegalArgumentException("All arrays must be of the same size");
        for (int i = 0; i < size; ++i)
            format("%10d %10d % .12e % .12e\n", row[i] + offset, column[i] + offset, dataR[i], dataI[i]);
    }

    /**
     * Prints the coordinate format to the underlying stream. One index pair and
     * entry on each line. The offset is added to each index, typically, this
     * can transform from a 0-based indicing to a 1-based.
     * @param row
     * @param offset
     * @param column
     * @param data
     */
    public void printCoordinate(int[] row, int[] column, int[] data, int offset) {
        int size = row.length;
        if (size != column.length || size != data.length)
            throw new IllegalArgumentException("All arrays must be of the same size");
        for (int i = 0; i < size; ++i)
            format("%10d %10d %19d\n", row[i] + offset, column[i] + offset, data[i]);
    }

    /**
     * Prints the coordinate format to the underlying stream. One index pair and
     * entry on each line. The offset is added to each index, typically, this
     * can transform from a 0-based indicing to a 1-based.
     * @param row
     * @param offset
     * @param column
     * @param data
     */
    public void printCoordinate(int[] row, int[] column, long[] data, int offset) {
        int size = row.length;
        if (size != column.length || size != data.length)
            throw new IllegalArgumentException("All arrays must be of the same size");
        for (int i = 0; i < size; ++i)
            format("%10d %10d %19d\n", row[i] + offset, column[i] + offset, data[i]);
    }

    /**
     * Prints the coordinates to the underlying stream. One index pair on each
     * line. The offset is added to each index, typically, this can transform
     * from a 0-based indicing to a 1-based.
     * @param row
     * @param offset
     * @param column
     */
    public void printPattern(int[] row, int[] column, int offset) {
        int size = row.length;
        if (size != column.length)
            throw new IllegalArgumentException("All arrays must be of the same size");
        for (int i = 0; i < size; ++i)
            format("%10d %10d\n", row[i] + offset, column[i] + offset);
    }

    /**
     * Prints the coordinates to the underlying stream. One index on each line.
     * The offset is added to each index, typically, this can transform from a
     * 0-based indicing to a 1-based.
     * @param index
     * @param offset
     */
    public void printPattern(int[] index, int offset) {
        int size = index.length;
        for (int i = 0; i < size; ++i)
            format("%10d\n", index[i] + offset);
    }

    /**
     * Prints the coordinate format to the underlying stream. One index pair and
     * entry on each line
     * @param row
     * @param data
     * @param column
     */
    public void printCoordinate(int[] row, int[] column, float[] data) {
        printCoordinate(row, column, data, 0);
    }

    /**
     * Prints the coordinate format to the underlying stream. One index pair and
     * entry on each line
     * @param row
     * @param data
     * @param column
     */
    public void printCoordinate(int[] row, int[] column, double[] data) {
        printCoordinate(row, column, data, 0);
    }

    /**
     * Prints the coordinate format to the underlying stream. One index pair and
     * entry on each line. The first double array specifies the real entries,
     * and the second is the imaginary entries
     * @param row
     * @param dataI
     * @param column
     * @param dataR
     */
    public void printCoordinate(int[] row, int[] column, float[] dataR, float[] dataI) {
        printCoordinate(row, column, dataR, dataI, 0);
    }

    /**
     * Prints the coordinate format to the underlying stream. One index pair and
     * entry on each line. The first double array specifies the real entries,
     * and the second is the imaginary entries
     * @param row
     * @param dataI
     * @param column
     * @param dataR
     */
    public void printCoordinate(int[] row, int[] column, double[] dataR, double[] dataI) {
        printCoordinate(row, column, dataR, dataI, 0);
    }

    /**
     * Prints the coordinate format to the underlying stream. One index pair and
     * entry on each line
     * @param row
     * @param data
     * @param column
     */
    public void printCoordinate(int[] row, int[] column, int[] data) {
        printCoordinate(row, column, data, 0);
    }

    /**
     * Prints the coordinate format to the underlying stream. One index pair and
     * entry on each line
     * @param row
     * @param data
     * @param column
     */
    public void printCoordinate(int[] row, int[] column, long[] data) {
        printCoordinate(row, column, data, 0);
    }

    /**
     * Prints the coordinates to the underlying stream. One index pair on each
     * line
     * @param row
     * @param column
     */
    public void printPattern(int[] row, int[] column) {
        printPattern(row, column, 0);
    }

    /**
     * Prints the coordinate format to the underlying stream. One index and
     * entry on each line
     * @param index
     * @param data
     */
    public void printCoordinate(int[] index, float[] data) {
        printCoordinate(index, data, 0);
    }

    /**
     * Prints the coordinate format to the underlying stream. One index and
     * entry on each line
     * @param index
     * @param data
     */
    public void printCoordinate(int[] index, double[] data) {
        printCoordinate(index, data, 0);
    }

    /**
     * Prints the coordinate format to the underlying stream. One index and
     * entry on each line. The first double array specifies the real entries,
     * and the second is the imaginary entries
     * @param index
     * @param dataI
     * @param dataR
     */
    public void printCoordinate(int[] index, float[] dataR, float[] dataI) {
        printCoordinate(index, dataR, dataI, 0);
    }

    /**
     * Prints the coordinate format to the underlying stream. One index and
     * entry on each line. The first double array specifies the real entries,
     * and the second is the imaginary entries
     * @param index
     * @param dataI
     * @param dataR
     */
    public void printCoordinate(int[] index, double[] dataR, double[] dataI) {
        printCoordinate(index, dataR, dataI, 0);
    }

    /**
     * Prints the coordinate format to the underlying stream. One index and
     * entry on each line
     * @param index
     * @param data
     */
    public void printCoordinate(int[] index, int[] data) {
        printCoordinate(index, data, 0);
    }

    /**
     * Prints the coordinate format to the underlying stream. One index and
     * entry on each line
     * @param index
     * @param data
     */
    public void printCoordinate(int[] index, long[] data) {
        printCoordinate(index, data, 0);
    }

    /**
     * Prints the coordinates to the underlying stream. One index on each line
     * @param index
     */
    public void printPattern(int[] index) {
        printPattern(index, 0);
    }

    /**
     * Prints all the comments. Prepends a '%' and appends a newline to every
     * comment
     * @param comments
     */
    public void printComments(String[] comments) {
        for (String comment : comments)
            println("%" + comment);
    }

}
