package com.campus.trade.VO;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageVO {

    private Long id;
    private Long itemId;
    private Long fromUserId;
    private String fromNickName;// 发送者昵称
    private String fromAvatar;// 发送者头像
    private String content;
    private LocalDateTime createTime;

    private String itemTitle;// 商品标题
    private Integer isRead;//0未读1已读



}
