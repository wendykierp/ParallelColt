/*
Copyright (C) 1999 CERN - European Organization for Nuclear Research.
Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose 
is hereby granted without fee, provided that the above copyright notice appear in all copies and 
that both that copyright notice and this permission notice appear in supporting documentation. 
CERN makes no representations about the suitability of this software for any purpose. 
It is provided "as is" without expressed or implied warranty.
 */
package cern.jet.math.tint;

import cern.colt.function.tint.IntFunction;
import cern.colt.function.tint.IntIntFunction;
import cern.colt.function.tint.IntIntProcedure;
import cern.colt.function.tint.IntProcedure;
import cern.jet.math.tdouble.DoubleArithmetic;
import cern.jet.math.tdouble.DoubleFunctions;

/**
 * Int Function objects to be passed to generic methods. Same as
 * {@link DoubleFunctions} except operating on longs.
 * <p>
 * For aliasing see {@link #intFunctions}.
 * 
 * @author wolfgang.hoschek@cern.ch
 * @version 1.0, 09/24/99
 */
public class IntFunctions extends Object {
    /**
     * Little trick to allow for "aliasing", that is, renaming this class.
     * Writing code like
     * <p>
     * <code>IntFunctions.chain(IntFunctions.plus,IntFunctions.mult(3),IntFunctions.chain(IntFunctions.square,IntFunctions.div(2)));</code>
     * <p>
     * is a bit awkward, to say the least. Using the aliasing you can instead
     * write
     * <p>
     * <code>IntFunctions F = IntFunctions.longFunctions; <br>
    F.chain(F.plus,F.mult(3),F.chain(F.square,F.div(2)));</code>
     * <p>
     */
    public static final IntFunctions intFunctions = new IntFunctions();

    /***************************************************************************
     * <H3>Unary functions</H3>
     **************************************************************************/
    /**
     * Function that returns <code>Math.abs(a) == (a < 0) ? -a : a</code>.
     */
    public static final IntFunction abs = new IntFunction() {
        public final int apply(int a) {
            return (a < 0) ? -a : a;
        }
    };

    /**
     * Function that returns <code>a--</code>.
     */
    public static final IntFunction dec = new IntFunction() {
        public final int apply(int a) {
            return a--;
        }
    };

    /**
     * Function that returns <code>(int) Arithmetic.factorial(a)</code>.
     */
    public static final IntFunction factorial = new IntFunction() {
        public final int apply(int a) {
            return (int) DoubleArithmetic.factorial(a);
        }
    };

    /**
     * Function that returns its argument.
     */
    public static final IntFunction identity = new IntFunction() {
        public final int apply(int a) {
            return a;
        }
    };

    /**
     * Function that returns <code>a++</code>.
     */
    public static final IntFunction inc = new IntFunction() {
        public final int apply(int a) {
            return a++;
        }
    };

    /**
     * Function that returns <code>-a</code>.
     */
    public static final IntFunction neg = new IntFunction() {
        public final int apply(int a) {
            return -a;
        }
    };

    /**
     * Function that returns <code>~a</code>.
     */
    public static final IntFunction not = new IntFunction() {
        public final int apply(int a) {
            return ~a;
        }
    };

    /**
     * Function that returns <code>a < 0 ? -1 : a > 0 ? 1 : 0</code>.
     */
    public static final IntFunction sign = new IntFunction() {
        public final int apply(int a) {
            return a < 0 ? -1 : a > 0 ? 1 : 0;
        }
    };

    /**
     * Function that returns <code>a * a</code>.
     */
    public static final IntFunction square = new IntFunction() {
        public final int apply(int a) {
            return a * a;
        }
    };

    /***************************************************************************
     * <H3>Binary functions</H3>
     **************************************************************************/

    /**
     * Function that returns <code>a & b</code>.
     */
    public static final IntIntFunction and = new IntIntFunction() {
        public final int apply(int a, int b) {
            return a & b;
        }
    };

    /**
     * Function that returns <code>a < b ? -1 : a > b ? 1 : 0</code>.
     */
    public static final IntIntFunction compare = new IntIntFunction() {
        public final int apply(int a, int b) {
            return a < b ? -1 : a > b ? 1 : 0;
        }
    };

    /**
     * Function that returns <code>a / b</code>.
     */
    public static final IntIntFunction div = new IntIntFunction() {
        public final int apply(int a, int b) {
            return a / b;
        }
    };

    /**
     * Function that returns <code>-(a / b)</code>.
     */
    public static final IntIntFunction divNeg = new IntIntFunction() {
        public final int apply(int a, int b) {
            return -(a / b);
        }
    };

