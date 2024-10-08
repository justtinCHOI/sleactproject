package com.unitekndt.mqnavigator.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class IChannel {
    private Long id;
    private String name;
    private Boolean isPrivate;
    private Long workspaceId;
}