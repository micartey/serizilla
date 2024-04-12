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

This project aims to easily serialize objects to and from byte streams. 
While Java presents its own features, there are some use cases, e.g. sharing objects between 
programming languages, sending objects over the network or creating protocols.

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

These byte streams are being deconstructed and reconstructed from objects to objects and thus 
can be shared between languages and networks.

## 🎈 Getting started

The serializer heavily depends on annotations as most of my other projects do as well. 
To get started, add the following annotation on top of you class:

```java
@Description(
    uuid = "SomeUniqueId"
)
public class MyTestClass {
    
    // With empty constructor
    
}
```

Fields that need to be serialized, need to be public and annotated.
As serizilla uses byte streams to create protocols, you are required to reserve byte lengths for each field.
The default length is `128` bytes per field. 

```java
@Serialize(1000)
public List<String> names = Arrays.asList("Hans", "Peter", "Dieter");

@Serialize(16) // Only 16 bytes long name
public String test;
```

### Serializing classes

You can also serialize classes that are not top level. 
This only requires to have an `toString` and `valueOf` method as many other Java classes already provide by default.
