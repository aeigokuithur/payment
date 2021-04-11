package com.unisedu.wx.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "camp_jcxk")
public class Jcxk {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String cardNumber;
    private String subject;

}
