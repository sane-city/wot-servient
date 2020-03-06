package city.sane;

import java.util.Objects;

/**
 * A tuple of three elements.
 */
public class Triple<A, B, C> {
    private final A first;
    private final B second;
    private final C third;

    public Triple(A first, B second, C third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    public A first() {
        return first;
    }

    public B second() {
        return second;
    }

    public C third() {
        return third;
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second, third);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Triple<?, ?, ?> triple = (Triple<?, ?, ?>) o;
        return Objects.equals(first, triple.first) &&
                Objects.equals(second, triple.second) &&
                Objects.equals(third, triple.third);
    }

    @Override
    public String toString() {
        return "Triple{" +
                "first=" + first +
                ", second=" + second +
                ", third=" + third +
                '}';
    }
}