    /**
     * Function that returns <code>a == b ? 1 : 0</code>.
     */
    public static final IntIntFunction equals = new IntIntFunction() {
        public final int apply(int a, int b) {
            return a == b ? 1 : 0;
        }
    };

    /**
     * Function that returns <code>a == b</code>.
     */
    public static final IntIntProcedure isEqual = new IntIntProcedure() {
        public final boolean apply(int a, int b) {
            return a == b;
        }
    };

    /**
     * Function that returns <code>a < b</code>.
     */
    public static final IntIntProcedure isLess = new IntIntProcedure() {
        public final boolean apply(int a, int b) {
            return a < b;
        }
    };

    /**
     * Function that returns <code>a > b</code>.
     */
    public static final IntIntProcedure isGreater = new IntIntProcedure() {
        public final boolean apply(int a, int b) {
            return a > b;
        }
    };

    /**
     * Function that returns <code>Math.max(a,b)</code>.
     */
    public static final IntIntFunction max = new IntIntFunction() {
        public final int apply(int a, int b) {
            return (a >= b) ? a : b;
        }
    };

    /**
     * Function that returns <code>Math.min(a,b)</code>.
     */
    public static final IntIntFunction min = new IntIntFunction() {
        public final int apply(int a, int b) {
            return (a <= b) ? a : b;
        }
    };

    /**
     * Function that returns <code>a - b</code>.
     */
    public static final IntIntFunction minus = new IntIntFunction() {
        public final int apply(int a, int b) {
            return a - b;
        }
    };

    /**
     * Function that returns <code>a % b</code>.
     */
    public static final IntIntFunction mod = new IntIntFunction() {
        public final int apply(int a, int b) {
            return a % b;
        }
    };

    /**
     * Function that returns <code>a * b</code>.
     */
    public static final IntIntFunction mult = new IntIntFunction() {
        public final int apply(int a, int b) {
            return a * b;
        }
    };

    /**
     * Function that returns <code>-(a * b)</code>.
     */
    public static final IntIntFunction multNeg = new IntIntFunction() {
        public final int apply(int a, int b) {
            return -(a * b);
        }
    };

    /**
     * Function that returns <code>a * b^2</code>.
     */
    public static final IntIntFunction multSquare = new IntIntFunction() {
        public final int apply(int a, int b) {
            return a * b * b;
        }
    };

    /**
     * Function that returns <code>a | b</code>.
     */
    public static final IntIntFunction or = new IntIntFunction() {
        public final int apply(int a, int b) {
            return a | b;
        }
    };

    /**
     * Function that returns <code>a + b</code>.
     */
    public static final IntIntFunction plus = new IntIntFunction() {
        public final int apply(int a, int b) {
            return a + b;
        }
    };

    /**
     * Function that returns <code>Math.abs(a) + Math.abs(b)</code>.
     */
    public static final IntIntFunction plusAbs = new IntIntFunction() {
        public final int apply(int a, int b) {
            return Math.abs(a) + Math.abs(b);
        }
    };

    /**
     * Function that returns <code>(int) Math.pow(a,b)</code>.
     */
    public static final IntIntFunction pow = new IntIntFunction() {
        public final int apply(int a, int b) {
            return (int) Math.pow(a, b);
        }
    };

    /**
     * Function that returns <code>a << b</code>.
     */
    public static final IntIntFunction shiftLeft = new IntIntFunction() {
        public final int apply(int a, int b) {
            return a << b;
        }
    };

    /**
     * Function that returns <code>a >> b</code>.
     */
    public static final IntIntFunction shiftRightSigned = new IntIntFunction() {
        public final int apply(int a, int b) {
            return a >> b;
        }
    };

    /**
     * Function that returns <code>a >>> b</code>.
     */
    public static final IntIntFunction shiftRightUnsigned = new IntIntFunction() {
        public final int apply(int a, int b) {
            return a >>> b;
        }
    };

    /**
     * Function that returns <code>a ^ b</code>.
     */
    public static final IntIntFunction xor = new IntIntFunction() {
        public final int apply(int a, int b) {
            return a ^ b;
        }
    };

    /**
     * Makes this class non instantiable, but still let's others inherit from
     * it.
     */
    protected IntFunctions() {
    }

    /**
     * Constructs a function that returns <code>a & b</code>. <code>a</code> is a
     * variable, <code>b</code> is fixed.
     * @param b
     * @return 
     */
    public static IntFunction and(final int b) {
        return new IntFunction() {
            public final int apply(int a) {
                return a & b;
            }
        };
    }

