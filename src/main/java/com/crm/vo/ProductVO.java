package com.crm.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "商品VO")
public class ProductVO {
    @Schema(description = "主键")
    private Integer id;

    @Schema(description = "商品名称")
    private String name;

    @Schema(description = "价格")
    private BigDecimal price;

    @Schema(description = "销量")
    private Integer sales;

    @Schema(description = "库存")
    private Integer stock;

    @Schema(description = "商品状态 0-初始化，1-上架，2-下架")
    private Integer status;

    @Schema(description = "商品状态名称")
    private String statusName;

    @Schema(description = "封面图")
    private String coverImage;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}