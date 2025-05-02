package me.phoenixra.atumconfig.api.tuples;

import lombok.AllArgsConstructor;
import lombok.Setter;

import java.util.Objects;

/**
 * Two values
 *
 * @param <A> The first value
 * @param <B> The second value
 */
@Setter
@AllArgsConstructor
public class PairRecord<A, B> {

    /**
     * The first item in the tuple.
     */
    private A first;

    /**
     * The second item in the tuple.
     */
    private B second;

    /**
     *
     * @return first value
     */
    public A first(){
        return first;
    }
    /**
     *
     * @return second value
     */
    public B second(){
        return second;
    }

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