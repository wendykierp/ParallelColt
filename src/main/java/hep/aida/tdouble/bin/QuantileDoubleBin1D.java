package hep.aida.tdouble.bin;

import cern.colt.list.tdouble.DoubleArrayList;
import cern.jet.random.tdouble.engine.DoubleRandomEngine;
import cern.jet.stat.tdouble.quantile.DoubleQuantileFinder;
import cern.jet.stat.tdouble.quantile.DoubleQuantileFinderFactory;

/**
 * 1-dimensional non-rebinnable bin holding <code>double</code> elements with
 * scalable quantile operations defined upon; Using little main memory, quickly
 * computes approximate quantiles over very large data sequences with and even
 * without a-priori knowledge of the number of elements to be filled;
 * Conceptually a strongly lossily compressed multiset (or bag); Guarantees to
 * respect the worst case approximation error specified upon instance
 * construction. First see the <a href="package-summary.html">package
 * summary</a> and javadoc <a href="package-tree.html">tree view</a> to get the
 * broad picture.
 * <p>
 * <b>Motivation and Problem:</b> Intended to help scale applications requiring
 * quantile computation. Quantile computation on very large data sequences is
 * problematic, for the following reasons: Computing quantiles requires sorting
 * the data sequence. To sort a data sequence the entire data sequence needs to
 * be available. Thus, data cannot be thrown away during filling (as done by
 * static bins like {@link StaticDoubleBin1D} and
 * {@link MightyStaticDoubleBin1D}). It needs to be kept, either in main memory
 * or on disk. There is often not enough main memory available. Thus, during
 * filling data needs to be streamed onto disk. Sorting disk resident data is
 * prohibitively time consuming. As a consequence, traditional methods either
 * need very large memories (like {@link DynamicDoubleBin1D}) or time consuming
 * disk based sorting.
 * <p>
 * This class proposes to efficiently solve the problem, at the expense of
 * producing approximate rather than exact results. It can deal with infinitely
 * many elements without resorting to disk. The main memory requirements are
 * smaller than for any other known approximate technique by an order of
 * magnitude. They get even smaller if an upper limit on the maximum number of
 * elements ever to be added is known a-priori.
 * <p>
 * <b>Approximation error:</b> The approximation guarantees are parametrizable
 * and explicit but probabilistic, and apply for arbitrary value distributions
 * and arrival distributions of the data sequence. In other words, this class
 * guarantees to respect the worst case approximation error specified upon
 * instance construction to a certain probability. Of course, if it is specified
 * that the approximation error should not exceed some number <i>very close</i>
 * to zero, this class will potentially consume just as much memory as any of
 * the traditional exact techniques would do. However, for errors larger than
 * 10<sup>-5</sup>, its memory requirements are modest, as shown by the table
 * below.
 * <p>
 * <b>Main memory requirements:</b> Given in megabytes, assuming a single
 * element (<code>double</code>) takes 8 byte. The number of elements required is
 * then <code>MB*1024*1024/8</code>.
 * <p>
 * <b>Parameters:</b>
 * <ul>
 * <li><i>epsilon</i> - the maximum allowed approximation error on quantiles; in
 * <code>[0.0,1.0]</code>. To get exact rather than approximate quantiles, set
 * <code>epsilon=0.0</code>;
 * 
 * <li><i>delta</i> - the probability allowed that the approximation error fails
 * to be smaller than epsilon; in <code>[0.0,1.0]</code>. To avoid probabilistic
 * answers, set <code>delta=0.0</code>. For example, <code>delta = 0.0001</code> is
 * equivalent to a confidence of <code>99.99%</code>.
 * 
 * <li><i>quantiles</i> - the number of quantiles to be computed; in
 * <code>[0,Integer.MAX_VALUE]</code>.
 * 
 * <li><i>is N known?</i> - specifies whether the exact size of the dataset over
 * which quantiles are to be computed is known.
 * 
 * <li>N<sub>max</sub> - the exact dataset size, if known. Otherwise, an upper
 * limit on the dataset size. If no upper limit is known set to infinity (
 * <code>Long.MAX_VALUE</code>).
 * </ul>
 * N<sub>max</sub>=inf - we are sure that exactly (<i>known</i>) or less than
 * (<i>unknown</i>) infinity elements will be added. <br>
 * N<sub>max</sub>=10<sup>6</sup> - we are sure that exactly (<i>known</i>) or
 * less than (<i>unknown</i>) 10<sup>6</sup> elements will be added. <br>
 * N<sub>max</sub>=10<sup>7</sup> - we are sure that exactly (<i>known</i>) or
 * less than (<i>unknown</i>) 10<sup>7</sup> elements will be added. <br>
 * N<sub>max</sub>=10<sup>8</sup> - we are sure that exactly (<i>known</i>) or
 * less than (<i>unknown</i>) 10<sup>8</sup> elements will be added.
 * <p>
 * 
 * 
 * 
 * <table width="75%" border="1" cellpadding="6" cellspacing="0" align="center">
 * <tr align="center" valign="middle">
 * <td width="20%" nowrap columnspan="13" bgcolor="#33CC66"><font
 * color="#000000"></font> <div align="center"><font color="#000000"
 * size="5">Required main memory [MB]</font></div></td>
 * </tr>
 * <tr align="center" valign="middle">
 * <td width="7%" nowrap rowspan="2" bgcolor="#FF9966"><font
 * color="#000000">#quantiles</font></td>
 * <td width="6%" nowrap rowspan="2" bgcolor="#FF9966"><div
 * align="center"></div> <div align="center"></div> <div align="center"><font
 * color="#000000">epsilon</font></div></td>
 * <td width="6%" nowrap rowspan="2" bgcolor="#FF9966"><font
 * color="#000000">delta</font></td>
 * <td width="1%" nowrap rowspan="31">&nbsp;</td>
 * <td nowrap columnspan="4" bgcolor="#FF9966"><font color="#000000">N
 * unknown</font></td>
 * <td width="1%" nowrap align="center" valign="middle" bgcolor="#C0C0C0" rowspan="31">
 * <font color="#000000"></font><font color="#000000"></font><font
 * color="#000000"></font><font color="#000000"></font><font
 * color="#000000"></font><font color="#000000"></font><font
 * color="#000000"></font><font color="#000000"></font><font
 * color="#000000"></font><font color="#000000"></font><font
 * color="#000000"></font><font color="#000000"></font><font
 * color="#000000"></font><font color="#000000"></font><font
 * color="#000000"></font><font color="#000000"></font><font
 * color="#000000"></font><font color="#000000"></font><font
 * color="#000000"></font><font color="#000000"></font><font
 * color="#000000"></font><font color="#000000"></font><font
 * color="#000000"></font><font color="#000000"></font><font
 * color="#000000"></font><font color="#000000"></font><font
 * color="#000000"></font><font color="#000000"></font><font
 * color="#000000"></font><font color="#000000"></font><font
 * color="#000000"></font><font color="#000000"></font><font
 * color="#000000"></font><font color="#000000"></font><font
 * color="#000000"></font><font color="#000000"></font><font
 * color="#000000"></font></td>
 * <td nowrap columnspan="4" bgcolor="#FF9966"><font color="#000000">N known</font>
 * </td>
 * </tr>
 * <tr align="center" valign="middle">
 * <td width="7%" nowrap bgcolor="#FF9966"><font
 * color="#000000">N<sub>max</sub>=inf</font></td>
 * <td width="9%" nowrap bgcolor="#FF9966"><font
 * color="#000000">N<sub>max</sub>=10<sup>6</sup></font></td>
 * <td width="9%" nowrap bgcolor="#FF9966"><font
 * color="#000000">N<sub>max</sub>=10<sup>7</sup></font></td>
 * <td width="9%" nowrap bgcolor="#FF9966"><font
 * color="#000000">N<sub>max</sub>=10<sup>8</sup></font></td>
 * <td width="8%" nowrap bgcolor="#FF9966"><font
 * color="#000000">N<sub>max</sub>=inf</font></td>
 * <td width="9%" nowrap bgcolor="#FF9966"><font
 * color="#000000">N<sub>max</sub>=10<sup>6</sup></font></td>
 * <td width="9%" nowrap bgcolor="#FF9966"><font
 * color="#000000">N<sub>max</sub>=10<sup>7</sup></font></td>
 * <td width="19%" nowrap bgcolor="#FF9966"><font
 * color="#000000">N<sub>max</sub>=10<sup>8</sup></font></td>
 * </tr>
 * <tr align="center" valign="middle">
 * <td nowrap bgcolor="#C0C0C0" columnspan="3"><font color="#000000"></font> <div
 * align="center"></div> <font color="#000000"></font></td>
 * <td nowrap columnspan="4">&nbsp;</td>
 * <td nowrap columnspan="4">&nbsp;</td>
 * </tr>
 * <tr align="center" valign="middle">
 * <td width="7%" nowrap bgcolor="#FFCCCC"><font color="#000000">any</font></td>
 * <td width="6%" nowrap bgcolor="#FFCCCC"><div align="center"><font
 * color="#000000">0</font></div></td>
 * <td width="6%" nowrap bgcolor="#FFCCCC"><font color="#000000">any</font></td>
 * <td width="7%" nowrap bgcolor="#66CCFF"><font color="#000000">infinity</font>
 * </td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">7.6</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">76</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">762</font></td>
 * <td width="8%" nowrap bgcolor="#66CCFF"><font color="#000000">infinity</font>
 * </td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">7.6</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">76</font></td>
 * <td width="19%" nowrap bgcolor="#66CCFF"><font color="#000000">762</font></td>
 * </tr>
 * <tr align="center" valign="middle">
 * <td width="7%" nowrap rowspan="6" bgcolor="#FFCCCC"><font
 * color="#000000">any</font></td>
 * <td width="6%" nowrap bgcolor="#FFCCCC"><div align="center"><font
 * color="#000000">10<sup> -1</sup></font></div></td>
 * <td width="6%" nowrap rowspan="6" bgcolor="#FFCCCC"><font
 * color="#000000">0</font></td>
 * <td width="7%" nowrap rowspan="6" bgcolor="#66CCFF"><font
 * color="#000000">infinity</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">0.003</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">0.005</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">0.006</font></td>
 * <td width="8%" nowrap bgcolor="#66CCFF"><font color="#000000">0.03</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">0.003</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">0.005</font></td>
 * <td width="19%" nowrap bgcolor="#66CCFF"><font color="#000000">0.006</font></td>
 * </tr>
 * <tr align="center" valign="middle">
 * <td width="6%" nowrap bgcolor="#FFCCCC"><div align="center"><font
 * color="#000000">10<sup> -2</sup></font></div></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">0.02</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">0.03</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">0.05</font></td>
 * <td width="8%" nowrap bgcolor="#66CCFF"><font color="#000000">0.31</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">0.02</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">0.03</font></td>
 * <td width="19%" nowrap bgcolor="#66CCFF"><font color="#000000">0.05</font></td>
 * </tr>
 * <tr align="center" valign="middle">
 * <td width="6%" nowrap bgcolor="#FFCCCC"><div align="center"><font
 * color="#000000">10<sup> -3</sup></font></div></td>
 * <td width="9%" nowrap align="center" valign="middle" bgcolor="#66CCFF"><font
 * color="#000000">0.12</font></td>
 * <td width="9%" nowrap align="center" valign="middle" bgcolor="#66CCFF"><font
 * color="#000000">0.2</font></td>
 * <td width="9%" nowrap align="center" valign="middle" bgcolor="#66CCFF"><font
 * color="#000000">0.3</font></td>
 * <td width="8%" nowrap bgcolor="#66CCFF"><font color="#000000">2.7</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">0.12</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">0.2</font></td>
 * <td width="19%" nowrap bgcolor="#66CCFF"><font color="#000000">0.3</font></td>
 * </tr>
 * <tr align="center" valign="middle">
 * <td width="6%" nowrap bgcolor="#FFCCCC"><div align="center"><font
 * color="#000000">10<sup> -4</sup></font></div></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">0.6</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">1.2</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">2.1</font></td>
 * <td width="8%" nowrap bgcolor="#66CCFF"><font color="#000000">26.9</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">0.6</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">1.2</font></td>
 * <td width="19%" nowrap bgcolor="#66CCFF"><font color="#000000">2.1</font></td>
 * </tr>
 * <tr align="center" valign="middle">
 * <td width="6%" nowrap bgcolor="#FFCCCC"><div align="center"><font
 * color="#000000">10<sup> -5</sup></font></div></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">2.5</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">6.4</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">11.6</font></td>
 * <td width="8%" nowrap bgcolor="#66CCFF"><font color="#000000">205</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">2.5</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">6.4</font></td>
 * <td width="19%" nowrap bgcolor="#66CCFF"><font color="#000000">11.6</font></td>
 * </tr>
 * <tr align="center" valign="middle">
 * <td width="6%" nowrap bgcolor="#FFCCCC"><div align="center"><font
 * color="#000000">10<sup> -6</sup></font></div></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">7.6</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">25.4</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">63.6</font></td>
 * <td width="8%" nowrap bgcolor="#66CCFF"><font color="#000000">1758</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">7.6</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">25.4</font></td>
 * <td width="19%" nowrap bgcolor="#66CCFF"><font color="#000000">63.6</font></td>
 * </tr>
 * <tr align="center" valign="middle">
 * <td nowrap bgcolor="#C0C0C0" columnspan="3"><font color="#000000"></font><font
 * color="#000000"></font><font color="#000000"></font></td>
 * <td nowrap columnspan="4">&nbsp;</td>
 * <td nowrap columnspan="4">&nbsp;</td>
 * </tr>
 * <tr align="center" valign="middle">
 * <td width="7%" nowrap bgcolor="#FFCCCC" rowspan="8"><font
 * color="#000000">100</font><font color="#000000"></font><font
 * color="#000000"></font><font color="#000000"></font><font
 * color="#000000"></font><font color="#000000"></font><font
 * color="#000000"></font><font color="#000000"></font></td>
 * <td width="6%" nowrap align="center" valign="middle" bgcolor="#FFCCCC" rowspan="2">
 * <div align="center"><font color="#000000">10<sup> -2</sup></font></div> <font
 * color="#000000"></font></td>
 * <td width="6%" nowrap bgcolor="#FFCCCC"><font color="#000000">10<sup>
 * -1</sup></font></td>
 * <td width="7%" nowrap bgcolor="#66CCFF"><font color="#000000">0.033</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">0.021</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">0.03</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">0.03</font></td>
 * <td width="8%" nowrap bgcolor="#66CCFF"><font color="#000000">0.020</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">0.020</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">0.020</font></td>
 * <td width="19%" nowrap bgcolor="#66CCFF"><font color="#000000">0.020</font></td>
 * </tr>
 * <tr align="center" valign="middle">
 * <td width="6%" nowrap bgcolor="#FFCCCC"><font color="#000000">10<sup>
 * -5</sup></font></td>
 * <td width="7%" nowrap bgcolor="#66CCFF"><font color="#000000">0.038</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">0.021</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">0.03</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">0.04</font></td>
 * <td width="8%" nowrap bgcolor="#66CCFF"><font color="#000000">0.024</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">0.020</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">0.020</font></td>
 * <td width="19%" nowrap bgcolor="#66CCFF"><font color="#000000">0.020</font></td>
 * </tr>
 * <tr align="center" valign="middle">
 * <td width="6%" nowrap align="center" valign="middle" bgcolor="#FFCCCC" rowspan="2">
 * <div align="center"><font color="#000000">10<sup> -3</sup></font></div> <font
 * color="#000000"></font></td>
 * <td width="6%" nowrap align="center" valign="middle" bgcolor="#FFCCCC"><font
 * color="#000000">10<sup> -1</sup></font></td>
 * <td width="7%" nowrap bgcolor="#66CCFF"><font color="#000000">0.48</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">0.12</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">0.2</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">0.3</font></td>
 * <td width="8%" nowrap bgcolor="#66CCFF"><font color="#000000">0.32</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">0.12</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">0.2</font></td>
 * <td width="19%" nowrap bgcolor="#66CCFF"><font color="#000000">0.3</font></td>
 * </tr>
 * <tr align="center" valign="middle">
 * <td width="6%" nowrap align="center" valign="middle" bgcolor="#FFCCCC"><font
 * color="#000000">10<sup> -5</sup></font></td>
 * <td width="7%" nowrap bgcolor="#66CCFF"><font color="#000000">0.54</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">0.12</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">0.2</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">0.3</font></td>
 * <td width="8%" nowrap bgcolor="#66CCFF"><font color="#000000">0.37</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">0.12</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">0.2</font></td>
 * <td width="19%" nowrap bgcolor="#66CCFF"><font color="#000000">0.3</font></td>
 * </tr>
 * <tr align="center" valign="middle">
 * <td width="6%" nowrap align="center" valign="middle" bgcolor="#FFCCCC" rowspan="2">
 * <div align="center"><font color="#000000">10<sup> -4</sup></font></div> <font
 * color="#000000"></font></td>
 * <td width="6%" nowrap align="center" valign="middle" bgcolor="#FFCCCC"><font
 * color="#000000">10<sup> -1</sup></font></td>
 * <td width="7%" nowrap bgcolor="#66CCFF"><font color="#000000">6.6</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">0.6</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">1.2</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">2.1</font></td>
 * <td width="8%" nowrap bgcolor="#66CCFF"><font color="#000000">4.6</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">0.6</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">1.2</font></td>
 * <td width="19%" nowrap bgcolor="#66CCFF"><font color="#000000">2.1</font></td>
 * </tr>
 * <tr align="center" valign="middle">
 * <td width="6%" nowrap align="center" valign="middle" bgcolor="#FFCCCC"><font
 * color="#000000">10<sup> -5</sup></font></td>
 * <td width="7%" nowrap bgcolor="#66CCFF"><font color="#000000">7.2</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">0.6</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">1.2</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">2.1</font></td>
 * <td width="8%" nowrap bgcolor="#66CCFF"><font color="#000000">5.2</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">0.6</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">1.2</font></td>
 * <td width="19%" nowrap bgcolor="#66CCFF"><font color="#000000">2.1</font></td>
 * </tr>
 * <tr align="center" valign="middle">
 * <td width="6%" nowrap align="center" valign="middle" bgcolor="#FFCCCC" rowspan="2">
 * <div align="center"><font color="#000000">10<sup> -5</sup></font></div> <font
 * color="#000000"></font></td>
 * <td width="6%" nowrap align="center" valign="middle" bgcolor="#FFCCCC"><font
 * color="#000000">10<sup> -1</sup></font></td>
 * <td width="7%" nowrap bgcolor="#66CCFF"><font color="#000000">86</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">2.5</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">6.4</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">11.6</font></td>
 * <td width="8%" nowrap bgcolor="#66CCFF"><font color="#000000">63</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">2.5</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">6.4</font></td>
 * <td width="19%" nowrap bgcolor="#66CCFF"><font color="#000000">11.6</font></td>
 * </tr>
 * <tr align="center" valign="middle">
 * <td width="6%" nowrap align="center" valign="middle" bgcolor="#FFCCCC"><font
 * color="#000000">10<sup> -5</sup></font></td>
 * <td width="7%" nowrap bgcolor="#66CCFF"><font color="#000000">94</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">2.5</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">6.4</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">11.6</font></td>
 * <td width="8%" nowrap bgcolor="#66CCFF"><font color="#000000">70</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">2.5</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">6.4</font></td>
 * <td width="19%" nowrap bgcolor="#66CCFF"><font color="#000000">11.6</font></td>
 * </tr>
 * <tr align="center" valign="middle">
 * <td nowrap bgcolor="#C0C0C0" columnspan="3"><font color="#000000"></font><font
 * color="#000000"></font><font color="#000000"></font></td>
 * <td nowrap columnspan="4">&nbsp;</td>
 * <td nowrap columnspan="4">&nbsp;</td>
 * </tr>
 * <tr align="center" valign="middle">
 * <td width="7%" nowrap align="center" valign="middle" bgcolor="#FFCCCC" rowspan="8">
 * <font color="#000000">10000</font><font color="#000000"></font><font
 * color="#000000"></font><font color="#000000"></font><font
 * color="#000000"></font><font color="#000000"></font><font
 * color="#000000"></font><font color="#000000"></font></td>
 * <td width="6%" nowrap align="center" valign="middle" bgcolor="#FFCCCC" rowspan="2">
 * <div align="center"><font color="#000000">10<sup> -2</sup></font></div> <font
 * color="#000000"></font></td>
 * <td width="6%" nowrap align="center" valign="middle" bgcolor="#FFCCCC"><font
 * color="#000000">10<sup> -1</sup></font></td>
 * <td width="7%" nowrap bgcolor="#66CCFF"><font color="#000000">0.04</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">0.02</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">0.03</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">0.04</font></td>
 * <td width="8%" nowrap bgcolor="#66CCFF"><font color="#000000">0.02</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">0.02</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">0.02</font></td>
 * <td width="19%" nowrap bgcolor="#66CCFF"><font color="#000000">0.02</font></td>
 * </tr>
 * <tr align="center" valign="middle">
 * <td width="6%" nowrap align="center" valign="middle" bgcolor="#FFCCCC"><font
 * color="#000000">10<sup> -5</sup></font></td>
 * <td width="7%" nowrap bgcolor="#66CCFF"><font color="#000000">0.04</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">0.02</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">0.03</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">0.04</font></td>
 * <td width="8%" nowrap bgcolor="#66CCFF"><font color="#000000">0.03</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">0.02</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">0.03</font></td>
 * <td width="19%" nowrap bgcolor="#66CCFF"><font color="#000000">0.03</font></td>
 * </tr>
 * <tr align="center" valign="middle">
 * <td width="6%" nowrap align="center" valign="middle" bgcolor="#FFCCCC" rowspan="2">
 * <div align="center"><font color="#000000">10<sup> -3</sup></font></div> <font
 * color="#000000"></font></td>
 * <td width="6%" nowrap align="center" valign="middle" bgcolor="#FFCCCC"><font
 * color="#000000">10<sup> -1</sup></font></td>
 * <td width="7%" nowrap bgcolor="#66CCFF"><font color="#000000">0.52</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">0.12</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">0.21</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">0.3</font></td>
 * <td width="8%" nowrap bgcolor="#66CCFF"><font color="#000000">0.35</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">0.12</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">0.21</font></td>
 * <td width="19%" nowrap bgcolor="#66CCFF"><font color="#000000">0.3</font></td>
 * </tr>
 * <tr align="center" valign="middle">
 * <td width="6%" nowrap align="center" valign="middle" bgcolor="#FFCCCC"><font
 * color="#000000">10<sup> -5</sup></font></td>
 * <td width="7%" nowrap bgcolor="#66CCFF"><font color="#000000">0.56</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">0.12</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">0.21</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">0.3</font></td>
 * <td width="8%" nowrap bgcolor="#66CCFF"><font color="#000000">0.38</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">0.12</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">0.21</font></td>
 * <td width="19%" nowrap bgcolor="#66CCFF"><font color="#000000">0.3</font></td>
 * </tr>
 * <tr align="center" valign="middle">
 * <td width="6%" nowrap align="center" valign="middle" bgcolor="#FFCCCC" rowspan="2">
 * <div align="center"><font color="#000000">10<sup> -4</sup></font></div> <font
 * color="#000000"></font></td>
 * <td width="6%" nowrap align="center" valign="middle" bgcolor="#FFCCCC"><font
 * color="#000000">10<sup> -1</sup></font></td>
 * <td width="7%" nowrap bgcolor="#66CCFF"><font color="#000000">7.0</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">0.64</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">1.2</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">2.1</font></td>
 * <td width="8%" nowrap bgcolor="#66CCFF"><font color="#000000">5.0</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">0.64</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">1.2</font></td>
 * <td width="19%" nowrap bgcolor="#66CCFF"><font color="#000000">2.1</font></td>
 * </tr>
 * <tr align="center" valign="middle">
 * <td width="6%" nowrap align="center" valign="middle" bgcolor="#FFCCCC"><font
 * color="#000000">10<sup> -5</sup></font></td>
 * <td width="7%" nowrap bgcolor="#66CCFF"><font color="#000000">7.5</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">0.64</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">1.2</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">2.1</font></td>
 * <td width="8%" nowrap bgcolor="#66CCFF"><font color="#000000">5.4</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">0.64</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">1.2</font></td>
 * <td width="19%" nowrap bgcolor="#66CCFF"><font color="#000000">2.1</font></td>
 * </tr>
 * <tr align="center" valign="middle">
 * <td width="6%" nowrap align="center" valign="middle" bgcolor="#FFCCCC" rowspan="2">
 * <div align="center"><font color="#000000">10<sup> -5</sup></font></div> <font
 * color="#000000"></font></td>
 * <td width="6%" nowrap align="center" valign="middle" bgcolor="#FFCCCC"><font
 * color="#000000">10<sup> -1</sup></font></td>
 * <td width="7%" nowrap bgcolor="#66CCFF"><font color="#000000">90</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">2.5</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">6.4</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">11.6</font></td>
 * <td width="8%" nowrap bgcolor="#66CCFF"><font color="#000000">67</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">2.5</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">6.4</font></td>
 * <td width="19%" nowrap bgcolor="#66CCFF"><font color="#000000">11.6</font></td>
 * </tr>
 * <tr align="center" valign="middle">
 * <td width="6%" nowrap align="center" valign="middle" bgcolor="#FFCCCC"><font
 * color="#000000">10<sup> -5</sup></font></td>
 * <td width="7%" nowrap bgcolor="#66CCFF"><font color="#000000">96</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">2.5</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">6.4</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">11.6</font></td>
 * <td width="8%" nowrap bgcolor="#66CCFF"><font color="#000000">71</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">2.5</font></td>
 * <td width="9%" nowrap bgcolor="#66CCFF"><font color="#000000">6.4</font></td>
 * <td width="19%" nowrap bgcolor="#66CCFF"><font color="#000000">11.6</font></td>
 * </tr>
 * <tr align="center" valign="middle">
 * <td width="19%" nowrap align="center" valign="middle" columnspan="3">&nbsp;</td>
 * <td width="34%" nowrap columnspan="4">&nbsp;</td>
 * <td width="45%" nowrap columnspan="4">&nbsp;</td>
 * </tr>
 * <tr align="center" valign="middle">
 * <td width="7%" nowrap align="center" valign="middle" bgcolor="#FF9966" rowspan="2">
 * <font color="#000000">#quantiles</font></td>
 * <td width="6%" nowrap align="center" valign="middle" bgcolor="#FF9966" rowspan="2">
 * epsilon</td>
 * <td width="6%" nowrap align="center" valign="middle" bgcolor="#FF9966" rowspan="2">
 * delta</td>
 * <td width="7%" nowrap bgcolor="#FF9966"><font
 * color="#000000">N<sub>max</sub>=inf</font></td>
 * <td width="9%" nowrap bgcolor="#FF9966"><font
 * color="#000000">N<sub>max</sub>=10<sup>6</sup></font></td>
 * <td width="9%" nowrap bgcolor="#FF9966"><font
 * color="#000000">N<sub>max</sub>=10<sup>7</sup></font></td>
 * <td width="9%" nowrap bgcolor="#FF9966"><font
 * color="#000000">N<sub>max</sub>=10<sup>8</sup></font></td>
 * <td width="7%" nowrap bgcolor="#FF9966"><font
 * color="#000000">N<sub>max</sub>=inf</font></td>
 * <td width="9%" nowrap bgcolor="#FF9966"><font
 * color="#000000">N<sub>max</sub>=10<sup>6</sup></font></td>
 * <td width="9%" nowrap bgcolor="#FF9966"><font
 * color="#000000">N<sub>max</sub>=10<sup>7</sup></font></td>
 * <td width="9%" nowrap bgcolor="#FF9966"><font
 * color="#000000">N<sub>max</sub>=10<sup>8</sup></font></td>
 * </tr>
 * <tr align="center" valign="middle">
 * <td nowrap columnspan="4" bgcolor="#FF9966"><font color="#000000">N
 * unknown</font></td>
 * <td nowrap columnspan="4" bgcolor="#FF9966"><font color="#000000">N known</font>
 * </td>
 * </tr>
 * <tr align="center" valign="middle">
 * <td width="20%" nowrap align="center" valign="middle" columnspan="13" bgcolor="#33CC66">
 * <font color="#000000" size="5">Required main memory [MB]</font></td>
 * </tr>
 * </table>
 * 
 * 
 * <p>
 * <b>Implementation:</b>
 * <p>
 * After: Gurmeet Singh Manku, Sridhar Rajagopalan and Bruce G. Lindsay, Random
 * Sampling Techniques for Space Efficient Online Computation of Order
 * Statistics of Large Datasets. Proc. of the 1999 ACM SIGMOD Int. Conf. on
 * Management of Data, Paper available <A
 * HREF="http://www-cad.eecs.berkeley.edu/~manku/papers/unknown.ps.gz">
 * here</A>.
 * <p>
 * and
 * <p>
 * Gurmeet Singh Manku, Sridhar Rajagopalan and Bruce G. Lindsay, Approximate
 * Medians and other Quantiles in One Pass and with Limited Memory, Proc. of the
 * 1998 ACM SIGMOD Int. Conf. on Management of Data, Paper available <A
 * HREF="http://www-cad.eecs.berkeley.edu/~manku/papers/quantiles.ps.gz">
 * here</A>.
 * <p>
 * The broad picture is as follows. Two concepts are used: <i>Shrinking</i> and
 * <i>Sampling</i>. Shrinking takes a data sequence, sorts it and produces a
 * shrinked data sequence by picking every k-th element and throwing away all
 * the rest. The shrinked data sequence is an approximation to the original data
 * sequence.
 * <p>
 * Imagine a large data sequence (residing on disk or being generated in memory
 * on the fly) and a main memory <i>block</i> of <code>n=b*k</code> elements (
 * <code>b</code> is the number of buffers, <code>k</code> is the number of elements per
 * buffer). Fill elements from the data sequence into the block until it is full
 * or the data sequence is exhausted. When the block (or a subset of buffers) is
 * full and the data sequence is not exhausted, apply shrinking to lossily
 * compress a number of buffers into one single buffer. Repeat these steps until
 * all elements of the data sequence have been consumed. Now the block is a
 * shrinked approximation of the original data sequence. Treating it as if it
 * would be the original data sequence, we can determine quantiles in main
 * memory.
 * <p>
 * Now, the whole thing boils down to the question of: Can we choose <code>b</code>
 * and <code>k</code> (the number of buffers and the buffer size) such that
 * <code>b*k</code> is minimized, yet quantiles determined upon the block are
 * <i>guaranteed</i> to be away from the true quantiles no more than some
 * <code>epsilon</code>? It turns out, we can. It also turns out that the required
 * main memory block size <code>n=b*k</code> is usually moderate (see the table
 * above).
 * <p>
 * The theme can be combined with random sampling to further reduce main memory
 * requirements, at the expense of probabilistic guarantees. Sampling filters
 * the data sequence and feeds only selected elements to the algorithm outlined
 * above. Sampling is turned on or off, depending on the parametrization.
 * <p>
 * This quick overview does not go into important details, such as assigning
 * proper <i>weights</i> to buffers, how to choose subsets of buffers to shrink,
 * etc. For more information consult the papers cited above.
 * 
 * <p>
 * <b>Time Performance:</b>
 * <p>
 * <div align="center">Pentium Pro 200 Mhz, SunJDK 1.2.2, NT, java -classic,<br>
 * filling 10 <sup>4</sup> elements at a time, reading 100 percentiles at a
 * time,<br>
 * hasSumOfLogarithms()=false, hasSumOfInversions()=false,
 * getMaxOrderForSumOfPowers()=2<br>
 * </div> <center>
 * <table border cellpadding="6" cellspacing="0" align="center" * width="623">
 * <tr valign="middle">
 * <td align="center" height="50" columnspan="9" bgcolor="#33CC66" nowrap><font
 * size="5">Performance</font></td>
 * </tr>
 * <tr valign="middle">
 * <td align="center" width="56" height="100" rowspan="2" bgcolor="#FF9966" nowrap>
 * Quantiles</td>
 * <td align="center" width="44" height="100" rowspan="2" bgcolor="#FF9966" nowrap>
 * Epsilon</td>
 * <td align="center" width="32" height="100" rowspan="2" bgcolor="#FF9966" nowrap>
 * Delta</td>
 * <td align="center" width="1" height="150" rowspan="7" nowrap>&nbsp;</td>
 * <td align="center" height="50" columnspan="2" bgcolor="#33CC66" nowrap><font
 * size="5">Filling</font> <br>
 * [#elements/sec]</td>
 * <td align="center" width="1" height="150" rowspan="7" nowrap>&nbsp;</td>
 * <td align="center" height="50" columnspan="2" bgcolor="#33CC66"><font
 * size="5">Quantile computation</font><br>
 * [#quantiles/sec]</td>
 * </tr>
 * <tr valign="middle" bgcolor="#FF9966" nowrap>
 * <td align="center" width="75" height="50" nowrap valign="middle"><font
 * color="#000000">N unknown,<br>
 * N<sub>max</sub>=inf</font></td>
 * <td align="center" width="77" height="50" nowrap valign="middle"><font
 * color="#000000">N known,<br>
 * N<sub>max</sub>=10<sup>7</sup></font></td>
 * <td align="center" width="106" height="50" nowrap valign="middle"><font
 * color="#000000">N unknown,<br>
 * N<sub>max</sub>=inf</font></td>
 * <td align="center" width="103" height="50" nowrap valign="middle"><font
 * color="#000000">N known,<br>
 * N<sub>max</sub>=10<sup>7</sup></font></td>
 * </tr>
 * <tr valign="middle">
 * <td align="center" height="31" columnspan="3" nowrap>&nbsp;</td>
 * <td align="center" height="31" columnspan="2" nowrap>&nbsp;</td>
 * <td align="center" height="31" columnspan="2" nowrap>&nbsp;</td>
 * </tr>
 * <tr valign="middle">
 * <td align="center" width="56" rowspan="4" bgcolor="#FFCCCC" nowrap>
 * 10<sup>4</sup></td>
 * <td align="center" width="44" bgcolor="#FFCCCC" nowrap>10 <sup> -1</sup></td>
 * <td align="center" width="32" bgcolor="#FFCCCC" nowrap rowspan="4">10 <sup>
 * -1</sup></td>
 * <td width="75" bgcolor="#66CCFF" nowrap align="center">
 * <p>
 * 1600000
 * </p>
 * </td>
 * <td width="77" bgcolor="#66CCFF" nowrap align="center">1300000</td>
 * <td align="center" width="106" bgcolor="#66CCFF" nowrap>250000</td>
 * <td align="center" width="103" bgcolor="#66CCFF" nowrap>130000</td>
 * </tr>
 * <tr valign="middle">
 * <td align="center" width="44" bgcolor="#FFCCCC">10 <sup> -2</sup></td>
 * <td width="75" bgcolor="#66CCFF" align="center">360000</td>
 * <td width="77" bgcolor="#66CCFF" align="center">1200000</td>
 * <td align="center" width="106" bgcolor="#66CCFF">50000</td>
 * <td align="center" width="103" bgcolor="#66CCFF">20000</td>
 * </tr>
 * <tr valign="middle">
 * <td align="center" width="44" bgcolor="#FFCCCC">10 <sup> -3</sup></td>
 * <td width="75" bgcolor="#66CCFF" align="center">150000</td>
 * <td width="77" bgcolor="#66CCFF" align="center">200000</td>
 * <td align="center" width="106" bgcolor="#66CCFF">3600</td>
 * <td align="center" width="103" bgcolor="#66CCFF">3000</td>
 * </tr>
 * <tr valign="middle">
 * <td align="center" width="44" bgcolor="#FFCCCC">10 <sup> -4</sup></td>
 * <td width="75" bgcolor="#66CCFF" align="center">120000</td>
 * <td width="77" bgcolor="#66CCFF" align="center">170000</td>
 * <td align="center" width="106" bgcolor="#66CCFF">80</td>
 * <td align="center" width="103" bgcolor="#66CCFF">1000</td>
 * </tr>
 * </table>
 * </center>
 * 
 * @see cern.jet.stat.tdouble.quantile
 * @author wolfgang.hoschek@cern.ch
 * @version 0.9, 03-Jul-99
 */
