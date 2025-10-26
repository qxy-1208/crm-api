package com.crm.service;

import com.crm.entity.Product;
import com.crm.vo.FileUrlVO;
import org.springframework.web.multipart.MultipartFile;

public interface CommonService {
    FileUrlVO upload(MultipartFile multipartFile);
}