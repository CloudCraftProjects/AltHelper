package tk.booky.althelper.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Jofkos
 * @link https://gist.github.com/Jofkos/d0c469528b032d820f42
 */
public class UUIDFetcher {

    @SuppressWarnings("unused")
    public static final long FEBRUARY_2015 = 1422748800000L;
    public static final Map<String, UUID> uuidCache = new HashMap<>();
    public static final Map<UUID, String> nameCache = new HashMap<>();

    private static final Gson gson = new GsonBuilder().registerTypeAdapter(UUID.class, new UUIDTypeAdapter()).create();
    private static final String UUID_URL = "https://api.mojang.com/users/profiles/minecraft/%s?at=%d";

    @SuppressWarnings("unused")
    private String name;
    @SuppressWarnings("unused")
    private UUID id;

    public static UUID getUUIDAt(String name, long timestamp) {
        name = name.toLowerCase();
        if (uuidCache.containsKey(name)) return uuidCache.get(name);
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(String.format(UUID_URL, name, timestamp / 1000)).openConnection();
            connection.setReadTimeout(5000);
            UUIDFetcher data = gson.fromJson(new BufferedReader(new InputStreamReader(connection.getInputStream())), UUIDFetcher.class);

            uuidCache.put(name, data.id);
            nameCache.put(data.id, data.name);

            return data.id;
        } catch (Throwable throwable) {
            return null;
        }
    }
}