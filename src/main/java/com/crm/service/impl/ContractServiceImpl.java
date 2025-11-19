package com.crm.service.impl;

import com.alibaba.excel.util.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.crm.common.exception.ServerException;
import com.crm.common.result.PageResult;
import com.crm.convert.ContractConvert;
import com.crm.entity.Approval;
import com.crm.entity.Contract;
import com.crm.entity.ContractProduct;
import com.crm.entity.Customer;
import com.crm.entity.Manager;
import com.crm.entity.Product;
import com.crm.enums.ContractStatusEnum;
import com.crm.mapper.ApprovalMapper;
import com.crm.mapper.ContractMapper;
import com.crm.mapper.ContractProductMapper;
import com.crm.mapper.ManagerMapper;
import com.crm.mapper.ProductMapper;
import com.crm.query.ApprovalQuery;
import com.crm.query.ContractQuery;
import com.crm.query.IdQuery;
import com.crm.security.user.SecurityUser;
import com.crm.service.ContractService;
import com.crm.service.EmailService;
import com.crm.vo.ContractTrendPieVO;
import com.crm.vo.ContractVO;
import com.crm.vo.ProductVO;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.crm.utils.NumberUtils.generateContractNumber;

/**
 * 合同服务实现类
 * 处理合同的分页查询、新增修改、审批流程及统计分析等业务逻辑
 *
 * @author crm
 * @since 2025-10-12
 */
@Service
@AllArgsConstructor
@Slf4j
public class ContractServiceImpl extends ServiceImpl<ContractMapper, Contract> implements ContractService {

    private final ContractMapper contractMapper;
    private final ContractProductMapper contractProductMapper;
    private final ApprovalMapper approvalMapper;
    private final ManagerMapper managerMapper;
    private final EmailService emailService;
    private final ProductMapper productMapper;

    /**
     * 合同分页查询
     * 支持按名称、客户ID、编号、状态筛选，仅查询当前登录用户签署的合同
     */
    @Override
    public PageResult<ContractVO> getPage(ContractQuery query) {
        Page<ContractVO> page = new Page<>(query.getPage(), query.getLimit());
        MPJLambdaWrapper<Contract> wrapper = new MPJLambdaWrapper<Contract>()
                .selectAll(Contract.class)
                .selectAs(Customer::getName, ContractVO::getCustomerName)
                .leftJoin(Customer.class, Customer::getId, Contract::getCustomerId)
                .eq(Contract::getOwnerId, SecurityUser.getManagerId())
                .eq(Contract::getDeleteFlag, 0)
                .orderByDesc(Contract::getCreateTime);

        // 筛选条件处理
        if (StringUtils.isNotBlank(query.getName())) {
            wrapper.like(Contract::getName, query.getName());
        }
        if (query.getCustomerId() != null) {
            wrapper.eq(Contract::getCustomerId, query.getCustomerId());
        }
        if (StringUtils.isNotBlank(query.getNumber())) {
            wrapper.like(Contract::getNumber, query.getNumber());
        }
        // 状态合法性校验（基于枚举）
        if (query.getStatus() != null) {
            if (ContractStatusEnum.getByValue(query.getStatus()) == null) {
                throw new ServerException("无效的合同状态");
            }
            wrapper.eq(Contract::getStatus, query.getStatus());
        }

        Page<ContractVO> resultPage = contractMapper.selectJoinPage(page, ContractVO.class, wrapper);

        // 关联合同产品信息
        resultPage.getRecords().forEach(vo -> {
            List<ContractProduct> products = contractProductMapper.selectList(
                    new LambdaQueryWrapper<ContractProduct>().eq(ContractProduct::getCId, vo.getId())
            );
            vo.setProducts(ContractConvert.INSTANCE.convertToProductVOList(products));
        });

        return new PageResult<>(resultPage.getRecords(), resultPage.getTotal());
    }

