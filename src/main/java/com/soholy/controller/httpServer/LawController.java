package com.soholy.controller.httpServer;

import com.soholy.common.R;
import com.soholy.entity.TLawEnforcementInstrument;
import com.soholy.model.req.BaseRequest;
import com.soholy.model.req.ReqDataType;
import com.soholy.service.EnLawService;
import com.soholy.service.codec.CodecService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.codec.Charsets;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;

@RestController
@RequestMapping(value = "/recive")
public class LawController {

    @Autowired
    private EnLawService enLawService;

    @Autowired
    private CodecService codecService;

    @ApiOperation("上传数据")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "device", value = "固定执法仪设备号（必须）", paramType = "path", required = true, dataType = "string"),
            @ApiImplicitParam(name = "mId", value = "消息ID（必须）", paramType = "path", required = true, dataType = "int"),
            @ApiImplicitParam(name = "dataType", value = "上报数据类型[1 epc码,2：温度，3图片，4心跳，5其他](必须)", required = true, paramType = "query", dataType = "int"),
            @ApiImplicitParam(name = "upTime", value = "上传时间(16个长度 必须) 格式yyyyyMMddHHmmss00", required = true, paramType = "query", dataType = "string"),
            @ApiImplicitParam(name = "version", value = "版本号(非必须)", required = false, paramType = "query", dataType = "double", defaultValue = "1.0"),
            @ApiImplicitParam(name = "isFinish", value = "结束指示符(非必须)", required = false, paramType = "query", dataType = "boolean", defaultValue = "true"),
            @ApiImplicitParam(name = "data", value = "数据体内容(非必须) 例如上传2个epc码,逗号分割 [111,222]", required = false, paramType = "query", dataType = "string"),
    })
    @PostMapping(value = "/{device}/{mId}")
    public R recive(@PathVariable(value = "device") String device, @PathVariable(value = "mId") Long mId,
                    @RequestParam(required = true, value = "dataType") int dataType,
                    @RequestParam(required = true, value = "upTime") String upTime,
                    @RequestParam(required = false, value = "version", defaultValue = "1.0") Float version,
                    @RequestParam(required = false, value = "isFinish", defaultValue = "true") Boolean isFinish,
                    @RequestParam(required = false, value = "data") String data) {

        try {
            if (dataType > 5 || dataType < 1) {
                return R.error("数据类型传输有误！");
            }
            if (upTime.length() != 16) {
                return R.error("上传时间格式错误！");
            }


            BaseRequest request = new BaseRequest();
            request.setmId(mId);
            request.setFinish(isFinish ? (byte) 0 : (byte) 1);
            request.setDeviceId(device);
            if (StringUtils.isNoneBlank(data)) {
                byte[] imeis = data.getBytes(Charsets.UTF_8);
                request.setDataLength(Long.valueOf(imeis.length));
                request.setContent(imeis);
            }
            request.setDataType(ReqDataType.EPC.format((byte) dataType));
            request.setVersion(version);
            request.setUpTime(upTime);
            request.setOrigin(codecService.encode(request));


            Map<BaseRequest, TLawEnforcementInstrument> map = enLawService.preproccess(Arrays.asList(request), true);
            if (map.size() > 0) {
                enLawService.dataHandle(map);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return R.error(e.getMessage());
        }
        return R.ok();
    }

}
