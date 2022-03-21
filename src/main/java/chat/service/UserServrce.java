package chat.service;

import chat.pojo.User;
import chat.pojo.UserDao.UserDao;

import java.util.HashMap;
import java.util.Map;

/**
 * PROJECT_NAME: ChatServer
 *
 * @author: lhd
 * 2022/3/15 0015 10:15
 * Description:
 */
public class UserServrce implements UserDao {

    /**
     * 利用 map 来模拟数据库
     */
    final Map<String,String> map = new HashMap<>();


    @Override
    public boolean setUser(Map<String,Object> user) {
        map.put("name", (String) user.get("name"));
        map.put("age",(String) user.get("age"));
        if(map.size()>1){
            System.out.println(map.get("age"));
            return true;
        }
        return false;
    }

    @Override
    public String getAge(String name) {
        if(map.containsValue("张三")){
            return map.get("age");
        }
        return map.get(name);
    }
}
