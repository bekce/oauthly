package com.sebworks.oauthly.controller;

import com.auth0.jwt.JWTSigner;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.JWTVerifyException;
import com.sebworks.oauthly.common.RegistrationValidator;
import com.sebworks.oauthly.common.SessionDataAccessor;
import com.sebworks.oauthly.dto.MeDto;
import com.sebworks.oauthly.dto.RegistrationDto;
import com.sebworks.oauthly.entity.Client;
import com.sebworks.oauthly.entity.User;
import com.sebworks.oauthly.repository.ClientRepository;
import com.sebworks.oauthly.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.security.InvalidKeyException;
import java.security.SignatureException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Created by Selim Eren Bekçe on 16.08.2017.
 */
@Controller
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private RegistrationValidator registrationValidator;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private SessionDataAccessor sessionDataAccessor;

    /** In seconds */
    @Value("${expire.cookie}")
    private int expireCookie;
    @Value("${oauth.server.jwt.secret}")
    private String jwtSecret;

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public String login(HttpServletRequest request, HttpServletResponse response, HttpSession session, Model model,
                        @RequestParam(value = "csrf_token") String csrf_token,
                        @RequestParam(value = "username") String username,
                        @RequestParam(value = "password") String password) {

        String csrf_token1 = (String) session.getAttribute("csrf_token");
        session.removeAttribute("csrf_token");
        if(csrf_token1 == null || !csrf_token1.equals(csrf_token)){
            model.addAttribute("error", "Request failed");
            return login(request, session, model);
        }
        User user = userRepository.findByUsername(username);
        if(user == null){
            user = userRepository.findByEmail(username);
        }
        if(user == null || !user.checkPassword(password)){
            model.addAttribute("error", "Invalid login");
            return login(request, session, model);
        }
        sessionDataAccessor.access().setUserId(user.getId());

        String cookie_value = prepareCookie(user);
        Cookie cookie = new Cookie("ltat", cookie_value);
        cookie.setHttpOnly(true);
        cookie.setMaxAge(expireCookie);
        cookie.setPath("/");
        response.addCookie(cookie);

        String redir = (String) session.getAttribute("redir");
        if(redir == null) redir = "/";
        return "redirect:"+redir;
    }

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String login(HttpServletRequest request, HttpSession session, Model model) {
        if(sessionDataAccessor.access().getUserId() != null){
            String redir = (String) session.getAttribute("redir");
            if(redir == null) redir = "/";
            return "redirect:"+redir;
        }

        String csrf_token = UUID.randomUUID().toString().replace("-", "");
        session.setAttribute("csrf_token", csrf_token);

        model.addAttribute("message", "");
        if(!model.containsAttribute("error"))
            model.addAttribute("error", "");
        model.addAttribute("csrf_token", csrf_token);

        return "login";
    }

    @RequestMapping(value = "/logout", method = {RequestMethod.GET, RequestMethod.POST})
    public String logout(HttpSession session, HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if(cookies != null) for (Cookie cookie : cookies) {
            if("ltat".equals(cookie.getName())){
                cookie.setMaxAge(0);
                response.addCookie(cookie);
            }
        }

        sessionDataAccessor.access().setUserId(null);
        session.invalidate();
        return "redirect:/login";
    }

    @RequestMapping(value = "/register", method = RequestMethod.GET)
    public String register(Model model) {
        model.addAttribute("dto", new RegistrationDto());
        return "register";
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public String register(@ModelAttribute("dto") @Valid RegistrationDto dto, BindingResult bindingResult,
                           Model model, HttpSession session) {
        registrationValidator.validate(dto, bindingResult);

        if (bindingResult.hasErrors()) {
            return "register";
        }

        User user = new User();
        user.setId(UUID.randomUUID().toString().replace("-",""));
        user.setEmail(dto.getEmail());
        user.setUsername(dto.getUsername());
        user.encryptThenSetPassword(dto.getPassword());
        if(userRepository.count() == 0)
            user.setAdmin(true);
        user = userRepository.save(user);

        sessionDataAccessor.access().setUserId(user.getId());

        String redir = (String) session.getAttribute("redir");
        if(redir == null) redir = "/";
        return "redirect:"+redir;
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String index() {
        return "redirect:/profile";
    }

    @RequestMapping(value = "/profile", method = RequestMethod.GET)
    public String profile(Model model) {
        User user = userRepository.findOne(sessionDataAccessor.access().getUserId());
        model.addAttribute("username", user.getUsername());
        model.addAttribute("email", user.getEmail());
        model.addAttribute("canCreateClients", user.isAdmin());
        if(user.isAdmin()){
            model.addAttribute("clients", clientRepository.findByOwnerId(user.getId()));
        }
        return "profile";
    }

    @RequestMapping(value = "/api/me", method = RequestMethod.GET)
    public @ResponseBody MeDto me(){
        User user = userRepository.findOne(sessionDataAccessor.access().getUserId());
        MeDto dto = new MeDto();
        dto.setName(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setId(user.getId());
        return dto;
    }

    @PostMapping("/client")
    public String addUpdateClient(@RequestParam(required = false) String id, String name, String redirectUri){
        User user = userRepository.findOne(sessionDataAccessor.access().getUserId());
        if(!user.isAdmin()){
            return "error";
        }
        if(id != null){
            Client client = clientRepository.findOne(id);
            if(!client.getOwnerId().equals(user.getId())){
                return "error";
            }
            client.setName(name);
            client.setRedirectUri(redirectUri);
            clientRepository.save(client);
        } else {
            Client client = new Client();
            client.setId(UUID.randomUUID().toString().replace("-",""));
            client.setSecret(UUID.randomUUID().toString().replace("-",""));
            client.setName(name);
            client.setRedirectUri(redirectUri);
            client.setOwnerId(user.getId());
            clientRepository.save(client);
        }
        return "redirect:profile";
    }

    private String prepareCookie(User user) {
        int hash = Objects.hash(user.getUsername(), user.getEmail(), user.getPassword());
        final long iat = System.currentTimeMillis() / 1000L; // issued at claim
        final long exp = iat + expireCookie; // expires claim
        final JWTSigner signer = new JWTSigner(jwtSecret);

        final HashMap<String, Object> claims = new HashMap<>();
        claims.put("exp", exp);
        claims.put("hash", hash);
        claims.put("user", user.getId());

        return signer.sign(claims);
    }

    public User validateCookie(String cookie_value){
        if(cookie_value == null)
            return null;
        try {
            final JWTVerifier verifier = new JWTVerifier(jwtSecret);
            final Map<String,Object> claims = verifier.verify(cookie_value);
            // check expiry date
            int exp = (int) claims.get("exp");
            if(exp < (System.currentTimeMillis() / 1000L))
                return null;
            // check user
            String user_id = (String) claims.get("user");
            User user = userRepository.findOne(user_id);
            if(user == null){
                return null;
            }
            // check hash value
            int receivedHash = (int) claims.get("hash");
            int correctHash = Objects.hash(user.getUsername(), user.getEmail(), user.getPassword());
            if(receivedHash != correctHash) {
                return null;
            }
            return user;
        } catch (JWTVerifyException | SignatureException | InvalidKeyException | NullPointerException e) {
            return null;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

}
