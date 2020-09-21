package com.example.sso_google.model;

import java.util.List;

public class EventList {
    private String timeZone;

    private List<Events> items;

    public List<Events> getItems() {
        return items;
    }

    public void setItems(List<Events> items) {
        this.items = items;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }
}