public class QuantileDoubleBin1D extends MightyStaticDoubleBin1D {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    protected DoubleQuantileFinder finder = null;

    /**
     * Not public; for use by subclasses only! Constructs and returns an empty
     * bin.
     */
    protected QuantileDoubleBin1D() {
        super(false, false, 2);
    }

    /**
     * Equivalent to
     * <code>new QuantileBin1D(false, Long.MAX_VALUE, epsilon, 0.001, 10000, new cern.jet.random.engine.DRand(new java.util.Date())</code>
     * .
     * @param epsilon
     */
    public QuantileDoubleBin1D(double epsilon) {
        this(false, Long.MAX_VALUE, epsilon, 0.001, 10000, new cern.jet.random.tdouble.engine.DRand(
                new java.util.Date()));
    }

    /**
     * Equivalent to
     * <code>new QuantileBin1D(known_N, N, epsilon, delta, quantiles, randomGenerator, false, false, 2)</code>
     * .
     * @param known_N
     * @param randomGenerator
     * @param N
     * @param quantiles
     * @param epsilon
     * @param delta
     */
    public QuantileDoubleBin1D(boolean known_N, long N, double epsilon, double delta, int quantiles,
            DoubleRandomEngine randomGenerator) {
        this(known_N, N, epsilon, delta, quantiles, randomGenerator, false, false, 2);
    }

