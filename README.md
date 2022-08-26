# Chromatophore

> Some species can rapidly change colour through mechanisms that translocate pigment and reorient reflective plates within chromatophores. This process, often used as a type of camouflage, is called physiological colour change or metachrosis

Chromatophore is a graphql-java compatible library that helps schema designers to evolve the schema and deprecate field
without impacting new clients.

**Note: This is mostly a POC and is in development right now.**

## How it works

When your GraphQL server receives at request, it pins every requested field to the requesting client. Going forward,
this client will always be served that version of the field.

When a field needs to be deprecated, we can simply deprecate it as usual:

```graphql
type Product {
    price: Int @deprecated(reason: "Price as a Int was a terrible idea!")
}
```

Usually, we'd introduce a field like `priceV2` or `priceObject` because `price` is already taken. However,
it sucks to make new clients pay the cost of our mistakes. Chromatophore lets you introduce new fields,
and expose them as an old name for new clients:

```graphql
type Product {
    price: Int @deprecated(reason: "Price as a Int was a terrible idea!")
    
    """
    Only you sees `priceV2`, for new clients, priceV2 will actually
    be exposed as `price`.
    """
    priceV2: Price @supersedesField(field: "price", version: 1)
}

type Price {
    cents: Int
}
```

## Client Upgrade

> TODO