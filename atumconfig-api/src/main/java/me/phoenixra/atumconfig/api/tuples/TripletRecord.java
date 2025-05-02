package me.phoenixra.atumconfig.api.tuples;
import java.util.Objects;

/**
 * Three values
 *
 * @param <A> The first value
 * @param <B> The second value
 * @param <C> The third value
 */

public record TripletRecord<A,B,C>(A first, B second, C third) {


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
