package com.xuorig.chromatophore.example.shows

import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller

@Controller
class ShowsController {
    private val shows = listOf(
        Show("Stranger Things", 2016),
        Show("Ozark", 2017),
        Show("The Crown", 2016),
        Show("Dead to Me", 2019),
        Show("Orange is the New Black", 2013)
    )

    data class Show(val title: String, val releaseYear: Int)

    @QueryMapping
    fun shows(): List<Show> {
        return shows
    }
}