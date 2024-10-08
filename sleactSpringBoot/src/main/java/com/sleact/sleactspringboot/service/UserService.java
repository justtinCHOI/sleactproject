package com.unitekndt.mqnavigator.service;

import com.unitekndt.mqnavigator.config.CustomSecurityConfig;
import com.unitekndt.mqnavigator.dto.IUser;
import com.unitekndt.mqnavigator.dto.IUserWithOnline;
import com.unitekndt.mqnavigator.dto.IWorkspace;
import com.unitekndt.mqnavigator.dto.UserRegistrationRequest;
import com.unitekndt.mqnavigator.entity.Channel;
import com.unitekndt.mqnavigator.entity.User;
import com.unitekndt.mqnavigator.entity.Workspace;
import com.unitekndt.mqnavigator.repository.ChannelRepository;
import com.unitekndt.mqnavigator.repository.UserRepository;
import com.unitekndt.mqnavigator.repository.WorkspaceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private ChannelRepository channelRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomSecurityConfig customSecurityConfig;


    public IUser entityToDto(User user) {
        List<IWorkspace> workspaces = user.getWorkspaces().stream()
                .map(workspace -> new IWorkspace(
                        workspace.getId(),
                        workspace.getName(),
                        workspace.getUrl(),
                        workspace.getOwner().getId()
                )).collect(Collectors.toList());

        return new IUser(
                user.getId(),
                user.getNickname(),
                user.getEmail(),
                workspaces
        );
    }

    public IUserWithOnline entityToDtoWithOnline(User user, boolean online) {
        IUser basicUser = entityToDto(user);
        return new IUserWithOnline(
                basicUser.getId(),
                basicUser.getNickname(),
                basicUser.getEmail(),
                basicUser.getWorkspaces(),
                online
        );
    }

    // 특정 워크스페이스의 특정 사용자 정보 조회 메서드
    public IUser getUserInWorkspace(String workspaceUrl, Long userId) {
        // 1. 워크스페이스 조회
        Workspace workspace = workspaceRepository.findByUrl(workspaceUrl)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 워크스페이스입니다."));

        // 2. 사용자 조회 및 워크스페이스 멤버 확인
        User user = userRepository.findById(userId)
                .filter(u -> u.getWorkspaces().contains(workspace))
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));

        // 3. IUser DTO로 변환 후 반환
        return entityToDto(user);
    }

    // 사용자 회원가입 메서드
    public boolean registerUser(UserRegistrationRequest request) {
        // 1. 기존 사용자 중복 확인
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return false; // 이미 사용 중인 이메일인 경우
        }

        // 2. 비밀번호 암호화
        String hashedPassword = customSecurityConfig.passwordEncoder().encode(request.getPassword());

        // 3. 사용자 생성
        User user = new User();
        user.setEmail(request.getEmail());
        user.setNickname(request.getNickname());
        user.setPassword(hashedPassword);
        userRepository.save(user);

        // 4. 기본 워크스페이스 및 채널에 사용자 추가
        Workspace defaultWorkspace = workspaceRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("기본 워크스페이스를 찾을 수 없습니다."));
        Channel defaultChannel = channelRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("기본 채널을 찾을 수 없습니다."));

        defaultWorkspace.getMembers().add(user);
        defaultChannel.getMembers().add(user);

        workspaceRepository.save(defaultWorkspace);
        channelRepository.save(defaultChannel);

        return true;
    }

}