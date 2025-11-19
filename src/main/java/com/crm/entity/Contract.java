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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 合同表实体类
 * 存储合同基本信息、金额、状态等核心数据
 *
 * @author crm
 * @since 2025-10-12
 */
@Getter
@Setter
@TableName("t_contract")
@ApiModel(value = "Contract对象", description = "合同信息实体类")
public class Contract {

    @ApiModelProperty(value = "主键id", example = "1")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "合同编号（唯一标识）", example = "HT20251012001")
    @TableField("number")
    private String number;

    @ApiModelProperty(value = "合同名称", example = "年度服务合作协议")
    @TableField("name")
    private String name;

    @ApiModelProperty(value = "合同总金额", example = "100000.00")
    @TableField("amount")
    private BigDecimal amount;

    @ApiModelProperty(value = "已收到款项金额", example = "50000.00")
    @TableField("received_amount")
    private BigDecimal receivedAmount;

    @ApiModelProperty(value = "签约日期", example = "2025-10-12")
    @TableField("sign_time")
    private LocalDate signTime;

    @ApiModelProperty(value = "关联客户ID", example = "1001")
    @TableField("customer_id")
    private Integer customerId;

    @ApiModelProperty(value = "关联商机ID", example = "2001")
    @TableField("opportunity_id")
    private Integer opportunityId;

    @ApiModelProperty(value = "合同状态：0-初始化，1-审核通过，2-审核未通过", example = "1")
    @TableField("status")
    private Integer status;

    @ApiModelProperty(value = "合同备注信息", example = "需在2025年12月前完成首次服务")
    @TableField("remark")
    private String remark;

    @ApiModelProperty(value = "逻辑删除标识：0-未删除，1-已删除", example = "0")
    @TableField(value = "delete_flag", fill = FieldFill.INSERT)
    @TableLogic
    private Integer deleteFlag;

    @ApiModelProperty(value = "记录创建时间", example = "2025-10-12 09:30:00")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @ApiModelProperty(value = "记录更新时间", example = "2025-10-12 15:45:00")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "创建人ID", example = "3001")
    @TableField("creater_id")
    private Integer createrId;

    @ApiModelProperty(value = "签约人ID", example = "3002")
    @TableField("owner_id")
    private Integer ownerId;

    @ApiModelProperty(value = "合同开始时间", example = "2025-11-01")
    @TableField("start_time")
    private LocalDate startTime;

    @ApiModelProperty(value = "合同结束时间", example = "2026-10-31")
    @TableField("end_time")
    private LocalDate endTime;
}