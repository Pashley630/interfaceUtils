package com.lgt.qa.functions;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class SerializableUtil {
    private final static ThreadLocal<Kryo> currentKryo = ThreadLocal.withInitial(SerializableUtil::createKryo);

    /**
     * 数组转对象
     *
     * @param bytes 要转成对象类型的字符数组
     * @return object
     */
    public static <T> T unSerialize(byte[] bytes, Class<T> type) {
        Kryo kryo = currentKryo.get();
        try (Input input = new Input(bytes)) {
            return kryo.readObject(input, type);
        }
    }

    /**
     * 对象转数组
     *
     * @param obj 要转成数组的对象
     * @return byte[]
     */
    public static byte[] serialize(Object obj) {
        Kryo kryo = currentKryo.get();
        try (Output output = new Output(512, 102400)) {
            kryo.writeObject(output, obj);
            return output.toBytes();
        }
    }

    private static Kryo createKryo() {
        return new Kryo();
    }
}