    /**
     * Constructs and returns an empty bin that, under the given constraints,
     * minimizes the amount of memory needed.
     * 
     * Some applications exactly know in advance over how many elements
     * quantiles are to be computed. Provided with such information the main
     * memory requirements of this class are small. Other applications don't
     * know in advance over how many elements quantiles are to be computed.
     * However, some of them can give an upper limit, which will reduce main
     * memory requirements. For example, if elements are selected from a
     * database and filled into histograms, it is usually not known in advance
     * how many elements are being filled, but one may know that at most
     * <code>S</code> elements, the number of elements in the database, are filled.
     * A third type of application knowns nothing at all about the number of
     * elements to be filled; from zero to infinitely many elements may actually
     * be filled. This method efficiently supports all three types of
     * applications.
     * 
     * @param known_N
     *            specifies whether the number of elements over which quantiles
     *            are to be computed is known or not.
     *            <p>
     * @param N
     *            if <code>known_N==true</code>, the number of elements over which
     *            quantiles are to be computed. if <code>known_N==false</code>, the
     *            upper limit on the number of elements over which quantiles are
     *            to be computed. In other words, the maximum number of elements
     *            ever to be added. If such an upper limit is a-priori unknown,
     *            then set <code>N = Long.MAX_VALUE</code>.
     *            <p>
     * @param epsilon
     *            the approximation error which is guaranteed not to be exceeded
     *            (e.g. <code>0.001</code>) (<code>0 &lt;= epsilon &lt;= 1</code>). To
     *            get exact rather than approximate quantiles, set
     *            <code>epsilon=0.0</code>;
     *            <p>
     * @param delta
     *            the allowed probability that the actual approximation error
     *            exceeds <code>epsilon</code> (e.g. 0.0001) (0 &lt;= delta &lt;=
     *            1). To avoid probabilistic answers, set <code>delta=0.0</code>.
     *            For example, <code>delta = 0.0001</code> is equivalent to a
     *            confidence of <code>99.99%</code>.
     *            <p>
     * @param quantiles
     *            the number of quantiles to be computed (e.g. <code>100</code>) (
     *            <code>quantiles &gt;= 1</code>). If unknown in advance, set this
     *            number large, e.g. <code>quantiles &gt;= 10000</code>.
     *            <p>
     * @param randomGenerator
     *            a uniform random number generator. Set this parameter to
     *            <code>null</code> to use a default generator seeded with the
     *            current time.
     *            <p>
     *            The next three parameters specify additional capabilities
     *            unrelated to quantile computation. They are identical to the
     *            one's defined in the constructor of the parent class
     *            {@link MightyStaticDoubleBin1D}.
     *            <p>
     * @param hasSumOfLogarithms
     *            Tells whether {@link #sumOfLogarithms()} can return meaningful
     *            results. Set this parameter to <code>false</code> if measures of
     *            sum of logarithms, geometric mean and product are not
     *            required.
     *            <p>
     * @param hasSumOfInversions
     *            Tells whether {@link #sumOfInversions()} can return meaningful
     *            results. Set this parameter to <code>false</code> if measures of
     *            sum of inversions, harmonic mean and sumOfPowers(-1) are not
     *            required.
     *            <p>
     * @param maxOrderForSumOfPowers
     *            The maximum order <code>k</code> for which
     *            {@link #sumOfPowers(int)} can return meaningful results. Set
     *            this parameter to at least 3 if the skew is required, to at
     *            least 4 if the kurtosis is required. In general, if moments
     *            are required set this parameter at least as large as the
     *            largest required moment. This method always substitutes
     *            <code>Math.max(2,maxOrderForSumOfPowers)</code> for the parameter
     *            passed in. Thus, <code>sumOfPowers(0..2)</code> always returns
     *            meaningful results.
     */
    public QuantileDoubleBin1D(boolean known_N, long N, double epsilon, double delta, int quantiles,
            DoubleRandomEngine randomGenerator, boolean hasSumOfLogarithms, boolean hasSumOfInversions,
            int maxOrderForSumOfPowers) {
        super(hasSumOfLogarithms, hasSumOfInversions, maxOrderForSumOfPowers);
        this.finder = DoubleQuantileFinderFactory.newDoubleQuantileFinder(known_N, N, epsilon, delta, quantiles,
                randomGenerator);
        this.clear();
    }

