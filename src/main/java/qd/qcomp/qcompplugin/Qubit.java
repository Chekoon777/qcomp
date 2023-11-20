package qd.qcomp.qcompplugin;

import org.apache.commons.math3.complex.Complex;

public class Qubit {
    public Complex[] coef = new Complex[2];

    public Qubit(boolean v) {
        coef[0] = v ? Complex.ONE : Complex.ZERO;
        coef[1] = v ? Complex.ZERO : Complex.ONE;
    }

    public Qubit(Complex ket0, Complex ket1) {
        coef[0] = ket0;
        coef[1] = ket1;
    }

    public Qubit copy() {
        return new Qubit(coef[0], coef[1]);
    }

    public String toString() {
        return String.format("§a|0>:§r %s\n§a|1>:§r %s",
            ComplextoString(coef[0]),
            ComplextoString(coef[1])
        );
    }

    public Qubit apply(Complex[][] gate) {
        Qubit newq = this.copy();
        newq.coef[0] = coef[0].multiply(gate[0][0]).add(coef[1].multiply(gate[0][1]));
        newq.coef[1] = coef[0].multiply(gate[1][0]).add(coef[1].multiply(gate[1][1]));
        return newq;
    }

    public Qubit mul(Complex scalar) {
        coef[0] = coef[0].multiply(scalar);
        coef[1] = coef[1].multiply(scalar);
        return this;
    }

    public Qubit add(Qubit q) {
        coef[0] = coef[0].add(q.coef[0]);
        coef[1] = coef[1].add(q.coef[1]);
        return this;
    }

    public float normalize() {
         float size = (float) Math.sqrt(Math.pow(coef[0].abs(),2) + Math.pow(coef[1].abs(),2));
         coef[0] = coef[0].divide(size);
         coef[1] = coef[1].divide(size);
         return size;
    }

    public static Complex StringtoComplex(String str) {
        String[] parts = str.split("[+-]");
        float realPart = Float.parseFloat(parts[0]);
        float imgPart = Float.parseFloat(parts[1].replace("i", ""));
        return new Complex(realPart, imgPart);
    }

    public static String ComplextoString(Complex cpx) {
        String op = cpx.getImaginary() >= 0 ? "§b+§r" : "§b-§r";
        return String.format("%.2f%s%.2f§bi§r", cpx.getReal(), op,
            cpx.getImaginary()<0 ? -cpx.getImaginary() : cpx.getImaginary());
    }
}
