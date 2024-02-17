package me.phoenixra.atumconfig.tuples;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Three not null values.
 *
 * @param <A> The first value type.
 * @param <B> The second value type.
 * @param <C> The third value type.
 */
@Setter
@Getter
@AllArgsConstructor
public class TripletRecord<A,B,C> {

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

    /**
     * The third item in the tuple.
     */
    @NotNull
    private C third;

    @Override
    public String toString() {
        return "TripletRecord{" +
                "first=" + first +
                ", second=" + second +
                ", third=" + third +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TripletRecord<?, ?, ?> that = (TripletRecord<?, ?, ?>) o;
        return Objects.equals(first, that.first) && Objects.equals(second, that.second) && Objects.equals(third, that.third);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second, third);
    }
}
