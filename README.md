# serizilla

<div align="center">
  <a href="https://www.oracle.com/java/">
    <img
      src="https://img.shields.io/badge/Written%20in-java-%23EF4041?style=for-the-badge"
      height="30"
    />
  </a>
  <a href="https://jitpack.io/#micartey/serizilla/master-SNAPSHOT">
    <img
      src="https://img.shields.io/badge/jitpack-master-%2321f21?style=for-the-badge"
      height="30"
    />
  </a>
</div>

## 📚 Introduction

This project aims to easily serialize objects to strings and strings to the corresponding objects. 
While Java presents its own features, there are some use cases, e.g. Sharing objects between programming languages or sending objects over the network when normal serialization does not meet the required criteria.

## 🎈 Getting started

The serializer heavily depends on annotations as most of my other projects do as well. 
Each class that should be serialized is called an `Packet` because this project was mainly developed for networking. 
To get started, add the following annotation on top of you class:

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

The last step is to provide all your fields with annotations.
By default, fields cannot be larger than 128 bytes.
This can be changed by editing the `length` variable

```java
@Packet.Value(name = "names", length = 1000)
public List<String> names = Arrays.asList("Hans", "Peter", "Dieter");

@Packet.Value(name = "test")
public String test;
```

### Serializing classes

You can also serialize classes that are not top level. 
This only requires to have an `toString` and `valueOf` method as many other Java classes already provide by default.
