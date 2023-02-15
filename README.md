# serizilla

<div align="center">
  <a href="https://www.oracle.com/java/">
    <img
      src="https://img.shields.io/badge/Written%20in-java-%23EF4041?style=for-the-badge"
      height="30"
    />
  </a>
  <a href="https://jitpack.io/#micartey/serializer/master-SNAPSHOT">
    <img
      src="https://img.shields.io/badge/jitpack-master-%2321f21?style=for-the-badge"
      height="30"
    />
  </a>
  <a href="https://micartey.github.io/serializer/docs" target="_blank">
    <img
      src="https://img.shields.io/badge/javadoc-reference-5272B4.svg?style=for-the-badge"
      height="30"
    />
  </a>
</div>

## 📚 Introduction

This project aims to easily serialize objects to strings and strings to the corresponding objects. While Java presents its own features, there are some use cases, e.g. Sharing objects between programming languages ​​or sending objects over the network when normal serialization does not meet the required criteria.

## 🔗 Build Tools

To use this porject as a dependency you might want to use a build tool like maven or gradle. An easy way for each and every project, is to use [jitpack](https://jitpack.io/#micartey/serializer/main-SNAPSHOT) as it makes it easy to implement and use. The following example is for maven specific, as I personally don't use gradle that much.

### Maven

First of all add a new repository to your `pom.xml` file to be able to download the dependecies provided by jitpack.

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

Lastly, after adding the repository to all your other repositories, you have to add the following segment to your dependencies.

```xml
<dependency>
    <groupId>com.github.micartey</groupId>
    <artifactId>serializer</artifactId>
    <version>main-SNAPSHOT</version>
</dependency>
```

## 🎈 Getting started

The serializer heavily depends on annotations as most of my other projects do as well. Each class that should be serialized is called an `Packet` because this project was mainly developed for networking. To get started, add the following annotation on top of you class:

```java
@Packet.Description(
    uuid = "SomeUniqueId"
)
```

You class also needs to extend the Packet classs. You also need a default constructor with no parameters.

```java
@Packet.Description(
    uuid = "SomeUniqueId"
)
public class MyTestClass extends Packet {

}
```

The last step is to provide all your fields with annotations. You can also specifiy that a field is not required but optional. That means that if the value is not present it will be ignored, however if present it will be set. Not specifiying a non optional field will result in an RuntimeException.

```java
@Packet.Value(name = "names")
public List<String> names = Arrays.asList("Hans", "Peter", "Dieter");

@Packet.Optional
@Packet.Value(name = "test")
public String test;
```

### Serializing classes

You can also serialize classes that are not top level. This only requires to have an `toString` and `valueOf` method as many other Java classes already provide by default.
