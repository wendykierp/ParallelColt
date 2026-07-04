/*
Copyright (C) 1999 CERN - European Organization for Nuclear Research.
Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose 
is hereby granted without fee, provided that the above copyright notice appear in all copies and 
that both that copyright notice and this permission notice appear in supporting documentation. 
CERN makes no representations about the suitability of this software for any purpose. 
It is provided "as is" without expressed or implied warranty.
 */
package cern.jet.math.tdouble;

import cern.colt.function.tdouble.DoubleDoubleFunction;
import cern.colt.function.tdouble.DoubleDoubleProcedure;
import cern.colt.function.tdouble.DoubleFunction;
import cern.colt.function.tdouble.DoubleProcedure;

// import com.imsl.math.Sfun;
/**
 * Function objects to be passed to generic methods. Contains the functions of
 * {@link java.lang.Math} as function objects, as well as a few more basic
 * functions.
 * <p>
 * Function objects conveniently allow to express arbitrary functions in a
 * generic manner. Essentially, a function object is an object that can perform
 * a function on some arguments. It has a minimal interface: a method
 * <code>apply</code> that takes the arguments, computes something and returns some
 * result value. Function objects are comparable to function pointers in C used
 * for call-backs.
 * <p>
 * Unary functions are of type {@link cern.colt.function.tdouble.DoubleFunction}
 * , binary functions of type
 * {@link cern.colt.function.tdouble.DoubleDoubleFunction}. All can be retrieved
 * via <code>public 
 static final</code> variables named after the function. Unary predicates are of
 * type {@link cern.colt.function.tdouble.DoubleProcedure}, binary predicates of
 * type {@link cern.colt.function.tdouble.DoubleDoubleProcedure}. All can be
 * retrieved via <code>public 
 static final</code> variables named <code>isXXX</code>.
 * 
 * <p>
 * Binary functions and predicates also exist as unary functions with the second
 * argument being fixed to a constant. These are generated and retrieved via
 * factory methods (again with the same name as the function). Example:
 * <ul>
 * <li><code>Functions.pow</code> gives the function <code>a<sup>b</sup></code>.
 * <li><code>Functions.pow.apply(2,3)==8</code>.
 * <li><code>Functions.pow(3)</code> gives the function <code>a<sup>3</sup></code>.
 * <li><code>Functions.pow(3).apply(2)==8</code>.
 * </ul>
 * More general, any binary function can be made an unary functions by fixing
 * either the first or the second argument. See methods
 * {@link #bindArg1(DoubleDoubleFunction,double)} and
 * {@link #bindArg2(DoubleDoubleFunction,double)}. The order of arguments can be
 * swapped so that the first argument becomes the second and vice-versa. See
 * method {@link #swapArgs(DoubleDoubleFunction)}. Example:
 * <ul>
 * <li><code>Functions.pow</code> gives the function <code>a<sup>b</sup></code>.
 * <li><code>Functions.bindArg2(Functions.pow,3)</code> gives the function
 * <code>x<sup>3</sup></code>.
 * <li><code>Functions.bindArg1(Functions.pow,3)</code> gives the function
 * <code>3<sup>x</sup></code>.
 * <li><code>Functions.swapArgs(Functions.pow)</code> gives the function
 * <code>b<sup>a</sup></code>.
 * </ul>
 * <p>
 * Even more general, functions can be chained (composed, assembled). Assume we
 * have two unary functions <code>g</code> and <code>h</code>. The unary function
 * <code>g(h(a))</code> applying both in sequence can be generated via
 * {@link #chain(DoubleFunction,DoubleFunction)}:
 * <ul>
 * <li><code>Functions.chain(g,h);</code>
 * </ul>
 * Assume further we have a binary function <code>f</code>. The binary function
 * <code>g(f(a,b))</code> can be generated via
 * {@link #chain(DoubleFunction,DoubleDoubleFunction)}:
 * <ul>
 * <li><code>Functions.chain(g,f);</code>
 * </ul>
 * The binary function <code>f(g(a),h(b))</code> can be generated via
 * {@link #chain(DoubleDoubleFunction,DoubleFunction,DoubleFunction)}:
 * <ul>
 * <li><code>Functions.chain(f,g,h);</code>
 * </ul>
 * Arbitrarily complex functions can be composed from these building blocks. For
 * example <code>sin(a) + cos<sup>2</sup>(b)</code> can be specified as follows:
 * <ul>
 * <li><code>chain(plus,sin,chain(square,cos));</code>
 * </ul>
 * or, of course, as
 * 
 * <pre>
 * new DoubleDoubleFunction() {
 *     public final double apply(double a, double b) {
 *         return Math.sin(a) + Math.pow(Math.cos(b), 2);
 *     }
 * }
 * </pre>
 * 
 * <p>
 * For aliasing see {@link #functions}. Try this
 * <table>
 * <td class="PRE">
 * 
 * <pre>
 * // should yield 1.4399560356056456 in all cases
 * double a = 0.5;
 * double b = 0.2;
 * double v = Math.sin(a) + Math.pow(Math.cos(b), 2);
 * System.out.println(v);
 * Functions F = Functions.functions;
 * DoubleDoubleFunction f = F.chain(F.plus, F.sin, F.chain(F.square, F.cos));
 * System.out.println(f.apply(a, b));
 * DoubleDoubleFunction g = new DoubleDoubleFunction() {
 *     public double apply(double a, double b) {
 *         return Math.sin(a) + Math.pow(Math.cos(b), 2);
 *     }
 * };
 * System.out.println(g.apply(a, b));
 * </pre>
 * 
 * </td>
 * </table>
 * 
 * <p>
 * <H3>Performance</H3>
 * 
 * Surprise. Using modern non-adaptive JITs such as SunJDK 1.2.2 (java -classic)
 * there seems to be no or only moderate performance penalty in using function
 * objects in a loop over traditional code in a loop. For complex nested
 * function objects (e.g.
 * <code>F.chain(F.abs,F.chain(F.plus,F.sin,F.chain(F.square,F.cos)))</code>) the
 * penalty is zero, for trivial functions (e.g. <code>F.plus</code>) the penalty is
 * often acceptable. <center>
 * <table border cellpadding="3" cellspacing="0" * align="center">
 * <tr valign="middle" bgcolor="#33CC66" nowrap align="center">
 * <td nowrap columnspan="7"><font size="+2">Iteration Performance [million
 * function evaluations per second]</font><br>
 * <font size="-1">Pentium Pro 200 Mhz, SunJDK 1.2.2, NT, java -classic, </font>
 * </td>
 * </tr>
 * <tr valign="middle" bgcolor="#66CCFF" nowrap align="center">
 * <td nowrap bgcolor="#FF9966" rowspan="2">&nbsp;</td>
 * <td bgcolor="#FF9966" columnspan="2">
 * <p>
 * 30000000 iterations
 * </p>
 * </td>
 * <td bgcolor="#FF9966" columnspan="2">3000000 iterations (10 times less)</td>
 * <td bgcolor="#FF9966" columnspan="2">&nbsp;</td>
 * </tr>
 * <tr valign="middle" bgcolor="#66CCFF" nowrap align="center">
 * <td bgcolor="#FF9966"> <code>F.plus</code></td>
 * <td bgcolor="#FF9966"><code>a+b</code></td>
 * <td bgcolor="#FF9966">
 * <code>F.chain(F.abs,F.chain(F.plus,F.sin,F.chain(F.square,F.cos)))</code></td>
 * <td bgcolor="#FF9966">
 * <code>Math.abs(Math.sin(a) + Math.pow(Math.cos(b),2))</code></td>
 * <td bgcolor="#FF9966">&nbsp;</td>
 * <td bgcolor="#FF9966">&nbsp;</td>
 * </tr>
 * <tr valign="middle" bgcolor="#66CCFF" nowrap align="center">
 * <td nowrap bgcolor="#FF9966">&nbsp;</td>
 * <td nowrap>10.8</td>
 * <td nowrap>29.6</td>
 * <td nowrap>0.43</td>
 * <td nowrap>0.35</td>
 * <td nowrap>&nbsp;</td>
 * <td nowrap>&nbsp;</td>
 * </tr>
 * </table>
 * </center>
 * 
 * 
 * @author wolfgang.hoschek@cern.ch
 * @version 1.0, 09/24/99
 */
