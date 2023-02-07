# Network

This module is a library for networking. Currently, it is only able to connect to an ABB robot, however, a goal is to
make it as multipurpose as possible.

It allows an API to be expressed as an interface. Annotated methods will return a callable network call, which can be
called either synchronously or asynchronously. The response is also converted into another interface type.

## Usage

A service is an interface which does not represent any entity. For example, the root endpoint '/' is a service, as it
doesn't perform any operation on any entity. To create an implementation of a service, use a 'NetworkEngine'.

```java

@Service
public interface ShopService {
    // Methods which return a service, will automatically return a new service of the specified type.
    AdminService getAdminService();

    @GET("/products")
    NetworkCall<List<ProductEntity>> getProducts();

    // A path containing '{___}' is replaced by the value of a matching parameter.
    @GET("/shelf/{shelf}")
    NetworkCall<ShelfEntity> getShelf(
            @Path("shelf") String shelf
    );

    @POST("/shelf")
    NetworkCall<ShelfEntity> newShelf(
            // The value of a parameter annotated with '@Field' is added as a field to the request body.
            // If the argument is null, it is ignored.
            @Field("name") String name
    );
} 
```

An entity is also an interface, but represents an entity. A unique property for entities is that paths can not only be
replaced by an argument, but also by a link of the response of which the entity represents.

Additionally, if a compact version of an entity is returned (for example, in a list) with a link to the complete entity,
the complete entity will automatically be retrieved if you try to retrieve a property or link which does not exist.

```java
// Only response objects with the type 'product' will be converted into this entity.
@Entity("product")
public interface ProductEntity {
    // Methods annotated with '@Property' will return the field 'name' of the response.
    @Property("name")
    String getName();

    // The property is converted to the method's return type.
    @Property("tag")
    Tag getTag();

    // A path containing '{@___}' is replaced by the link with the specified relationship.
    @DELETE("{@self}")
    NetworkCall<Void> delete();

    enum Tag {
        // If the name isn't the same as what is sent and received, annotate the constant with '@Deserializable', 
        // otherwise the name of the field is used.
        @Deserializable("bat")
        BATTERY,
        BOOK,
        TOY
    }

}
```