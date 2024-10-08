package com.unitekndt.mqnavigator.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class IUser {
    private Long id;
    private String nickname;
    private String email;
    private List<IWorkspace> workspaces;
}
