package ch.ge.ve.protopoc.service.model;

import ch.ge.ve.protopoc.service.support.Hash;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;

/**
 * Model class representing a shuffle proof, as provided in algorithm 5.44
 */
public class ShuffleProof {
    private final T t;
    private final S s;
    private final List<BigInteger> bold_c;
    private final List<BigInteger> bold_c_circ;

    public ShuffleProof(T t, S s, List<BigInteger> bold_c, List<BigInteger> bold_c_circ) {
        this.t = t;
        this.s = s;
        this.bold_c = bold_c;
        this.bold_c_circ = bold_c_circ;
    }

    public T getT() {
        return t;
    }

    public S getS() {
        return s;
    }

    public List<BigInteger> getBold_c() {
        return bold_c;
    }

    public List<BigInteger> getBold_c_circ() {
        return bold_c_circ;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShuffleProof that = (ShuffleProof) o;
        return Objects.equals(t, that.t) &&
                Objects.equals(s, that.s) &&
                Objects.equals(bold_c, that.bold_c) &&
                Objects.equals(bold_c_circ, that.bold_c_circ);
    }

    @Override
    public int hashCode() {
        return Objects.hash(t, s, bold_c, bold_c_circ);
    }

    public static class T implements Hash.Hashable {
        private final BigInteger t_1;
        private final BigInteger t_2;
        private final BigInteger t_3;
        private final List<BigInteger> t_4;
        private final List<BigInteger> t_circ;


        public T(BigInteger t_1, BigInteger t_2, BigInteger t_3, List<BigInteger> t_4, List<BigInteger> t_circ) {
            Preconditions.checkNotNull(t_1);
            Preconditions.checkNotNull(t_2);
            Preconditions.checkNotNull(t_3);
            Preconditions.checkNotNull(t_4);
            Preconditions.checkArgument(t_4.size() == 2, "t_4 should hold two numbers");
            Preconditions.checkNotNull(t_circ);
            Preconditions.checkArgument(t_circ.size() > 0);
            this.t_1 = t_1;
            this.t_2 = t_2;
            this.t_3 = t_3;
            this.t_4 = t_4;
            this.t_circ = t_circ;
        }

        @Override
        public Object[] elementsToHash() {
            return new Object[]{t_1, t_2, t_3, t_4, t_circ};
        }

        public BigInteger getT_1() {
            return t_1;
        }

        public BigInteger getT_2() {
            return t_2;
        }

        public BigInteger getT_3() {
            return t_3;
        }

        public List<BigInteger> getT_4() {
            return ImmutableList.copyOf(t_4);
        }

        public List<BigInteger> getT_circ() {
            return ImmutableList.copyOf(t_circ);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            T t = (T) o;
            return Objects.equals(t_1, t.t_1) &&
                    Objects.equals(t_2, t.t_2) &&
                    Objects.equals(t_3, t.t_3) &&
                    Objects.equals(t_4, t.t_4) &&
                    Objects.equals(t_circ, t.t_circ);
        }

        @Override
        public int hashCode() {
            return Objects.hash(t_1, t_2, t_3, t_4, t_circ);
        }
    }

    public static class S {
        private final BigInteger s_1;
        private final BigInteger s_2;
        private final BigInteger s_3;
        private final BigInteger s_4;
        private final List<BigInteger> s_circ;
        private final List<BigInteger> s_prime;

        public S(BigInteger s_1, BigInteger s_2, BigInteger s_3, BigInteger s_4,
                 List<BigInteger> s_circ, List<BigInteger> s_prime) {
            Preconditions.checkNotNull(s_1);
            Preconditions.checkNotNull(s_2);
            Preconditions.checkNotNull(s_3);
            Preconditions.checkNotNull(s_4);
            Preconditions.checkNotNull(s_circ);
            Preconditions.checkArgument(s_circ.size() > 0);
            Preconditions.checkNotNull(s_prime);
            Preconditions.checkArgument(s_circ.size() == s_prime.size(),
                    "s_circ and s_prime should have equal lengths");
            this.s_1 = s_1;
            this.s_2 = s_2;
            this.s_3 = s_3;
            this.s_4 = s_4;
            this.s_circ = s_circ;
            this.s_prime = s_prime;
        }

        public BigInteger getS_1() {
            return s_1;
        }

        public BigInteger getS_2() {
            return s_2;
        }

        public BigInteger getS_3() {
            return s_3;
        }

        public BigInteger getS_4() {
            return s_4;
        }

        public List<BigInteger> getS_circ() {
            return ImmutableList.copyOf(s_circ);
        }

        public List<BigInteger> getS_prime() {
            return ImmutableList.copyOf(s_prime);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            S s = (S) o;
            return Objects.equals(s_1, s.s_1) &&
                    Objects.equals(s_2, s.s_2) &&
                    Objects.equals(s_3, s.s_3) &&
                    Objects.equals(s_4, s.s_4) &&
                    Objects.equals(s_circ, s.s_circ) &&
                    Objects.equals(s_prime, s.s_prime);
        }

        @Override
        public int hashCode() {
            return Objects.hash(s_1, s_2, s_3, s_4, s_circ, s_prime);
        }
    }
}
