package chat.pojo.UserDao;

import chat.pojo.User;

import java.util.Map;

/**
 * @author Administrator
 */
public interface UserDao {
    boolean setUser(Map<String,Object> user);
    String getAge(String name);
}
