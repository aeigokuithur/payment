package com.unisedu.wx.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "camp_user_academic")
public class AcademicUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    private String name;
    private String gender;
    private String nation;
    private String province;
    private String major;
    private String city;
    private String cardNumber;
    private String graduateTime;
    private String parentName;
    private String parentMobile;
    private String school;
    private String classSort;
    private String gradeSort;
    private String openId;
    private String place;
}
