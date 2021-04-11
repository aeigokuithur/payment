package com.unisedu.wx.repository;

import com.unisedu.wx.entity.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Long>{

    User findByOpenId(String openid);
}
