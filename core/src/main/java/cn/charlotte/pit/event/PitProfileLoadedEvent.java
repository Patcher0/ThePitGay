package cn.charlotte.pit.event;

import cn.charlotte.pit.data.PlayerProfile;
import cn.charlotte.pit.data.operator.IOperator;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: EmptyIrony
 * @Date: 2021/1/1 0:54
 */
@Getter
@AllArgsConstructor
public class PitProfileLoadedEvent extends PitEvent {

    private final PlayerProfile playerProfile;
    private final IOperator operator;

}
