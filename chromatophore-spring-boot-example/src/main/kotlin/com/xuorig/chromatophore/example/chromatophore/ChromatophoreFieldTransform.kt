package com.xuorig.chromatophore.example.chromatophore

import javax.persistence.*

@Entity
@Table(name = "field_transforms", indexes = [Index(name = "fieldNameUniqueIndex", columnList = "fieldName,client_id", unique = true)])
class ChromatophoreFieldTransform(
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    val id: Int? = null,

    @Column(nullable = false)
    val fieldName: String,

    @Column(nullable = false)
    val version: Int,

    @ManyToOne
    @JoinColumn(name = "client_id")
    val client: ChromatophoreClient
)