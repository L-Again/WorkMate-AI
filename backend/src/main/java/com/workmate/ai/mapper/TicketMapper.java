package com.workmate.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.workmate.ai.entity.Ticket;
import com.workmate.ai.vo.TicketListVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TicketMapper extends BaseMapper<Ticket> {

    Long countMyTickets(@Param("userId") Long userId,
                        @Param("status") String status);

    List<TicketListVO> selectMyTicketPage(@Param("userId") Long userId,
                                          @Param("status") String status,
                                          @Param("offset") Long offset,
                                          @Param("pageSize") Long pageSize);

    Long countAdminTickets(@Param("status") String status,
                           @Param("keyword") String keyword);

    List<TicketListVO> selectAdminTicketPage(@Param("status") String status,
                                             @Param("keyword") String keyword,
                                             @Param("offset") Long offset,
                                             @Param("pageSize") Long pageSize);
}