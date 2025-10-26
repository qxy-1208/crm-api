package com.crm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.entity.Product;
import com.crm.query.ProductQuery;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 商品Mapper接口（基于MyBatis-Plus注解，无XML）
 */
public interface ProductMapper extends BaseMapper<Product> {

    // 分页查询（注解方式编写SQL，替代XML）
    @Select("SELECT id, name, price, sales, stock, status, " +
            "cover_image, description, create_time, update_time " +
            "FROM t_product " +
            "WHERE 1=1 " +
            "<if test='query.name != null and query.name != \"\"'>" +
            "AND name LIKE CONCAT('%', #{query.name}, '%') " +
            "</if>" +
            "<if test='query.status != null'>" +
            "AND status = #{query.status} " +
            "</if>" +
            "ORDER BY update_time DESC")
    IPage<Product> selectProductPage(Page<Product> page, @Param("query") ProductQuery query);
}