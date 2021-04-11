package com.unisedu.wx.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "camp_user_jcxk")
public class JcxkUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;
    private String name;
    private String nation;
    private String gender;
    private String cardNumber;
    private String mobile;
    private String province;
    private String school;
    private String grade;
    private String className;
    private String major;
    private String subject;
    private String health;
    private String teacher;
    private String parentName;
    private String parentMobile;
    private String college;
    private String openId;
    private String photo;
}
