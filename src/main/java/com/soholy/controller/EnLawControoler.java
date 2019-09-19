package com.soholy.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.soholy.common.R;
import com.soholy.common.ReqPage;
import com.soholy.entity.TEnLawData;
import com.soholy.model.req.ReqDataType;
import com.soholy.service.EnLawService;
import com.soholy.utils.ByteUtils;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class EnLawControoler {

    @Autowired
    EnLawService enLawService;


    /**
     * https://119.147.209.163:8082/s16/datas?deviceId=1
     *
     * @param page
     * @param deviceId
     * @param dataType
     * @param type
     * @param request
     * @return
     */
    @ApiOperation("获取测试数据")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "deviceId", value = "查找设备imei号码(非必须)", paramType = "query", required = false, dataType = "string"),
            @ApiImplicitParam(name = "dataType", value = "从解码后数据中过滤[1ecp,2温度，3图片，4心跳，5其他](非必须)", paramType = "query", dataType = "int"),
            @ApiImplicitParam(name = "type", value = "数据类型[0上报数据，1下发数据](非必须)", required = false, paramType = "query", dataType = "int"),
    })
    @RequestMapping(value = "/datas", method = RequestMethod.GET)
    public R datas(HttpServletRequest request, ReqPage page,
                   @RequestParam(required = false) String deviceId,
                   @RequestParam(required = false) Integer dataType,
                   @RequestParam(required = false) Integer type) {

//        if (StringUtils.isNoneBlank(deviceId) && deviceId.length() != 15) {
//            return R.error("params err >>:deviceId！");
//        }
        if (dataType != null && (dataType < 0 || dataType > 5)) {
            return R.error("params err >>:dataType！");
        }
        if (type != null && (type < 0 || type > 1)) {
            return R.error("params err >>:dataType！");
        }
        String dataTypeEm = null;
        if (dataType != null)
            dataTypeEm = ReqDataType.EPC.format((byte) dataType.intValue()).toString();

        if (page == null || StringUtils.isBlank(request.getParameter("limit"))) {
            page = new ReqPage(1, 10);
        }
        List<TEnLawData> datas = enLawService.findTestData(page, deviceId, dataTypeEm, type);
        if (datas != null && datas.size() > 0) {
            return R.ok(JSON.parseArray(JSON.toJSONString(datas)).stream()
                    .map(x -> {
                        JSONObject json = null;
                        if (x != null) {
                            try {
                                json = (JSONObject) x;
                                Integer intType = json.getInteger("type");
                                if (intType != null)
                                    json.put("__type", intType == 0 ? "上报" : intType == 1 ? "下发" : "其他");
                                Integer intCodec = json.getInteger("codec");
                                if (intCodec != null)
                                    json.put("__codec", intCodec == 0 ? "原始数据" : intCodec == 1 ? "解码数据" : "其他");
                            } catch (Exception e) {
                            }

                            try {
                                JSONObject json2 = new JSONObject();
                                String objStr = json.getString("binaryData");
                                String objStr2 = StringUtils.substringBetween(objStr.replaceAll(" ", ""), "[", "]");

                                String[] bytes = objStr2.split(",");
                                int index = 0;

                                String[] out = new String[2];
                                System.arraycopy(bytes, index, out, 0, out.length);
                                json2.put("mark", Arrays.toString(out));
                                index += out.length;

                                out = new String[2];
                                System.arraycopy(bytes, index, out, 0, out.length);
                                json2.put("version", Arrays.toString(out));
                                index += out.length;

                                out = new String[1];
                                System.arraycopy(bytes, index, out, 0, out.length);
                                json2.put("dataType",Arrays.toString(out));
                                index += out.length;

                                out = new String[4];
                                System.arraycopy(bytes, index, out, 0, out.length);
                                json2.put("mId",Arrays.toString(out));
                                index += out.length;

                                out = new String[8];
                                System.arraycopy(bytes, index, out, 0, out.length);
                                json2.put("deviceId",Arrays.toString(out));
                                index += out.length;

                                out = new String[8];
                                System.arraycopy(bytes, index, out, 0, out.length);
                                json2.put("upTime",Arrays.toString(out));
                                index += out.length;

                                out = new String[1];
                                System.arraycopy(bytes, index, out, 0, out.length);
                                json2.put("finish",Arrays.toString(out));
                                index += out.length;

                                out = new String[4];
                                System.arraycopy(bytes, index, out, 0, out.length);
                                json2.put("dataLength",Arrays.toString(out));
                                index += out.length;

                                json.put("extData", json2);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }
                        return json == null ? x : json;
                    }).collect(Collectors.toList()));
        }
        return R.ok(datas);
    }


    @ApiOperation("测试连通性")
    @GetMapping("/test2")
    R getss() {
        return R.ok("ok");
    }
}
