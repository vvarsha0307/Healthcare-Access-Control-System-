package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "user_shift")
public class UserShift {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "shift_name")
    private String shiftName;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    public UserShift() {}

    public UserShift(Long userId, String shiftName,
                     LocalDate startDate, LocalDate endDate) {
        this.userId    = userId;
        this.shiftName = shiftName;
        this.startDate = startDate;
        this.endDate   = endDate;
    }

    public Long getId()                      { return id; }
    public void setId(Long id)               { this.id = id; }

    public Long getUserId()                  { return userId; }
    public void setUserId(Long userId)       { this.userId = userId; }

    public String getShiftName()             { return shiftName; }
    public void setShiftName(String s)       { this.shiftName = s; }

    public LocalDate getStartDate()          { return startDate; }
    public void setStartDate(LocalDate d)    { this.startDate = d; }

    public LocalDate getEndDate()            { return endDate; }
    public void setEndDate(LocalDate d)      { this.endDate = d; }
}