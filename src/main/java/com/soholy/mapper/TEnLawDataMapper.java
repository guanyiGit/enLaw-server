package com.soholy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.soholy.common.ReqPage;
import com.soholy.entity.TEnLawData;
import com.soholy.model.RecordDetail;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author GuanY
 * @since 2019-09-04
 */
public interface TEnLawDataMapper extends BaseMapper<TEnLawData> {

    List<TEnLawData> datas(@Param("page") ReqPage page,
                           @Param("deviceId") String deviceId,
                           @Param("dataTypeEm") String dataTypeEm,
                           @Param("type") Integer type);

    RecordDetail findRecordDetailByDeivceIdAndInsId(@Param("deviceId") Long deviceId,@Param("instrumentId") String instrumentId);
}
