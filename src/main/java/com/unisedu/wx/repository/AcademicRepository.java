package com.unisedu.wx.repository;

import com.unisedu.wx.entity.AcademicUser;
import org.springframework.data.repository.CrudRepository;

public interface AcademicRepository extends CrudRepository<AcademicUser, Long> {
    AcademicUser findByOpenId(String openid);
}
