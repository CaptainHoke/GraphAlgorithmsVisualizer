package engineer.omnis.graphviz;

public final class Utility {
    private Utility() {
    }
    public static final int BIG_INT = Integer.MAX_VALUE - 1_000_000;

    public static int clamp(int val, int min, int max) {
        return Math.max(min, Math.min(max, val));
    }
    public static int remap(int val, int fromLeft, int fromRight, int toLeft, int toRight) {
        assert fromLeft < fromRight;
        assert toLeft < toRight;

        int fromLength = fromRight - fromLeft;
        int distInFrom = val - fromLeft;
        double t = (double) distInFrom / fromLength;
        int toLength = toRight - toLeft;

        return (int) (toLength * t + toLeft);
    }
}
