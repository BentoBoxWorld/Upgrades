package world.bentobox.upgrades.dataobjects.adapter;

import com.google.gson.*;
import world.bentobox.upgrades.dataobjects.prices.PriceDB;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class PriceDBAdapter implements JsonSerializer<List<PriceDB>>, JsonDeserializer<List<PriceDB>> {

    private static final String PACKAGE = "world.bentobox.upgrades.dataobjects.prices.";

    @Override
    public List<PriceDB> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        List<PriceDB> list = new ArrayList<>();
        JsonArray jsonArray = json.getAsJsonArray();

        jsonArray.forEach((entry) -> {
            JsonObject obj = entry.getAsJsonObject();
            String type = obj.get("class")
                    .getAsString();
            JsonElement parameters = obj.get("parameters");

            try {
                list.add(context.deserialize(parameters, Class.forName(PACKAGE + type)));
            } catch (ClassNotFoundException e) {
                throw new JsonParseException("Unknown element type: " + type, e);
            }
        });

        return list;
    }

    @Override
    public JsonElement serialize(List<PriceDB> src, Type typeOfSrc, JsonSerializationContext context) {
        JsonArray jsonArray = new JsonArray();

        src.forEach((obj) -> {
            JsonObject result = new JsonObject();

            result.add("class", new JsonPrimitive(obj.getClass()
                    .getSimpleName()));
            result.add("parameters", context.serialize(obj, obj.getClass()));

            jsonArray.add(result);
        });

        return jsonArray;
    }
}