public class DoubleFunctions extends Object {
    /**
     * Little trick to allow for "aliasing", that is, renaming this class.
     * Writing code like
     * <p>
     * <code>Functions.chain(Functions.plus,Functions.sin,Functions.chain(Functions.square,Functions.cos));</code>
     * <p>
     * is a bit awkward, to say the least. Using the aliasing you can instead
     * write
     * <p>
     * <code>Functions F = Functions.functions; <br>
    F.chain(F.plus,F.sin,F.chain(F.square,F.cos));</code>
     */
    public static final DoubleFunctions functions = new DoubleFunctions();

    /***************************************************************************
     * <H3>Unary functions</H3>
     **************************************************************************/
    /**
     * Function that returns <code>Math.abs(a)</code>.
     */
    public static final DoubleFunction abs = new DoubleFunction() {
        public final double apply(double a) {
            return Math.abs(a);
        }
    };

    /**
     * Function that returns <code>Math.acos(a)</code>.
     */
    public static final DoubleFunction acos = new DoubleFunction() {
        public final double apply(double a) {
            return Math.acos(a);
        }
    };

    /**
     * Function that returns <code>com.imsl.math.Sfun.acosh(a)</code>.
     */
    /*
     * public static final DoubleFunction acosh = new DoubleFunction() { public
     * final double apply(double a) { return Sfun.acosh(a); } };
     */

    /**
     * Function that returns <code>Math.asin(a)</code>.
     */
    public static final DoubleFunction asin = new DoubleFunction() {
        public final double apply(double a) {
            return Math.asin(a);
        }
    };

