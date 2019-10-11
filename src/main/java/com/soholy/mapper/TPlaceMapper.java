package com.soholy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.soholy.entity.TPlace;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author GuanY
 * @since 2019-09-06
 */
public interface TPlaceMapper extends BaseMapper<TPlace> {

    TPlace findAdminInfoByInstrumentId(@Param("instrumentId") String instrumentId);
}
