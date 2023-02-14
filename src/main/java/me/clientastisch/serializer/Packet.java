package me.clientastisch.serializer;

import lombok.Getter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public abstract class Packet {

    @Getter private final Description description;

    public Packet() {
        if (!this.getClass().isAnnotationPresent(Description.class))
            throw new RuntimeException("Description annotation missing on class: " + this.getClass());
        description = this.getClass().getAnnotation(Description.class);
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface Description {
        String uuid();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Value {
        String name();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Optional {

    }
}
