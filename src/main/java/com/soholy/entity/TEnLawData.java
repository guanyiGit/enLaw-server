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
 * @since 2019-09-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class TEnLawData extends Model<TEnLawData> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "seq", type = IdType.AUTO)
    private Long seq;

    /**
     * 0上报，1下发
     */
    private Integer type;

    /**
     * 0 origin,1 codec
     */
    private Integer codec;

    private String binaryData;

    private String codecData;

    private LocalDateTime saveTime;


    @Override
    protected Serializable pkVal() {
        return this.seq;
    }

}
