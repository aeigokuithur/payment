package com.unisedu.wx.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "camp_user_study")
public class StudyUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private String gender;
    private String cardNumber;
    private String province;
    private String school;
    private String grade;
    private String mobile;
    private String email;
    private String award;
    private Integer type;
    private String openId;

}
