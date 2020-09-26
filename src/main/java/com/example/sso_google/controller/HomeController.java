package com.example.sso_google.controller;

import com.example.sso_google.model.EventList;
import com.example.sso_google.model.EventViewModel;
import com.example.sso_google.model.UserDetails;
import com.example.sso_google.service.SsoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.thymeleaf.util.StringUtils;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    @Autowired
    private OAuth2AuthorizedClientService authorizedClientService;

    @Autowired
    private SsoService ssoService;

    @GetMapping("/")
    public String home(Model model,Principal principal) {
        String message = principal!=null ? "Welcome "+((DefaultOAuth2User) ((OAuth2AuthenticationToken) principal).getPrincipal()).getAttributes().get("name"):"You are not logged in";
        model.addAttribute("message",message);
        return "home";
    }

    @GetMapping("/loginSuccess")
    public String loginSuccess(Model model, OAuth2AuthenticationToken authentication){
        OAuth2AuthorizedClient client = authorizedClientService
                .loadAuthorizedClient(
                        authentication.getAuthorizedClientRegistrationId(),
                        authentication.getName());
        String userInfoEndpointUri = client.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUri();

        if (!StringUtils.isEmpty(userInfoEndpointUri)) {
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + client.getAccessToken()
                    .getTokenValue());
            HttpEntity entity = new HttpEntity("", headers);
            ResponseEntity<UserDetails> response = restTemplate
                    .exchange(userInfoEndpointUri, HttpMethod.GET, entity, UserDetails.class);
            UserDetails userAttributes = response.getBody();
            model.addAttribute("name", userAttributes.getName());
        }

        return "success";
    }
    @GetMapping("/calendar")
    public String calendar(Model model,OAuth2AuthenticationToken authentication){

        EventList eventList =ssoService.getCalenderData(authentication);
        List<EventViewModel> events =ssoService.getEventProcessedData(eventList);
        List<String> availableSlot =ssoService.getAvailableTimeSlot(eventList);
        model.addAttribute("events",events);
        model.addAttribute("availableSlot",availableSlot);

        return "calendar";
    }


}
