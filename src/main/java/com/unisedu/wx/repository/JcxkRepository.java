package com.unisedu.wx.repository;

import com.unisedu.wx.entity.Jcxk;
import org.springframework.data.repository.CrudRepository;

public interface JcxkRepository extends CrudRepository<Jcxk, Long> {

    public Jcxk findByCardNumberAndSubject(String cardNumber,String subject);
}
