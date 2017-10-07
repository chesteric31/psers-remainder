package be.chesteric31.psersremainder

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestTemplate
import java.time.LocalDate
import java.time.LocalDateTime

@Component
class ScheduledTasks {

    val psersApi = "https://psers-api.herokuapp.com/api"
    val tvMazeApi = "http://api.tvmaze.com"

    //Fire at 12:00 PM (noon) every day
    //@Scheduled(cron = "0 0 12 * * ?")
    @Scheduled(fixedRate = 60000)
    fun checkEpisodes() {
        val log = LoggerFactory.getLogger(ScheduledTasks::class.java)
        log.info("Time to wake!")

        val restTemplate = RestTemplateBuilder().build()
        val wrapper = restTemplate.getForObject("$psersApi/users", UserWrapper::class.java)
        val showsMap = convertToShowsMap(wrapper)
        for (show in showsMap) {
            try {
                val url = "$tvMazeApi/shows/${show.key}/episodesbydate?date=${LocalDate.now()}"
                        //"http://api.tvmaze.com/shows/11/episodesbydate?date=2017-09-21"
                val showsToday = restTemplate.getForObject(url, List::class.java)
                if (showsToday != null) {
                    for (user in show.value) {
                        notify(user, show)
                    }
                }
            } catch (e: HttpStatusCodeException) {
                if (e.statusCode.is4xxClientError) {
                    log.info("Nothing to notify for show: ${show.key}")
                }
            }
        }
        log.info("Time to sleep...")
    }

    private fun notify(user: String, show: MutableMap.MutableEntry<Int, MutableList<String>>) {
        val log = LoggerFactory.getLogger(ScheduledTasks::class.java)
        log.info(user)
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED
        val map = LinkedMultiValueMap<String, String>()
        val restTemplate = RestTemplateBuilder().build()
        val currentShow = restTemplate.getForObject("$tvMazeApi/shows/${show.key}", Show::class.java)
        map.add("show_title", currentShow.name)
        val request = HttpEntity<MultiValueMap<String, String>>(map, headers)
        val postUrl = //"https://psers-api.herokuapp.com/api/user/" + "egxHmlLaAGg:APA91bFYOrHq9s-G72nNgut2j1doNYvCQrkD9mMVWfi6LbjH4aLjEo14V_j0zPsYJ2-CYFBZRmT16a93ORN0ncAGVrg2Sft1s930qAKPJtq9B_VrJ2SA0LxB6ldAO6wJKzuYw51PQ2sr" + "/notify"
                "$psersApi/user/$user/notify"
        val postForEntity = restTemplate.postForEntity(postUrl, request, String::class.java)
        log.info("$postForEntity")
    }

    private fun convertToShowsMap(wrapper: UserWrapper): LinkedHashMap<Int, MutableList<String>> {
        val showsMap = LinkedHashMap<Int, MutableList<String>>()
        for (user in wrapper.users) {
            for (show in user.shows) {
                var listOfUsers = showsMap.get(key = show)
                if (listOfUsers == null) {
                    listOfUsers = mutableListOf()
                }
                listOfUsers.add(user.id)
                showsMap.put(show, listOfUsers)
            }
        }
        return showsMap
    }
}
