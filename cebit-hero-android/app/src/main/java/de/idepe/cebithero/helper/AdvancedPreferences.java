package de.idepe.cebithero.helper;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

public class AdvancedPreferences {
    private Context context;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private static Gson GSON = new Gson();

    public AdvancedPreferences(Context context, String namePreferences, int mode) {
        this.context = context;
        if (namePreferences == null || namePreferences.equals("")) {
            namePreferences = "complex_preferences";
        }
        preferences = context.getSharedPreferences(namePreferences, mode);
        editor = preferences.edit();
    }

    public void putObject(String key, Object object) {
        if (key.isEmpty() || key == null) {
            throw new IllegalArgumentException("key is empty or null");
        }

        String obj = GSON.toJson(object);

        if (obj == null || obj.isEmpty()) {
            editor.remove(key);
        } else {
            editor.putString(key, obj);
        }
    }

    public void commit() {
        editor.commit();
    }

    public <T> T getObject(String key, Class<T> a) {

        String gson = preferences.getString(key, null);
        if (gson == null) {
            return null;
        } else {
            try {
                return GSON.fromJson(gson, a);
            } catch (Exception e) {
                throw new IllegalArgumentException("Object storaged with key " + key + " is instanceof other class");
            }
        }
    }

}