    /**
     * Function that returns <code>com.imsl.math.Sfun.asinh(a)</code>.
     */
    /*
     * public static final DoubleFunction asinh = new DoubleFunction() { public
     * final double apply(double a) { return Sfun.asinh(a); } };
     */

    /**
     * Function that returns <code>Math.atan(a)</code>.
     */
    public static final DoubleFunction atan = new DoubleFunction() {
        public final double apply(double a) {
            return Math.atan(a);
        }
    };

    /**
     * Function that returns <code>com.imsl.math.Sfun.atanh(a)</code>.
     */
    /*
     * public static final DoubleFunction atanh = new DoubleFunction() { public
     * final double apply(double a) { return Sfun.atanh(a); } };
     */

    /**
     * Function that returns <code>Math.ceil(a)</code>.
     */
    public static final DoubleFunction ceil = new DoubleFunction() {
        public final double apply(double a) {
            return Math.ceil(a);
        }
    };

    /**
     * Function that returns <code>Math.cos(a)</code>.
     */
    public static final DoubleFunction cos = new DoubleFunction() {
        public final double apply(double a) {
            return Math.cos(a);
        }
    };

    /**
     * Function that returns <code>com.imsl.math.Sfun.cosh(a)</code>.
     */
    /*
     * public static final DoubleFunction cosh = new DoubleFunction() { public
     * final double apply(double a) { return Sfun.cosh(a); } };
     */

    /**
     * Function that returns <code>com.imsl.math.Sfun.cot(a)</code>.
     */
    /*
     * public static final DoubleFunction cot = new DoubleFunction() { public
     * final double apply(double a) { return Sfun.cot(a); } };
     */

    /**
     * Function that returns <code>com.imsl.math.Sfun.erf(a)</code>.
     */
    /*
     * public static final DoubleFunction erf = new DoubleFunction() { public
     * final double apply(double a) { return Sfun.erf(a); } };
     */

    /**
     * Function that returns <code>com.imsl.math.Sfun.erfc(a)</code>.
     */
    /*
     * public static final DoubleFunction erfc = new DoubleFunction() { public
     * final double apply(double a) { return Sfun.erfc(a); } };
     */

    /**
     * Function that returns <code>Math.exp(a)</code>.
     */
    public static final DoubleFunction exp = new DoubleFunction() {
        public final double apply(double a) {
            return Math.exp(a);
        }
    };

    /**
     * Function that returns <code>Math.floor(a)</code>.
     */
    public static final DoubleFunction floor = new DoubleFunction() {
        public final double apply(double a) {
            return Math.floor(a);
        }
    };

    /**
     * Function that returns <code>com.imsl.math.Sfun.gamma(a)</code>.
     */
    /*
     * public static final DoubleFunction gamma = new DoubleFunction() { public
     * final double apply(double a) { return Sfun.gamma(a); } };
     */

    /**
     * Function that returns its argument.
     */
    public static final DoubleFunction identity = new DoubleFunction() {
        public final double apply(double a) {
            return a;
        }
    };

    /**
     * Function that returns <code>1.0 / a</code>.
     */
    public static final DoubleFunction inv = new DoubleFunction() {
        public final double apply(double a) {
            return 1.0 / a;
        }
    };

    /**
     * Function that returns <code>Math.log(a)</code>.
     */
    public static final DoubleFunction log = new DoubleFunction() {
        public final double apply(double a) {
            return Math.log(a);
        }
    };

    /**
     * Function that returns <code>com.imsl.math.Sfun.log10(a)</code>.
     */
    /*
     * public static final DoubleFunction log10 = new DoubleFunction() { public
     * final double apply(double a) { return Sfun.log10(a); } };
     */

    /**
     * Function that returns <code>Math.log(a) / Math.log(2)</code>.
     */
    public static final DoubleFunction log2 = new DoubleFunction() {
        // 1.0 / Math.log(2) == 1.4426950408889634
        public final double apply(double a) {
            return Math.log(a) * 1.4426950408889634;
        }
    };

    /**
     * Function that returns <code>com.imsl.math.Sfun.logGamma(a)</code>.
     */
    /*
     * public static final DoubleFunction logGamma = new DoubleFunction() {
     * public final double apply(double a) { return Sfun.logGamma(a); } };
     */

    /**
     * Function that returns <code>-a</code>.
     */
    public static final DoubleFunction neg = new DoubleFunction() {
        public final double apply(double a) {
            return -a;
        }
    };

    /**
     * Function that returns <code>Math.rint(a)</code>.
     */
    public static final DoubleFunction rint = new DoubleFunction() {
        public final double apply(double a) {
            return Math.rint(a);
        }
    };

    /**
     * Function that returns <code>a < 0 ? -1 : a > 0 ? 1 : 0</code>.
     */
    public static final DoubleFunction sign = new DoubleFunction() {
        public final double apply(double a) {
            return a < 0 ? -1 : a > 0 ? 1 : 0;
        }
    };

