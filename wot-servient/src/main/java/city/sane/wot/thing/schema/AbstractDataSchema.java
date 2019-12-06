package city.sane.wot.thing.schema;

public abstract class AbstractDataSchema<T> implements DataSchema<T> {
    @Override
    public int hashCode() {
        return getType().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof DataSchema)) {
            return false;
        }
        return getType().equals(((DataSchema) obj).getType());
    }
}
