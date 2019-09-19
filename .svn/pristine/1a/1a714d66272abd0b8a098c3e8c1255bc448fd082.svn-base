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
public class TDevice extends Model<TDevice> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "device_id", type = IdType.AUTO)
    private Long deviceId;

    private String deviceNumber;

    /**
     * 0禁用，1启用, 2注销,3丢失，4损坏
     */
    private Integer deviceStatus;

    private LocalDateTime startTime;

    private String deviceBrand;

    private Integer deviceModelId;

    private LocalDateTime productionTime;

    private String deviceRemarks;

    /**
     * 0:RF/小设备，1：项圈/大设备
     */
    private Integer deviceType;

    private LocalDateTime creationTime;

    private Integer orgId;

    /**
     * 0正常模式，1省电模式
     */
    private Integer deviceWorkPattern;

    private Integer uploadIntervalTime;

    private String imei;

    private String devicePwd;

    private Integer deviceFactoryId;

    private String deviceName;

    private String verifyCode;

    private String psk;

    private String deviceBatch;

    private String deviceIdIot;


    @Override
    protected Serializable pkVal() {
        return this.deviceId;
    }

}