    /**
     * Function that returns <code>Math.sin(a)</code>.
     */
    public static final DoubleFunction sin = new DoubleFunction() {
        public final double apply(double a) {
            return Math.sin(a);
        }
    };

    /**
     * Function that returns <code>com.imsl.math.Sfun.sinh(a)</code>.
     */
    /*
     * public static final DoubleFunction sinh = new DoubleFunction() { public
     * final double apply(double a) { return Sfun.sinh(a); } };
     */

    /**
     * Function that returns <code>Math.sqrt(a)</code>.
     */
    public static final DoubleFunction sqrt = new DoubleFunction() {
        public final double apply(double a) {
            return Math.sqrt(a);
        }
    };

    /**
     * Function that returns <code>a * a</code>.
     */
    public static final DoubleFunction square = new DoubleFunction() {
        public final double apply(double a) {
            return a * a;
        }
    };

    /**
     * Function that returns <code>Math.tan(a)</code>.
     */
    public static final DoubleFunction tan = new DoubleFunction() {
        public final double apply(double a) {
            return Math.tan(a);
        }
    };

    /**
     * Function that returns <code>com.imsl.math.Sfun.tanh(a)</code>.
     */
    /*
     * public static final DoubleFunction tanh = new DoubleFunction() { public
     * final double apply(double a) { return Sfun.tanh(a); } };
     */

    /**
     * Function that returns <code>Math.toDegrees(a)</code>.
     */
    /*
     * public static final DoubleFunction toDegrees = new DoubleFunction() {
     * public final double apply(double a) { return Math.toDegrees(a); } };
     */

    /**
     * Function that returns <code>Math.toRadians(a)</code>.
     */
    /*
     * public static final DoubleFunction toRadians = new DoubleFunction() {
     * public final double apply(double a) { return Math.toRadians(a); } };
     */

    /***************************************************************************
     * <H3>Binary functions</H3>
     **************************************************************************/

    /**
     * Function that returns <code>Math.atan2(a,b)</code>.
     */
    public static final DoubleDoubleFunction atan2 = new DoubleDoubleFunction() {
        public final double apply(double a, double b) {
            return Math.atan2(a, b);
        }
    };

    /**
     * Function that returns <code>com.imsl.math.Sfun.logBeta(a,b)</code>.
     */
    /*
     * public static final DoubleDoubleFunction logBeta = new
     * DoubleDoubleFunction() { public final double apply(double a, double b) {
     * return Sfun.logBeta(a,b); } };
     */

    /**
     * Function that returns <code>a < b ? -1 : a > b ? 1 : 0</code>.
     */
    public static final DoubleDoubleFunction compare = new DoubleDoubleFunction() {
        public final double apply(double a, double b) {
            return a < b ? -1 : a > b ? 1 : 0;
        }
    };

    /**
     * Function that returns <code>a / b</code>.
     */
    public static final DoubleDoubleFunction div = new DoubleDoubleFunction() {
        public final double apply(double a, double b) {
            return a / b;
        }
    };

    /**
     * Function that returns <code>-(a / b)</code>.
     */
    public static final DoubleDoubleFunction divNeg = new DoubleDoubleFunction() {
        public final double apply(double a, double b) {
            return -(a / b);
        }
    };

    /**
     * Function that returns <code>a == b ? 1 : 0</code>.
     */
    public static final DoubleDoubleFunction equals = new DoubleDoubleFunction() {
        public final double apply(double a, double b) {
            return a == b ? 1 : 0;
        }
    };

    /**
     * Function that returns <code>a > b ? 1 : 0</code>.
     */
    public static final DoubleDoubleFunction greater = new DoubleDoubleFunction() {
        public final double apply(double a, double b) {
            return a > b ? 1 : 0;
        }
    };

    /**
     * Function that returns <code>Math.IEEEremainder(a,b)</code>.
     */
    public static final DoubleDoubleFunction IEEEremainder = new DoubleDoubleFunction() {
        public final double apply(double a, double b) {
            return Math.IEEEremainder(a, b);
        }
    };

    /**
     * Function that returns <code>a == b</code>.
     */
    public static final DoubleDoubleProcedure isEqual = new DoubleDoubleProcedure() {
        public final boolean apply(double a, double b) {
            return a == b;
        }
    };

    /**
     * Function that returns <code>a < b</code>.
     */
    public static final DoubleDoubleProcedure isLess = new DoubleDoubleProcedure() {
        public final boolean apply(double a, double b) {
            return a < b;
        }
    };

    /**
     * Function that returns <code>a > b</code>.
     */
    public static final DoubleDoubleProcedure isGreater = new DoubleDoubleProcedure() {
        public final boolean apply(double a, double b) {
            return a > b;
        }
    };

    /**
     * Function that returns <code>a < b ? 1 : 0</code>.
     */
    public static final DoubleDoubleFunction less = new DoubleDoubleFunction() {
        public final double apply(double a, double b) {
            return a < b ? 1 : 0;
        }
    };

