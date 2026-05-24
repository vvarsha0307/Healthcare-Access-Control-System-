package com.example.demo.DTO;
public class MainLoginRequest {
    private String hospitalEmail;
    private String staffId;
    public String getHospitalEmail() {
        return hospitalEmail;
    }
    public void setHospitalEmail(String hospitalEmail) {
        this.hospitalEmail = hospitalEmail;
    }
    public String getStaffId() {
        return staffId;
    }
    public void setStaffId(String staffId) {
        this.staffId = staffId;
    }
}
