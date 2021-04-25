package advancedweb.com.service;


import advancedweb.com.Entity.User;
import advancedweb.com.dao.UserRepository;
import advancedweb.com.security.jwt.JwtTokenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@Service
public class UserService {

    Logger logger= LoggerFactory.getLogger(UserService.class);
    private UserRepository userRepository;

    private JwtTokenUtil jwtTokenUtil;

    private AuthenticationManager authenticationManager;

    @Autowired
    public UserService(UserRepository userRepository,JwtTokenUtil jwtTokenUtil,AuthenticationManager authenticationManager){
      this.userRepository = userRepository;
      this.jwtTokenUtil = jwtTokenUtil;
      this.authenticationManager = authenticationManager;
    }

    public Map<String,Object> register(Map<String ,String> request) {
        Map<String,Object> result = new HashMap<>();
        String username=request.get("username");
        String password = request.get("password");
        logger.info("password:"+password);
        //首次注册
        if(userRepository.findByUsername(username)==null){
            //新建用户
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            User user=new User(username,encoder.encode(password));
            userRepository.save(user);
            result.put("status","success");
        }else {
            result.put("status","fail");
            result.put("message","duplicated register");
        }
        return result;
    }

    public Map<String,Object> login(String username, String password) {
        Map<String,Object> result = new HashMap<>();
        UsernamePasswordAuthenticationToken uptoken = new UsernamePasswordAuthenticationToken(username,password);
        Authentication authentication = authenticationManager.authenticate(uptoken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        User user = userRepository.findByUsername(username);
        if (user!=null){
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            if (encoder.matches(password,user.getPassword())){
                String token = jwtTokenUtil.generateToken(user);
                result.put("status","success");
                result.put("token",token);
            }else {
                result.put("status","fail");
                result.put("message","wrong password");
            }
        }else {
            result.put("status","fail");
            result.put("message","user does not exit");
        }

       return result;
    }

}
