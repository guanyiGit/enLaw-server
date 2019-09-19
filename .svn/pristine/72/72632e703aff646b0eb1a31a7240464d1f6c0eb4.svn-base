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
public class TPlace extends Model<TPlace> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "place_Id", type = IdType.AUTO)
    private Integer placeId;

    /**
     * 场所名称
     */
    private String placeName;

    /**
     * 区域Id
     */
    private String placeDistrictid;

    /**
     * 场所类型
     */
    private String placeType;

    /**
     * 场所管理人员姓名
     */
    private String placeManagement;

    /**
     * 场所管理人员电话
     */
    private String placePhone;

    /**
     * 场所详细地址
     */
    private String placeAddress;

    /**
     * 场所状态
     */
    private Integer placeStatus;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更改时间
     */
    private LocalDateTime updateTime;


    @Override
    protected Serializable pkVal() {
        return this.placeId;
    }

}