    /**
     * Function that returns <code>Math.log(a) / Math.log(b)</code>.
     */
    public static final DoubleDoubleFunction lg = new DoubleDoubleFunction() {
        public final double apply(double a, double b) {
            return Math.log(a) / Math.log(b);
        }
    };

    /**
     * Function that returns <code>Math.max(a,b)</code>.
     */
    public static final DoubleDoubleFunction max = new DoubleDoubleFunction() {
        public final double apply(double a, double b) {
            return Math.max(a, b);
        }
    };

    /**
     * Function that returns <code>Math.min(a,b)</code>.
     */
    public static final DoubleDoubleFunction min = new DoubleDoubleFunction() {
        public final double apply(double a, double b) {
            return Math.min(a, b);
        }
    };

    /**
     * Function that returns <code>a - b</code>.
     */
    public static final DoubleDoubleFunction minus = plusMultSecond(-1);

    /*
     * new DoubleDoubleFunction() { public final double apply(double a, double
     * b) { return a - b; } };
     */

    /**
     * Function that returns <code>a % b</code>.
     */
    public static final DoubleDoubleFunction mod = new DoubleDoubleFunction() {
        public final double apply(double a, double b) {
            return a % b;
        }
    };

    /**
     * Function that returns <code>a * b</code>.
     */
    public static final DoubleDoubleFunction mult = new DoubleDoubleFunction() {
        public final double apply(double a, double b) {
            return a * b;
        }
    };

    /**
     * Function that returns <code>-(a * b)</code>.
     */
    public static final DoubleDoubleFunction multNeg = new DoubleDoubleFunction() {
        public final double apply(double a, double b) {
            return -(a * b);
        }
    };

    /**
     * Function that returns <code>a * b^2</code>.
     */
    public static final DoubleDoubleFunction multSquare = new DoubleDoubleFunction() {
        public final double apply(double a, double b) {
            return a * b * b;
        }
    };

    /**
     * Function that returns <code>a + b</code>.
     */
    public static final DoubleDoubleFunction plus = plusMultSecond(1);
    //        new DoubleDoubleFunction() {
    //            public final double apply(double a, double b) {
    //                return a + b;
    //            }
    //        };

    /**
     * Function that returns <code>Math.abs(a) + Math.abs(b)</code>.
     */
    public static final DoubleDoubleFunction plusAbs = new DoubleDoubleFunction() {
        public final double apply(double a, double b) {
            return Math.abs(a) + Math.abs(b);
        }
    };

    /**
     * Function that returns <code>Math.pow(a,b)</code>.
     */
    public static final DoubleDoubleFunction pow = new DoubleDoubleFunction() {
        public final double apply(double a, double b) {
            return Math.pow(a, b);
        }
    };

    /**
     * Makes this class non instantiable, but still let's others inherit from
     * it.
     */
    protected DoubleFunctions() {
    }

    /**
     * Constructs a function that returns <code>(from<=a && a<=to) ? 1 : 0</code>.
     * <code>a</code> is a variable, <code>from</code> and <code>to</code> are fixed.
     * @param from
     * @param to
     * @return 
     */
    public static DoubleFunction between(final double from, final double to) {
        return new DoubleFunction() {
            public final double apply(double a) {
                return (from <= a && a <= to) ? 1 : 0;
            }
        };
    }

    /**
     * Constructs a unary function from a binary function with the first operand
     * (argument) fixed to the given constant <code>c</code>. The second operand is
     * variable (free).
     * 
     * @param function
     *            a binary function taking operands in the form
     *            <code>function.apply(c,var)</code>.
     * @param c
     * @return the unary function <code>function(c,var)</code>.
     */
    public static DoubleFunction bindArg1(final DoubleDoubleFunction function, final double c) {
        return new DoubleFunction() {
            public final double apply(double var) {
                return function.apply(c, var);
            }
        };
    }

    /**
     * Constructs a unary function from a binary function with the second
     * operand (argument) fixed to the given constant <code>c</code>. The first
     * operand is variable (free).
     * 
     * @param function
     *            a binary function taking operands in the form
     *            <code>function.apply(var,c)</code>.
     * @param c
     * @return the unary function <code>function(var,c)</code>.
     */
    public static DoubleFunction bindArg2(final DoubleDoubleFunction function, final double c) {
        return new DoubleFunction() {
            public final double apply(double var) {
                return function.apply(var, c);
            }
        };
    }

    /**
     * Constructs the function <code>f( g(a), h(b) )</code>.
     * 
     * @param f
     *            a binary function.
     * @param g
     *            a unary function.
     * @param h
     *            a unary function.
     * @return the binary function <code>f( g(a), h(b) )</code>.
     */
    public static DoubleDoubleFunction chain(final DoubleDoubleFunction f, final DoubleFunction g,
            final DoubleFunction h) {
        return new DoubleDoubleFunction() {
            public final double apply(double a, double b) {
                return f.apply(g.apply(a), h.apply(b));
            }
        };
    }

