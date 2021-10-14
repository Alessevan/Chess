package fr.bakaaless.chessserver.mutual;

public class Vector {

    int x;
    int y;

    int amplifier;

    public Vector(final int x, final int y) {
        this(x, y, 1);
    }

    public Vector(final int x, final int y, final int amplifier) {
        this.x = x;
        this.y = y;
        this.amplifier = amplifier;
    }

    public void add(final int x, final int y) {
        this.add(x, y, false);
    }

    public void add(final int x, final int y, final boolean amplifier) {
        this.x += x * (amplifier ? this.amplifier : 1);
        this.y += y * (amplifier ? this.amplifier : 1);
    }

    @Override
    public String toString() {
        return "Vector {" +
                "  x=" + x +
                ", y=" + y +
                ", amplifier=" + amplifier +
                "  }";
    }
}
