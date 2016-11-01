package ch.ge.ve.protopoc.service.model;

/**
 * This class defines the security parameters to be used
 */
public class SecurityParameters {
    /**
     * Minimal security level &lambda;
     */
    public int lambda;

    /**
     * Output length of collision-resistant hash-function l (in bits)
     */
    public int l;

    /**
     * Deterrence factor &epsilon; (with 0 &lt; &epsilon; &le; 1)
     */
    public double epsilon;
}
