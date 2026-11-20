package com.campus.trade.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {

    /** 主键，交给数据库自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    /** 密码永远不返回给前端 */
    @JsonIgnore
    private String password;

    private String nickname;
    private String avatar;
    private String phone;
    private String school;

    /** 0=用户 1=管理员 */
    private Integer role;

    /** 1=正常 0=禁用 */
    private Integer status;

    /** 逻辑删除标记 */
    @TableLogic
    private Integer deleted;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