    /**
     * Adds the part of the specified list between indexes <code>from</code>
     * (inclusive) and <code>to</code> (inclusive) to the receiver.
     * 
     * @param list
     *            the list of which elements shall be added.
     * @param from
     *            the index of the first element to be added (inclusive).
     * @param to
     *            the index of the last element to be added (inclusive).
     * @throws IndexOutOfBoundsException
     *             if
     *             <code>list.size()&gt;0 &amp;&amp; (from&lt;0 || from&gt;to || to&gt;=list.size())</code>
     *             .
     */

    public synchronized void addAllOfFromTo(DoubleArrayList list, int from, int to) {
        super.addAllOfFromTo(list, from, to);
        if (this.finder != null)
            this.finder.addAllOfFromTo(list, from, to);
    }

    /**
     * Removes all elements from the receiver. The receiver will be empty after
     * this call returns.
     */

    public synchronized void clear() {
        super.clear();
        if (this.finder != null)
            this.finder.clear();
    }

    /**
     * Returns a deep copy of the receiver.
     * 
     * @return a deep copy of the receiver.
     */

    public synchronized Object clone() {
        QuantileDoubleBin1D clone = (QuantileDoubleBin1D) super.clone();
        if (this.finder != null)
            clone.finder = (DoubleQuantileFinder) clone.finder.clone();
        return clone;
    }

