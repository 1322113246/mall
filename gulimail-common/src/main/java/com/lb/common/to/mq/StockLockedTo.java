package com.lb.common.to.mq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class StockLockedTo {
    private Long taskId;//库存工作单
    private StockDetailTo detail;//工作单详细id
}
