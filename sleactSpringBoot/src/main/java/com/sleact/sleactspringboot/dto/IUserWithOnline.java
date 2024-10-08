package com.unitekndt.mqnavigator.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class IUserWithOnline extends IUser {
    private boolean online;

    public IUserWithOnline(Long id, String nickname, String email, List<IWorkspace> workspaces, boolean online) {
        super(id, nickname, email, workspaces);
        this.online = online;
    }
}