    /**
     * Computes the deviations from the receiver's measures to another bin's
     * measures.
     * 
     * @param other
     *            the other bin to compare with
     * @return a summary of the deviations.
     */

    public String compareWith(AbstractDoubleBin1D other) {
        StringBuffer buf = new StringBuffer(super.compareWith(other));
        if (other instanceof QuantileDoubleBin1D) {
            QuantileDoubleBin1D q = (QuantileDoubleBin1D) other;
            buf.append("25%, 50% and 75% Quantiles: " + relError(quantile(0.25), q.quantile(0.25)) + ", "
                    + relError(quantile(0.5), q.quantile(0.5)) + ", " + relError(quantile(0.75), q.quantile(0.75)));
            buf.append("\nquantileInverse(mean): " + relError(quantileInverse(mean()), q.quantileInverse(q.mean()))
                    + " %");
            buf.append("\n");
        }
        return buf.toString();
    }

    /**
     * Returns the median.
     * @return 
     */
    public double median() {
        return quantile(0.5);
    }

    /**
     * Computes and returns the phi-quantile.
     * 
     * @param phi
     *            the percentage for which the quantile is to be computed. phi
     *            must be in the interval <code>(0.0,1.0]</code>.
     * @return the phi quantile element.
     */
    public synchronized double quantile(double phi) {
        return quantiles(new DoubleArrayList(new double[] { phi })).get(0);
    }

