package com.lgt.qa.functions;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;


/**
 * <p>ClassName: ObjectUtils</p>
 * <p>Description: Object操作工具类</p>
 */
public class ObjectUtils implements Serializable {

    private static Logger logger = LoggerFactory.getLogger(ObjectUtils.class);

    public final static Kryo kryo = new Kryo();
    static {
        kryo.setRegistrationRequired(false);
        kryo.setMaxDepth(20);
    }

    /**
     * Description :将对像转换为字节
     * @param  obj Object
     * @return byte[]
     */
    public static byte[] ObjectToByte(Object obj) {
        ByteArrayOutputStream out = null;
        Output output = null;
        try {
            out = new ByteArrayOutputStream();
            output = new Output(out, 1024);
            kryo.writeClassAndObject(output, obj);
            return output.toBytes();
        } catch (Exception e) {
            logger.error("ObjectUtils.ObjectToByte(Object obj)  throw IOException...");
            throw (e);
        } finally {
            if (null != out) {
                try {
                    out.close();
                    out = null;
                } catch (IOException e) {
                }
            }
            if (null != output) {
                output.close();
                output = null;
            }
        }
    }

    /**
     * Description :将字节转换为对象
     * @param  bytes byte[]
     * @return obj Object
     */
    public static Object ByteToObject(byte[] bytes) {
        Input input = null;
        try {
            input = new Input(bytes, 0, 1024);
            return kryo.readClassAndObject(input);
        } catch (Exception e) {
            logger.error("ObjectUtils.ByteToObject(byte[] bytes)  throw IOException...");
            throw (e);
        } finally {
            if (null != input) {
                input.close();
                input = null;
            }
        }
    }
}