    /**
     * Constructs the function <code>g( h(a,b) )</code>.
     * 
     * @param g
     *            a unary function.
     * @param h
     *            a binary function.
     * @return the unary function <code>g( h(a,b) )</code>.
     */
    public static DoubleDoubleFunction chain(final DoubleFunction g, final DoubleDoubleFunction h) {
        return new DoubleDoubleFunction() {
            public final double apply(double a, double b) {
                return g.apply(h.apply(a, b));
            }
        };
    }

    /**
     * Constructs the function <code>g( h(a) )</code>.
     * 
     * @param g
     *            a unary function.
     * @param h
     *            a unary function.
     * @return the unary function <code>g( h(a) )</code>.
     */
    public static DoubleFunction chain(final DoubleFunction g, final DoubleFunction h) {
        return new DoubleFunction() {
            public final double apply(double a) {
                return g.apply(h.apply(a));
            }
        };
    }

    /**
     * Constructs a function that returns <code>a < b ? -1 : a > b ? 1 : 0</code>.
     * <code>a</code> is a variable, <code>b</code> is fixed.
     * @param b
     * @return 
     */
    public static DoubleFunction compare(final double b) {
        return new DoubleFunction() {
            public final double apply(double a) {
                return a < b ? -1 : a > b ? 1 : 0;
            }
        };
    }

    /**
     * Constructs a function that returns the constant <code>c</code>.
     * @param c
     * @return 
     */
    public static DoubleFunction constant(final double c) {
        return new DoubleFunction() {
            public final double apply(double a) {
                return c;
            }
        };
    }

    /**
     * Demonstrates usage of this class.
     */
    public static void demo1() {
        cern.jet.math.tdouble.DoubleFunctions F = cern.jet.math.tdouble.DoubleFunctions.functions;
        double a = 0.5;
        double b = 0.2;
        double v = Math.sin(a) + Math.pow(Math.cos(b), 2);
        System.out.println(v);
        DoubleDoubleFunction f = DoubleFunctions.chain(DoubleFunctions.plus, DoubleFunctions.sin, DoubleFunctions
                .chain(DoubleFunctions.square, DoubleFunctions.cos));
        // DoubleDoubleFunction f = F.chain(plus,sin,F.chain(square,cos));
        System.out.println(f.apply(a, b));
        DoubleDoubleFunction g = new DoubleDoubleFunction() {
            public final double apply(double x, double y) {
                return Math.sin(x) + Math.pow(Math.cos(y), 2);
            }
        };
        System.out.println(g.apply(a, b));
        DoubleFunction m = DoubleFunctions.plus(3);
        DoubleFunction n = DoubleFunctions.plus(4);
        System.out.println(m.apply(0));
        System.out.println(n.apply(0));
    }

    /**
     * Benchmarks and demonstrates usage of trivial and complex functions.
     * @param size
     */
    public static void demo2(int size) {
        cern.jet.math.tdouble.DoubleFunctions F = cern.jet.math.tdouble.DoubleFunctions.functions;
        System.out.println("\n\n");
        double a = 0.0;
        double b = 0.0;
        double v = Math.abs(Math.sin(a) + Math.pow(Math.cos(b), 2));
        // double v = Math.sin(a) + Math.pow(Math.cos(b),2);
        // double v = a + b;
        System.out.println(v);

        // DoubleDoubleFunction f = F.chain(F.plus,F.identity,F.identity);
        DoubleDoubleFunction f = DoubleFunctions.chain(DoubleFunctions.abs, DoubleFunctions.chain(DoubleFunctions.plus,
                DoubleFunctions.sin, DoubleFunctions.chain(DoubleFunctions.square, DoubleFunctions.cos)));
        // DoubleDoubleFunction f =
        // F.chain(F.plus,F.sin,F.chain(F.square,F.cos));
        // DoubleDoubleFunction f = F.plus;

        System.out.println(f.apply(a, b));
        DoubleDoubleFunction g = new DoubleDoubleFunction() {
            public final double apply(double x, double y) {
                return Math.abs(Math.sin(x) + Math.pow(Math.cos(y), 2));
            }
            // public final double apply(double x, double y) { return x+y; }
        };
        System.out.println(g.apply(a, b));

        // emptyLoop
        cern.colt.Timer emptyLoop = new cern.colt.Timer().start();
        a = 0;
        b = 0;
        double sum = 0;
        for (int i = size; --i >= 0;) {
            sum += a;
            a++;
            b++;
        }
        emptyLoop.stop().display();
        System.out.println("empty sum=" + sum);

        cern.colt.Timer timer = new cern.colt.Timer().start();
        a = 0;
        b = 0;
        sum = 0;
        for (int i = size; --i >= 0;) {
            sum += Math.abs(Math.sin(a) + Math.pow(Math.cos(b), 2));
            // sum += a + b;
            a++;
            b++;
        }
        timer.stop().display();
        System.out.println("evals / sec = " + size / timer.minus(emptyLoop).seconds());
        System.out.println("sum=" + sum);

        timer.reset().start();
        a = 0;
        b = 0;
        sum = 0;
        for (int i = size; --i >= 0;) {
            sum += f.apply(a, b);
            a++;
            b++;
        }
        timer.stop().display();
        System.out.println("evals / sec = " + size / timer.minus(emptyLoop).seconds());
        System.out.println("sum=" + sum);

        timer.reset().start();
        a = 0;
        b = 0;
        sum = 0;
        for (int i = size; --i >= 0;) {
            sum += g.apply(a, b);
            a++;
            b++;
        }
        timer.stop().display();
        System.out.println("evals / sec = " + size / timer.minus(emptyLoop).seconds());
        System.out.println("sum=" + sum);

    }

