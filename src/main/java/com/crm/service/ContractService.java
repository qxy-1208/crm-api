package com.crm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.crm.common.result.PageResult;
import com.crm.entity.Contract;
import com.crm.query.ApprovalQuery;
import com.crm.query.ContractQuery;
import com.crm.query.IdQuery;
import com.crm.vo.ContractTrendPieVO;
import com.crm.vo.ContractVO;

import java.util.List;

/**
 * 合同服务接口
 * 提供合同的CRUD、分页查询、审批流程及统计分析等业务功能
 *
 * @author crm
 * @since 2025-10-12
 */
public interface ContractService extends IService<Contract> {

    /**
     * 合同列表 - 分页查询
     *
     * @param query 分页查询条件（包含页码、页大小、筛选条件等）
     * @return 分页结果（包含合同VO列表及总条数）
     */
    PageResult<ContractVO> getPage(ContractQuery query);

    /**
     * 新增或修改合同信息
     * 自动判断ID是否存在：存在则更新，不存在则新增
     *
     * @param contractVO 合同VO对象（包含合同基本信息）
     */
    void saveOrUpdate(ContractVO contractVO);

    /**
     * 按合同状态统计饼图数据
     * 统计各状态（初始化、审核通过、未通过等）的合同数量
     *
     * @return 饼图数据列表（包含状态名称和对应数量）
     */
    List<ContractTrendPieVO> getContractStatusPieData();

    /**
     * 开启合同审批流程
     * 将合同状态更新为待审核，并触发审批流程
     *
     * @param idQuery 包含合同ID的查询对象
     */
    void startApproval(IdQuery idQuery);

    /**
     * 处理合同审批操作
     * 支持审核通过/拒绝，并更新合同状态
     *
     * @param query 审批参数（包含合同ID、审批结果、审核意见等）
     */
    void approvalContract(ApprovalQuery query);

    /**
     * 统计当日合同审核总数（首页用）
     * 包含当日审核通过和拒绝的合同总数量
     *
     * @return 当日审核总数
     */
    Integer countTodayApprovalTotal();
}