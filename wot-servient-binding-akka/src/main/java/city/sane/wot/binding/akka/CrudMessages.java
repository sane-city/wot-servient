package city.sane.wot.binding.akka;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * This class contains message types for <a href="https://en.wikipedia.org/wiki/Create,_read,_update_and_delete">CRUD operations</a>.
 */
public class CrudMessages {
    static abstract class Failed implements Serializable {
        public final List<String> errors;

        public Failed(List<String> errors) {
            this.errors = errors;
        }

        public Failed(Throwable e) {
            this(Arrays.asList(e.toString()));
        }
    }

    // Index
    static public class GetAll implements Serializable {
    }

    static public class RespondGetAll<K, E> implements Serializable {
        public final Map<K, E> entities;

        public RespondGetAll(Map<K, E> entities) {
            this.entities = entities;
        }
    }

    static public class GetAllFailed extends Failed {
        public GetAllFailed(List<String> errors) {
            super(errors);
        }

        public GetAllFailed(Throwable e) {
            super(e);
        }
    }

    // Get
    static public class Get<K> implements Serializable {
        public final K id;

        public Get(K id) {
            this.id = id;
        }
    }

    static public class RespondGet<E> implements Serializable {
        public final E entity;

        public RespondGet(E entity) {
            this.entity = entity;
        }
    }

    static public class GetFailed extends Failed {
        public GetFailed(List<String> errors) {
            super(errors);
        }

        public GetFailed(Throwable e) {
            super(e);
        }
    }

    // Create
    static public class Create<E> implements Serializable {
        public final E entity;

        public Create(E entity) {
            this.entity = entity;
        }
    }

    static public class Created<E> {
        public final E entity;

        public Created(E entity) {
            this.entity = entity;
        }
    }

    static public class CreationFailed extends Failed {
        public CreationFailed(List<String> errors) {
            super(errors);
        }

        public CreationFailed(Throwable e) {
            super(e);
        }
    }

    // Update
    static public class Update<K, E> implements Serializable {
        public final K id;
        public final E entity;

        public Update(K id, E entity) {
            this.id = id;
            this.entity = entity;
        }
    }

    static public class UpdateFailed extends Failed {
        public UpdateFailed(List<String> errors) {
            super(errors);
        }

        public UpdateFailed(Throwable e) {
            super(e);
        }
    }

    static public class Updated<E> implements Serializable {
        public final E entity;

        public Updated(E entity) {
            this.entity = entity;
        }
    }

    // Delete
    static public class Delete<K> implements Serializable {
        public final K id;

        public Delete(K id) {
            this.id = id;
        }
    }

    static public class Deleted<K> implements Serializable {
        public final K id;

        public Deleted(K id) {
            this.id = id;
        }
    }

    static public class DeleteFailed extends Failed {
        public DeleteFailed(List<String> errors) {
            super(errors);
        }

        public DeleteFailed(Throwable e) {
            super(e);
        }
    }
}
