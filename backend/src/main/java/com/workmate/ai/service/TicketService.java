package com.workmate.ai.service;

import com.workmate.ai.common.PageResult;
import com.workmate.ai.dto.TicketCreateDTO;
import com.workmate.ai.dto.TicketStatusUpdateDTO;
import com.workmate.ai.vo.TicketListVO;
import com.workmate.ai.vo.TicketVO;

public interface TicketService {

    TicketVO createTicket(Long userId, TicketCreateDTO request);

    PageResult<TicketListVO> listMyTickets(Long userId, Long pageNum, Long pageSize, String status);

    TicketVO getTicketDetail(Long userId, Long ticketId);

    PageResult<TicketListVO> listAdminTickets(Long userId, Long pageNum, Long pageSize,
                                              String status, String keyword);

    TicketVO updateTicketStatus(Long userId, Long ticketId, TicketStatusUpdateDTO request);
}