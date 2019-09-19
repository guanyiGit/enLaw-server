package com.soholy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableId;
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
 * @since 2019-09-19
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class TLawRecord extends Model<TLawRecord> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "r_id", type = IdType.ID_WORKER_STR)
    private String rId;

    /**
     * 版本号
     */
    private String rVersion;

    /**
     * 数据类型 1：表示epc码 2：表示温度 3：表示图片 4：表示心跳 5：其他
     */
    @TableField("r_dataType")
    private Integer rDatatype;

    /**
     * 消息ID
     */
    @TableField("r_mId")
    private Integer rMid;

    /**
     * 设备内部集成的 通讯模块的 IMEI 号
     */
    private String rLawImei;

    /**
     * 数据产生的时间
     */
    @TableField("r_upTime")
    private LocalDateTime rUptime;

    /**
     * 0，没有后续数据  1，有后续数据
     */
    private Integer rFinish;

    /**
     * 数据内容长度
     */
    private Integer rDataLen;

    /**
     * 数据体内容
     */
    private byte[] rContent;

    /**
     * 元数据
     */
    private byte[] rBinary;

    /**
     * 执法仪
     */
    private String instrumentId;

    private LocalDateTime creationTime;


    @Override
    protected Serializable pkVal() {
        return this.rId;
    }

}
