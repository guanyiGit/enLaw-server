package com.soholy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
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
 * @since 2019-09-06
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class TLawEnforcementInstrument extends Model<TLawEnforcementInstrument> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "instrument_id", type = IdType.AUTO)
    private Integer instrumentId;

    /**
     * 执法仪编号
     */
    private String instrumentCode;

    /**
     * 外键关联，场所id
     */
    private Integer instrumentPlaceid;

    /**
     * 出厂商
     */
    private String instrumentTheVendor;

    /**
     * 出厂批次
     */
    private String instrumentTheFactoryBatch;

    /**
     * 执法仪状态
     */
    private Integer instrumentStatus;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    private LocalDateTime updateTime;


    @Override
    protected Serializable pkVal() {
        return this.instrumentId;
    }

}
