package advancedweb.com.endpoint;

import org.springframework.web.socket.server.standard.ServerEndpointRegistration;

import javax.servlet.http.HttpSession;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;


public class GetHttpSessionConfigurator extends ServerEndpointConfig.Configurator {
    @Override
    public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
      HttpSession httpSession =  (HttpSession) request.getHttpSession();
      //将httpSession对象存储在对象中
        System.out.println(HttpSession.class.getName()==null);
        System.out.println(httpSession==null);
        sec.getUserProperties().put(HttpSession.class.getName(),httpSession);
    }


}
