package org.bingo90;

public class SimpleRNG {

    private static final int MASK = (1 << 31) - 1;

    private int value;

    public int nextInt() {
        value = (value * 1_103_515_245 + 12_345) & MASK;
        return value;
    }

    public int nextInt(int n) {
        return nextInt() % n;
    }

}
