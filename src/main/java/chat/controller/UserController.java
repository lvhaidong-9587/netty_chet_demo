package chat.controller;

import chat.pojo.User;
import chat.service.UserServrce;

import java.util.Map;

/**
 * PROJECT_NAME: ChatServer
 *
 * @author: lhd
 * 2022/3/15 0015 10:36
 * Description:
 */
public class UserController {

    private final UserServrce userServrce = new UserServrce();

    public String getUserAge(String name){
      return userServrce.getAge(name);
    }

    public void setUser(Map<String,Object> user){
        userServrce.setUser(user);

    }
}
