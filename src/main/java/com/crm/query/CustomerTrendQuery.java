package com.crm.query;

import com.crm.common.model.Query;
import lombok.Data;

import java.util.List;

@Data
public class CustomerTrendQuery extends Query {
    private List<String> timeRange;

    private String transactionType;

    private String timeFormat;
}
