/*-------------------------------------------------------------------------------------------------
 - #%L                                                                                            -
 - chvote-protocol-poc                                                                            -
 - %%                                                                                             -
 - Copyright (C) 2016 - 2017 République et Canton de Genève                                       -
 - %%                                                                                             -
 - This program is free software: you can redistribute it and/or modify                           -
 - it under the terms of the GNU Affero General Public License as published by                    -
 - the Free Software Foundation, either version 3 of the License, or                              -
 - (at your option) any later version.                                                            -
 -                                                                                                -
 - This program is distributed in the hope that it will be useful,                                -
 - but WITHOUT ANY WARRANTY; without even the implied warranty of                                 -
 - MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the                                   -
 - GNU General Public License for more details.                                                   -
 -                                                                                                -
 - You should have received a copy of the GNU Affero General Public License                       -
 - along with this program. If not, see <http://www.gnu.org/licenses/>.                           -
 - #L%                                                                                            -
 -------------------------------------------------------------------------------------------------*/

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
public final class ShuffleProof {
    private final T t;
    private final S s;
    private final List<BigInteger> bold_c;
    private final List<BigInteger> bold_c_hat;

    public ShuffleProof(T t, S s, List<BigInteger> bold_c, List<BigInteger> bold_c_hat) {
        this.t = t;
        this.s = s;
        this.bold_c = ImmutableList.copyOf(bold_c);
        this.bold_c_hat = ImmutableList.copyOf(bold_c_hat);
    }

    public T getT() {
        return t;
    }

    public S getS() {
        return s;
    }

    public List<BigInteger> getBold_c() {
        return ImmutableList.copyOf(bold_c);
    }

    public List<BigInteger> getBold_c_hat() {
        return ImmutableList.copyOf(bold_c_hat);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShuffleProof that = (ShuffleProof) o;
        return Objects.equals(t, that.t) &&
                Objects.equals(s, that.s) &&
                Objects.equals(bold_c, that.bold_c) &&
                Objects.equals(bold_c_hat, that.bold_c_hat);
    }

    @Override
    public int hashCode() {
        return Objects.hash(t, s, bold_c, bold_c_hat);
    }

    public static final class T implements Hash.Hashable {
        private final BigInteger t_1;
        private final BigInteger t_2;
        private final BigInteger t_3;
        private final List<BigInteger> t_4;
        private final List<BigInteger> t_hat;


        public T(BigInteger t_1, BigInteger t_2, BigInteger t_3, List<BigInteger> t_4, List<BigInteger> t_hat) {
            Preconditions.checkNotNull(t_1);
            Preconditions.checkNotNull(t_2);
            Preconditions.checkNotNull(t_3);
            Preconditions.checkNotNull(t_4);
            Preconditions.checkArgument(t_4.size() == 2, "t_4 should hold two numbers");
            Preconditions.checkNotNull(t_hat);
            Preconditions.checkArgument(t_hat.size() > 0);
            this.t_1 = t_1;
            this.t_2 = t_2;
            this.t_3 = t_3;
            this.t_4 = ImmutableList.copyOf(t_4);
            this.t_hat = ImmutableList.copyOf(t_hat);
        }

        @Override
        public Object[] elementsToHash() {
            return new Object[]{t_1, t_2, t_3, ImmutableList.copyOf(t_4), ImmutableList.copyOf(t_hat)};
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

        public List<BigInteger> getT_hat() {
            return ImmutableList.copyOf(t_hat);
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
                    Objects.equals(t_hat, t.t_hat);
        }

        @Override
        public int hashCode() {
            return Objects.hash(t_1, t_2, t_3, t_4, t_hat);
        }
    }

    public static final class S {
        private final BigInteger s_1;
        private final BigInteger s_2;
        private final BigInteger s_3;
        private final BigInteger s_4;
        private final List<BigInteger> s_hat;
        private final List<BigInteger> s_prime;

        public S(BigInteger s_1, BigInteger s_2, BigInteger s_3, BigInteger s_4,
                 List<BigInteger> s_hat, List<BigInteger> s_prime) {
            Preconditions.checkNotNull(s_1);
            Preconditions.checkNotNull(s_2);
            Preconditions.checkNotNull(s_3);
            Preconditions.checkNotNull(s_4);
            Preconditions.checkNotNull(s_hat);
            Preconditions.checkArgument(s_hat.size() > 0);
            Preconditions.checkNotNull(s_prime);
            Preconditions.checkArgument(s_hat.size() == s_prime.size(),
                    "s_hat and s_prime should have equal lengths");
            this.s_1 = s_1;
            this.s_2 = s_2;
            this.s_3 = s_3;
            this.s_4 = s_4;
            this.s_hat = ImmutableList.copyOf(s_hat);
            this.s_prime = ImmutableList.copyOf(s_prime);
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

        public List<BigInteger> getS_hat() {
            return ImmutableList.copyOf(s_hat);
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
                    Objects.equals(s_hat, s.s_hat) &&
                    Objects.equals(s_prime, s.s_prime);
        }

        @Override
        public int hashCode() {
            return Objects.hash(s_1, s_2, s_3, s_4, s_hat, s_prime);
        }
    }
}