    /**
     * Returns how many percent of the elements contained in the receiver are
     * <code>&lt;= element</code>. Does linear interpolation if the element is not
     * contained but lies in between two contained elements.
     * 
     * @param element
     *            the element to search for.
     * @return the percentage <code>phi</code> of elements <code>&lt;= element</code> (
     *         <code>0.0 &lt;= phi &lt;=1.0)</code>.
     */
    public synchronized double quantileInverse(double element) {
        return finder.phi(element);
    }

    /**
     * Returns the quantiles of the specified percentages. For implementation
     * reasons considerably more efficient than calling
     * {@link #quantile(double)} various times.
     * 
     * @param phis
     *            the percentages for which quantiles are to be computed. Each
     *            percentage must be in the interval <code>(0.0,1.0]</code>.
     *            <code>percentages</code> must be sorted ascending.
     * @return the quantiles.
     */
    public synchronized DoubleArrayList quantiles(cern.colt.list.tdouble.DoubleArrayList phis) {
        return finder.quantileElements(phis);
    }

    /**
     * Returns how many elements are contained in the range
     * <code>[minElement,maxElement]</code>. Does linear interpolation if one or
     * both of the parameter elements are not contained. Returns exact or
     * approximate results, depending on the parametrization of this class or
     * subclasses.
     * 
     * @param minElement
     *            the minimum element to search for.
     * @param maxElement
     *            the maximum element to search for.
     * @return the number of elements in the range.
     */
    public int sizeOfRange(double minElement, double maxElement) {
        return (int) Math.round(size() * (quantileInverse(maxElement) - quantileInverse(minElement)));
    }