    /**
     * 新增或修改合同
     * 包含合同编号生成、状态默认值设置、产品关联关系处理
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdate(ContractVO contractVO) {
        boolean isNew = contractVO.getId() == null;

        // 新增校验：合同名称唯一性
        if (isNew && contractMapper.exists(new LambdaQueryWrapper<Contract>()
                .eq(Contract::getName, contractVO.getName())
                .eq(Contract::getDeleteFlag, 0))) {
            throw new ServerException("合同名称已存在");
        }

        // 转换VO为实体并设置基础信息
        Contract contract = ContractConvert.INSTANCE.convert(contractVO);
        contract.setCreaterId(SecurityUser.getManagerId());
        contract.setOwnerId(SecurityUser.getManagerId());
        contract.setReceivedAmount(contract.getReceivedAmount() == null ? BigDecimal.ZERO : contract.getReceivedAmount());

        // 设置默认状态（初始化）
        if (contract.getStatus() == null) {
            contract.setStatus(ContractStatusEnum.INIT.getValue());
        }

        // 新增/更新合同主表
        if (isNew) {
            contract.setNumber(generateContractNumber());
            contractMapper.insert(contract);
            log.info("新增合同ID：{}", contract.getId());
        } else {
            // 修改校验：合同存在性及审核状态
            Contract oldContract = contractMapper.selectById(contractVO.getId());
            if (oldContract == null) {
                throw new ServerException("合同不存在");
            }
            if (ContractStatusEnum.UNDER_REVIEW.getValue().equals(oldContract.getStatus())) {
                throw new ServerException("审核中合同无法修改");
            }
            contractMapper.updateById(contract);
        }

        // 处理合同与产品的关联关系
        handleContractProducts(contract.getId(), contractVO.getProducts());
    }

    /**
     * 按状态统计合同数量（饼图数据）
     */
    @Override
    public List<ContractTrendPieVO> getContractStatusPieData() {
        Integer managerId = SecurityUser.getManagerId();
        List<ContractTrendPieVO> pieData = contractMapper.countByStatus(managerId);

        // 计算各状态占比
        int total = pieData.stream().mapToInt(ContractTrendPieVO::getCount).sum();
        pieData.forEach(item -> {
            item.setProportion(total > 0 ? (double) item.getCount() / total * 100 : 0);
        });

        return pieData;
    }

    /**
     * 启动合同审批流程
     * 仅初始化状态的合同可发起审批
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void startApproval(IdQuery idQuery) {
        Contract contract = contractMapper.selectById(idQuery.getId());
        if (contract == null) {
            throw new ServerException("合同不存在");
        }
        // 状态校验：仅初始化状态可发起审批
        if (!ContractStatusEnum.INIT.getValue().equals(contract.getStatus())) {
            throw new ServerException("只有初始化状态的合同可发起审核");
        }

        // 更新为审核中状态
        contract.setStatus(ContractStatusEnum.UNDER_REVIEW.getValue());
        contract.setUpdateTime(LocalDateTime.now());
        contractMapper.updateById(contract);
    }

    /**
     * 处理合同审批（通过/拒绝）
     * 包含审核记录保存、状态更新及邮件通知
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approvalContract(ApprovalQuery query) {
        // 审核意见必填校验
        if (StringUtils.isBlank(query.getComment())) {
            throw new ServerException("请填写审核意见");
        }

        Contract contract = contractMapper.selectById(query.getId());
        if (contract == null) {
            throw new ServerException("合同不存在");
        }
        // 状态校验：仅审核中可操作
        if (!ContractStatusEnum.UNDER_REVIEW.getValue().equals(contract.getStatus())) {
            throw new ServerException("合同未在审核状态");
        }

        // 保存审核记录
        Approval approval = new Approval();
        approval.setStatus(query.getType());
        approval.setCreaterId(SecurityUser.getManagerId());
        approval.setContractId(query.getId());
        approval.setComment(query.getComment());
        approval.setCreateTime(LocalDateTime.now());
        approvalMapper.insert(approval);

        // 更新合同状态（通过/拒绝）
        Integer targetStatus = query.getType() == 0
                ? ContractStatusEnum.APPROVED.getValue()
                : ContractStatusEnum.REJECTED.getValue();
        contract.setStatus(targetStatus);
        contract.setUpdateTime(LocalDateTime.now());
        contractMapper.updateById(contract);

        // 发送审核结果邮件通知
        sendApprovalEmail(contract, query.getType() == 0, query.getComment());
    }

    /**
     * 统计当日审核总数（通过+拒绝）
     */
    @Override
    public Integer countTodayApprovalTotal() {
        String today = LocalDate.now().toString();
        Integer managerId = SecurityUser.getManagerId();

        int approved = contractMapper.countByStatusAndDate(
                managerId, today, ContractStatusEnum.APPROVED.getValue()
        );
        int rejected = contractMapper.countByStatusAndDate(
                managerId, today, ContractStatusEnum.REJECTED.getValue()
        );

        return approved + rejected;
    }

