package com.unisedu.wx.repository;

import com.unisedu.wx.entity.StudyUser;
import com.unisedu.wx.entity.User;
import org.springframework.data.repository.CrudRepository;

public interface StudyUserRepository extends CrudRepository<StudyUser, Long>{

    StudyUser findByOpenId(String openid);
}
