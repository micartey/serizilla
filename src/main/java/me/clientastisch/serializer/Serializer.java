package me.clientastisch.serializer;

import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class Serializer {

    private final CopyOnWriteArrayList<Class<? extends Packet>> classes = new CopyOnWriteArrayList<>();

    private final int headerLength;

    public Serializer(int headerLength) {
        this.headerLength = headerLength;
    }

    public Serializer() {
        this(40);
    }

    /**
     * Add a new class to the known packets
     *
     * @param packet Packet class
     */
    public void register(Class<? extends Packet> packet) {
        classes.add(packet);
    }

    /**
     * Remove a class from the known packets
     *
     * @param packet Packet class
     */
    public void unregister(Class<? extends Packet> packet) {
        classes.remove(packet);
    }

    /**
     * Serialize a packet to string
     * The first 40 bytes are the packet uuid
     *
     * @param packet Packet instance
     * @return serialized packet
     */
    @SneakyThrows
    public byte[] serialize(Packet packet) {
        List<byte[]> parts = new LinkedList<>();

        // Reserve bytes for packet identifier (uuid)
        byte[] uuid = packet.getDescription().uuid().getBytes(StandardCharsets.US_ASCII);
        byte[] uuidPart = new byte[this.headerLength];
        System.arraycopy(uuid, 0, uuidPart, 0, uuid.length);
        parts.add(uuidPart);

        for (Field field : getFields(packet.getClass())) {
            Packet.Value value = field.getAnnotation(Packet.Value.class);

            byte[] contents = String.valueOf(field.get(packet)).getBytes(StandardCharsets.US_ASCII);
            byte[] part = new byte[value.length()];

            // Make sure that the reserved length is sufficient
            if (part.length < contents.length) {
                throw new IllegalStateException(String.format(
                        "Content for field %s exceedes reserved length of %s bytes",
                        field.getName(),
                        value.length())
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
    public Packet deserialize(byte[] bytes) {
        List<byte[]> parts = new LinkedList<>();

        // Reserve bytes for packet identifier (uuid)
        byte[] uuid = new byte[this.headerLength];
        System.arraycopy(bytes, 0, uuid, 0, 40);
        uuid = removeTail(uuid);

        Optional<Class<? extends Packet>> packet = getPacketByUUID(new String(uuid));

        if (!packet.isPresent())
            return null;

        Packet instance = packet.get().newInstance();

        int offset = this.headerLength;
        for (Field field : getFields(instance.getClass())) {
            Packet.Value value = field.getAnnotation(Packet.Value.class);

            byte[] contents = new byte[value.length()];
            System.arraycopy(bytes, offset, contents, 0, value.length());
            contents = removeTail(contents);

            offset += value.length();

            String content = new String(contents, StandardCharsets.US_ASCII);
            field.set(instance, convert(field.getType(), content));
        }

        return instance;
    }

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
    private List<Field> getFields(Class<? extends Packet> packet) {
        return Arrays.stream(packet.getFields()).filter(field -> field.isAnnotationPresent(Packet.Value.class)).collect(Collectors.toList());
    }

    /**
     * Get the class by the description in case it
     * exists
     *
     * @param uuid packet description
     * @return the optional of the class if found
     */
    private Optional<Class<? extends Packet>> getPacketByUUID(String uuid) {
        return classes.stream().filter(var -> var.getAnnotation(Packet.Description.class).uuid().equals(uuid)).findFirst();
    }

    /**
     * Parses a string to its required type
     *
     * @param type Field type
     * @param name Field content
     * @return content parsed to field type
     */
    private Object convert(Class<?> type, String name) {
        try {
            if (type.equals(List.class))
                return Arrays.asList(name.replaceAll("^\\[|]$", "").split(", "));

            String className = type.equals(long.class) ? "java.lang.Long" : type.equals(int.class) ? "java.lang.Integer" : type.equals(double.class) ? "java.lang.Double" : type.equals(float.class) ? "java.lang.Float" : type.equals(byte.class) ? "java.lang.Byte" : type.equals(boolean.class) ? "java.lang.Boolean" : type.equals(short.class) ? "java.lang.Short" : type.getName();
            Method method = Class.forName(className).getMethod("valueOf", String.class);
            return method.invoke(null, name);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException |
                 ClassNotFoundException e) {
            return name;
        }
    }
}
