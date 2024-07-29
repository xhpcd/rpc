package com.xhpcd.rpc.util;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtobufIOUtil;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

public class ProtostuffUtil {

    /**
     * 序列化
     * @param o
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> byte[] serializer(T obj) {
        /*Schema schema = RuntimeSchema.getSchema(o.getClass());
        return ProtobufIOUtil.toByteArray(o, schema, LinkedBuffer.allocate(256));*/
        Class<T> cls = (Class<T>) obj.getClass();
        // this is lazily created and cached by RuntimeSchema
        // so its safe to call RuntimeSchema.getSchema(Foo.class) over and over
        // The getSchema method is also thread-safe
        Schema<T> schema = RuntimeSchema.getSchema(cls);
        // Re-use (manage) this buffer to avoid allocating on every serialization
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
     * @param bytes
     * @param clazz
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T deserializer(byte[] bytes, Class<T> clazz) {

   /*     T obj = null;
        try {
            obj = clazz.newInstance();
            Schema schema = RuntimeSchema.getSchema(obj.getClass());
            ProtostuffIOUtil.mergeFrom(bytes, obj, schema);
        } catch (IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
        return obj;*/

        Schema<T> schema = RuntimeSchema.getSchema(clazz);
        // 反序列化
        T obj = schema.newMessage();
        ProtostuffIOUtil.mergeFrom(bytes, obj, schema);
        return obj;
    }
}
