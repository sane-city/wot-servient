package city.sane;

/**
 * This interface helps to implement the <a href="https://en.wikipedia.org/wiki/Builder_pattern">Builder
 * Pattern</a>.
 *
 * @param <T>
 */
public interface ObjectBuilder<T> {
    T build();
}
