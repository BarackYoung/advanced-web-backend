package advancedweb.com.service;


import advancedweb.com.Entity.Answerlogs;
import advancedweb.com.Entity.Log;
import advancedweb.com.Entity.User;
import advancedweb.com.dao.AnsLogsRepository;
import advancedweb.com.dao.LogRepository;
import advancedweb.com.dao.UserRepository;
import advancedweb.com.security.jwt.JwtTokenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class UserService {

    Logger logger= LoggerFactory.getLogger(UserService.class);
    private UserRepository userRepository;

    private JwtTokenUtil jwtTokenUtil;

    private AuthenticationManager authenticationManager;

    private LogRepository logRepository;

    private AnsLogsRepository ansLogsRepository;

    @Autowired
    public UserService(UserRepository userRepository,JwtTokenUtil jwtTokenUtil,AuthenticationManager authenticationManager,LogRepository logRepository, AnsLogsRepository ansLogsRepository){
      this.userRepository = userRepository;
      this.jwtTokenUtil = jwtTokenUtil;
      this.authenticationManager = authenticationManager;
      this.logRepository = logRepository;
      this.ansLogsRepository = ansLogsRepository;
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
        User user = userRepository.findByUsername(username);
        if (user!=null){
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            if (encoder.matches(password,user.getPassword())){
                UsernamePasswordAuthenticationToken uptoken = new UsernamePasswordAuthenticationToken(username,password);
                Authentication authentication = authenticationManager.authenticate(uptoken);
                SecurityContextHolder.getContext().setAuthentication(authentication);
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

    /**
     * 记录用户行为
     * */
    public void log(String log){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = simpleDateFormat.format(new Date(System.currentTimeMillis()));
        UserDetails user = (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = user.getUsername();
        Log myLog = new Log(username,time,log);
        logRepository.save(myLog);
    }
    /**
     * 获取行为记录
     * */
    public List<Log> getLog(){
        UserDetails user = (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = user.getUsername();
        return logRepository.getAllByUsername(username);
    }

    /**
     * 更新用户成绩
     */

    public void updatePoint(String username, int point){
        Answerlogs answerlogs = ansLogsRepository.findByUsername(username);
        if(answerlogs == null){
            Answerlogs answerlogs1 = new Answerlogs();
            answerlogs1.setUsername(username);
            answerlogs1.setHighest(point);
            ansLogsRepository.save(answerlogs1);
        }else {
            if(answerlogs.getHighest() < point){
                ansLogsRepository.updatePoint(answerlogs.getId(),point);
            }
        }
    }

    /**
     * 获得排行榜
     */
    public List<Map<String,Integer>> getRankList(){
        List<Answerlogs> list = ansLogsRepository.getAllByDESC();
        List<Map<String, Integer>> ans = new ArrayList<>();
        for(Answerlogs answerlogs:list){
            Map<String,Integer> map = new HashMap<>();
            map.put(answerlogs.getUsername(),answerlogs.getHighest());
            ans.add(map);
        }
        return ans;
    }


}
