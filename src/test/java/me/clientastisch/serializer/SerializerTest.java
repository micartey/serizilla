package me.clientastisch.serializer;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@Packet.Description(
        uuid = "Serializer.Test"
)
public class SerializerTest extends Packet {

    @Field(value = 50)
    public TestClass test = new TestClass("Hallo");

    @Field(value = 100)
    public List<String> name = Arrays.asList("hey", "du", "da");

    @Test
    public void onTest() {
        Serializer serializer = new Serializer();
        serializer.register(SerializerTest.class);

        byte[] serialize = serializer.serialize(new SerializerTest());
        SerializerTest serializerTest = (SerializerTest) serializer.deserialize(serialize);

        assertNotNull(serializerTest);
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