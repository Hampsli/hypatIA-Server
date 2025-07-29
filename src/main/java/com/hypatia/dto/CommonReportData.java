package com.hypatia.dto;

import com.hypatia.entity.User;

import java.time.LocalDateTime;

public class CommonReportData {
    private UserDto user;
    private String setReportTypeDisplayName;
    private String organizationName;
    private String footer;
    private LocalDateTime dateTime;

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public UserDto getUser() {
        return user;
    }

    public void setUser(UserDto user) {
        this.user = user;
    }

    public String setReportTypeDisplayName() {
        return setReportTypeDisplayName;
    }

    public void setReportTypeDisplayName(String reportTypeDisplay) {
        this.setReportTypeDisplayName = reportTypeDisplay;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public String getFooter() {
        return footer;
    }

    public void setFooter(String footer) {
        this.footer = footer;
    }
}
