package me.phoenixra.atumconfig.tuples;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Three nullable values.
 *
 * @param <A> The first value type.
 * @param <B> The second value type.
 * @param <C> The third value type.
 */
@Setter
@Getter
@AllArgsConstructor
public class Triplet<A,B,C> {

    /**
     * The first item in the tuple.
     */
    @Nullable
    private A first;

    /**
     * The second item in the tuple.
     */
    @Nullable
    private B second;

    /**
     * The third item in the tuple.
     */
    @Nullable
    private C third;

    @Override
    public String toString() {
        return "Triplet{" +
                "first=" + first +
                ", second=" + second +
                ", third=" + third +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Triplet<?, ?, ?> triplet = (Triplet<?, ?, ?>) o;
        return Objects.equals(first, triplet.first) && Objects.equals(second, triplet.second) && Objects.equals(third, triplet.third);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second, third);
    }
}
