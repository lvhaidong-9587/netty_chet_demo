package chat.until;

import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.util.Map;

/**
 * PROJECT_NAME: ChatServer
 *
 * @author: lhd
 * 2022/3/19 0019 15:42
 * Description:
 */
public class ReflectUntil {

    public static Class<?> getClazz(Map map)throws Exception{
        //获取调用的类名
        Object className = map.get("className");
        //调用相应的类
        Class<?> clazz = URLClassLoader.getSystemClassLoader().loadClass((String) className);
        return clazz;
    }

}
