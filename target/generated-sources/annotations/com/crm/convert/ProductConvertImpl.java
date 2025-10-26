package com.crm.convert;

import com.crm.entity.Product;
import com.crm.vo.ProductVO;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-10-26T10:36:35+0800",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.1 (Oracle Corporation)"
)
public class ProductConvertImpl implements ProductConvert {

    @Override
    public ProductVO convert(Product source) {
        if ( source == null ) {
            return null;
        }

        ProductVO productVO = new ProductVO();

        productVO.setId( source.getId() );
        productVO.setName( source.getName() );
        productVO.setPrice( source.getPrice() );
        productVO.setSales( source.getSales() );
        productVO.setStock( source.getStock() );
        productVO.setStatus( source.getStatus() );
        productVO.setCoverImage( source.getCoverImage() );
        productVO.setCreateTime( source.getCreateTime() );

        return productVO;
    }

    @Override
    public List<ProductVO> convertList(List<Product> list) {
        if ( list == null ) {
            return null;
        }

        List<ProductVO> list1 = new ArrayList<ProductVO>( list.size() );
        for ( Product product : list ) {
            list1.add( convert( product ) );
        }

        return list1;
    }
}