    /**
     * Divides (rebins) a copy of the receiver at the given <i>percentage
     * boundaries</i> into bins and returns these bins, such that each bin
     * <i>approximately</i> reflects the data elements of its range.
     * 
     * The receiver is not physically rebinned (divided); it stays unaffected by
     * this operation. The returned bins are such that <i>if</i> one would have
     * filled elements into multiple bins instead of one single all encompassing
     * bin only, those multiple bins would have <i>approximately</i> the same
     * statistics measures as the one's returned by this method.
     * <p>
     * The <code>split(...)</code> methods are particularly well suited for
     * real-time interactive rebinning (the famous "scrolling slider" effect).
     * <p>
     * Passing equi-distant percentages like
     * <code>(0.0, 0.2, 0.4, 0.6, 0.8, 1.0)</code> into this method will yield bins
     * of an <i>equi-depth histogram</i>, i.e. a histogram with bin boundaries
     * adjusted such that each bin contains the same number of elements, in this
     * case 20% each. Equi-depth histograms can be useful if, for example, not
     * enough properties of the data to be captured are known a-priori to be
     * able to define reasonable bin boundaries (partitions). For example, when
     * guesses about minimas and maximas are strongly unreliable. Or when
     * chances are that by focussing too much on one particular area other
     * important areas and characters of a data set may be missed.
     * <p>
     * <b>Implementation:</b>
     * <p>
     * The receiver is divided into <code>s = percentages.size()-1</code> intervals
     * (bins). For each interval <code>I</code>, its minimum and maximum elements
     * are determined based upon quantile computation. Further, each interval
     * <code>I</code> is split into <code>k</code> equi-percent-distant subintervals
     * (sub-bins). In other words, an interval is split into subintervals such
     * that each subinterval contains the same number of elements.
     * <p>
     * For each subinterval <code>S</code>, its minimum and maximum are determined,
     * again, based upon quantile computation. They yield an approximate
     * arithmetic mean <code>am = (min+max)/2</code> of the subinterval. A
     * subinterval is treated as if it would contain only elements equal to the
     * mean <code>am</code>. Thus, if the subinterval contains, say, <code>n</code>
     * elements, it is assumed to consist of <code>n</code> mean elements
     * <code>(am,am,...,am)</code>. A subinterval's sum of elements, sum of squared
     * elements, sum of inversions, etc. are then approximated using such a
     * sequence of mean elements.
     * <p>
     * Finally, the statistics measures of an interval <code>I</code> are computed
     * by summing up (integrating) the measures of its subintervals.
     * <p>
     * <b>Accuracy</b>:
     * <p>
     * Depending on the accuracy of quantile computation and the number of
     * subintervals per interval (the resolution). Objects of this class compute
     * exact or approximate quantiles, depending on the parameters used upon
     * instance construction. Objects of subclasses may <i>always</i> compute
     * exact quantiles, as is the case for {@link DynamicDoubleBin1D}. Most
     * importantly for this class <code>QuantileBin1D</code>, a reasonably small
     * epsilon (e.g. 0.01, perhaps 0.001) should be used upon instance
     * construction. The confidence parameter <code>delta</code> is less important,
     * you may find <code>delta=0.00001</code> appropriate. <br>
     * The larger the resolution, the smaller the approximation error, up to
     * some limit. Integrating over only a few subintervals per interval will
     * yield very crude approximations. If the resolution is set to a reasonably
     * large number, say 10..100, more small subintervals are integrated,
     * resulting in more accurate results. <br>
     * Note that for good accuracy, the number of quantiles computable with the
     * given approximation guarantees should upon instance construction be
     * specified, so as to satisfy
     * <p>
     * <code>quantiles > resolution * (percentages.size()-1)</code>
     * <p>
     * <p>
     * <b>Example:</b>
     * <p>
     * <code>resolution=2, percentList = (0.0, 0.1, 0.2, 0.5, 0.9, 1.0)</code> means
     * the receiver is to be split into 5 bins: <br>
     * <ul>
     * <li>bin 0 ranges from <code>[0%..10%)</code> and holds the smallest 10% of
     * the sorted elements.
     * <li>bin 1 ranges from <code>[10%..20%)</code> and holds the next smallest 10%
     * of the sorted elements.
     * <li>bin 2 ranges from <code>[20%..50%)</code> and holds the next smallest 30%
     * of the sorted elements.
     * <li>bin 3 ranges from <code>[50%..90%)</code> and holds the next smallest 40%
     * of the sorted elements.
     * <li>bin 4 ranges from <code>[90%..100%)</code> and holds the largest 10% of
     * the sorted elements.
     * </ul>
     * <p>
     * The statistics measures for each bin are to be computed at a resolution
     * of 2 subbins per bin. Thus, the statistics measures of a bin are the
     * integrated measures over 2 subbins, each containing the same amount of
     * elements:
     * <ul>
     * <li>bin 0 has a subbin ranging from <code>[ 0%.. 5%)</code> and a subbin
     * ranging from <code>[ 5%..10%)</code>.
     * <li>bin 1 has a subbin ranging from <code>[10%..15%)</code> and a subbin
     * ranging from <code>[15%..20%)</code>.
     * <li>bin 2 has a subbin ranging from <code>[20%..35%)</code> and a subbin
     * ranging from <code>[35%..50%)</code>.
     * <li>bin 3 has a subbin ranging from <code>[50%..70%)</code> and a subbin
     * ranging from <code>[70%..90%)</code>.
     * <li>bin 4 has a subbin ranging from <code>[90%..95%)</code> and a subbin
     * ranging from <code>[95%..100%)</code>.
     * </ul>
     * <p>
     * Lets concentrate on the subbins of bin 0.
     * <ul>
     * <li>Assume the subbin <code>A=[0%..5%)</code> has a minimum of <code>300</code>
     * and a maximum of <code>350</code> (0% of all elements are less than 300, 5%
     * of all elements are less than 350).
     * <li>Assume the subbin <code>B=[5%..10%)</code> has a minimum of <code>350</code>
     * and a maximum of <code>550</code> (5% of all elements are less than 350, 10%
     * of all elements are less than 550).
     * </ul>
     * <p>
     * Assume the entire data set consists of <code>N=100</code> elements.
     * <ul>
     * <li>Then subbin A has an approximate mean of <code>300+350 / 2 = 325</code>,
     * a size of <code>N*(5%-0%) = 100*5% = 5</code> elements, an approximate sum of
     * <code>325 * 100*5% = 1625</code>, an approximate sum of squares of
     * <code>325<sup>2</sup> * 100*5% = 528125</code>, an approximate sum of
     * inversions of <code>(1.0/325) * 100*5% = 0.015</code>, etc.
     * <li>Analogously, subbin B has an approximate mean of
     * <code>350+550 / 2 = 450</code>, a size of <code>N*(10%-5%) = 100*5% = 5</code>
     * elements, an approximate sum of <code>450 * 100*5% = 2250</code>, an
     * approximate sum of squares of <code>450<sup>2</sup> * 100*5% = 1012500</code>
     * , an approximate sum of inversions of <code>(1.0/450) * 100*5% = 0.01</code>,
     * etc.
     * </ul>
     * <p>
     * Finally, the statistics measures of bin 0 are computed by summing up
     * (integrating) the measures of its subintervals: Bin 0 has a size of
     * <code>N*(10%-0%)=10</code> elements (we knew that already), sum of
     * <code>1625+2250=3875</code>, sum of squares of
     * <code>528125+1012500=1540625</code>, sum of inversions of
     * <code>0.015+0.01=0.025</code>, etc. From these follow other measures such as
     * <code>mean=3875/10=387.5, rms = sqrt(1540625 / 10)=392.5</code>, etc. The
     * other bins are computes analogously.
     * 
     * @param percentages
     *            the percentage boundaries at which the receiver shall be
     *            split.
     * @param k
     *            the desired number of subintervals per interval.
     * @return 
     */
    public synchronized MightyStaticDoubleBin1D[] splitApproximately(DoubleArrayList percentages, int k) {
        /*
         * percentages = [p0, p1, p2, ..., p(size-2), p(size-1)] defines bins
         * [p0,p1), [p1,p2), ..., [p(size-2),p(size-1)) each bin is divided into
         * k equi-percent-distant sub bins (subintervals). e.g. k = 2 means
         * "compute" with a resolution (accuracy) of 2 subbins (subintervals)
         * per bin,
         * 
         * percentages = [0.1, 0.2, 0.3, ..., 0.9, 1.0] means bin 0 holds the
         * first 0.1-0.0=10% of the sorted elements, bin 1 holds the next
         * 0.2-0.1=10% of the sorted elements, ...
         * 
         * bins = [0.1, 0.2), [0.2, 0.3), ..., [0.9, 1.0) subBins = [0.1, 0.15,
         * 0.2, 0.25, 0.3, ....]
         * 
         * [0.1, 0.15) [0.15, 0.2) [0.3, 0.35) [0.35, 0.4)
         * 
         * [0.2, 0.25) [0.25, 0.3)
         * 
         */
        int percentSize = percentages.size();
        if (k < 1 || percentSize < 2)
            throw new IllegalArgumentException();

        double[] percent = percentages.elements();
        int noOfBins = percentSize - 1;

        // construct subintervals
        double[] subBins = new double[1 + k * (percentSize - 1)];
        subBins[0] = percent[0];
        int c = 1;

        for (int i = 0; i < noOfBins; i++) {
            double step = (percent[i + 1] - percent[i]) / k;
            for (int j = 1; j <= k; j++) {
                subBins[c++] = percent[i] + j * step;
            }
        }

        // compute quantile elements;
        double[] quantiles = quantiles(new DoubleArrayList(subBins)).elements();

        // collect summary statistics for each bin.
        // one bin's statistics are the integrated statistics of its
        // subintervals.
        MightyStaticDoubleBin1D[] splitBins = new MightyStaticDoubleBin1D[noOfBins];
        int maxOrderForSumOfPowers = getMaxOrderForSumOfPowers();
        maxOrderForSumOfPowers = Math.min(10, maxOrderForSumOfPowers); // don't
        // compute
        // tons
        // of
        // measures

        int dataSize = this.size();
        c = 0;
        for (int i = 0; i < noOfBins; i++) { // for each bin
            double step = (percent[i + 1] - percent[i]) / k;
            double binSum = 0;
            double binSumOfSquares = 0;
            double binSumOfLogarithms = 0;
            double binSumOfInversions = 0;
            double[] binSumOfPowers = null;
            if (maxOrderForSumOfPowers > 2) {
                binSumOfPowers = new double[maxOrderForSumOfPowers - 2];
            }

            double binMin = quantiles[c++];
            double safe_min = binMin;
            double subIntervalSize = dataSize * step;

            for (int j = 1; j <= k; j++) { // integrate all subintervals
                double binMax = quantiles[c++];
                double binMean = (binMin + binMax) / 2;
                binSum += binMean * subIntervalSize;
                binSumOfSquares += binMean * binMean * subIntervalSize;
                if (this.hasSumOfLogarithms) {
                    binSumOfLogarithms += (Math.log(binMean)) * subIntervalSize;
                }
                if (this.hasSumOfInversions) {
                    binSumOfInversions += (1 / binMean) * subIntervalSize;
                }
                if (maxOrderForSumOfPowers >= 3)
                    binSumOfPowers[0] += binMean * binMean * binMean * subIntervalSize;
                if (maxOrderForSumOfPowers >= 4)
                    binSumOfPowers[1] += binMean * binMean * binMean * binMean * subIntervalSize;
                for (int p = 5; p <= maxOrderForSumOfPowers; p++) {
                    binSumOfPowers[p - 3] += Math.pow(binMean, p) * subIntervalSize;
                }

                binMin = binMax;
            }
            c--;

            // example: bin(0) contains (0.2-0.1) == 10% of all elements
            int binSize = (int) Math.round((percent[i + 1] - percent[i]) * dataSize);
            double binMax = binMin;
            binMin = safe_min;

            // fill statistics
            splitBins[i] = new MightyStaticDoubleBin1D(this.hasSumOfLogarithms, this.hasSumOfInversions,
                    maxOrderForSumOfPowers);
            if (binSize > 0) {
                splitBins[i].size = binSize;
                splitBins[i].min = binMin;
                splitBins[i].max = binMax;
                splitBins[i].sum = binSum;
                splitBins[i].sum_xx = binSumOfSquares;
                splitBins[i].sumOfLogarithms = binSumOfLogarithms;
                splitBins[i].sumOfInversions = binSumOfInversions;
                splitBins[i].sumOfPowers = binSumOfPowers;
            }
            /*
             * double binMean = binSum / binSize;
             * System.out.println("size="+binSize);
             * System.out.println("min="+binMin);
             * System.out.println("max="+binMax);
             * System.out.println("mean="+binMean);
             * System.out.println("sum_x="+binSum);
             * System.out.println("sum_xx="+binSumOfSquares);
             * System.out.println("rms="+Math.sqrt(binSumOfSquares / binSize));
             * System.out.println();
             */

        }
        return splitBins;
    }

