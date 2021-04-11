package com.unisedu.wx.entity;
import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "camp_user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private String gender;
    private String cardNumber;
    private String mobile;
    private String email;
    private String province;
    private String city;
    private String address;
    private String school;
    private String grade;
    private String parentName;
    private String parentMobile;
    private String userType;
    private String openId;

}
