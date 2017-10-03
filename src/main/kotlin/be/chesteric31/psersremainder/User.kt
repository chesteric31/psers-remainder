package be.chesteric31.psersremainder

import com.fasterxml.jackson.annotation.JsonProperty

data class User(@JsonProperty("user_id") val id: String, @JsonProperty("watching_shows_tvmaze_ids") val shows: List<Int>)
