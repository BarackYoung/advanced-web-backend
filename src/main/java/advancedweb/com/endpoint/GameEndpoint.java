package advancedweb.com.endpoint;


import advancedweb.com.security.jwt.JwtRequestFilter;
import advancedweb.com.security.jwt.JwtTokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpSession;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint(value = "/game/{scene}/{token}")
@Component
@Slf4j
public class GameEndpoint {
    Logger logger = LoggerFactory.getLogger(GameEndpoint.class);

    /**
     * 存储在线用户一个场景（scene）对应一群在线用户Map<userID,GameEndpoint>
     * */
    private static Map<String,Map<String,GameEndpoint>> sceneAndOnlineUsers = new ConcurrentHashMap<>();
    private static Map<String,String> onlineUsersAndScene = new ConcurrentHashMap<>();
    private Session session;
    private String userID;
    private String scene;
    private String token;

    @OnOpen
    public void onOpen(Session session,@PathParam("token") String token,@PathParam("scene") String scene){
        logger.info("获得的token:"+token);
        logger.info("场景："+scene);
        token = token.substring(6);
        if (JwtRequestFilter.tokenMap.containsKey(token)){
            String userID = JwtRequestFilter.tokenMap.get(token);
            this.session = session;
            this.userID = userID;
            this.token = token;
            this.scene = scene;
            /**
             * 将当前用户添加到对应的场景的在线用户中
             * */
            onlineUsersAndScene.put(userID,scene);
            if (!sceneAndOnlineUsers.containsKey(scene)){
                Map<String,GameEndpoint> map = new ConcurrentHashMap<>();
                map.put(userID,this);
                sceneAndOnlineUsers.put(scene,map);
            }else {
                sceneAndOnlineUsers.get(scene).put(userID,this);
            }
            logger.info("上线用户："+userID);
            /**
             *发送一条系统消息通知同一场景的其他用户上线用户
             * */
            try {
                Map<String,Object> message = MessageUtils.generateMessage(true,"online","System",userID);
                Map<String,GameEndpoint> onlineUsers = sceneAndOnlineUsers.get(scene);
                for (Map.Entry<String,GameEndpoint> entry:onlineUsers.entrySet()){
                    entry.getValue().session.getBasicRemote().sendText(MessageUtils.gson.toJson(message));
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }



    }

    @OnMessage
    public void onMessage(String message, Session session){
        logger.info("得到一条消息："+message);
        logger.info("场景："+scene);
        Map<String,GameEndpoint> onlineUsers = sceneAndOnlineUsers.get(scene);
//        String userID = JwtRequestFilter.tokenMap.get(token);
        Map<String,Object> messageMap = MessageUtils.gson.fromJson(message,Map.class);
        boolean isSystem = (boolean) messageMap.get("isSystem");
        String type = messageMap.get("type").toString();
        String toUsername = messageMap.get("toUsername").toString();
        Object content = messageMap.get("content");
        logger.info("是否是系统消息："+String.valueOf(isSystem));
        logger.info("发送的用户名："+toUsername);
        logger.info("type:"+type);
        logger.info("content:"+content.toString());
        if (isSystem){
            /**
             * 坐标更新
             * */
            if (type.equals("coordinate")){
                Map<String,Object> sendMessageMap = MessageUtils.generateMessage(true,"coordinate",this.userID,content);
                try {
                    for (Map.Entry<String,GameEndpoint> endpointEntry:onlineUsers.entrySet()){
                        endpointEntry.getValue().session.getBasicRemote().sendText(MessageUtils.gson.toJson(sendMessageMap));
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        }else {
            /**
             * 发送信息给用户
             * */
          if (type.equals("chat")){
              Map<String,Object> sendMessageMap = MessageUtils.generateMessage(false,"chat",this.userID,content);
              if (onlineUsers.containsKey(toUsername)){
                  try {
                      onlineUsers.get(toUsername).session.getBasicRemote().sendText(MessageUtils.gson.toJson(sendMessageMap));
                  } catch (IOException e) {
                      e.printStackTrace();
                  }
              }
          }

        }

    }

    @OnClose
    public void onClose(){
        Map<String,GameEndpoint> onlineUsers = sceneAndOnlineUsers.get(scene);
        Map<String,Object> sendMessageMap = MessageUtils.generateMessage(true,"offline",this.userID,this.userID);
        try {
            for (Map.Entry<String,GameEndpoint> endpointEntry:onlineUsers.entrySet()){
                endpointEntry.getValue().session.getBasicRemote().sendText(MessageUtils.gson.toJson(sendMessageMap));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @OnError
    public void onError(Throwable e){

    }
}
