package advancedweb.com.endpoint;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class MessageUtils {
    public static Gson gson = new Gson();
    public static Map<String,Object> generateMessage(boolean isSystem,String type,String fromUsername,Object content){
        Map<String,Object> map = new HashMap<>();
        map.put("isSystem",isSystem);
        map.put("type",type);
        map.put("fromUsername",fromUsername);
        map.put("content",content);
        return map;
    }
}