    /**
     * 处理合同与产品的关联关系（新增/修改/删除）
     */
    private void handleContractProducts(Integer contractId, List<ProductVO> newProductList) {
        if (newProductList == null) {
            return;
        }

        // 查询原有产品关联
        List<ContractProduct> oldProducts = contractProductMapper.selectList(
                new LambdaQueryWrapper<ContractProduct>().eq(ContractProduct::getCId, contractId)
        );

        // 1. 新增产品关联
        List<ProductVO> addedProducts = newProductList.stream()
                .filter(np -> oldProducts.stream().noneMatch(op -> op.getPId().equals(np.getPId())))
                .toList();
        for (ProductVO productVO : addedProducts) {
            Product product = checkProduct(productVO.getPId(), productVO.getCount());
            decreaseStock(product, productVO.getCount());
            contractProductMapper.insert(builderContractProduct(contractId, product, productVO.getCount()));
        }

        // 2. 修改产品数量
        List<ProductVO> changedProducts = newProductList.stream()
                .filter(np -> oldProducts.stream()
                        .anyMatch(op -> op.getPId().equals(np.getPId()) && !op.getCount().equals(np.getCount())))
                .toList();
        for (ProductVO productVO : changedProducts) {
            ContractProduct oldProduct = oldProducts.stream()
                    .filter(op -> op.getPId().equals(productVO.getPId()))
                    .findFirst().orElseThrow();

            Product product = checkProduct(productVO.getPId(), 0);
            int diff = productVO.getCount() - oldProduct.getCount();

            // 调整库存
            if (diff > 0) {
                decreaseStock(product, diff);
            } else if (diff < 0) {
                increaseStock(product, -diff);
            }

            // 更新关联关系
            oldProduct.setCount(productVO.getCount());
            oldProduct.setTotalPrice(product.getPrice().multiply(BigDecimal.valueOf(productVO.getCount())));
            contractProductMapper.updateById(oldProduct);
        }

        // 3. 删除产品关联
        List<ContractProduct> removedProducts = oldProducts.stream()
                .filter(op -> newProductList.stream().noneMatch(np -> np.getPId().equals(op.getPId())))
                .toList();
        for (ContractProduct removed : removedProducts) {
            Product product = productMapper.selectById(removed.getPId());
            if (product != null) {
                increaseStock(product, removed.getCount());
            }
            contractProductMapper.deleteById(removed.getId());
        }
    }

    /**
     * 构建合同产品关联实体
     */
    private ContractProduct builderContractProduct(Integer contractId, Product product, int count) {
        ContractProduct contractProduct = new ContractProduct();
        contractProduct.setCId(contractId);
        contractProduct.setPId(product.getId());
        contractProduct.setPName(product.getName());
        contractProduct.setPrice(product.getPrice());
        contractProduct.setCount(count);
        contractProduct.setTotalPrice(product.getPrice().multiply(BigDecimal.valueOf(count)));
        return contractProduct;
    }

    /**
     * 检查商品存在性及库存是否充足
     */
    private Product checkProduct(Integer productId, int count) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new ServerException("商品不存在");
        }
        if (product.getStock() < count) {
            throw new ServerException("商品库存不足");
        }
        return product;
    }

    /**
     * 增加商品库存
     */
    private void increaseStock(Product product, int count) {
        product.setStock(product.getStock() + count);
        product.setSales(product.getSales() - count);
        productMapper.updateById(product);
    }

    /**
     * 减少商品库存
     */
    private void decreaseStock(Product product, int count) {
        product.setStock(product.getStock() - count);
        product.setSales(product.getSales() + count);
        productMapper.updateById(product);
    }

    /**
     * 发送审核结果邮件通知
     */
    private void sendApprovalEmail(Contract contract, boolean isApproved, String comment) {
        try {
            Manager seller = managerMapper.selectById(contract.getCreaterId());
            if (seller == null || StringUtils.isBlank(seller.getEmail())) {
                log.warn("合同创建人邮箱不存在，无法发送邮件。合同ID: {}", contract.getId());
                return;
            }

            String subject = isApproved ? "合同审核通过通知" : "合同审核未通过通知";
            String content = String.format(
                    "您的合同《%s》已%s审核！\n审核意见：%s\n合同编号：%s\n审核时间：%s",
                    contract.getName(),
                    isApproved ? "通过" : "未通过",
                    comment,
                    contract.getNumber(),
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            );

            emailService.sendSimpleMail(seller.getEmail(), subject, content);
        } catch (Exception e) {
            log.error("发送审核邮件失败，合同ID: {}", contract.getId(), e);
            // 邮件失败不影响主流程
        }
    }
}