package advancedweb.com.security.jwt;

import advancedweb.com.service.JwtUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    public static ConcurrentHashMap<String,String> tokenMap = new ConcurrentHashMap<>();

    private JwtUserDetailsService jwtUserDetailsService;
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    public JwtRequestFilter(JwtUserDetailsService jwtUserDetailsService,JwtTokenUtil jwtTokenUtil){
        this.jwtUserDetailsService = jwtUserDetailsService;
        this.jwtTokenUtil=jwtTokenUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // maybe some problem? TODO: Implement the filter.
        String authHeader=request.getHeader("Authorization");
//        logger.info("得到的头部："+authHeader);
        String tokenHead="Bearer ";

        if(authHeader==null) {
            // websocket连接时，令牌放在url参数上，以后重构
            String url = request.getRequestURL().toString();
            logger.info("获取到的url:"+url);
            String[] urls = url.split("/");
            if (urls[urls.length-1].startsWith("token")){
             authHeader = tokenHead+urls[urls.length-1].substring(6);
            }
        }

        if (authHeader!=null&&authHeader.startsWith(tokenHead)){
            String authToken=authHeader.substring(tokenHead.length());
            String username=jwtTokenUtil.getUsernameFromToken(authToken);
            if (username!=null&& SecurityContextHolder.getContext().getAuthentication()==null){
                UserDetails userDetails=this.jwtUserDetailsService.loadUserByUsername(username);
                if(jwtTokenUtil.validateToken(authToken,userDetails)){
                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken=
                            new UsernamePasswordAuthenticationToken(userDetails,
                                    null,userDetails.getAuthorities());
                    usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                    tokenMap.put(authToken,username);
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}

