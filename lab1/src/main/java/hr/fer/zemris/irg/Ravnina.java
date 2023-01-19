package hr.fer.zemris.irg;

import java.util.Objects;

public class Ravnina {
    private final int A;
    private final int B;
    private final int C;

    public Ravnina(int a, int b, int c) {
        A = a;
        B = b;
        C = c;
    }

    public int getA() {
        return A;
    }

    public int getB() {
        return B;
    }

    public int getC() {
        return C;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ravnina ravnina = (Ravnina) o;
        return A == ravnina.A && B == ravnina.B && C == ravnina.C;
    }

    @Override
    public int hashCode() {
        return Objects.hash(A, B, C);
    }
}
