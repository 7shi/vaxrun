package vax_interpreter;

import java.util.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

class IntData {
    private byte[] val;
    private DataType type;

    public IntData(byte[] vl, DataType tp) {
        this.val = Arrays.copyOf(vl, tp.size);
        this.type = tp;
    }

    public IntData(int val, DataType tp) {
        this(intToBytes(val), tp);
    }

    public IntData(int val) {
        this(val, DataType.L);
    }

    public IntData(long val, DataType tp) {
        this(longToBytes(val), tp);
    }

    public IntData(long val) {
        this(val, DataType.Q);
    }

    public byte[] bytes() {
        return val;
    }

    public int sint() {
        return bytesToInt(val);
    }

    public int uint() {
        int mask = ~(int)(0xffffffffL << (type.size << 3));
        return sint() & mask;
    }

    public long slong() {
        return bytesToLong(val);
    }

    public DataType dataType() {
        return type;
    }

    public int size() {
        return type.size;
    }

    public static IntData bitInvert(IntData src) {
        byte[] invVal = new byte[src.val.length];
        for (int i = 0; i < src.val.length; i++) {
            invVal[i] = (byte)(~src.val[i]);
        }
        return new IntData(invVal, src.type);
    }

    public static IntData negativeFloat(IntData src) {
        assert !src.isIntValue() : "Integer value is called negativeFloat()";

        byte[] negVal = new byte[src.val.length];
        System.arraycopy(src.val, 0, negVal, 0, src.val.length);
        negVal[1] ^= 0x80;
        return new IntData(negVal, src.type);
    }

    public boolean isNegValue() {
        if (isIntValue()) {
            return (val[val.length - 1] & 0x80) != 0;
        } else {
            return (val[1] & 0x80) != 0;
        }
    }

    public boolean isZeroValue() {
        for (byte b : val) {
            if (b != 0) {
                return false;
            }
        }
        return true;
    }

    public boolean isMinusZeroFloatValue() {
        if (isIntValue()) {
            return false;
        }
        if (val[0] != 0) {
            return false;
        }
        if ((val[1] & 0xff) != 0x80) {
            return false;
        }
        for (int i = 2; i < val.length; i++) {
            if (val[i] != 0) {
                return false;
            }
        }
        return true;
    }

    public boolean isLargestNegativeInteger() {
        if (!isIntValue()) {
            return false;
        }
        if ((val[val.length - 1] & 0xff) != 0x80) {
            return false;
        }
        for (int i = 0; i < val.length - 1; i++) {
            if (val[i] != 0) {
                return false;
            }
        }
        return true;
    }

    private boolean isIntValue() {
        switch (type) {
        default:
        case B: case W: case L: case Q: case O:
            return true;
        case F: case D: case G: case H:
            return false;
        }
    }

    private static int bytesToInt(byte[] val) {
        ByteBuffer bbuf = ByteBuffer.wrap(val).order(ByteOrder.LITTLE_ENDIAN);
        switch (val.length) {
        case 1:
            return bbuf.get();
        case 2:
            return bbuf.getShort();
        default:
            return bbuf.getInt();
        }
    }

    private static long bytesToLong(byte[] val) {
        ByteBuffer bbuf = ByteBuffer.wrap(val).order(ByteOrder.LITTLE_ENDIAN);
        switch (val.length) {
        case 1:
            return bbuf.get();
        case 2:
            return bbuf.getShort();
        case 4:
            return bbuf.getInt();
        default:
            return bbuf.getLong();
        }
    }

    private static byte[] intToBytes(int val) {
        return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(val).array();
    }

    private static byte[] longToBytes(long val) {
        return ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putLong(val).array();
    }

    public String hexString() {
        StringBuilder sb = new StringBuilder("0x");
        for (int i = val.length - 1; i >= 0; i--) {
            sb.append(String.format("%02x", val[i]));
        }
        return sb.toString();
    }
}

