package me.phoenixra.atumconfig.api.tuples;
import java.util.Objects;

/**
 * Two values
 *
 * @param <A> The first value
 * @param <B> The second value
 */
public record PairRecord<A, B>(A first, B second) {


    @Override
    public String toString() {
        return "PairRecord{" +
                "first=" + first +
                ", second=" + second +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PairRecord<?, ?> pair = (PairRecord<?, ?>) o;
        return Objects.equals(first, pair.first) && Objects.equals(second, pair.second);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }
}
