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

This project aims to easily serialize and to the corresponding objects. 
While Java presents its own features, there are some use cases, e.g. Sharing objects between programming languages or sending objects over the network when normal serialization does not meet the required criteria.

With the 2.0, you are able to create byte streams that are similar to those of protocols:

```
                   0         15 16      31 32             95
                    ┌──────────┬──────────┬───────────────┐ 
                0   │   Type   │  Topic   │   unique id   │ 
                    ├──────────┴──────────┴───────────────┤ 
                    │                                     │ 
                    │               Message               │ 
                96  │          (Variable length)          │ 
                    │                                     │ 
                    └─────────────────────────────────────┘ 
```

These byte streams are being deconstructed and reconstructed from objects to objects and thus be shared
between languages and networks.

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

The last step is to provide all fields of interest with annotations.
You always need to specify the byte length.

```java
@Packet.Value(1000)
public List<String> names = Arrays.asList("Hans", "Peter", "Dieter");

@Packet.Value(16)
public String test;
```

### Serializing classes

You can also serialize classes that are not top level. 
This only requires to have an `toString` and `valueOf` method as many other Java classes already provide by default.
