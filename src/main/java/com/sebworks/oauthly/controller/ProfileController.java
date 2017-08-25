package com.sebworks.oauthly.controller;

import com.sebworks.oauthly.common.SessionDataAccessor;
import com.sebworks.oauthly.dto.MeDto;
import com.sebworks.oauthly.entity.Client;
import com.sebworks.oauthly.entity.Grant;
import com.sebworks.oauthly.entity.User;
import com.sebworks.oauthly.repository.ClientRepository;
import com.sebworks.oauthly.repository.UserRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

/**
 * Created by Selim Eren Bekçe on 25.08.2017.
 */
@Controller
public class ProfileController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private SessionDataAccessor sessionDataAccessor;
    @Autowired
    private DiscourseController discourseController;

    /** In seconds */
    @Value("${jwt.expire.cookie}")
    private int expireCookie;
    @Value("${jwt.secret}")
    private String jwtSecret;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String index() {
        return "redirect:/profile";
    }

    @ModelAttribute
    public void populateModel(Model model){
        String userId = sessionDataAccessor.access().getUserId();
        if(userId == null) {
            return;
        }
        User user = userRepository.findOne(userId);
        if(user == null){
            return;
        }
        model.addAttribute("username", user.getUsername());
        model.addAttribute("email", user.getEmail());
        model.addAttribute("canCreateClients", user.isAdmin());
        model.addAttribute("isAdmin", user.isAdmin());
        if(user.isAdmin()){
            model.addAttribute("clients", clientRepository.findByOwnerId(user.getId()));
            model.addAttribute("discourse", discourseController.getDto());
        }
    }

    @GetMapping("/profile")
    public String profile() {
        return "profile";
    }

    @PostMapping("/profile/password")
    public String changePassword(String oldPassword, String newPassword, String newPassword2,
                                 RedirectAttributes redirectAttributes){
        User user = userRepository.findOne(sessionDataAccessor.access().getUserId());
        if(StringUtils.isAnyBlank(oldPassword, newPassword, newPassword2)){
            redirectAttributes.addFlashAttribute("error", "All fields are required");
        }
        else if(!user.checkPassword(oldPassword)){
            redirectAttributes.addFlashAttribute("error", "Current password is invalid");
        }
        else if (newPassword.length() < 4 || newPassword.length() > 32) {
            redirectAttributes.addFlashAttribute("error", "Please use between 4 and 32");
        }
        else if (!newPassword.equals(newPassword2)) {
            redirectAttributes.addFlashAttribute("error", "These passwords don't match");
        }
        else {
            user.encryptThenSetPassword(newPassword);
            userRepository.save(user);
            redirectAttributes.addFlashAttribute("success", "Success! You have changed your password.");
        }
        return "redirect:/profile";
    }

    @RequestMapping(value = "/api/me", method = RequestMethod.GET)
    public @ResponseBody MeDto me(@RequestAttribute("grant") Grant grant) {
        User user = userRepository.findOne(grant.getUserId());
        MeDto dto = new MeDto();
        dto.setName(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setId(user.getId());
        return dto;
    }

    @PostMapping("/profile/client")
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
        return "redirect:/profile";
    }

}
