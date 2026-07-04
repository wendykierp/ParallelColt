package hep.aida.tdouble.bin;

/**
 * Function objects computing dynamic bin aggregations; to be passed to generic
 * methods.
 * 
 * @see cern.colt.matrix.tdouble.algo.DoubleFormatter
 * @see cern.colt.matrix.tdouble.algo.DoubleStatistic
 * @author wolfgang.hoschek@cern.ch
 * @version 1.0, 09/24/99
 */
public class DoubleBinFunctions1D extends Object {
    /**
     * Little trick to allow for "aliasing", that is, renaming this class. Using
     * the aliasing you can instead write
     * <p>
     * <code>BinFunctions F = BinFunctions.functions; <br>
    someAlgo(F.max);</code>
     */
    public static final DoubleBinFunctions1D functions = new DoubleBinFunctions1D();

    /**
     * Function that returns <code>bin.max()</code>.
     */
    public static final DoubleBinFunction1D max = new DoubleBinFunction1D() {
        public final double apply(DynamicDoubleBin1D bin) {
            return bin.max();
        }

        public final String name() {
            return "Max";
        }
    };

    /**
     * Function that returns <code>bin.mean()</code>.
     */
    public static final DoubleBinFunction1D mean = new DoubleBinFunction1D() {
        public final double apply(DynamicDoubleBin1D bin) {
            return bin.mean();
        }

        public final String name() {
            return "Mean";
        }
    };

    /**
     * Function that returns <code>bin.median()</code>.
     */
    public static final DoubleBinFunction1D median = new DoubleBinFunction1D() {
        public final double apply(DynamicDoubleBin1D bin) {
            return bin.median();
        }

        public final String name() {
            return "Median";
        }
    };

    /**
     * Function that returns <code>bin.min()</code>.
     */
    public static final DoubleBinFunction1D min = new DoubleBinFunction1D() {
        public final double apply(DynamicDoubleBin1D bin) {
            return bin.min();
        }

        public final String name() {
            return "Min";
        }
    };

    /**
     * Function that returns <code>bin.rms()</code>.
     */
    public static final DoubleBinFunction1D rms = new DoubleBinFunction1D() {
        public final double apply(DynamicDoubleBin1D bin) {
            return bin.rms();
        }

        public final String name() {
            return "RMS";
        }
    };

    /**
     * Function that returns <code>bin.size()</code>.
     */
    public static final DoubleBinFunction1D size = new DoubleBinFunction1D() {
        public final double apply(DynamicDoubleBin1D bin) {
            return bin.size();
        }

        public final String name() {
            return "Size";
        }
    };

    /**
     * Function that returns <code>bin.standardDeviation()</code>.
     */
    public static final DoubleBinFunction1D stdDev = new DoubleBinFunction1D() {
        public final double apply(DynamicDoubleBin1D bin) {
            return bin.standardDeviation();
        }

        public final String name() {
            return "StdDev";
        }
    };

    /**
     * Function that returns <code>bin.sum()</code>.
     */
    public static final DoubleBinFunction1D sum = new DoubleBinFunction1D() {
        public final double apply(DynamicDoubleBin1D bin) {
            return bin.sum();
        }

        public final String name() {
            return "Sum";
        }
    };

    /**
     * Function that returns <code>bin.sumOfLogarithms()</code>.
     */
    public static final DoubleBinFunction1D sumLog = new DoubleBinFunction1D() {
        public final double apply(DynamicDoubleBin1D bin) {
            return bin.sumOfLogarithms();
        }

        public final String name() {
            return "SumLog";
        }
    };

    /**
     * Function that returns <code>bin.geometricMean()</code>.
     */
    public static final DoubleBinFunction1D geometricMean = new DoubleBinFunction1D() {
        public final double apply(DynamicDoubleBin1D bin) {
            return bin.geometricMean();
        }

        public final String name() {
            return "GeomMean";
        }
    };

    /**
     * Makes this class non instantiable, but still let's others inherit from
     * it.
     */
    protected DoubleBinFunctions1D() {
    }

    /**
     * Function that returns <code>bin.quantile(percentage)</code>.
     * 
     * @param percentage
     *            the percentage of the quantile (<code>0 &lt;= percentage &lt;= 1</code>
     *            ).
     * @return 
     */
    public static DoubleBinFunction1D quantile(final double percentage) {
        return new DoubleBinFunction1D() {
            public final double apply(DynamicDoubleBin1D bin) {
                return bin.quantile(percentage);
            }

            public final String name() {
                return new cern.colt.matrix.FormerFactory().create("%1.2G").form(percentage * 100) + "% Q.";
            }
        };
    }
}
