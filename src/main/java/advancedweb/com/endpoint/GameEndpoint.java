package advancedweb.com.endpoint;


import advancedweb.com.security.jwt.JwtRequestFilter;
import advancedweb.com.security.jwt.JwtTokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpSession;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint(value = "/game/{token}")
@Component
@Slf4j
public class GameEndpoint {
    Logger logger = LoggerFactory.getLogger(GameEndpoint.class);

    private static Map<String,GameEndpoint> onlineUsers = new ConcurrentHashMap<>();

    private Session session;

    @OnOpen
    public void onOpen(Session session,@PathParam("token") String token){
        logger.info("获得的token:"+token);
        token = token.substring(6);
        logger.info("gameEndpoint中获取到的tokenMap:"+JwtRequestFilter.tokenMap);
        if (JwtRequestFilter.tokenMap.containsKey(token)){
            String userID = JwtRequestFilter.tokenMap.get(token);
            this.session = session;
            //获取httpSession对象
            //从httpSession中获取userName
            System.out.println("上线用户:"+userID);
            //将当前对象存储到容器里面
            onlineUsers.put(userID,this);
            logger.info("上线用户："+userID);
            //通过广播把上线信息推送给客户端
            try {
                for (Map.Entry<String,GameEndpoint> entry:onlineUsers.entrySet()){
                    entry.getValue().session.getBasicRemote().sendText(entry.getKey()+"上线了");
                }
            }catch (Exception e){
                e.printStackTrace();
            }

        }



    }

    @OnMessage
    public void onMessage(String message, Session session,@PathParam("token") String token){
        logger.info("得到一条消息："+message);
        logger.info("得到的token:"+token);
    }

    @OnClose
    public void onClose(){

    }
    @OnError
    public void onError(Throwable e){

    }
}
