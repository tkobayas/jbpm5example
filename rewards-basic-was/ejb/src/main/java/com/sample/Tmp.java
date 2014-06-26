package com.sample;

import javax.annotation.Resource;
import javax.transaction.UserTransaction;

public class Tmp {
    
  @Resource
  private UserTransaction ut;
    
    public void test() {
        System.out.println(ut);
    }

}
