package com.centurionlauncher.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDate;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FamilyInfoDTO {
    private Long familyId;
    private Long ownerId;
    private List<FamilyMemberDTO> members;
    private List<FamilyGameDTO> games;
    private SubscriptionPlanDTO subscriptionPlan;
    private LocalDate expDate;

    // Getters and Setters
    public Long getFamilyId() {
        return familyId;
    }

    public void setFamilyId(Long familyId) {
        this.familyId = familyId;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public List<FamilyMemberDTO> getMembers() {
        return members;
    }

    public void setMembers(List<FamilyMemberDTO> members) {
        this.members = members;
    }

    public List<FamilyGameDTO> getGames() {
        return games;
    }

    public void setGames(List<FamilyGameDTO> games) {
        this.games = games;
    }

    public SubscriptionPlanDTO getSubscriptionPlan() {
        return subscriptionPlan;
    }

    public void setSubscriptionPlan(SubscriptionPlanDTO subscriptionPlan) {
        this.subscriptionPlan = subscriptionPlan;
    }

    public LocalDate getExpDate() {
        return expDate;
    }

    public void setExpDate(LocalDate expDate) {
        this.expDate = expDate;
    }
}