    /**
     * Constructs a function that returns <code>a / b</code>. <code>a</code> is a
     * variable, <code>b</code> is fixed.
     * @param b
     * @return 
     */
    public static DoubleFunction div(final double b) {
        return mult(1 / b);
    }

    /**
     * Constructs a function that returns <code>a == b ? 1 : 0</code>. <code>a</code> is
     * a variable, <code>b</code> is fixed.
     * @param b
     * @return 
     */
    public static DoubleFunction equals(final double b) {
        return new DoubleFunction() {
            public final double apply(double a) {
                return a == b ? 1 : 0;
            }
        };
    }

    /**
     * Constructs a function that returns <code>a > b ? 1 : 0</code>. <code>a</code> is
     * a variable, <code>b</code> is fixed.
     * @param b
     * @return 
     */
    public static DoubleFunction greater(final double b) {
        return new DoubleFunction() {
            public final double apply(double a) {
                return a > b ? 1 : 0;
            }
        };
    }

    /**
     * Constructs a function that returns <code>Math.IEEEremainder(a,b)</code>.
     * <code>a</code> is a variable, <code>b</code> is fixed.
     * @param b
     * @return 
     */
    public static DoubleFunction IEEEremainder(final double b) {
        return new DoubleFunction() {
            public final double apply(double a) {
                return Math.IEEEremainder(a, b);
            }
        };
    }

    /**
     * Constructs a function that returns <code>from<=a && a<=to</code>. <code>a</code>
     * is a variable, <code>from</code> and <code>to</code> are fixed.
     * @param from
     * @param to
     * @return 
     */
    public static DoubleProcedure isBetween(final double from, final double to) {
        return new DoubleProcedure() {
            public final boolean apply(double a) {
                return from <= a && a <= to;
            }
        };
    }

    /**
     * Constructs a function that returns <code>a == b</code>. <code>a</code> is a
     * variable, <code>b</code> is fixed.
     * @param b
     * @return 
     */
    public static DoubleProcedure isEqual(final double b) {
        return new DoubleProcedure() {
            public final boolean apply(double a) {
                return a == b;
            }
        };
    }

    /**
     * Constructs a function that returns <code>a > b</code>. <code>a</code> is a
     * variable, <code>b</code> is fixed.
     * @param b
     * @return 
     */
    public static DoubleProcedure isGreater(final double b) {
        return new DoubleProcedure() {
            public final boolean apply(double a) {
                return a > b;
            }
        };
    }

    /**
     * Constructs a function that returns <code>a < b</code>. <code>a</code> is a
     * variable, <code>b</code> is fixed.
     * @param b
     * @return 
     */
    public static DoubleProcedure isLess(final double b) {
        return new DoubleProcedure() {
            public final boolean apply(double a) {
                return a < b;
            }
        };
    }

    /**
     * Constructs a function that returns <code>a < b ? 1 : 0</code>. <code>a</code> is
     * a variable, <code>b</code> is fixed.
     * @param b
     * @return 
     */
    public static DoubleFunction less(final double b) {
        return new DoubleFunction() {
            public final double apply(double a) {
                return a < b ? 1 : 0;
            }
        };
    }

    /**
     * Constructs a function that returns <code><code>Math.log(a) / Math.log(b)</code>
     * </code>. <code>a</code> is a variable, <code>b</code> is fixed.
     * @param b
     * @return 
     */
    public static DoubleFunction lg(final double b) {
        return new DoubleFunction() {
            private final double logInv = 1 / Math.log(b); // cached for speed

            public final double apply(double a) {
                return Math.log(a) * logInv;
            }
        };
    }

    /**
     * Tests various methods of this class.
     * @param args
     */
    protected static void main(String args[]) {
        int size = Integer.parseInt(args[0]);
        demo2(size);
        // demo1();
    }

    /**
     * Constructs a function that returns <code>Math.max(a,b)</code>. <code>a</code> is
     * a variable, <code>b</code> is fixed.
     * @param b
     * @return 
     */
    public static DoubleFunction max(final double b) {
        return new DoubleFunction() {
            public final double apply(double a) {
                return Math.max(a, b);
            }
        };
    }

    /**
     * Constructs a function that returns <code>Math.min(a,b)</code>. <code>a</code> is
     * a variable, <code>b</code> is fixed.
     * @param b
     * @return 
     */
    public static DoubleFunction min(final double b) {
        return new DoubleFunction() {
            public final double apply(double a) {
                return Math.min(a, b);
            }
        };
    }

