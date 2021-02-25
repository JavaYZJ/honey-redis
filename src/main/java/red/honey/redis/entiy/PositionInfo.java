package red.honey.redis.entiy;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author yangzhijie
 * @date 2021/2/25 15:43
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PositionInfo implements Serializable {

    /**
     * 位置名
     */
    private String member;

    /**
     * 经度
     */
    private Double longitude;

    /**
     * 纬度
     */
    private Double latitude;

}
