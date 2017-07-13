package de.joesch_it.chillweather.weather.deserializer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import de.joesch_it.chillweather.weather.data.Current;


public class CurrentDeserializer implements JsonDeserializer {

    @Override public Current deserialize(
            JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {

        final JsonObject jsonString = json.getAsJsonObject();

        final String timezone = jsonString.get("timezone").getAsString();

        final JsonObject jsonCurrent = jsonString.get("currently").getAsJsonObject();

        final JsonObject jsonFlags = jsonString.get("flags").getAsJsonObject();
        final String unit = jsonFlags.get("units").getAsString();

        final Current current = new Current();

        current.setUnit(unit);
        current.setTime(jsonCurrent.get("time").getAsLong());
        current.setHumidity(jsonCurrent.get("humidity").getAsDouble());
        current.setIcon(jsonCurrent.get("icon").getAsString());
        current.setPrecipChance(jsonCurrent.get("precipProbability").getAsDouble());
        current.setSummary(jsonCurrent.get("summary").getAsString());
        current.setTemperature(jsonCurrent.get("temperature").getAsDouble());
        current.setTimezone(timezone);

        return current;
    }
}
