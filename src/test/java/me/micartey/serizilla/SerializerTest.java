package me.micartey.serizilla;

import me.micartey.serizilla.annotation.Description;
import me.micartey.serizilla.annotation.Serialize;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@Description(
        uuid = "Serializer.Test"
)
public class SerializerTest {

    @Serialize(50)
    private TestClass test = new TestClass("Hallo");

    @Serialize(100)
    private List<String> name = Arrays.asList("hey", "du", "da");

    @Test
    public void onTest() {
        Serializer serializer = new Serializer();
        serializer.register(SerializerTest.class);

        byte[] serialize = serializer.serialize(new SerializerTest());
        SerializerTest serializerTest = (SerializerTest) serializer.deserialize(serialize);

        assertNotNull(serializerTest);
    }

    public static class TestClass {

        private final String value;

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