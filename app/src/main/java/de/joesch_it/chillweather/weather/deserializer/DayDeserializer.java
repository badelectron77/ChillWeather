package de.joesch_it.chillweather.weather.deserializer;


import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import de.joesch_it.chillweather.weather.data.Day;

public class DayDeserializer implements JsonDeserializer {

    @Override public Day[] deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

        final JsonObject jsonString = json.getAsJsonObject();

        final String timezone = jsonString.get("timezone").getAsString();

        final JsonObject jsonDaily = jsonString.get("daily").getAsJsonObject();

        final JsonObject jsonFlags = jsonString.get("flags").getAsJsonObject();
        final String unit = jsonFlags.get("units").getAsString();

        final JsonArray data = jsonDaily.get("data").getAsJsonArray();

        final Day[] days = new Day[data.size()];

        for (int i = 0; i < days.length; i++) {

            final JsonObject jsonDay = data.get(i).getAsJsonObject();
            final Day day = new Day();

            day.setUnit(unit);
            day.setSummary(jsonDay.get("summary").getAsString());
            day.setTemperatureMax(jsonDay.get("temperatureMax").getAsDouble());
            day.setIcon(jsonDay.get("icon").getAsString());
            day.setTime(jsonDay.get("time").getAsLong());
            day.setTimezone(timezone);
            days[i] = day;
        }

        return days;
    }
}
