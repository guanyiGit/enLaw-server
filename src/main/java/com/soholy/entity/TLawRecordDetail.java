package com.soholy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author GuanY
 * @since 2019-09-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class TLawRecordDetail extends Model<TLawRecordDetail> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "rd_id", type = IdType.UUID)
    private String rdId;

    /**
     * 记录id
     */
    private String rId;

    /**
     * 消息描述
     */
    private String rdDesc;

    /**
     * 执法仪id
     */
    private String instrumentId;

    /**
     * 设备ID
     */
    private Long deviceId;

    private LocalDateTime creationTime;


    @Override
    protected Serializable pkVal() {
        return this.rdId;
    }

}
