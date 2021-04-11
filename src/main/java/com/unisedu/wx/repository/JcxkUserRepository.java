package com.unisedu.wx.repository;

import com.unisedu.wx.entity.AcademicUser;
import com.unisedu.wx.entity.JcxkUser;
import org.springframework.data.repository.CrudRepository;

public interface JcxkUserRepository extends CrudRepository<JcxkUser, Long> {
    JcxkUser findByOpenId(String openid);
}
