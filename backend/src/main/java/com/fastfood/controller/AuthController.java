package com.fastfood.controller;

import com.fastfood.dto.request.LoginRequest;
import com.fastfood.entity.system.User;
import com.fastfood.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        
        User user = userRepository.findByUsername(loginRequest.getUsername());

        // Test chay: So sánh trực tiếp chữ nhập vào với chữ lưu trong Database
        if (user != null && user.getPasswordHash().equals(loginRequest.getPassword())) {
            
            HttpSession session = request.getSession(true);
            session.setAttribute("USER_INFO", user);

            Map<String, Object> response = new HashMap<>();
            response.put("username", user.getUsername());
            response.put("fullName", user.getFullName());
            response.put("role", user.getRole().getRoleName()); 

            return ResponseEntity.ok(response);
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Sai tài khoản hoặc mật khẩu!");
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate(); 
        }
        return ResponseEntity.ok("Đăng xuất thành công");
    }
}