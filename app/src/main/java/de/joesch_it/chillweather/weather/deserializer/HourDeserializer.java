package de.joesch_it.chillweather.weather.deserializer;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import de.joesch_it.chillweather.weather.data.Hour;

public class HourDeserializer implements JsonDeserializer {

    @Override public Hour[] deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

        final JsonObject jsonString = json.getAsJsonObject();

        final String timezone = jsonString.get("timezone").getAsString();

        final JsonObject jsonHourly = jsonString.get("hourly").getAsJsonObject();

        final JsonObject jsonFlags = jsonString.get("flags").getAsJsonObject();
        final String unit = jsonFlags.get("units").getAsString();

        final JsonArray data = jsonHourly.get("data").getAsJsonArray();

        final Hour[] hours = new Hour[data.size()];

        for (int i = 0; i < hours.length; i++) {

            final JsonObject jsonHour = data.get(i).getAsJsonObject();
            final Hour hour = new Hour();

            hour.setUnit(unit);
            hour.setSummary(jsonHour.get("summary").getAsString());
            hour.setTemperature(jsonHour.get("temperature").getAsDouble());
            hour.setIcon(jsonHour.get("icon").getAsString());
            hour.setTime(jsonHour.get("time").getAsLong());
            hour.setTimezone(timezone);
            hours[i] = hour;
        }

        return hours;
    }
}
