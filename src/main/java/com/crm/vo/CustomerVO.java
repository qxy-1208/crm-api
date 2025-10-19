package com.crm.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.crm.convert.CustomerLevelConverter;
import com.crm.convert.CustomerSourceConverter;
import com.crm.convert.FollowUpStatusConverter;
import com.crm.convert.GenderConverter;
import com.crm.convert.IsKeyDecisionMakerConverter;
import com.crm.utils.DateUtils;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Email;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 客户信息VO类
 *
 * @author QXY
 */
@Data
@ExcelIgnoreUnannotated
@ContentRowHeight(105)
@ColumnWidth(24)
public class CustomerVO {

    @Schema(description = "主键")
    private Integer id;

    @Schema(description = "客户名称")
    @NotBlank(message = "客户名称不能为空")
    @ExcelProperty("客户名称")
    private String name;

    @Schema(description = "手机号")
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @ExcelProperty("手机号")
    private String phone;

    @Schema(description = "邮箱")
    @Email(message = "邮箱格式不正确")
    @ExcelProperty("邮箱")
    private String email;

    @Schema(description = "客户级别（1-一级客户，2-二级客户，3-三级客户）")
    @NotNull(message = "客户级别不能为空")
    @ExcelProperty(value = "客户级别", converter = CustomerLevelConverter.class)
    private Integer level;

    @Schema(description = "客户来源（具体值根据业务定义）")
    @NotNull(message = "客户来源不能为空")
    @ExcelProperty(value = "客户来源", converter = CustomerSourceConverter.class)
    private Integer source;

    @Schema(description = "客户地址")
    @ExcelProperty("客户地址")
    private String address;

    @Schema(description = "跟进状态（0-待跟进，1-跟进中，2-已完成，3-暂停跟进）")
    @ExcelProperty(value = "跟进状态", converter = FollowUpStatusConverter.class)
    private Integer followStatus;

    @Schema(description = "下次跟进时间")
    @JsonFormat(pattern = DateUtils.DATE_TIME_PATTERN)
    @ExcelProperty("下次跟进时间")
    private LocalDateTime nextFollowStatus;

    @Schema(description = "备注信息")
    private String remark;

    @Schema(description = "创建人ID")
    private Integer createrId;

    @Schema(description = "创建人名称")
    private String createrName;

    @Schema(description = "是否转入公海（0-未转入，1-已转入）")
    private Integer isPublic;

    @Schema(description = "客户所属员工ID")
    private Integer ownerId;

    @Schema(description = "客户所属员工名称")
    private String ownerName;

    @Schema(description = "是否为关键决策人（0-是，1-否）")
    @ExcelProperty(value = "是否为关键决策人", converter = IsKeyDecisionMakerConverter.class)
    private Integer isKeyDecisionMaker;

    @Schema(description = "性别（0-男，1-女，2-保密）")
    @ExcelProperty(value = "性别", converter = GenderConverter.class)
    private Integer gender;

    @Schema(description = "成交次数")
    @ExcelProperty("成交次数")
    private Integer dealCount;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = DateUtils.DATE_TIME_PATTERN)
    private LocalDateTime createTime;
}