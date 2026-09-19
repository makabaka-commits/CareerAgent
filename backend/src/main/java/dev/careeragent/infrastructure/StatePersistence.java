package dev.careeragent.infrastructure;

import java.util.Optional;

public interface StatePersistence {
    Optional<StoreSnapshot> load();
    void save(StoreSnapshot snapshot);
}
