package me.clientastisch.serializer;

import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Serializer {

    private final CopyOnWriteArrayList<Class<? extends Packet>> classes = new CopyOnWriteArrayList<>();

    private final String separator, splitter;

    /**
     * Create a new Serializer
     *
     * @param separator string to separate fields
     * @param splitter string to separator field name from content
     */
    public Serializer(String separator, String splitter) {
        this.separator = separator;
        this.splitter = splitter;
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
     *
     * @param packet Packet instance
     * @return serialized packet
     */
    public String serialize(Packet packet) {
        StringBuilder builder = new StringBuilder(packet.getDescription().uuid());

        getFields(packet.getClass()).forEach(field -> {
            try {
                builder.append(separator).append(field.getAnnotation(Packet.Value.class).name()).append(splitter).append(field.get(packet));
            } catch (IllegalAccessException exception) {
                exception.printStackTrace();
            }
        });

        return builder.toString();
    }

    /**
     * Create a packet instance from a serialized
     * string
     *
     * @param string serialized input
     * @return packet instance
     */
    @SneakyThrows
    public Packet deserialize(String string) {
        String[] split = string.split(Pattern.quote(separator));

        Optional<Class<? extends Packet>> packet = getPacketByUUID(split[0]);

        if(packet.isPresent()) {
            CopyOnWriteArrayList<Field> fields = new CopyOnWriteArrayList<>(getFields(packet.get()));
            Object instance = packet.get().newInstance();

            for(String word : Arrays.stream(split).skip(1).collect(Collectors.toList())) {
                Field field = Arrays.stream(instance.getClass().getFields())
                        .filter(var -> var.isAnnotationPresent(Packet.Value.class))
                        .filter(var -> var.getAnnotation(Packet.Value.class).name().equals(word.split(Pattern.quote(splitter))[0])).findFirst().orElse(null);
                field.set(instance, convert(field.getType(), word.split(Pattern.quote(splitter))[1]));
                fields.remove(field);
            }

            fields.removeIf(remaining -> remaining.isAnnotationPresent(Packet.Optional.class));

            if(fields.size() > 0)
                throw new RuntimeException("Non-optional Fields are undefinded: " + new ArrayList<>(fields));

            return (Packet) instance;
        }

        return null;
    }

    /**
     * Check if a serialized string truly is one of the
     * known packets
     *
     * @param string serialized input
     * @return whether a packet is valid or not
     */
    public boolean isValid(String string) {
        try {
            return deserialize(string) != null;
        } catch (Exception ex) {
            return false;
        }
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
        return Optional.ofNullable(classes.stream().filter(var -> var.getAnnotation(Packet.Description.class).uuid().equals(uuid)).findFirst().orElse(null));
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

            String className = type.equals(int.class) ? "java.lang.Integer" : type.equals(double.class) ? "java.lang.Double" : type.equals(float.class) ? "java.lang.Float" : type.equals(byte.class) ? "java.lang.Byte" : type.equals(boolean.class) ? "java.lang.Boolean" : type.equals(short.class) ? "java.lang.Short" : type.getName();
            Method method = Class.forName(className).getMethod("valueOf", String.class);
            return method.invoke(null, name);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | ClassNotFoundException e) {
            return name;
        }
    }
}
