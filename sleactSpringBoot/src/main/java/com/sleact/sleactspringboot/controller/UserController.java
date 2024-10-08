package com.unitekndt.mqnavigator.controller;

import com.unitekndt.mqnavigator.dto.IUser;
import com.unitekndt.mqnavigator.dto.UserRegistrationRequest;
import com.unitekndt.mqnavigator.entity.User;
import com.unitekndt.mqnavigator.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    // 현재 로그인된 사용자 정보 조회
    @GetMapping("/users")
    public ResponseEntity<Object> getCurrentUser(@AuthenticationPrincipal User currentUser) {
        // 사용자가 로그인되어 있으면 사용자 정보를 반환하고, 그렇지 않으면 false 반환
        if (currentUser != null) {
            return ResponseEntity.ok(new IUser(currentUser.getId(), currentUser.getNickname(), currentUser.getEmail(), new ArrayList<>()));
        } else {
            return ResponseEntity.ok(false);
        }
    }

    // 특정 워크스페이스의 특정 사용자 정보 조회
    @GetMapping("/{workspaceUrl}/users/{userId}")
    public ResponseEntity<IUser> getUserInWorkspace(
            @PathVariable String workspaceUrl,  // 워크스페이스 URL
            @PathVariable Long userId           // 사용자 ID
    ) {
        IUser userDto = userService.getUserInWorkspace(workspaceUrl, userId);

        if (userDto != null) {
            return ResponseEntity.ok(userDto);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    // 사용자 회원가입
    @PostMapping("/users")
    public ResponseEntity<String> registerUser(@RequestBody UserRegistrationRequest request) {
        boolean isUserCreated = userService.registerUser(request);

        if (isUserCreated) {
            return ResponseEntity.status(HttpStatus.CREATED).body("ok");
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("이미 사용 중인 아이디입니다.");
        }
    }

}
