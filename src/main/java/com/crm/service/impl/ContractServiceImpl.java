package com.crm.service.impl;

import com.alibaba.excel.util.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.exception.ServerException;
import com.crm.common.result.PageResult;
import com.crm.convert.ContractConvert;
import com.crm.entity.*;
import com.crm.mapper.ApprovalMapper;
import com.crm.mapper.ContractMapper;
import com.crm.mapper.ContractProductMapper;
import com.crm.mapper.ProductMapper;
import com.crm.query.ApprovalQuery;
import com.crm.query.ContractQuery;
import com.crm.query.IdQuery;
import com.crm.security.user.SecurityUser;
import com.crm.service.ContractService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.crm.vo.ContractVO;
import com.crm.vo.ProductVO;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static com.crm.utils.NumberUtils.generateContractNumber;
/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author crm
 * @since 2025-10-12
 */
@Service
@AllArgsConstructor
@Slf4j
public class ContractServiceImpl extends ServiceImpl<ContractMapper, Contract> implements ContractService {

    private final ContractProductMapper contractProductMapper;

    private final ProductMapper productMapper;

    private final ApprovalMapper approvalMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approvalContract(ApprovalQuery query) {
        Contract contract = baseMapper.selectById(query.getId());
        if (contract == null) {
            throw new ServerException("合同不存在");
        }

        if (contract.getStatus() != 1) {
            throw new ServerException("合同还未发起审核或已审核，请勿重复提交");
        }
        // 添加审核内容，判断审核状态
        String approvalContent = query.getType() == 0 ? "合同审核通过" : "合同审核未通过";
        Integer contractStatus = query.getType() == 0 ? 2 : 3;
        Approval approval = new Approval();
        approval.setType(0);
        approval.setStatus(query.getType());
        approval.setCreaterId(SecurityUser.getManagerId());
        approval.setContractId(contract.getId());
        approval.setComment(approvalContent);
        approvalMapper.insert(approval);
        contract.setStatus(contractStatus);
        baseMapper.updateById(contract);
    }
    @Override
    public void startApproval(IdQuery idQuery) {
        Contract contract = baseMapper.selectById(idQuery.getId());
        if (contract == null) {
            throw new ServerException("合同不存在");
        }
        if (contract.getStatus() != 0) {
            throw new ServerException("该合同已审核通过，请勿重复提交");
        }
        contract.setStatus(1);
        baseMapper.updateById(contract);
    }

