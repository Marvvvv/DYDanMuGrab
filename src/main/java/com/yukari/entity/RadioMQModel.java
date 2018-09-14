package com.yukari.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;


@Getter
@Setter
public class RadioMQModel implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id; // 数据库id
    private Integer radioType; // 1:礼物广播 2：贵族开通广播
    private String gift_name;
    private String gift_src;
    private Integer roomId; // 房间号
    private String achorName; // 主播昵称
    private String giveName; // 送礼人
    private String date; // 时间戳(计算宝箱开启时间)


}
