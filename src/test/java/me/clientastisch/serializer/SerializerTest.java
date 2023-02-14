package me.clientastisch.serializer;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@Packet.Description(
        uuid = "Serializer.Test"
)
public class SerializerTest extends Packet {

    @Packet.Value(name = "test")
    public TestClass test = new TestClass("Hallo");

    @Packet.Optional
    @Packet.Value(name = "name")
    public List<String> name = Arrays.asList("hey", "du", "da");

    @Test
    public void onTest() {
        Serializer serializer = new Serializer("&-&", "#+*#");
        serializer.register(SerializerTest.class);

        String serialize = serializer.serialize(new SerializerTest());
        System.out.println(serialize);

        Packet object = serializer.deserialize(serialize);
        System.out.println(serializer.serialize(object));

        assertNotNull(object);
    }

    public static class TestClass {

        private String value;

        public TestClass(String value) {
            this.value = value;
        }

        public static TestClass valueOf(String input) {
            return new TestClass(input.substring(17, input.length() - 2));
        }

        @Override
        public String toString() {
            return "TestClass{" +
                    "value='" + value + '\'' +
                    '}';
        }
    }
}