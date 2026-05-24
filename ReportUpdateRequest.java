package com.example.demo.DTO;

public class ReportUpdateRequest {

    private String diagnosis;
    private String treatment;

    public ReportUpdateRequest() {
    }

    public ReportUpdateRequest(String diagnosis, String treatment) {
        this.diagnosis = diagnosis;
        this.treatment = treatment;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public String getTreatment() {
        return treatment;
    }

    public void setTreatment(String treatment) {
        this.treatment = treatment;
    }

}