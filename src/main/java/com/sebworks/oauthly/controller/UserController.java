package com.sebworks.oauthly.controller;

import com.auth0.jwt.JWTSigner;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.JWTVerifyException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sebworks.oauthly.common.RegistrationValidator;
import com.sebworks.oauthly.common.SessionDataAccessor;
import com.sebworks.oauthly.common.Utils;
import com.sebworks.oauthly.dto.RegistrationDto;
import com.sebworks.oauthly.entity.User;
import com.sebworks.oauthly.repository.UserRepository;
import com.sebworks.oauthly.service.MailService;
import com.sebworks.oauthly.service.TemplateService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.SignatureException;
import java.util.*;

/**
 * Created by Selim Eren Bekçe on 16.08.2017.
 */
@Controller
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private RegistrationValidator registrationValidator;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SessionDataAccessor sessionDataAccessor;
    @Autowired
    private TemplateService templateService;
    @Autowired
    private MailService mailService;

    /** In seconds */
    @Value("${jwt.expire.cookie}")
    private int expireCookie;
    @Value("${jwt.expire.resetCode}")
    private int expireResetCode;
    @Value("${jwt.secret}")
    private String jwtSecret;
    @Value("${recaptcha.secret}")
    private String recaptchaSecret;


    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String login(HttpServletRequest request, HttpSession session, Model model) {
        if(sessionDataAccessor.access().getUserId() != null){
            String redir = (String) session.getAttribute("redir");
            if(redir == null) redir = "/";
            return "redirect:"+redir;
        }
        String csrf_token = UUID.randomUUID().toString().replace("-", "");
        session.setAttribute("csrf_token", csrf_token);
        model.addAttribute("csrf_token", csrf_token);
        return "login";
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public String login(HttpServletRequest request, HttpServletResponse response, HttpSession session, Model model,
                        RedirectAttributes redirectAttributes,
                        @RequestParam(value = "csrf_token") String csrf_token,
                        @RequestParam(value = "username") String username,
                        @RequestParam(value = "password") String password) {

        String csrf_token1 = (String) session.getAttribute("csrf_token");
        session.removeAttribute("csrf_token");
        if(csrf_token1 == null || !csrf_token1.equals(csrf_token)){
            redirectAttributes.addFlashAttribute("error", "Request failed");
            return "redirect:login";
        }
        User user = findUser(username);
        if(user == null || !user.checkPassword(password)){
            redirectAttributes.addFlashAttribute("error", "Invalid login");
            return "redirect:login";
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

    private User findUser(@RequestParam(value = "username") String username) {
        String normalizedUsername = Utils.normalizeUsername(username);
        User user = userRepository.findByUsernameNormalized(normalizedUsername);
        if(user == null){
            user = userRepository.findByEmail(username.toLowerCase(Locale.ENGLISH));
        }
        return user;
    }

    @RequestMapping(value = "/logout", method = {RequestMethod.GET, RequestMethod.POST})
    public String logout(HttpSession session, HttpServletRequest request, HttpServletResponse response,
                         @RequestHeader(value = "Referer", required = false) String referer) {
        Cookie[] cookies = request.getCookies();
        if(cookies != null) for (Cookie cookie : cookies) {
            if("ltat".equals(cookie.getName())){
                cookie.setMaxAge(0);
                response.addCookie(cookie);
            }
        }

        sessionDataAccessor.access().setUserId(null);
        session.invalidate();
        if(referer != null){
            return "redirect:"+referer;
        }
        return "redirect:/login";
    }

    @RequestMapping(value = "/register", method = RequestMethod.GET)
    public String register(Model model) {
        model.addAttribute("dto", new RegistrationDto());
        return "register";
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public String register(@ModelAttribute("dto") @Valid RegistrationDto dto, BindingResult bindingResult, HttpSession session,
                           HttpServletRequest request, @RequestParam("g-recaptcha-response") String g_recaptcha_response) {

        if (!checkRecaptcha(request.getRemoteHost(), g_recaptcha_response)){
            log.info("recaptcha failed, g_recaptcha_response={}", g_recaptcha_response);
            bindingResult.addError(new ObjectError("g-recaptcha-response", "Invalid captcha"));
            return "register";
        }

        registrationValidator.validate(dto, bindingResult);

        if (bindingResult.hasErrors()) {
            return "register";
        }

        User user = new User();
        user.setId(UUID.randomUUID().toString().replace("-",""));
        user.setEmail(dto.getEmail());
        user.setUsername(dto.getUsername());
        user.setUsernameNormalized(dto.getUsernameNormalized());
        user.setCreationTime(System.currentTimeMillis());
        user.encryptThenSetPassword(dto.getPassword());
        if(userRepository.count() == 0)
            user.setAdmin(true);
        user = userRepository.save(user);

        sessionDataAccessor.access().setUserId(user.getId());

        String redir = (String) session.getAttribute("redir");
        if(redir == null) redir = "/";
        return "redirect:"+redir;
    }

    @GetMapping("reset-password")
    public String resetPassword(Model model, @RequestParam(required = false) String reset_code){

        if(StringUtils.isNotBlank(reset_code)){
            User user = validateResetCode(reset_code);
            if(user != null) {
                model.addAttribute("step", 3);
                model.addAttribute("reset_code", reset_code);
                model.addAttribute("username", user.getUsername());
            }
            else {
                model.addAttribute("step", 1);
                model.addAttribute("error", "Invalid reset code");
            }
        } else {
            model.addAttribute("step", 1);
        }
        return "reset-password";
    }
    @PostMapping("reset-password")
    public String resetPassword(Model model,
                                @ModelAttribute("step") Integer step,
                                @RequestParam(required = false) String username,
                                @RequestParam(required = false) String newPassword,
                                @RequestParam(required = false) String newPassword2,
                                @RequestParam(required = false) String reset_code,
                                HttpServletRequest request, @RequestParam("g-recaptcha-response") String g_recaptcha_response,
                                HttpSession session, RedirectAttributes redirectAttributes){

        if (!checkRecaptcha(request.getRemoteHost(), g_recaptcha_response)){
            log.info("recaptcha failed, g_recaptcha_response={}", g_recaptcha_response);
            redirectAttributes.addFlashAttribute("error", "Invalid captcha");
            return "redirect:reset-password" + (reset_code != null ? "?reset_code="+reset_code : "");
        }

        if(step == 1){
            User user = findUser(username);
            if(user == null){
                model.addAttribute("error", "Given user was not found.");
                return "reset-password";
            }

            String prepareResetCode = prepareResetCode(user);
            log.info("Sending reset-password email. reset_code="+prepareResetCode);
            String content = templateService.getResetPasswordTemplate(request, user, prepareResetCode);
            mailService.sendEmail(user.getEmail(), "Reset your password", content);

            model.addAttribute("info", "Your password reset link was just sent to your email address. Please check your inbox and click on the provided link to continue.");
            model.addAttribute("step", 2);
            return "reset-password";
        }

        if(step == 3){
            User user = validateResetCode(reset_code);
            if(user == null){
                redirectAttributes.addFlashAttribute("step", 1);
                redirectAttributes.addFlashAttribute("error", "Invalid reset code");
            } else {
                if(StringUtils.isAnyBlank(newPassword, newPassword2)){
                    redirectAttributes.addFlashAttribute("error", "All fields are required");
                } else if(Utils.newPasswordCheck(newPassword, newPassword2) != null){
                    redirectAttributes.addFlashAttribute("error", Utils.newPasswordCheck(newPassword, newPassword2));
                } else {
                    // pass
                    user.encryptThenSetPassword(newPassword);
                    userRepository.save(user);
                    redirectAttributes.addFlashAttribute("success", "Your password was reset! Please login to continue");
                    return "redirect:/login";
                }
            }
            return "redirect:reset-password?reset_code="+reset_code;
        }

        return "error";
    }


    private String prepareResetCode(User user) {
        int hash = Objects.hash(user.getUsername(), user.getEmail(), user.getPassword(), user.getLastUpdateTime());
        final long iat = System.currentTimeMillis() / 1000L; // issued at claim
        final long exp = iat + expireResetCode; // expires claim
        final JWTSigner signer = new JWTSigner(jwtSecret);

        final HashMap<String, Object> claims = new HashMap<>();
        claims.put("vt", 5); //type=reset_code
        claims.put("exp", exp);
        claims.put("hash", hash);
        claims.put("user", user.getId());

        return signer.sign(claims);
    }

    public User validateResetCode(String reset_code){
        if(reset_code == null)
            return null;
        try {
            final JWTVerifier verifier = new JWTVerifier(jwtSecret);
            final Map<String,Object> claims = verifier.verify(reset_code);
            // first check version
            int type = (int) claims.get("vt");
            if(type != 5){
                return null;
            }
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
            int correctHash = Objects.hash(user.getUsername(), user.getEmail(), user.getPassword(), user.getLastUpdateTime());
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


    private String prepareCookie(User user) {
        int hash = Objects.hash(user.getUsername(), user.getEmail(), user.getPassword());
        final long iat = System.currentTimeMillis() / 1000L; // issued at claim
        final long exp = iat + expireCookie; // expires claim
        final JWTSigner signer = new JWTSigner(jwtSecret);

        final HashMap<String, Object> claims = new HashMap<>();
        claims.put("vt", 4); //type=cookie_ltat
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
            // first check version
            int type = (int) claims.get("vt");
            if(type != 4){
                return null;
            }
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

    private boolean checkRecaptcha(String ipAddress, String response){
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map= new LinkedMultiValueMap<>();
        map.add("response", response);
        map.add("secret", recaptchaSecret);
        map.add("remoteip", ipAddress);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        ResponseEntity<String> responseEntity = restTemplate.postForEntity( "https://www.google.com/recaptcha/api/siteverify", request , String.class );
        if(responseEntity.getStatusCodeValue() == 200){
            try {
                JsonNode jsonNode = objectMapper.readValue(responseEntity.getBody(), JsonNode.class);
                return jsonNode.get("success").asBoolean();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
        return false;
    }

}
