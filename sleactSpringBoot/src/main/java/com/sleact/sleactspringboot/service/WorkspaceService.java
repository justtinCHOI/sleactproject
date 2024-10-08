package com.unitekndt.mqnavigator.service;

import com.unitekndt.mqnavigator.dto.IChannel;
import com.unitekndt.mqnavigator.dto.IUser;
import com.unitekndt.mqnavigator.dto.IWorkspace;
import com.unitekndt.mqnavigator.dto.WorkspaceCreationRequest;
import com.unitekndt.mqnavigator.entity.Channel;
import com.unitekndt.mqnavigator.entity.User;
import com.unitekndt.mqnavigator.entity.Workspace;
import com.unitekndt.mqnavigator.repository.ChannelRepository;
import com.unitekndt.mqnavigator.repository.UserRepository;
import com.unitekndt.mqnavigator.repository.WorkspaceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class WorkspaceService {

    @Autowired
    private WorkspaceRepository workspaceRepository;
    @Autowired
    private ChannelService channelService;
    @Autowired
    private ChannelRepository channelRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;

    // Workspace -> IWorkspace로 변환
    public IWorkspace entityToDto(Workspace workspace) {
        return new IWorkspace(
                workspace.getId(),
                workspace.getName(),
                workspace.getUrl(),
                workspace.getOwner().getId()
        );
    }

    // 사용자 ID로 워크스페이스 목록을 조회하고, DTO로 변환하여 반환
    public List<IWorkspace> getWorkspacesByUserDto(Long userId) {
        List<Workspace> workspaces = workspaceRepository.findAllByMembersId(userId);
        return workspaces.stream()
                .map(this::entityToDto) // entityToDto() 메서드를 사용해 변환
                .collect(Collectors.toList());
    }

    // 워크스페이스 생성하고 DTO로 변환하여 반환
    public IWorkspace createWorkspaceDto(WorkspaceCreationRequest request, User currentUser) {
        Optional<Workspace> existingWorkspace = workspaceRepository.findByUrl(request.getUrl());
        if (existingWorkspace.isPresent()) {
            throw new RuntimeException("이미 사용 중인 URL입니다."); // 예외 처리
        }

        Workspace newWorkspace = new Workspace();
        newWorkspace.setName(request.getWorkspace());
        newWorkspace.setUrl(request.getUrl());
        newWorkspace.setOwner(currentUser);

        // 사용자를 멤버로 추가
        newWorkspace.getMembers().add(currentUser);

        // 워크스페이스 저장
        Workspace createdWorkspace = workspaceRepository.save(newWorkspace);

        // 기본 채널 생성
        Channel defaultChannel = new Channel();
        defaultChannel.setName("일반");
        defaultChannel.setWorkspace(createdWorkspace);
        defaultChannel.getMembers().add(currentUser);

        // 채널 저장
        channelService.createChannel(defaultChannel);

        // 저장된 워크스페이스를 DTO로 변환하여 반환
        return entityToDto(createdWorkspace);
    }

    // 특정 워크스페이스에 속한 사용자의 채널 목록을 조회하고, DTO로 변환하여 반환
    public List<IChannel> getUserChannelsDto(String workspaceUrl, Long userId) {
        Workspace workspace = workspaceRepository.findByUrl(workspaceUrl)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 워크스페이스입니다."));

        return workspace.getChannels().stream()
                .filter(channel -> channel.getMembers().stream()
                        .anyMatch(user -> user.getId().equals(userId)))
                .map(channelService::entityToDto) // channelService의 entityToDto 사용
                .collect(Collectors.toList());
    }

    // 워크스페이스 멤버 목록 조회 메서드
    public List<IUser> getWorkspaceMembers(String workspaceUrl) {
        // 1. 워크스페이스 조회
        Workspace workspace = workspaceRepository.findByUrl(workspaceUrl)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 워크스페이스입니다."));

        // 2. 멤버 엔티티를 IUser DTO로 변환하여 반환
        return workspace.getMembers().stream()
                .map(userService::entityToDto)
                .collect(Collectors.toList());
    }



    // 워크스페이스 멤버 초대 메서드
    public boolean inviteMemberToWorkspace(String workspaceUrl, String email) {
        // 1. 워크스페이스 조회
        Workspace workspace = workspaceRepository.findByUrl(workspaceUrl)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 워크스페이스입니다."));

        // 2. 사용자 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));

        // 3. 사용자 워크스페이스 멤버 추가
        workspace.getMembers().add(user);
        workspaceRepository.save(workspace);

        // 기본 채널에 사용자 추가
         Channel defaultChannel = workspace.getChannels().stream()
                 .filter(channel -> channel.getName().equals("일반"))
                 .findFirst()
                 .orElseThrow(() -> new RuntimeException("기본 채널이 없습니다."));
         defaultChannel.getMembers().add(user);

        return true;
    }

    // 워크스페이스 멤버 제거 메서드
    public boolean removeMemberFromWorkspace(String workspaceUrl, Long memberId) {
        // 1. 워크스페이스 조회
        Workspace workspace = workspaceRepository.findByUrl(workspaceUrl)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 워크스페이스입니다."));

        // 2. 멤버 조회
        User member = userRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 멤버입니다."));

        // 3. 워크스페이스에서 멤버 제거
        if (workspace.getMembers().contains(member)) {
            workspace.getMembers().remove(member);
            workspaceRepository.save(workspace); // 변경 사항 저장
            return true;
        }

        return false;
    }

}