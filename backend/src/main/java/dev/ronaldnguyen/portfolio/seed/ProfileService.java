package dev.ronaldnguyen.portfolio.seed;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

/** Loads the CV data (content/profile.json, bundled as a classpath resource). */
@ApplicationScoped
public class ProfileService {

    private volatile JsonObject profile;

    public static JsonObject load() {
        try (InputStream in = ProfileService.class.getClassLoader().getResourceAsStream("profile.json")) {
            if (in == null) {
                throw new IllegalStateException("profile.json not found on the classpath");
            }
            try (JsonReader reader = Json.createReader(in)) {
                return reader.readObject();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public JsonObject profile() {
        JsonObject p = profile;
        if (p == null) {
            synchronized (this) {
                if (profile == null) {
                    profile = load();
                }
                p = profile;
            }
        }
        return p;
    }

    public String email() {
        return profile().getString("email", "");
    }
}
