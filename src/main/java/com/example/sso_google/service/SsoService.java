package com.example.sso_google.service;

import com.example.sso_google.model.EventList;
import com.example.sso_google.model.Events;
import com.example.sso_google.model.UserDetails;
import org.omg.CORBA.Environment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


import java.time.LocalDate;
import java.time.LocalTime;


@Service
public class SsoService {

    @Autowired
    private OAuth2AuthorizedClientService authorizedClientService;

    @Value("${api.google.calender}")
    String calendarApiUrl;


    public EventList getCalenderData(OAuth2AuthenticationToken authentication) {
        EventList eventList =new EventList();
        OAuth2AuthorizedClient client = authorizedClientService
                .loadAuthorizedClient(
                        authentication.getAuthorizedClientRegistrationId(),
                        authentication.getName());

        String timeMin = LocalDate.now().atTime(LocalTime.MIN).toString().concat(":00Z");
        String timeMax = LocalDate.now().atTime(LocalTime.MAX).toString().concat("Z");
        String calenderUrl = calendarApiUrl+"?timeMin="+timeMin+"&timeMax="+timeMax;
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + client.getAccessToken()
                    .getTokenValue());

            HttpEntity entity = new HttpEntity("", headers);

            ResponseEntity<EventList> response = restTemplate
                    .exchange(calenderUrl, HttpMethod.GET, entity, EventList.class);
            eventList = response.getBody();
        }catch (Exception e){

            System.out.println("Exeption"+e);
        }


        return eventList;
    }
}


