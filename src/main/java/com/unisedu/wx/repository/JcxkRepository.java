package com.unisedu.wx.repository;

import com.unisedu.wx.entity.Jcxk;
import org.springframework.data.repository.CrudRepository;

public interface JcxkRepository extends CrudRepository<Jcxk, Long> {

    Jcxk findByCardNumber(String cardNumber);
}