    @Override
    public PageResult<ContractVO> getPage(ContractQuery query) {
        Page<ContractVO> page = new Page<>();
        MPJLambdaWrapper<Contract> wrapper = new MPJLambdaWrapper<>();
        if (StringUtils.isNotBlank(query.getName())) {
            wrapper.like(Contract::getName, query.getName());
        }
        if (query.getCustomerId() != null) {
            wrapper.eq(Contract::getCustomerId, query.getCustomerId());
        }
        if (StringUtils.isNotBlank(query.getNumber())) {
            wrapper.like(Contract::getNumber, query.getNumber());
        }
        if (query.getStatus() != null) {
            wrapper.eq(Contract::getStatus, query.getStatus());
        }
        wrapper.orderByDesc(Contract::getCreateTime);
        // 只查询目前登录的员工签署的合同列表
        Integer managerId = SecurityUser.getManagerId();
        log.info("查询目前登录的员工：{}的合同列表", managerId);
        wrapper.selectAll(Contract.class)
                .selectAs(Customer::getName, ContractVO::getCustomerName)
                .leftJoin(Customer.class, Customer::getId, Contract::getCustomerId)
                .eq(Contract::getOwnerId, managerId);
        Page<ContractVO> result = baseMapper.selectJoinPage(page, ContractVO.class, wrapper);

        if (!result.getRecords().isEmpty()) {
            result.getRecords().forEach(contractVO -> {
                // 修改此处：使用正确的LambdaQueryWrapper语法
                List<ContractProduct> contractProducts = contractProductMapper.selectList(
                        new LambdaQueryWrapper<ContractProduct>()
                                .eq(ContractProduct::getCId, contractVO.getId())
                );
                contractVO.setProducts(ContractConvert.INSTANCE.convertToProductVOList(contractProducts));
            });
        }
        return new PageResult<>(result.getRecords(), result.getTotal());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdate(ContractVO contractVO) {
        boolean isNew = contractVO.getId() == null;
        if (isNew && baseMapper.exists(new LambdaQueryWrapper<Contract>().eq(Contract::getName, contractVO.getName()))) {
            throw new ServerException("合同名称已存在，请勿重复添加");
        }

//      转换并保存合同关系
        Contract contract = ContractConvert.INSTANCE.convert(contractVO);
        contract.setCreaterId(SecurityUser.getManagerId());
        contract.setOwnerId(SecurityUser.getManagerId());

        // 处理合同金额（设置默认值）
        if (contract.getReceivedAmount() == null) {
            contract.setReceivedAmount(BigDecimal.ZERO);
        }

        // 处理合同状态（设置默认值）
        if (contract.getStatus() == null) {
            contract.setStatus(0); // 0 代表“待审核”或“初始状态”，需与业务逻辑一致
        }

        if (isNew) {
            contract.setNumber(generateContractNumber());

            baseMapper.insert(contract);
            log.info("新增合同ID：{}", contract.getId()); // 验证合同ID是否生成
        } else {
            Contract oldContract = baseMapper.selectById(contractVO.getId());
            if (oldContract == null) {
                throw new ServerException("合同不存在");
            }
            if (oldContract.getStatus() == 1) {
                throw new ServerException("合同正在审核中，请勿执行修改操作");
            }
            baseMapper.updateById(contract);
        }
//        处理商品和合同的关联关系
        handleContractProducts(contract.getId(), contractVO.getProducts());
    }

    private void handleContractProducts(Integer contractId, List<ProductVO> newProductList) {
        if (newProductList == null) return;

        List<ContractProduct> oldProducts = contractProductMapper.selectList(
                new LambdaQueryWrapper<ContractProduct>().eq(ContractProduct::getCId, contractId)
        );

        // === 1. 新增商品 ===
        List<ProductVO> added = newProductList.stream()
                .filter(np -> oldProducts.stream().noneMatch(op -> op.getPId().equals(np.getPId())))
                .toList();

        for (ProductVO p : added) {
            Product product = checkProduct(p.getPId(), p.getCount());
            decreaseStock(product, p.getCount());
            ContractProduct cp = builderContractProduct(contractId, product, p.getCount());
            contractProductMapper.insert(cp);
        }

        // === 2. 修改数量 ===
        List<ProductVO> changed = newProductList.stream()
                .filter(np -> oldProducts.stream()
                        .anyMatch(op -> op.getPId().equals(np.getPId()) && !op.getCount().equals(np.getCount())))
                .toList();

        for (ProductVO p : changed) {
            ContractProduct old = oldProducts.stream()
                    .filter(op -> op.getPId().equals(p.getPId()))
                    .findFirst().orElseThrow();

            Product product = checkProduct(p.getPId(), 0);
            int diff = p.getCount() - old.getCount();

            // 库存调整
            if (diff > 0) decreaseStock(product, diff);
            else if (diff < 0) increaseStock(product, -diff);

            // 更新合同商品
            old.setCount(p.getCount());
            old.setPrice(product.getPrice());
            old.setTotalPrice(product.getPrice().multiply(BigDecimal.valueOf(p.getCount())));
            contractProductMapper.updateById(old);
        }

        // === 3. 删除商品 ===
        List<ContractProduct> removed = oldProducts.stream()
                .filter(op -> newProductList.stream().noneMatch(np -> np.getPId().equals(op.getPId())))
                .toList();

        for (ContractProduct rm : removed) {
            Product product = productMapper.selectById(rm.getPId());
            if (product != null) increaseStock(product, rm.getCount());
            contractProductMapper.deleteById(rm.getId());
        }
    }

    // 创建关联关系
    private ContractProduct builderContractProduct(Integer contractId, Product product, int count){
        ContractProduct contractProduct = new ContractProduct();
        contractProduct.setCId(contractId);
        contractProduct.setPId(product.getId());
        contractProduct.setPName(product.getName());
        contractProduct.setPrice(product.getPrice());
        contractProduct.setCount(count);
        contractProduct.setTotalPrice(product.getPrice().multiply(new BigDecimal(count)));
        return contractProduct;
    }

    //    检查商品数量
    private Product checkProduct(Integer productId, int count){
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new ServerException("商品不存在");
        }
        if (product.getStock() < count) {
            throw new ServerException("商品库存不足");
        }
        return product;
    }

    //    增加库存
    private void increaseStock(Product product, int count){
        product.setStock(product.getStock() + count);
        product.setSales(product.getSales() - count);
        productMapper.updateById(product);
    }

    //    减少库存
    private void decreaseStock(Product product, int count){
        product.setStock(product.getStock() - count);
        product.setSales(product.getSales() + count);
        productMapper.updateById(product);
    }
}