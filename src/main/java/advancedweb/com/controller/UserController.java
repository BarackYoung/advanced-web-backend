package advancedweb.com.controller;


import advancedweb.com.service.JwtUserDetailsService;
import advancedweb.com.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@CrossOrigin
@RestController
public class UserController {
    Logger logger= LoggerFactory.getLogger(UserController.class);

    private UserService userService;


    @Autowired
    public UserController(UserService userService){
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String,String> request){
        logger.info("获得的请求："+request);
        return ResponseEntity.ok(userService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String,String> request){
        return ResponseEntity.ok(userService.login(request.get("username"),request.get("password")));
    }

    @PostMapping("/test")
    public ResponseEntity<?> test(){
        return ResponseEntity.ok("Hail Hydra!");
    }

    @PostMapping("/log")
    public ResponseEntity<?> log(@RequestBody Map<String,String> map){
        userService.log(map.get("log"));
        return ResponseEntity.ok("ok");
    }
    @GetMapping("/getLog")
    public ResponseEntity<?> getLog(){
         return ResponseEntity.ok(userService.getLog());
    }

    @PostMapping("/updatePoint")
    public ResponseEntity<?> update(HttpServletRequest request){


        Map<String,String> map = new HashMap<>();
        Enumeration<String> er = request.getParameterNames();
        while (er.hasMoreElements()) {
            String name = (String) er.nextElement();
            String value = request.getParameter(name);
            System.out.println(name+"---------"+value);
            map.put(name,value);
        }


        userService.updatePoint(map.get("identity"), Integer.parseInt(map.get("points")));
        return ResponseEntity.ok("ok");
    }

    @PostMapping("/getRankList")
    public ResponseEntity<?> getRankList(){
        return ResponseEntity.ok(userService.getRankList());
    }

}
