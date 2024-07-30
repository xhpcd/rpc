package com.xhpcd.rpc.util;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

public class ProtostuffUtil {

    /**
     * 序列化

     */
    @SuppressWarnings("unchecked")
    public static <T> byte[] serializer(T obj) {

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

    /**
     * 反序列化
     */
    @SuppressWarnings("unchecked")
    public static <T> T deserializer(byte[] bytes, Class<T> clazz) {


        Schema<T> schema = RuntimeSchema.getSchema(clazz);
        // 反序列化
        T obj = schema.newMessage();
        ProtostuffIOUtil.mergeFrom(bytes, obj, schema);
        return obj;
    }
}
