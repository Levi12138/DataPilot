package com.datapilot.server.ai.query;

import com.datapilot.pojo.dto.AiQueryDTO;
import com.datapilot.pojo.vo.AiQueryVO;

public interface AiQueryService {

    AiQueryVO execute(AiQueryDTO aiQueryDTO);
}
