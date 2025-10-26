package com.crm.convert;

import com.crm.entity.Product;
import com.crm.enums.ProductStatusEnum;
import com.crm.vo.ProductVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface ProductConvert {
    ProductConvert INSTANCE = Mappers.getMapper(ProductConvert.class);

//    @Mapperapping(target = "statusName", expression = "java(com.crm.enums.ProductStatusEnum.getDescByCode(source.getStatus()))")
    ProductVO convert(Product source);

    List<ProductVO> convertList(List<Product> list);
}