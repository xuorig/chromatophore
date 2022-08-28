package com.xuorig.chromatophore.example.chromatophore

import javax.persistence.*

@Entity
class ChromatophoreClient(
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    val id: Int? = null,

    @Column(nullable = false)
    val name: String
)