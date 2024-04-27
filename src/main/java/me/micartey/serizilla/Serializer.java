package me.micartey.serizilla;

import lombok.SneakyThrows;
import me.micartey.serizilla.annotation.Description;
import me.micartey.serizilla.annotation.Serialize;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class Serializer {

    private final CopyOnWriteArrayList<Class<?>> classes = new CopyOnWriteArrayList<>();

    private final Charset charset;
    private final int headerLength;

    public Serializer(int headerLength, Charset charset) {
        this.headerLength = headerLength;
        this.charset = charset;
    }

    public Serializer() {
        this(40, StandardCharsets.US_ASCII);
    }

    /**
     * Add a new class to the known packets
     *
     * @param packet Packet class
     */
    public void register(Class<?> packet) {
        classes.add(packet);
    }

    /**
     * Remove a class from the known packets
     *
     * @param packet Packet class
     */
    public void unregister(Class<?> packet) {
        classes.remove(packet);
    }

    /**
     * Serialize a packet to string
     *
     * @param packet Packet instance
     * @return serialized packet
     */
    @SneakyThrows
    public byte[] serialize(Object packet) {
        if (!packet.getClass().isAnnotationPresent(Description.class))
            throw new IllegalStateException(String.format("%s annotation is missing", Description.class.getName()));

        List<byte[]> parts = new LinkedList<>();

        // Reserve bytes for packet identifier (uuid)
        byte[] uuid = packet.getClass().getAnnotation(Description.class).uuid().getBytes(this.charset);
        byte[] uuidPart = new byte[this.headerLength];
        System.arraycopy(uuid, 0, uuidPart, 0, uuid.length);
        parts.add(uuidPart);

        for (Field field : getFields(packet.getClass())) {
            Serialize value = field.getAnnotation(Serialize.class);

            byte[] contents = String.valueOf(field.get(packet)).getBytes(this.charset);
            byte[] part = new byte[value.value()];

            // Make sure that the reserved length is sufficient
            if (part.length < contents.length) {
                throw new IllegalStateException(String.format(
                        "Content for field %s exceedes reserved length of %s bytes",
                        field.getName(),
                        value.value())
                );
            }

            System.arraycopy(contents, 0, part, 0, contents.length);
            parts.add(part);
        }

        int totalLength = parts.stream().mapToInt(bytes -> bytes.length).sum();
        byte[] payload = new byte[totalLength];

        int offset = 0;
        for (byte[] bytes : parts) {
            System.arraycopy(bytes, 0, payload, offset, bytes.length);
            offset += bytes.length;
        }

        return payload;
    }

    /**
     * Create a packet instance from a serialized
     * string
     *
     * @param bytes serialized input
     * @return packet instance
     */
    @SneakyThrows
    public Object deserialize(byte[] bytes) {
        // Reserve bytes for packet identifier (uuid)
        byte[] uuid = new byte[this.headerLength];
        System.arraycopy(bytes, 0, uuid, 0, this.headerLength);
        uuid = removeTail(uuid);

        Optional<Class<?>> packet = getPacketByUUID(new String(uuid));

        if (!packet.isPresent())
            return null;

        Object instance = packet.get().newInstance();

        int offset = this.headerLength;
        for (Field field : getFields(instance.getClass())) {
            Serialize value = field.getAnnotation(Serialize.class);

            byte[] contents = new byte[value.value()];
            System.arraycopy(bytes, offset, contents, 0, Math.min(value.value(), bytes.length - offset));
            contents = removeTail(contents);

            offset += value.value();

            String content = new String(contents, this.charset);
            field.set(instance, convert(field.getType(), content));
        }

        return instance;
    }

    /**
     * Remove the tailing 0 value bytes
     *
     * @param array byte array
     * @return byte array without tailing 0 value bytes
     */
    private byte[] removeTail(byte[] array) {
        int index;
        for (index = 0; index < array.length; index++)
            if (array[index] == 0)
                break;

        byte[] trimArray = new byte[index];
        System.arraycopy(array, 0, trimArray, 0, index);

        return trimArray;
    }

    /**
     * Get all public fields of a class
     *
     * @param packet Packet class
     * @return all Fields in a List
     */
    private List<Field> getFields(Class<?> packet) {
        return Arrays.stream(packet.getDeclaredFields())
                .peek(field -> field.setAccessible(true))
                .filter(field -> field.isAnnotationPresent(Serialize.class))
                .collect(Collectors.toList());
    }

    /**
     * Get the class by the description in case it
     * exists
     *
     * @param uuid packet description
     * @return the optional of the class if found
     */
    private Optional<Class<?>> getPacketByUUID(String uuid) {
        return classes.stream().filter(var -> var.getAnnotation(Description.class).uuid().equals(uuid)).findFirst();
    }

    /**
     * Parses a string to its required type
     *
     * @param type Field type
     * @param name Field content
     * @return content parsed to field type
     */
    private Object convert(Class<?> type, String name) {
        if (name.equals("null"))
            return null;

        try {
            if (type.equals(List.class)) {
                String data = name.replaceAll("^\\[|]$", "");

                if (data.isEmpty())
                    return new ArrayList<>();

                return Arrays.asList(data.split(", "));
            }

            String className = type.equals(long.class) ? "java.lang.Long" : type.equals(int.class) ? "java.lang.Integer" : type.equals(double.class) ? "java.lang.Double" : type.equals(float.class) ? "java.lang.Float" : type.equals(byte.class) ? "java.lang.Byte" : type.equals(boolean.class) ? "java.lang.Boolean" : type.equals(short.class) ? "java.lang.Short" : type.getName();
            Method method = Class.forName(className).getMethod("valueOf", String.class);
            return method.invoke(null, name);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException |
                 ClassNotFoundException e) {
            return name;
        }
    }
}
