package me.phoenixra.atumconfig.api.tuples;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

/**
 * Three values
 *
 * @param <A> The first value
 * @param <B> The second value
 * @param <C> The third value
 */
@Setter
@AllArgsConstructor
public class TripletRecord<A,B,C> {

    /**
     * The first item in the tuple.
     */
    private A first;

    /**
     * The second item in the tuple.
     */
    private B second;

    /**
     * The third item in the tuple.
     */
    private C third;


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

    /**
     *
     * @return third value
     */
    public C third(){
        return third;
    }
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