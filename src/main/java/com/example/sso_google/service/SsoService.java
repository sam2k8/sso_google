package com.example.sso_google.service;

import com.example.sso_google.model.EventList;
import com.example.sso_google.model.EventViewModel;
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


import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


@Service
public class SsoService {

    @Autowired
    private OAuth2AuthorizedClientService authorizedClientService;

    @Value("${api.google.calender}")
    String calendarApiUrl;


    public EventList getCalenderData(OAuth2AuthenticationToken authentication) {
       // List<EventViewModel> eventViewModels =new ArrayList<>();
        EventList eventList=new EventList();
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

            if (response!=null){
                eventList=response.getBody();
            }

        }catch (Exception e){

            System.out.println("Exeption"+e);
        }

        return eventList;
    }
    public List<EventViewModel> getEventProcessedData(EventList eventList){
        List<EventViewModel> eventViewModels=new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZZZZZ");
        DateTimeFormatter timeFormat= DateTimeFormatter.ofPattern("hh:mm a");
        for (Events event:eventList.getItems()) {
            EventViewModel eventViewModel=new EventViewModel();
            eventViewModel.setDescription(event.getDescription());
            eventViewModel.setLocation(event.getLocation());
            eventViewModel.setSummary(event.getSummary());
            eventViewModel.setEnd(LocalDateTime.parse(event.getEnd().getDateTime(),formatter).toLocalTime().format(timeFormat).toString());
            eventViewModel.setStart(LocalDateTime.parse(event.getStart().getDateTime(),formatter).toLocalTime().format(timeFormat).toString());
            eventViewModels.add(eventViewModel);

        }
        return eventViewModels;
    }
    public List<String> getAvailableTimeSlot(EventList eventList){

        List<String> availableTimeSlot=new ArrayList<>();
        LocalDateTime startTime = LocalDate.now().atTime(LocalTime.MIN);
        LocalDateTime endTime = LocalDate.now().atTime(LocalTime.MAX);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZZZZZ");
        DateTimeFormatter timeFormat= DateTimeFormatter.ofPattern("hh:mm a");
        for (Events event:eventList.getItems()) {
            LocalDateTime eventStartTime=LocalDateTime.parse(event.getStart().getDateTime(),formatter);
            if(eventStartTime.toLocalTime().compareTo(startTime.toLocalTime())>0){
                String timeSlot=startTime.toLocalTime().format(timeFormat).toString()
                        .concat("--")
                        .concat(eventStartTime.toLocalTime().format(timeFormat));
                availableTimeSlot.add(timeSlot);
                startTime=LocalDateTime.parse(event.getEnd().getDateTime(),formatter);
            }else if (eventStartTime.toLocalTime().compareTo(startTime.toLocalTime())==0){
                startTime=LocalDateTime.parse(event.getEnd().getDateTime(),formatter);
            }

        }
        if (startTime.compareTo(endTime)<0){
            String availableTime=startTime.toLocalTime().format(timeFormat).toString()
                    .concat("--")
                    .concat(endTime.toLocalTime().format(timeFormat));
            availableTimeSlot.add(availableTime);
        }

        return availableTimeSlot;
    }
}


