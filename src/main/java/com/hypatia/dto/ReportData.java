package com.hypatia.dto;

import com.hypatia.entity.AiInteraction;

public class ReportData {
    CommonReportData commonReportData;
    UserProfileData profileData;
    AIResponse aiResponse;

    public ReportData() {
        commonReportData=new CommonReportData();
        profileData=new UserProfileData();
        aiResponse=new AIResponse();

    }

    public AIResponse getAiResponse() {
        return aiResponse;
    }

    public void setAiResponse(AIResponse aiResponse) {
        this.aiResponse = aiResponse;
    }

    public CommonReportData getCommonReportData() {
        return commonReportData;
    }

    public void setCommonReportData(CommonReportData commonReportData) {
        this.commonReportData = commonReportData;
    }

    public UserProfileData getProfileData() {
        return profileData;
    }

    public void setProfileData(UserProfileData profileData) {
        this.profileData = profileData;
    }


}
