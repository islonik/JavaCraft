package my.javacraft.elastic.validation;

public interface PositiveNumber {

    static int positiveOrDefault(int value, int defaultValue) {
        return value > 0 ? value : defaultValue;
    }

    static long positiveOrDefault(long value, long defaultValue) {
        return value > 0 ? value : defaultValue;
    }
}