    /**
     * Divides (rebins) a copy of the receiver at the given <i>interval
     * boundaries</i> into bins and returns these bins, such that each bin
     * <i>approximately</i> reflects the data elements of its range.
     * 
     * For each interval boundary of the axis (including -infinity and
     * +infinity), computes the percentage (quantile inverse) of elements less
     * than the boundary. Then lets
     * {@link #splitApproximately(DoubleArrayList,int)} do the real work.
     * 
     * @param axis
     *            an axis defining interval boundaries.
     * @param k
     *            the desired number of subintervals per interval.
     * @return 
     */
    public synchronized MightyStaticDoubleBin1D[] splitApproximately(hep.aida.tdouble.DoubleIAxis axis, int k) {
        DoubleArrayList percentages = new DoubleArrayList(new hep.aida.tdouble.ref.DoubleConverter().edges(axis));
        percentages.beforeInsert(0, Double.NEGATIVE_INFINITY);
        percentages.add(Double.POSITIVE_INFINITY);
        for (int i = percentages.size(); --i >= 0;) {
            percentages.set(i, quantileInverse(percentages.get(i)));
        }

        return splitApproximately(percentages, k);
    }

    /**
     * Returns a String representation of the receiver.
     * @return 
     */

    public synchronized String toString() {
        StringBuffer buf = new StringBuffer(super.toString());
        buf.append("25%, 50%, 75% Quantiles: " + quantile(0.25) + ", " + quantile(0.5) + ", " + quantile(0.75));
        // buf.append("10%, 25%, 50%, 75%, 90% Quantiles: "+quantile(0.1) + ",
        // "+ quantile(0.25) + ", "+ quantile(0.5) + ", " + quantile(0.75) + ",
        // " + quantile(0.9));
        buf.append("\nquantileInverse(median): " + quantileInverse(median()));
        buf.append("\n");
        return buf.toString();
    }
}
