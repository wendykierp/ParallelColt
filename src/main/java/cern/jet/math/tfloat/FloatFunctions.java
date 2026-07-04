/*
Copyright (C) 1999 CERN - European Organization for Nuclear Research.
Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose 
is hereby granted without fee, provided that the above copyright notice appear in all copies and 
that both that copyright notice and this permission notice appear in supporting documentation. 
CERN makes no representations about the suitability of this software for any purpose. 
It is provided "as is" without expressed or implied warranty.
 */
package cern.jet.math.tfloat;

import cern.colt.function.tfloat.FloatFloatFunction;
import cern.colt.function.tfloat.FloatFloatProcedure;
import cern.colt.function.tfloat.FloatFunction;
import cern.colt.function.tfloat.FloatProcedure;

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
 * Unary functions are of type {@link cern.colt.function.tfloat.FloatFunction} ,
 * binary functions of type {@link cern.colt.function.tfloat.FloatFloatFunction}
 * . All can be retrieved via <code>public 
 static final</code> variables named after the function. Unary predicates are of
 * type {@link cern.colt.function.tfloat.FloatProcedure}, binary predicates of
 * type {@link cern.colt.function.tfloat.FloatFloatProcedure}. All can be
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
 * {@link #bindArg1(FloatFloatFunction,float)} and
 * {@link #bindArg2(FloatFloatFunction,float)}. The order of arguments can be
 * swapped so that the first argument becomes the second and vice-versa. See
 * method {@link #swapArgs(FloatFloatFunction)}. Example:
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
 * {@link #chain(FloatFunction,FloatFunction)}:
 * <ul>
 * <li><code>Functions.chain(g,h);</code>
 * </ul>
 * Assume further we have a binary function <code>f</code>. The binary function
 * <code>g(f(a,b))</code> can be generated via
 * {@link #chain(FloatFunction,FloatFloatFunction)}:
 * <ul>
 * <li><code>Functions.chain(g,f);</code>
 * </ul>
 * The binary function <code>f(g(a),h(b))</code> can be generated via
 * {@link #chain(FloatFloatFunction,FloatFunction,FloatFunction)}:
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
 * new FloatFloatFunction() {
 *     public final float apply(float a, float b) {
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
 * float a = 0.5;
 * float b = 0.2;
 * float v = Math.sin(a) + Math.pow(Math.cos(b), 2);
 * System.out.println(v);
 * Functions F = Functions.functions;
 * FloatFloatFunction f = F.chain(F.plus, F.sin, F.chain(F.square, F.cos));
 * System.out.println(f.apply(a, b));
 * FloatFloatFunction g = new FloatFloatFunction() {
 *     public float apply(float a, float b) {
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
public class FloatFunctions extends Object {
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
    public static final FloatFunctions functions = new FloatFunctions();

    /***************************************************************************
     * <H3>Unary functions</H3>
     **************************************************************************/
    /**
     * Function that returns <code>Math.abs(a)</code>.
     */
    public static final FloatFunction abs = new FloatFunction() {
        public final float apply(float a) {
            return Math.abs(a);
        }
    };

    /**
     * Function that returns <code>Math.acos(a)</code>.
     */
    public static final FloatFunction acos = new FloatFunction() {
        public final float apply(float a) {
            return (float) Math.acos(a);
        }
    };

    /**
     * Function that returns <code>com.imsl.math.Sfun.acosh(a)</code>.
     */
    /*
     * public static final FloatFunction acosh = new FloatFunction() { public
     * final float apply(float a) { return Sfun.acosh(a); } };
     */

    /**
     * Function that returns <code>Math.asin(a)</code>.
     */
    public static final FloatFunction asin = new FloatFunction() {
        public final float apply(float a) {
            return (float) Math.asin(a);
        }
    };

    /**
     * Function that returns <code>com.imsl.math.Sfun.asinh(a)</code>.
     */
    /*
     * public static final FloatFunction asinh = new FloatFunction() { public
     * final float apply(float a) { return Sfun.asinh(a); } };
     */

    /**
     * Function that returns <code>Math.atan(a)</code>.
     */
    public static final FloatFunction atan = new FloatFunction() {
        public final float apply(float a) {
            return (float) Math.atan(a);
        }
    };

    /**
     * Function that returns <code>com.imsl.math.Sfun.atanh(a)</code>.
     */
    /*
     * public static final FloatFunction atanh = new FloatFunction() { public
     * final float apply(float a) { return Sfun.atanh(a); } };
     */

    /**
     * Function that returns <code>Math.ceil(a)</code>.
     */
    public static final FloatFunction ceil = new FloatFunction() {
        public final float apply(float a) {
            return (float) Math.ceil(a);
        }
    };

    /**
     * Function that returns <code>Math.cos(a)</code>.
     */
    public static final FloatFunction cos = new FloatFunction() {
        public final float apply(float a) {
            return (float) Math.cos(a);
        }
    };

    /**
     * Function that returns <code>com.imsl.math.Sfun.cosh(a)</code>.
     */
    /*
     * public static final FloatFunction cosh = new FloatFunction() { public
     * final float apply(float a) { return Sfun.cosh(a); } };
     */

    /**
     * Function that returns <code>com.imsl.math.Sfun.cot(a)</code>.
     */
    /*
     * public static final FloatFunction cot = new FloatFunction() { public
     * final float apply(float a) { return Sfun.cot(a); } };
     */

    /**
     * Function that returns <code>com.imsl.math.Sfun.erf(a)</code>.
     */
    /*
     * public static final FloatFunction erf = new FloatFunction() { public
     * final float apply(float a) { return Sfun.erf(a); } };
     */

    /**
     * Function that returns <code>com.imsl.math.Sfun.erfc(a)</code>.
     */
    /*
     * public static final FloatFunction erfc = new FloatFunction() { public
     * final float apply(float a) { return Sfun.erfc(a); } };
     */

    /**
     * Function that returns <code>Math.exp(a)</code>.
     */
    public static final FloatFunction exp = new FloatFunction() {
        public final float apply(float a) {
            return (float) Math.exp(a);
        }
    };

    /**
     * Function that returns <code>Math.floor(a)</code>.
     */
    public static final FloatFunction floor = new FloatFunction() {
        public final float apply(float a) {
            return (float) Math.floor(a);
        }
    };

    /**
     * Function that returns <code>com.imsl.math.Sfun.gamma(a)</code>.
     */
    /*
     * public static final FloatFunction gamma = new FloatFunction() { public
     * final float apply(float a) { return Sfun.gamma(a); } };
     */

    /**
     * Function that returns its argument.
     */
    public static final FloatFunction identity = new FloatFunction() {
        public final float apply(float a) {
            return a;
        }
    };

    /**
     * Function that returns <code>1.0 / a</code>.
     */
    public static final FloatFunction inv = new FloatFunction() {
        public final float apply(float a) {
            return (float) (1.0 / a);
        }
    };

    /**
     * Function that returns <code>Math.log(a)</code>.
     */
    public static final FloatFunction log = new FloatFunction() {
        public final float apply(float a) {
            return (float) Math.log(a);
        }
    };

    /**
     * Function that returns <code>com.imsl.math.Sfun.log10(a)</code>.
     */
    /*
     * public static final FloatFunction log10 = new FloatFunction() { public
     * final float apply(float a) { return Sfun.log10(a); } };
     */

    /**
     * Function that returns <code>Math.log(a) / Math.log(2)</code>.
     */
    public static final FloatFunction log2 = new FloatFunction() {
        // 1.0 / Math.log(2) == 1.4426950408889634
        public final float apply(float a) {
            return (float) (Math.log(a) * 1.4426950408889634);
        }
    };

    /**
     * Function that returns <code>com.imsl.math.Sfun.logGamma(a)</code>.
     */
    /*
     * public static final FloatFunction logGamma = new FloatFunction() {
     * public final float apply(float a) { return Sfun.logGamma(a); } };
     */

    /**
     * Function that returns <code>-a</code>.
     */
    public static final FloatFunction neg = new FloatFunction() {
        public final float apply(float a) {
            return -a;
        }
    };

    /**
     * Function that returns <code>Math.rint(a)</code>.
     */
    public static final FloatFunction rint = new FloatFunction() {
        public final float apply(float a) {
            return (float) Math.rint(a);
        }
    };

    /**
     * Function that returns <code>a < 0 ? -1 : a > 0 ? 1 : 0</code>.
     */
    public static final FloatFunction sign = new FloatFunction() {
        public final float apply(float a) {
            return a < 0 ? -1 : a > 0 ? 1 : 0;
        }
    };

    /**
     * Function that returns <code>Math.sin(a)</code>.
     */
    public static final FloatFunction sin = new FloatFunction() {
        public final float apply(float a) {
            return (float) Math.sin(a);
        }
    };

    /**
     * Function that returns <code>com.imsl.math.Sfun.sinh(a)</code>.
     */
    /*
     * public static final FloatFunction sinh = new FloatFunction() { public
     * final float apply(float a) { return Sfun.sinh(a); } };
     */

    /**
     * Function that returns <code>Math.sqrt(a)</code>.
     */
    public static final FloatFunction sqrt = new FloatFunction() {
        public final float apply(float a) {
            return (float) Math.sqrt(a);
        }
    };

    /**
     * Function that returns <code>a * a</code>.
     */
    public static final FloatFunction square = new FloatFunction() {
        public final float apply(float a) {
            return a * a;
        }
    };

    /**
     * Function that returns <code>Math.tan(a)</code>.
     */
    public static final FloatFunction tan = new FloatFunction() {
        public final float apply(float a) {
            return (float) Math.tan(a);
        }
    };

    /**
     * Function that returns <code>com.imsl.math.Sfun.tanh(a)</code>.
     */
    /*
     * public static final FloatFunction tanh = new FloatFunction() { public
     * final float apply(float a) { return Sfun.tanh(a); } };
     */

    /**
     * Function that returns <code>Math.toDegrees(a)</code>.
     */
    /*
     * public static final FloatFunction toDegrees = new FloatFunction() {
     * public final float apply(float a) { return Math.toDegrees(a); } };
     */

    /**
     * Function that returns <code>Math.toRadians(a)</code>.
     */
    /*
     * public static final FloatFunction toRadians = new FloatFunction() {
     * public final float apply(float a) { return Math.toRadians(a); } };
     */

    /***************************************************************************
     * <H3>Binary functions</H3>
     **************************************************************************/

    /**
     * Function that returns <code>Math.atan2(a,b)</code>.
     */
    public static final FloatFloatFunction atan2 = new FloatFloatFunction() {
        public final float apply(float a, float b) {
            return (float) Math.atan2(a, b);
        }
    };

    /**
     * Function that returns <code>com.imsl.math.Sfun.logBeta(a,b)</code>.
     */
    /*
     * public static final FloatFloatFunction logBeta = new
     * FloatFloatFunction() { public final float apply(float a, float b) {
     * return Sfun.logBeta(a,b); } };
     */

    /**
     * Function that returns <code>a < b ? -1 : a > b ? 1 : 0</code>.
     */
    public static final FloatFloatFunction compare = new FloatFloatFunction() {
        public final float apply(float a, float b) {
            return a < b ? -1 : a > b ? 1 : 0;
        }
    };

    /**
     * Function that returns <code>a / b</code>.
     */
    public static final FloatFloatFunction div = new FloatFloatFunction() {
        public final float apply(float a, float b) {
            return a / b;
        }
    };

    /**
     * Function that returns <code>-(a / b)</code>.
     */
    public static final FloatFloatFunction divNeg = new FloatFloatFunction() {
        public final float apply(float a, float b) {
            return -(a / b);
        }
    };

    /**
     * Function that returns <code>a == b ? 1 : 0</code>.
     */
    public static final FloatFloatFunction equals = new FloatFloatFunction() {
        public final float apply(float a, float b) {
            return a == b ? 1 : 0;
        }
    };

    /**
     * Function that returns <code>a > b ? 1 : 0</code>.
     */
    public static final FloatFloatFunction greater = new FloatFloatFunction() {
        public final float apply(float a, float b) {
            return a > b ? 1 : 0;
        }
    };

    /**
     * Function that returns <code>Math.IEEEremainder(a,b)</code>.
     */
    public static final FloatFloatFunction IEEEremainder = new FloatFloatFunction() {
        public final float apply(float a, float b) {
            return (float) Math.IEEEremainder(a, b);
        }
    };

    /**
     * Function that returns <code>a == b</code>.
     */
    public static final FloatFloatProcedure isEqual = new FloatFloatProcedure() {
        public final boolean apply(float a, float b) {
            return a == b;
        }
    };

    /**
     * Function that returns <code>a < b</code>.
     */
    public static final FloatFloatProcedure isLess = new FloatFloatProcedure() {
        public final boolean apply(float a, float b) {
            return a < b;
        }
    };

    /**
     * Function that returns <code>a > b</code>.
     */
    public static final FloatFloatProcedure isGreater = new FloatFloatProcedure() {
        public final boolean apply(float a, float b) {
            return a > b;
        }
    };

    /**
     * Function that returns <code>a < b ? 1 : 0</code>.
     */
    public static final FloatFloatFunction less = new FloatFloatFunction() {
        public final float apply(float a, float b) {
            return a < b ? 1 : 0;
        }
    };

    /**
     * Function that returns <code>Math.log(a) / Math.log(b)</code>.
     */
    public static final FloatFloatFunction lg = new FloatFloatFunction() {
        public final float apply(float a, float b) {
            return (float) (Math.log(a) / Math.log(b));
        }
    };

    /**
     * Function that returns <code>Math.max(a,b)</code>.
     */
    public static final FloatFloatFunction max = new FloatFloatFunction() {
        public final float apply(float a, float b) {
            return Math.max(a, b);
        }
    };

    /**
     * Function that returns <code>Math.min(a,b)</code>.
     */
    public static final FloatFloatFunction min = new FloatFloatFunction() {
        public final float apply(float a, float b) {
            return Math.min(a, b);
        }
    };

    /**
     * Function that returns <code>a - b</code>.
     */
    public static final FloatFloatFunction minus = plusMultSecond(-1);

    /*
     * new FloatFloatFunction() { public final float apply(float a, float
     * b) { return a - b; } };
     */

    /**
     * Function that returns <code>a % b</code>.
     */
    public static final FloatFloatFunction mod = new FloatFloatFunction() {
        public final float apply(float a, float b) {
            return a % b;
        }
    };

    /**
     * Function that returns <code>a * b</code>.
     */
    public static final FloatFloatFunction mult = new FloatFloatFunction() {
        public final float apply(float a, float b) {
            return a * b;
        }
    };

    /**
     * Function that returns <code>-(a * b)</code>.
     */
    public static final FloatFloatFunction multNeg = new FloatFloatFunction() {
        public final float apply(float a, float b) {
            return -(a * b);
        }
    };

    /**
     * Function that returns <code>a * b^2</code>.
     */
    public static final FloatFloatFunction multSquare = new FloatFloatFunction() {
        public final float apply(float a, float b) {
            return a * b * b;
        }
    };

    /**
     * Function that returns <code>a + b</code>.
     */
    public static final FloatFloatFunction plus = plusMultSecond(1);
    //        new FloatFloatFunction() {
    //            public final float apply(float a, float b) {
    //                return a + b;
    //            }
    //        };

    /**
     * Function that returns <code>Math.abs(a) + Math.abs(b)</code>.
     */
    public static final FloatFloatFunction plusAbs = new FloatFloatFunction() {
        public final float apply(float a, float b) {
            return Math.abs(a) + Math.abs(b);
        }
    };

    /**
     * Function that returns <code>Math.pow(a,b)</code>.
     */
    public static final FloatFloatFunction pow = new FloatFloatFunction() {
        public final float apply(float a, float b) {
            return (float) Math.pow(a, b);
        }
    };

    /**
     * Makes this class non instantiable, but still let's others inherit from
     * it.
     */
    protected FloatFunctions() {
    }

    /**
     * Constructs a function that returns <code>(from<=a && a<=to) ? 1 : 0</code>.
     * <code>a</code> is a variable, <code>from</code> and <code>to</code> are fixed.
     * @param from
     * @param to
     * @return 
     */
    public static FloatFunction between(final float from, final float to) {
        return new FloatFunction() {
            public final float apply(float a) {
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
    public static FloatFunction bindArg1(final FloatFloatFunction function, final float c) {
        return new FloatFunction() {
            public final float apply(float var) {
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
    public static FloatFunction bindArg2(final FloatFloatFunction function, final float c) {
        return new FloatFunction() {
            public final float apply(float var) {
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
    public static FloatFloatFunction chain(final FloatFloatFunction f, final FloatFunction g, final FloatFunction h) {
        return new FloatFloatFunction() {
            public final float apply(float a, float b) {
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
    public static FloatFloatFunction chain(final FloatFunction g, final FloatFloatFunction h) {
        return new FloatFloatFunction() {
            public final float apply(float a, float b) {
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
    public static FloatFunction chain(final FloatFunction g, final FloatFunction h) {
        return new FloatFunction() {
            public final float apply(float a) {
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
    public static FloatFunction compare(final float b) {
        return new FloatFunction() {
            public final float apply(float a) {
                return a < b ? -1 : a > b ? 1 : 0;
            }
        };
    }

    /**
     * Constructs a function that returns the constant <code>c</code>.
     * @param c
     * @return 
     */
    public static FloatFunction constant(final float c) {
        return new FloatFunction() {
            public final float apply(float a) {
                return c;
            }
        };
    }

    /**
     * Demonstrates usage of this class.
     */
    public static void demo1() {
        cern.jet.math.tfloat.FloatFunctions F = cern.jet.math.tfloat.FloatFunctions.functions;
        float a = 0.5f;
        float b = 0.2f;
        float v = (float) (Math.sin(a) + Math.pow(Math.cos(b), 2));
        System.out.println(v);
        FloatFloatFunction f = FloatFunctions.chain(FloatFunctions.plus, FloatFunctions.sin, FloatFunctions.chain(
                FloatFunctions.square, FloatFunctions.cos));
        // FloatFloatFunction f = F.chain(plus,sin,F.chain(square,cos));
        System.out.println(f.apply(a, b));
        FloatFloatFunction g = new FloatFloatFunction() {
            public final float apply(float x, float y) {
                return (float) (Math.sin(x) + Math.pow(Math.cos(y), 2));
            }
        };
        System.out.println(g.apply(a, b));
        FloatFunction m = FloatFunctions.plus(3);
        FloatFunction n = FloatFunctions.plus(4);
        System.out.println(m.apply(0));
        System.out.println(n.apply(0));
    }

    /**
     * Benchmarks and demonstrates usage of trivial and complex functions.
     * @param size
     */
    public static void demo2(int size) {
        cern.jet.math.tfloat.FloatFunctions F = cern.jet.math.tfloat.FloatFunctions.functions;
        System.out.println("\n\n");
        float a = 0.0f;
        float b = 0.0f;
        float v = (float) Math.abs(Math.sin(a) + Math.pow(Math.cos(b), 2));
        // float v = Math.sin(a) + Math.pow(Math.cos(b),2);
        // float v = a + b;
        System.out.println(v);

        // FloatFloatFunction f = F.chain(F.plus,F.identity,F.identity);
        FloatFloatFunction f = FloatFunctions.chain(FloatFunctions.abs, FloatFunctions.chain(FloatFunctions.plus,
                FloatFunctions.sin, FloatFunctions.chain(FloatFunctions.square, FloatFunctions.cos)));
        // FloatFloatFunction f =
        // F.chain(F.plus,F.sin,F.chain(F.square,F.cos));
        // FloatFloatFunction f = F.plus;

        System.out.println(f.apply(a, b));
        FloatFloatFunction g = new FloatFloatFunction() {
            public final float apply(float x, float y) {
                return (float) Math.abs(Math.sin(x) + Math.pow(Math.cos(y), 2));
            }
            // public final float apply(float x, float y) { return x+y; }
        };
        System.out.println(g.apply(a, b));

        // emptyLoop
        cern.colt.Timer emptyLoop = new cern.colt.Timer().start();
        a = 0;
        b = 0;
        float sum = 0;
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
    public static FloatFunction div(final float b) {
        return mult(1 / b);
    }

    /**
     * Constructs a function that returns <code>a == b ? 1 : 0</code>. <code>a</code> is
     * a variable, <code>b</code> is fixed.
     * @param b
     * @return 
     */
    public static FloatFunction equals(final float b) {
        return new FloatFunction() {
            public final float apply(float a) {
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
    public static FloatFunction greater(final float b) {
        return new FloatFunction() {
            public final float apply(float a) {
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
    public static FloatFunction IEEEremainder(final float b) {
        return new FloatFunction() {
            public final float apply(float a) {
                return (float) Math.IEEEremainder(a, b);
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
    public static FloatProcedure isBetween(final float from, final float to) {
        return new FloatProcedure() {
            public final boolean apply(float a) {
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
    public static FloatProcedure isEqual(final float b) {
        return new FloatProcedure() {
            public final boolean apply(float a) {
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
    public static FloatProcedure isGreater(final float b) {
        return new FloatProcedure() {
            public final boolean apply(float a) {
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
    public static FloatProcedure isLess(final float b) {
        return new FloatProcedure() {
            public final boolean apply(float a) {
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
    public static FloatFunction less(final float b) {
        return new FloatFunction() {
            public final float apply(float a) {
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
    public static FloatFunction lg(final float b) {
        return new FloatFunction() {
            private final double logInv = 1 / Math.log(b); // cached for speed

            public final float apply(float a) {
                return (float) (Math.log(a) * logInv);
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
    public static FloatFunction max(final float b) {
        return new FloatFunction() {
            public final float apply(float a) {
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
    public static FloatFunction min(final float b) {
        return new FloatFunction() {
            public final float apply(float a) {
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
    public static FloatFunction minus(final float b) {
        return plus(-b);
    }

    /**
     * Constructs a function that returns <code>a - b*constant</code>. <code>a</code>
     * and <code>b</code> are variables, <code>constant</code> is fixed.
     * @param constant
     * @return 
     */
    public static FloatFloatFunction minusMult(final float constant) {
        return plusMultSecond(-constant);
    }

    /**
     * Constructs a function that returns <code>a % b</code>. <code>a</code> is a
     * variable, <code>b</code> is fixed.
     * @param b
     * @return 
     */
    public static FloatFunction mod(final float b) {
        return new FloatFunction() {
            public final float apply(float a) {
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
    public static FloatFunction mult(final float b) {
        return new FloatMult(b);
        /*
         * return new FloatFunction() { public final float apply(float a) {
         * return a * b; } };
         */
    }

    /**
     * Constructs a function that returns <code>a + b</code>. <code>a</code> is a
     * variable, <code>b</code> is fixed.
     * @param b
     * @return 
     */
    public static FloatFunction plus(final float b) {
        return new FloatFunction() {
            public final float apply(float a) {
                return a + b;
            }
        };
    }

    public static FloatFloatFunction multSecond(final float constant) {

        return new FloatFloatFunction() {
            public final float apply(float a, float b) {
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
    public static FloatFloatFunction plusMultSecond(final float constant) {
        return new FloatPlusMultSecond(constant);
    }

    /**
     * Constructs a function that returns <code>a * constant + b</code>. <code>a</code>
     * and <code>b</code> are variables, <code>constant</code> is fixed.
     * @param constant
     * @return 
     */
    public static FloatFloatFunction plusMultFirst(final float constant) {
        return new FloatPlusMultFirst(constant);
    }

    /**
     * Constructs a function that returns <code>Math.pow(a,b)</code>. <code>a</code> is
     * a variable, <code>b</code> is fixed.
     * @param b
     * @return 
     */
    public static FloatFunction pow(final float b) {
        return new FloatFunction() {
            public final float apply(float a) {
                return (float) Math.pow(a, b);
            }
        };
    }

    /**
     * Constructs a function that returns a new uniform random number in the
     * open unit interval <code>(0.0,1.0)</code> (excluding 0.0 and 1.0).
     * Currently the engine is
     * {@link cern.jet.random.tfloat.engine.FloatMersenneTwister} and is seeded
     * with the current time.
     * <p>
     * Note that any random engine derived from
     * {@link cern.jet.random.tfloat.engine.FloatRandomEngine} and any random
     * distribution derived from
     * {@link cern.jet.random.tfloat.AbstractFloatDistribution} are function
     * objects, because they implement the proper interfaces. Thus, if you are
     * not happy with the default, just pass your favourite random generator to
     * function evaluating methods.
     * @return 
     */
    public static FloatFunction random() {
        return new RandomFloatFunction();
    }

    // TODO
    private static class RandomFloatFunction implements FloatFunction {

        public float apply(float argument) {
            return (float) Math.random();
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
    public static FloatFunction round(final float precision) {
        return new FloatFunction() {
            public final float apply(float a) {
                return (float) (Math.rint(a / precision) * precision);
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
    public static FloatFloatFunction swapArgs(final FloatFloatFunction function) {
        return new FloatFloatFunction() {
            public final float apply(float a, float b) {
                return function.apply(b, a);
            }
        };
    }
}
