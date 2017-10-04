package be.chesteric31.psersremainder

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
import java.time.LocalDate
import java.time.LocalDateTime

@Component
class ScheduledTasks {

    //Fire at 12:00 PM (noon) every day
    //@Scheduled(cron = "0 0 12 * * ?")
    @Scheduled(fixedRate = 60000)
    fun checkEpisodes() {
        val log = LoggerFactory.getLogger(ScheduledTasks::class.java)
        val now = LocalDateTime.now()
        log.info("The time is now $now")

        val restTemplate = RestTemplateBuilder().build();
        val wrapper = restTemplate.getForObject("https://psers-api.herokuapp.com/api/users", UserWrapper::class.java)
        //println(wrapper)
        val showsMap = convertToShowsMap(wrapper)
        for (show in showsMap) {
            log.info("Now:  ${LocalDate.now()} - show: ${show.key} - users: ${show.value}")
            try {
                val url = "http://api.tvmaze.com/shows/${show.key}/episodesbydate?date=${LocalDate.now()}"
                        //"http://api.tvmaze.com/shows/11/episodesbydate?date=2017-09-21"
                val showsToday = restTemplate.getForObject(url, List::class.java)
                if (showsToday != null) {
                    for (user in show.value) {
                        println(user)
                        val headers = HttpHeaders()
                        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED

                        val map = LinkedMultiValueMap<String, String>()
                        val currentShow = restTemplate.getForObject("http://api.tvmaze.com/shows/${show.key}", Show::class.java)
                        //println(currentShow.name)
                        map.add("show_title", currentShow.name)
                        val request = HttpEntity<MultiValueMap<String, String>>(map, headers)
                        val postUrl = //"https://psers-api.herokuapp.com/api/user/" + "egxHmlLaAGg:APA91bFYOrHq9s-G72nNgut2j1doNYvCQrkD9mMVWfi6LbjH4aLjEo14V_j0zPsYJ2-CYFBZRmT16a93ORN0ncAGVrg2Sft1s930qAKPJtq9B_VrJ2SA0LxB6ldAO6wJKzuYw51PQ2sr" + "/notify"
                                "https://psers-api.herokuapp.com/api/user/$user/notify"
                        val postForEntity = restTemplate.postForEntity(postUrl, request, String::class.java)
                        println(postForEntity)
                    }
                }
            } catch (e: HttpStatusCodeException) {
                if (e.statusCode.is4xxClientError) {
                    log.info("Nothing to notify for show: ${show.key}")
                }
            }
        }
    }

    private fun convertToShowsMap(wrapper: UserWrapper): LinkedHashMap<Int, MutableList<String>> {
        val showsMap = LinkedHashMap<Int, MutableList<String>>()
        for (user in wrapper.users) {
            //println("${user.id}: ${user.shows}")
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
