package chat.until;

import java.util.HashMap;
import java.util.Map;

/**
 * PROJECT_NAME: ChatServer
 *
 * @author: lhd
 * 2022/3/19 0019 15:39
 * Description:
 */
public class StingToMapUntil {
    public static Map<String,Object> getStringToMap(String str){
        //根据逗号截取字符串数组
        String[] str1 = str.split(",");
        //创建Map对象
        Map<String,Object> map = new HashMap<>();
        //循环加入map集合
        for (int i = 0; i < str1.length; i++) {
            //根据":"截取字符串数组
            String[] str2 = str1[i].split(":");
            //str2[0]为KEY,str2[1]为值
            map.put(str2[0],str2[1]);
        }
        return map;
    }
}
