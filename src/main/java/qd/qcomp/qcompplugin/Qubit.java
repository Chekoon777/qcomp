package qd.qcomp.qcompplugin;
import org.apache.commons.math3.complex.Complex;

public class Qubit {
    public boolean valid;
    public boolean selsig;
    public Complex[] coef = new Complex[2];

    public Qubit(boolean v) {
        valid = v;
        selsig = false;
        coef[0] = v ? Complex.ONE : Complex.ZERO;
        coef[1] = Complex.ZERO;
    }

    public Qubit(boolean v, Complex ket0, Complex ket1) {
        valid = v;
        selsig = false;
        coef[0] = ket0;
        coef[1] = ket1;
    }

    public Qubit apply(Complex[][] gate) {
        Qubit newq = new Qubit(true);
        newq.coef[0] = gate[0][0].multiply(coef[0]).add(gate[0][1].multiply(coef[1]));
        newq.coef[1] = gate[1][0].multiply(coef[0]).add(gate[1][1].multiply(coef[1]));
        return newq;
    }
}
