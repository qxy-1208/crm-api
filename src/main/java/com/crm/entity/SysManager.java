package com.crm.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 系统管理员表实体类
 * 存储系统管理员账号、基本信息及状态数据
 *
 * @author alani
 * @since 生成时间由MyBatis-Plus自动维护
 */
@Getter
@Setter
@TableName("sys_manager")
@ApiModel(value = "SysManager对象", description = "系统管理员信息")
public class SysManager {

    @ApiModelProperty(value = "主键ID", example = "1")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "登录账号（唯一）", example = "admin")
    @TableField("account")
    private String account;

    @ApiModelProperty(value = "管理员昵称", example = "系统管理员")
    @TableField("nickname")
    private String nickname;

    @ApiModelProperty(value = "登录密码（加密存储）", example = "e10adc3949ba59abbe56e057f20f883e")
    @TableField("password")
    private String password;

    @ApiModelProperty(value = "账号状态：0-禁用，1-正常", example = "1")
    @TableField("status")
    private Integer status;

    @ApiModelProperty(value = "逻辑删除标识：0-未删除，1-已删除", example = "0")
    @TableField(value = "delete_flag", fill = FieldFill.INSERT)
    @TableLogic
    private Integer deleteFlag;

    @ApiModelProperty(value = "创建时间", example = "2025-10-12 09:00:00")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间", example = "2025-10-12 16:30:00")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "所属部门ID", example = "3")
    @TableField(value = "depart_id")
    private Integer departId;

    @ApiModelProperty(value = "邮箱地址", example = "admin@example.com")
    @TableField("email")
    private String email;
}