package dev.careeragent.infrastructure;

import tools.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@ConditionalOnProperty(name = "app.persistence.mode", havingValue = "jdbc")
public class JdbcStatePersistence implements StatePersistence {
    private final JdbcClient jdbc;
    private final ObjectMapper json;

    public JdbcStatePersistence(JdbcClient jdbc, ObjectMapper json) {
        this.jdbc = jdbc;
        this.json = json;
        jdbc.sql("CREATE TABLE IF NOT EXISTS career_state (id INTEGER PRIMARY KEY, payload TEXT NOT NULL, updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP)").update();
    }

    @Override public Optional<StoreSnapshot> load() {
        return jdbc.sql("SELECT payload FROM career_state WHERE id = 1").query(String.class).optional()
                .map(value -> {
                    try { return json.readValue(value, StoreSnapshot.class); }
                    catch (Exception e) { throw new IllegalStateException("无法读取持久化状态", e); }
                });
    }

    @Override public void save(StoreSnapshot snapshot) {
        try {
            String payload = json.writeValueAsString(snapshot);
            int updated = jdbc.sql("UPDATE career_state SET payload = :payload, updated_at = CURRENT_TIMESTAMP WHERE id = 1")
                    .param("payload", payload).update();
            if (updated == 0) jdbc.sql("INSERT INTO career_state (id, payload, updated_at) VALUES (1, :payload, CURRENT_TIMESTAMP)")
                    .param("payload", payload).update();
        } catch (Exception e) { throw new IllegalStateException("无法保存持久化状态", e); }
    }
}
