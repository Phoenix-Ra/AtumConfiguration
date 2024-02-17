package me.phoenixra.atumconfig.tuples;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Two not null values.
 *
 * @param <A> The first value type.
 * @param <B> The second value type.
 */
@Setter
@Getter
@AllArgsConstructor
public class PairRecord<A, B> {
    /**
     * The first item in the tuple.
     */
    @NotNull
    private A first;

    /**
     * The second item in the tuple.
     */
    @NotNull
    private B second;

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