    /**
     * Constructs a function that returns <code>(from<=a && a<=to) ? 1 : 0</code>.
     * <code>a</code> is a variable, <code>from</code> and <code>to</code> are fixed.
     * @param from
     * @param to
     * @return 
     */
    public static IntFunction between(final int from, final int to) {
        return new IntFunction() {
            public final int apply(int a) {
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
    public static IntFunction bindArg1(final IntIntFunction function, final int c) {
        return new IntFunction() {
            public final int apply(int var) {
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
    public static IntFunction bindArg2(final IntIntFunction function, final int c) {
        return new IntFunction() {
            public final int apply(int var) {
                return function.apply(var, c);
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
    public static IntFunction chain(final IntFunction g, final IntFunction h) {
        return new IntFunction() {
            public final int apply(int a) {
                return g.apply(h.apply(a));
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
    public static IntIntFunction chain(final IntFunction g, final IntIntFunction h) {
        return new IntIntFunction() {
            public final int apply(int a, int b) {
                return g.apply(h.apply(a, b));
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
    public static IntIntFunction chain(final IntIntFunction f, final IntFunction g, final IntFunction h) {
        return new IntIntFunction() {
            public final int apply(int a, int b) {
                return f.apply(g.apply(a), h.apply(b));
            }
        };
    }

    /**
     * Constructs a function that returns <code>a < b ? -1 : a > b ? 1 : 0</code>.
     * <code>a</code> is a variable, <code>b</code> is fixed.
     * @param b
     * @return 
     */
    public static IntFunction compare(final int b) {
        return new IntFunction() {
            public final int apply(int a) {
                return a < b ? -1 : a > b ? 1 : 0;
            }
        };
    }

    /**
     * Constructs a function that returns the constant <code>c</code>.
     * @param c
     * @return 
     */
    public static IntFunction constant(final int c) {
        return new IntFunction() {
            public final int apply(int a) {
                return c;
            }
        };
    }

    /**
     * Constructs a function that returns <code>a / b</code>. <code>a</code> is a
     * variable, <code>b</code> is fixed.
     * @param b
     * @return 
     */
    public static IntFunction div(final int b) {
        return new IntFunction() {
            public final int apply(int a) {
                return a / b;
            }
        };
    }

    /**
     * Constructs a function that returns <code>a == b ? 1 : 0</code>. <code>a</code> is
     * a variable, <code>b</code> is fixed.
     * @param b
     * @return 
     */
    public static IntFunction equals(final int b) {
        return new IntFunction() {
            public final int apply(int a) {
                return a == b ? 1 : 0;
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
    public static IntProcedure isBetween(final int from, final int to) {
        return new IntProcedure() {
            public final boolean apply(int a) {
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
    public static IntProcedure isEqual(final int b) {
        return new IntProcedure() {
            public final boolean apply(int a) {
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
    public static IntProcedure isGreater(final int b) {
        return new IntProcedure() {
            public final boolean apply(int a) {
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
    public static IntProcedure isLess(final int b) {
        return new IntProcedure() {
            public final boolean apply(int a) {
                return a < b;
            }
        };
    }

    /**
     * Constructs a function that returns <code>Math.max(a,b)</code>. <code>a</code> is
     * a variable, <code>b</code> is fixed.
     * @param b
     * @return 
     */
    public static IntFunction max(final int b) {
        return new IntFunction() {
            public final int apply(int a) {
                return (a >= b) ? a : b;
            }
        };
    }

    /**
     * Constructs a function that returns <code>Math.min(a,b)</code>. <code>a</code> is
     * a variable, <code>b</code> is fixed.
     * @param b
     * @return 
     */
    public static IntFunction min(final int b) {
        return new IntFunction() {
            public final int apply(int a) {
                return (a <= b) ? a : b;
            }
        };
    }

    /**
     * Constructs a function that returns <code>a - b</code>. <code>a</code> is a
     * variable, <code>b</code> is fixed.
     * @param b
     * @return 
     */
    public static IntFunction minus(final int b) {
        return new IntFunction() {
            public final int apply(int a) {
                return a - b;
            }
        };
    }

    /**
     * Constructs a function that returns <code>a - b*constant</code>. <code>a</code>
     * and <code>b</code> are variables, <code>constant</code> is fixed.
     * @param constant
     * @return 
     */
    public static IntIntFunction minusMult(final int constant) {
        return plusMultSecond(-constant);
    }

    /**
     * Constructs a function that returns <code>a % b</code>. <code>a</code> is a
     * variable, <code>b</code> is fixed.
     * @param b
     * @return 
     */
    public static IntFunction mod(final int b) {
        return new IntFunction() {
            public final int apply(int a) {
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
    public static IntFunction mult(final int b) {
        return new IntFunction() {
            public final int apply(int a) {
                return a * b;
            }
        };
    }

    /**
     * Constructs a function that returns <code>a | b</code>. <code>a</code> is a
     * variable, <code>b</code> is fixed.
     * @param b
     * @return 
     */
    public static IntFunction or(final int b) {
        return new IntFunction() {
            public final int apply(int a) {
                return a | b;
            }
        };
    }

    /**
     * Constructs a function that returns <code>a + b</code>. <code>a</code> is a
     * variable, <code>b</code> is fixed.
     * @param b
     * @return 
     */
    public static IntFunction plus(final int b) {
        return new IntFunction() {
            public final int apply(int a) {
                return a + b;
            }
        };
    }

    /**
     * Constructs a function that returns <code>b*constant</code>.
     * @param constant
     * @return 
     */
    public static IntIntFunction multSecond(final int constant) {

        return new IntIntFunction() {
            public final int apply(int a, int b) {
                return b * constant;
            }
        };

    }

    /**
     * Constructs a function that returns <code>(int) Math.pow(a,b)</code>.
     * <code>a</code> is a variable, <code>b</code> is fixed.
     * @param b
     * @return 
     */
    public static IntFunction pow(final int b) {
        return new IntFunction() {
            public final int apply(int a) {
                return (int) Math.pow(a, b);
            }
        };
    }

    /**
     * Constructs a function that returns <code>a + b*constant</code>. <code>a</code>
     * and <code>b</code> are variables, <code>constant</code> is fixed.
     * @param constant
     * @return 
     */
    public static IntIntFunction plusMultSecond(final int constant) {
        return new IntPlusMultSecond(constant);
    }

    /**
     * Constructs a function that returns <code>a * constant + b</code>. <code>a</code>
     * and <code>b</code> are variables, <code>constant</code> is fixed.
     * @param constant
     * @return 
     */
    public static IntIntFunction plusMultFirst(final int constant) {
        return new IntPlusMultFirst(constant);
    }

    /**
     * Constructs a function that returns a 32 bit uniformly distributed random
     * number in the closed longerval <code>[Int.MIN_VALUE,Integer.MAX_VALUE]</code>
     * (including <code>Int.MIN_VALUE</code> and <code>Integer.MAX_VALUE</code>). Currently
     * the engine is
     * {@link cern.jet.random.tdouble.engine.DoubleMersenneTwister} and is
     * seeded with the current time.
     * <p>
     * Note that any random engine derived from
     * {@link cern.jet.random.tdouble.engine.DoubleRandomEngine} and any random
     * distribution derived from
     * {@link cern.jet.random.tdouble.AbstractDoubleDistribution} are function
     * objects, because they implement the proper longerfaces. Thus, if you are
     * not happy with the default, just pass your favourite random generator to
     * function evaluating methods.
     * @return 
     */
    public static IntFunction random() {
        return new cern.jet.random.tdouble.engine.DoubleMersenneTwister(new java.util.Date());
    }

    /**
     * Constructs a function that returns <code>a << b</code>. <code>a</code> is a
     * variable, <code>b</code> is fixed.
     * @param b
     * @return 
     */
    public static IntFunction shiftLeft(final int b) {
        return new IntFunction() {
            public final int apply(int a) {
                return a << b;
            }
        };
    }

    /**
     * Constructs a function that returns <code>a >> b</code>. <code>a</code> is a
     * variable, <code>b</code> is fixed.
     * @param b
     * @return 
     */
    public static IntFunction shiftRightSigned(final int b) {
        return new IntFunction() {
            public final int apply(int a) {
                return a >> b;
            }
        };
    }

    /**
     * Constructs a function that returns <code>a >>> b</code>. <code>a</code> is a
     * variable, <code>b</code> is fixed.
     * @param b
     * @return 
     */
    public static IntFunction shiftRightUnsigned(final int b) {
        return new IntFunction() {
            public final int apply(int a) {
                return a >>> b;
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
    public static IntIntFunction swapArgs(final IntIntFunction function) {
        return new IntIntFunction() {
            public final int apply(int a, int b) {
                return function.apply(b, a);
            }
        };
    }

    /**
     * Constructs a function that returns <code>a | b</code>. <code>a</code> is a
     * variable, <code>b</code> is fixed.
     * @param b
     * @return 
     */
    public static IntFunction xor(final int b) {
        return new IntFunction() {
            public final int apply(int a) {
                return a ^ b;
            }
        };
    }
}
