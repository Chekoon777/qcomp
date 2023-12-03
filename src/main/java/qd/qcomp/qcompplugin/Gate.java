package qd.qcomp.qcompplugin;

import org.apache.commons.math3.complex.Complex;

public class Gate {
    public static final Complex[][] X = {
        { Complex.ZERO, Complex.ONE },
        { Complex.ONE, Complex.ZERO },
    };

    public static final Complex[][] Y = {
        { Complex.ZERO, new Complex(0, -1) },
        { Complex.I, Complex.ZERO },
    };

    public static final Complex[][] Z = {
        { Complex.ONE, Complex.ZERO },
        { Complex.ZERO, new Complex(-1) },
    };

    public static final Complex[][] H = {
        { new Complex(1/Math.sqrt(2)), new Complex(1/Math.sqrt(2))  },
        { new Complex(1/Math.sqrt(2)), new Complex(-1/Math.sqrt(2)) },
    };

    // A can be modified with command "/customgate"
    public static Complex[][] A = {
            { Complex.ONE, Complex.ZERO },
            { Complex.ZERO, Complex.ONE },
    };
}
