package advancedweb.com.service;

import advancedweb.com.Entity.User;
import advancedweb.com.dao.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class JwtUserDetailsService implements UserDetailsService {
    Logger logger= LoggerFactory.getLogger(JwtUserDetailsService.class);

    private UserRepository userRepository;

    @Autowired
    public JwtUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //  may be some problems?  TODO: Implement the function.
        User user=userRepository.findByUsername(username);
        if (user==null){
            throw new UsernameNotFoundException("User: '" + username + "' not found.");
        }
        else{
            if (logger.isDebugEnabled()){
                logger.debug("loadByUsername被调用 user信息 "+user.toString());
            }
            //返回一个新的user对象
            return new User(user.getUsername(),user.getPassword());
        }

    }
}
