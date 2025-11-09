package com.crm.query;

import com.crm.common.model.Query;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "操作日志查询参数")
public class OperLogQuery extends Query {
    @Schema(description = "操作人名称")
    private String operName;

    @Schema(description = "业务类型（0其它 1新增 2修改 3删除）")
    private Integer operType;

    @Schema(description = "操作状态（0正常 1异常）")
    private Integer status;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;
}