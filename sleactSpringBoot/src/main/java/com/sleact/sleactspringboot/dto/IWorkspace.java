package com.unitekndt.mqnavigator.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class IWorkspace {
    private Long id;
    private String name;
    private String url;
    private Long ownerId;
}