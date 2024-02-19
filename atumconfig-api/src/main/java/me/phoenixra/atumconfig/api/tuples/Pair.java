package me.phoenixra.atumconfig.api.tuples;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Two nullable values.
 *
 * @param <A> The first value type.
 * @param <B> The second value type.
 */
@Setter
@Getter
@AllArgsConstructor
public class Pair<A, B> {
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

    @Override
    public String toString() {
        return "Pair{" +
                "first=" + first +
                ", second=" + second +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return Objects.equals(first, pair.first) && Objects.equals(second, pair.second);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }
}
