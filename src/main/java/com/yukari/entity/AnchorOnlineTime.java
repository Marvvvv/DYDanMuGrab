package com.yukari.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class AnchorOnlineTime implements Serializable {

    private Integer id;
    private Integer room_id;
    private String onlineTime;
    private String offlineTime;
    private String online_length;
    private Integer online_status;

}

