package com.htwg.mobilelearning.helper;

import java.lang.reflect.Type;

import org.bson.types.ObjectId;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonDeserializationContext;

public class ObjectIdTypeAdapter implements JsonSerializer<ObjectId>, JsonDeserializer<ObjectId> {

    @Override
    public JsonElement serialize(ObjectId src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.toHexString());
    }

    @Override
    public ObjectId deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return new ObjectId(json.getAsString());
    }

}
