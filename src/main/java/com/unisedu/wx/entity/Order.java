package com.unisedu.wx.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "camp_order")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String title;
    private String orderNo;
    private String status;
    private String userId;
    private String price;
    private Date createTime;
    private Date updateTime;
    private String orderType;
    private String website;
    private Integer company;
}
