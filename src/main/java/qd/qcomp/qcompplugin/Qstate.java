package qd.qcomp.qcompplugin;
import org.apache.commons.math3.complex.Complex;

import java.util.ArrayList;
import java.util.List;

public class Qstate {
    public Qubit q;
    public int numaffected;
    public List<Qubit> ifzero;
    public List<Qubit> ifone;
    public boolean selsig;

    public Qstate(boolean v) {
        q = new Qubit(v);
        ifzero = new ArrayList<>();
        ifone = new ArrayList<>();
        numaffected = 0;
        selsig = false;
    }

    public Qstate(Complex ket0, Complex ket1) {
        q = new Qubit(ket0, ket1);
        ifzero = new ArrayList<>();
        ifone = new ArrayList<>();
        numaffected = 0;
        selsig = false;
    }

    public Qstate setselsig(boolean sel) {
        Qstate newq = this.copy();
        newq.selsig = sel;
        return newq;
    }

    public Qstate copy() {
        Qstate newq = new Qstate(q.coef[0], q.coef[1]);
        newq.numaffected = numaffected;
        newq.ifzero = new ArrayList<>(ifzero);
        newq.ifone = new ArrayList<>(ifone);
        newq.selsig = selsig;
        return newq;
    }

    public String toString() {
        return q.toString();
    }

    public void addAffected(Qubit if_zero, Qubit if_one) {
        numaffected += 1;
        ifzero.add(if_zero);
        ifone.add(if_one);
    }

    public Qstate apply(Complex[][] gate) {
        Qstate newq = this.copy();
        q = q.apply(gate);
        for(int i=0; i<numaffected; i++) {
            newq.ifzero.set(i, ifzero.get(i).mul(gate[0][0]).add(ifone.get(i).mul(gate[0][1])));
            newq.ifone.set(i, ifzero.get(i).mul(gate[1][0]).add(ifone.get(i).mul(gate[1][1])));
        }
        return newq;
    }
}
