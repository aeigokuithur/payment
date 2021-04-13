package com.unisedu.wx.repository;

import com.unisedu.wx.entity.Jcxk;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface JcxkRepository extends CrudRepository<Jcxk, Long> {

    //Jcxk findByCardNumber(String cardNumber);



    List<Jcxk> findByCardNumber(String cardNumber);
}