    /**
     * Constructs a function that returns <code>a - b</code>. <code>a</code> is a
     * variable, <code>b</code> is fixed.
     * @param b
     * @return 
     */
    public static DoubleFunction minus(final double b) {
        return plus(-b);
    }

    /**
     * Constructs a function that returns <code>a - b*constant</code>. <code>a</code>
     * and <code>b</code> are variables, <code>constant</code> is fixed.
     * @param constant
     * @return 
     */
    public static DoubleDoubleFunction minusMult(final double constant) {
        return plusMultSecond(-constant);
    }

    /**
     * Constructs a function that returns <code>a % b</code>. <code>a</code> is a
     * variable, <code>b</code> is fixed.
     * @param b
     * @return 
     */
    public static DoubleFunction mod(final double b) {
        return new DoubleFunction() {
            public final double apply(double a) {
                return a % b;
            }
        };
    }

    /**
     * Constructs a function that returns <code>a * b</code>. <code>a</code> is a
     * variable, <code>b</code> is fixed.
     * @param b
     * @return 
     */
    public static DoubleFunction mult(final double b) {
        return new DoubleMult(b);
        /*
         * return new DoubleFunction() { public final double apply(double a) {
         * return a * b; } };
         */
    }

    /**
     * Constructs a function that returns <code>a + b</code>. <code>a</code> is a
     * variable, <code>b</code> is fixed.
     * @param b
     * @return 
     */
    public static DoubleFunction plus(final double b) {
        return new DoubleFunction() {
            public final double apply(double a) {
                return a + b;
            }
        };
    }

    /**
     * Constructs a function that returns <code>b*constant</code>.
     * @param constant
     * @return 
     */
    public static DoubleDoubleFunction multSecond(final double constant) {

        return new DoubleDoubleFunction() {
            public final double apply(double a, double b) {
                return b * constant;
            }
        };

    }

    /**
     * Constructs a function that returns <code>a + b*constant</code>. <code>a</code>
     * and <code>b</code> are variables, <code>constant</code> is fixed.
     * @param constant
     * @return 
     */
    public static DoubleDoubleFunction plusMultSecond(final double constant) {
        return new DoublePlusMultSecond(constant);
    }

    /**
     * Constructs a function that returns <code>a * constant + b</code>. <code>a</code>
     * and <code>b</code> are variables, <code>constant</code> is fixed.
     * @param constant
     * @return 
     */
    public static DoubleDoubleFunction plusMultFirst(final double constant) {
        return new DoublePlusMultFirst(constant);
    }

    /**
     * Constructs a function that returns <code>Math.pow(a,b)</code>. <code>a</code> is
     * a variable, <code>b</code> is fixed.
     * @param b
     * @return 
     */
    public static DoubleFunction pow(final double b) {
        return new DoubleFunction() {
            public final double apply(double a) {
                return Math.pow(a, b);
            }
        };
    }

    /**
     * Constructs a function that returns a new uniform random number in the
     * open unit interval <code>(0.0,1.0)</code> (excluding 0.0 and 1.0).
     * Currently the engine is
     * {@link cern.jet.random.tdouble.engine.DoubleMersenneTwister} and is
     * seeded with the current time.
     * <p>
     * Note that any random engine derived from
     * {@link cern.jet.random.tdouble.engine.DoubleRandomEngine} and any random
     * distribution derived from
     * {@link cern.jet.random.tdouble.AbstractDoubleDistribution} are function
     * objects, because they implement the proper interfaces. Thus, if you are
     * not happy with the default, just pass your favourite random generator to
     * function evaluating methods.
     * @return 
     */
    public static DoubleFunction random() {
        return new RandomDoubleFunction();
    }

    // TODO
    private static class RandomDoubleFunction implements DoubleFunction {

        public double apply(double argument) {
            return Math.random();
        }

    }

    /**
     * Constructs a function that returns the number rounded to the given
     * precision; <code>Math.rint(a/precision)*precision</code>. Examples:
     * 
     * <pre>
     * precision = 0.01 rounds 0.012 --&gt; 0.01, 0.018 --&gt; 0.02
     * precision = 10   rounds 123   --&gt; 120 , 127   --&gt; 130
     * </pre>
     * @param precision
     * @return 
     */
    public static DoubleFunction round(final double precision) {
        return new DoubleFunction() {
            public final double apply(double a) {
                return Math.rint(a / precision) * precision;
            }
        };
    }

    /**
     * Constructs a function that returns <code>function.apply(b,a)</code>, i.e.
     * applies the function with the first operand as second operand and the
     * second operand as first operand.
     * 
     * @param function
     *            a function taking operands in the form
     *            <code>function.apply(a,b)</code>.
     * @return the binary function <code>function(b,a)</code>.
     */
    public static DoubleDoubleFunction swapArgs(final DoubleDoubleFunction function) {
        return new DoubleDoubleFunction() {
            public final double apply(double a, double b) {
                return function.apply(b, a);
            }
        };
    }
}
