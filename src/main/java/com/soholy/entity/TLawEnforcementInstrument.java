package com.soholy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
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
public class TLawEnforcementInstrument extends Model<TLawEnforcementInstrument> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "instrument_id", type = IdType.AUTO)
    private Integer instrumentId;

    /**
     * 执法仪名称
     */
    private String instrumentName;

    /**
     * 执法仪编号
     */
    private String instrumentCode;

    private String instrumentImei;

    /**
     * 型号
     */
    private String instrumentTheVendor;

    /**
     * 出厂批次
     */
    private String instrumentTheFactoryBatch;

    /**
     * 执法仪状态（与字典表关联）
     */
    private Integer instrumentStatus;

    /**
     * 执法仪类型（0未激活，1已激活）
     */
    private Integer instrumentType;

    /**
     * 出厂日期
     */
    private LocalDateTime instrumentFactoryTime;

    /**
     * 品牌
     */
    private String instrumentBrand;

    /**
     * 出厂商
     */
    @TableField("instrument_factoryId")
    private Integer instrumentFactoryid;

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
