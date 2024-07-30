package com.xhpcd.rpc.common;

import com.google.gson.*;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

public interface Serializer {
    <T> T deserialize(Class<T> clazz, byte[] bytes) throws IOException, ClassNotFoundException;
    <T> byte[] serialize(T object) throws IOException;

    enum Algorithm implements Serializer{
        JAVA{
            @Override
            public <T> T deserialize(Class<T> clazz, byte[] bytes) throws IOException, ClassNotFoundException {

                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
                ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
                return (T)objectInputStream.readObject();
            }

            @Override
            public <T> byte[] serialize(T object) throws IOException {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(bos);
                objectOutputStream.writeObject(object);
                return bos.toByteArray();
            }
        },

        JSON{
            @Override
            public <T> T deserialize(Class<T> clazz, byte[] bytes) throws IOException, ClassNotFoundException {
                Gson gson = new GsonBuilder().registerTypeAdapter(Class.class, new adapter()).create();
                String json = new String(bytes, StandardCharsets.UTF_8);
                T t = gson.fromJson(json, clazz);
                return t;
            }

            @Override
            public <T> byte[] serialize(T object) throws IOException {
                Gson gson = new GsonBuilder().registerTypeAdapter(Class.class, new adapter()).create();
                byte[] bytes = gson.toJson(object).getBytes(StandardCharsets.UTF_8);
                return bytes;
            }
        },

        PROTOBUF{
            @Override
            public <T> T deserialize(Class<T> clazz, byte[] bytes) throws IOException, ClassNotFoundException {
                Schema<T> schema = RuntimeSchema.getSchema(clazz);
                // 反序列化
                T obj = schema.newMessage();
                ProtostuffIOUtil.mergeFrom(bytes, obj, schema);
                return obj;
            }

            @Override
            public <T> byte[] serialize(T obj) throws IOException {
                Class<T> cls = (Class<T>) obj.getClass();

                Schema<T> schema = RuntimeSchema.getSchema(cls);

                LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);

                //序列化
                byte[] protostuff;
                try{
                    protostuff = ProtostuffIOUtil.toByteArray(obj, schema, buffer);
                    return protostuff;
                }catch (Exception e){
                    e.printStackTrace();
                    return null;
                }finally
                {
                    buffer.clear();
                }
            }
        },


    }
    class adapter implements JsonSerializer<Class<?>>, JsonDeserializer<Class<?>> {

        @Override
        public Class<?> deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            try {

                String asString = jsonElement.getAsString();
                return Class.forName(asString);
            }catch (Exception e){
                throw new JsonParseException(e);
            }

        }

        @Override
        public JsonElement serialize(Class<?> aClass, Type type, JsonSerializationContext jsonSerializationContext) {

            return new JsonPrimitive(aClass.getName());
        }
    }
}
