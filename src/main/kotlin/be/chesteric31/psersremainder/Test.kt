package be.chesteric31.psersremainder

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.core.ParameterizedTypeReference
import org.springframework.web.client.getForObject
import org.springframework.http.HttpHeaders
import org.springframework.web.client.HttpStatusCodeException
import kotlin.reflect.jvm.internal.impl.load.kotlin.JvmType
import org.springframework.util.MultiValueMap
import org.springframework.http.HttpEntity
import org.springframework.http.MediaType
import org.springframework.util.LinkedMultiValueMap
import org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED




fun main(args: Array<String>) {
    val json = "{ \"user_id\": \"1\", \"watching_shows_tvmaze_ids\": [1,2]}"
    val mapper = jacksonObjectMapper()
    val user = mapper.readValue<User>(json)
    println(user)
    val bigJson = "{ \"success\": true, \"users\": [{\"user_id\": \"1\", \"watching_shows_tvmaze_ids\": [1,2]},{\"user_id\": \"2\", \"watching_shows_tvmaze_ids\": [3,4]}]}"
    val userWrapper = mapper.readValue<UserWrapper>(bigJson)
    println(userWrapper)

    val restTemplate = RestTemplateBuilder().build();
    val wrapper = restTemplate.getForObject("https://psers-api.herokuapp.com/api/users", UserWrapper::class.java)
    //println(wrapper)
    val showsMap = LinkedHashMap<Integer, MutableList<String>>()
    for (user in wrapper.users) {
        //println("${user.id}: ${user.shows}")
        for (show in user.shows) {
            var listOfUsers = showsMap.get(show)
            if (listOfUsers == null) {
                listOfUsers = mutableListOf()
            }
            listOfUsers?.add(user.id)
            showsMap.put(show, listOfUsers)
        }
    }
    for (show in showsMap) {
        println(show.key)
        println(show.value)
    }
    try {
        val showsToday = restTemplate
                .getForObject(
                        "http://api.tvmaze.com/shows/11/episodesbydate?date=2017-09-21", List::class.java)
        println(showsToday)
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED

        val map = LinkedMultiValueMap<String, String>()
        map.add("show_title", "Flash")

        val request = HttpEntity<MultiValueMap<String, String>>(map, headers)

        val postForEntity = restTemplate.postForEntity("https://psers-api.herokuapp.com/api/user/" + "egxHmlLaAGg:APA91bFYOrHq9s-G72nNgut2j1doNYvCQrkD9mMVWfi6LbjH4aLjEo14V_j0zPsYJ2-CYFBZRmT16a93ORN0ncAGVrg2Sft1s930qAKPJtq9B_VrJ2SA0LxB6ldAO6wJKzuYw51PQ2sr" + "/notify", request, String::class.java)
        println(postForEntity)
    } catch (e: HttpStatusCodeException) {
        if (e.statusCode.is4xxClientError) {
            println("Not found for ")
        }
    }
}

