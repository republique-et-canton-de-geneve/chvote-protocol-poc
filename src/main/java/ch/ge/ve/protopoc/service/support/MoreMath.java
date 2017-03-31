package ch.ge.ve.protopoc.service.support;

/**
 * Additional math primitives missing from {@link Math}
 */
public class MoreMath {
    public static double log2(double value) {
        return Math.log(value) / Math.log(2);
    }
}
