package ch.epfl.javelo;

/**
 * Bits
 * This class offers methods which help to manipulate bit strings
 *
 * @author Wesley Nana Davies(344592)
 * @author David Farah (341017)
 */

public final class Bits {
    private Bits() {
    }

    /**
     * Returns the value of an extracted bit string (signed)
     *
     * @param value  : initial integer value
     * @param start  : start bit index (right)
     * @param length : length of the extracted bit string
     * @return the value
     */
    public static int extractSigned(int value, int start, int length) {
        Preconditions.checkArgument(start >= 0 && start <= 31
                && length <= (32 - start) && length > 0);

        value = value << 32 - (start + length);
        value = value >> 32 - length;
        return value;
    }

    /**
     * Returns the value of an extracted bit string (unsigned)
     *
     * @param value  : initial integer value
     * @param start  : start bit index (right)
     * @param length : length of the extracted bit string
     * @return the value
     */
    public static int extractUnsigned(int value, int start, int length) {
        Preconditions.checkArgument(start >= 0 && start <= 31
                && length <= (32 - start) && length < 32 && length > 0);

        value = value << 32 - (start + length);
        value = value >>> 32 - length;
        return value;
    }
}
