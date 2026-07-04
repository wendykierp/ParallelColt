package hep.aida.tfloat;

/**
 * A Java interface corresponding to the AIDA 3D Histogram.
 * <p>
 * <b>Note</b> All methods that accept a bin number as an argument will also
 * accept the constants OVERFLOW or UNDERFLOW as the argument, and as a result
 * give the contents of the resulting OVERFLOW or UNDERFLOW bin.
 * 
 * @see <a href="http://wwwinfo.cern.ch/asd/lhc++/AIDA/">AIDA</a>
 * @author Pavel Binko, Dino Ferrero Merlino, Wolfgang Hoschek, Tony Johnson,
 *         Andreas Pfeiffer, and others.
 * @version 1.0, 23/03/2000
 */
public interface FloatIHistogram3D extends FloatIHistogram {
    /**
     * The number of entries (ie the number of times fill was called for this
     * bin).
     * 
     * @param indexX
     *            the x bin number (0...Nx-1) or OVERFLOW or UNDERFLOW.
     * @param indexY
     *            the y bin number (0...Ny-1) or OVERFLOW or UNDERFLOW.
     * @param indexZ
     *            the z bin number (0...Nz-1) or OVERFLOW or UNDERFLOW.
     * @return 
     */
    public int binEntries(int indexX, int indexY, int indexZ);

    /**
     * The error on this bin.
     * 
     * @param indexX
     *            the x bin number (0...Nx-1) or OVERFLOW or UNDERFLOW.
     * @param indexY
     *            the y bin number (0...Ny-1) or OVERFLOW or UNDERFLOW.
     * @param indexZ
     *            the z bin number (0...Nz-1) or OVERFLOW or UNDERFLOW.
     * @return 
     */
    public float binError(int indexX, int indexY, int indexZ);

    /**
     * Total height of the corresponding bin (ie the sum of the weights in this
     * bin).
     * 
     * @param indexX
     *            the x bin number (0...Nx-1) or OVERFLOW or UNDERFLOW.
     * @param indexY
     *            the y bin number (0...Ny-1) or OVERFLOW or UNDERFLOW.
     * @param indexZ
     *            the z bin number (0...Nz-1) or OVERFLOW or UNDERFLOW.
     * @return 
     */
    public float binHeight(int indexX, int indexY, int indexZ);

    /**
     * Fill the histogram with weight 1; equivalent to <code>fill(x,y,z,1)</code>..
     * @param x
     * @param z
     * @param y
     */
    public void fill(float x, float y, float z);

    /**
     * Fill the histogram with specified weight.
     * @param x
     * @param weight
     * @param y
     * @param z
     */
    public void fill(float x, float y, float z, float weight);

    /**
     * Returns the mean of the histogram, as calculated on filling-time
     * projected on the X axis.
     * @return 
     */
    public float meanX();

    /**
     * Returns the mean of the histogram, as calculated on filling-time
     * projected on the Y axis.
     * @return 
     */
    public float meanY();

    /**
     * Returns the mean of the histogram, as calculated on filling-time
     * projected on the Z axis.
     * @return 
     */
    public float meanZ();

    /**
     * Indexes of the in-range bins containing the smallest and largest
     * binHeight(), respectively.
     * 
     * @return <code>{minBinX,minBinY,minBinZ, maxBinX,maxBinY,maxBinZ}</code>.
     */
    public int[] minMaxBins();

    /**
     * Create a projection parallel to the XY plane. Equivalent to
     * <code>sliceXY(UNDERFLOW,OVERFLOW)</code>.
     * @return 
     */
    public FloatIHistogram2D projectionXY();

    /**
     * Create a projection parallel to the XZ plane. Equivalent to
     * <code>sliceXZ(UNDERFLOW,OVERFLOW)</code>.
     * @return 
     */
    public FloatIHistogram2D projectionXZ();

    /**
     * Create a projection parallel to the YZ plane. Equivalent to
     * <code>sliceYZ(UNDERFLOW,OVERFLOW)</code>.
     * @return 
     */
    public FloatIHistogram2D projectionYZ();

    /**
     * Returns the rms of the histogram as calculated on filling-time projected
     * on the X axis.
     * @return 
     */
    public float rmsX();

    /**
     * Returns the rms of the histogram as calculated on filling-time projected
     * on the Y axis.
     * @return 
     */
    public float rmsY();

    /**
     * Returns the rms of the histogram as calculated on filling-time projected
     * on the Z axis.
     * @return 
     */
    public float rmsZ();

    /**
     * Create a slice parallel to the XY plane at bin indexZ and one bin wide.
     * Equivalent to <code>sliceXY(indexZ,indexZ)</code>.
     * @param indexZ
     * @return 
     */
    public FloatIHistogram2D sliceXY(int indexZ);

    /**
     * Create a slice parallel to the XY plane, between "indexZ1" and "indexZ2"
     * (inclusive). The returned IHistogram2D represents an instantaneous
     * snapshot of the histogram at the time the slice was created.
     * @param indexZ1
     * @param indexZ2
     * @return 
     */
    public FloatIHistogram2D sliceXY(int indexZ1, int indexZ2);

    /**
     * Create a slice parallel to the XZ plane at bin indexY and one bin wide.
     * Equivalent to <code>sliceXZ(indexY,indexY)</code>.
     * @param indexY
     * @return 
     */
    public FloatIHistogram2D sliceXZ(int indexY);

    /**
     * Create a slice parallel to the XZ plane, between "indexY1" and "indexY2"
     * (inclusive). The returned IHistogram2D represents an instantaneous
     * snapshot of the histogram at the time the slice was created.
     * @param indexY1
     * @param indexY2
     * @return 
     */
    public FloatIHistogram2D sliceXZ(int indexY1, int indexY2);

    /**
     * Create a slice parallel to the YZ plane at bin indexX and one bin wide.
     * Equivalent to <code>sliceYZ(indexX,indexX)</code>.
     * @param indexX
     * @return 
     */
    public FloatIHistogram2D sliceYZ(int indexX);

    /**
     * Create a slice parallel to the YZ plane, between "indexX1" and "indexX2"
     * (inclusive). The returned IHistogram2D represents an instantaneous
     * snapshot of the histogram at the time the slice was created.
     * @param indexX1
     * @param indexX2
     * @return 
     */
    public FloatIHistogram2D sliceYZ(int indexX1, int indexX2);

    /**
     * Return the X axis.
     * @return 
     */
    public FloatIAxis xAxis();

    /**
     * Return the Y axis.
     * @return 
     */
    public FloatIAxis yAxis();

    /**
     * Return the Z axis.
     * @return 
     */
    public FloatIAxis zAxis();
}
