package com.soholy.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.soholy.common.ReqPage;
import com.soholy.common.ServerConstant;
import com.soholy.entity.*;
import com.soholy.mapper.*;
import com.soholy.model.BizRequest;
import com.soholy.model.RecordDetail;
import com.soholy.model.req.BaseRequest;
import com.soholy.model.req.ReqDataType;
import com.soholy.service.EnLawService;
import com.soholy.utils.ByteUtils;
import com.soholy.utils.CacheUtils;
import com.soholy.utils.HttpClientUtil;
import com.soholy.utils.HttpResult;
import com.soholy.utils.fdfs.FastDFSClient;
import lombok.extern.java.Log;
import org.apache.commons.codec.Charsets;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.csource.common.NameValuePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


@Service
@Log
/*
 CREATE TABLE `t_en_law_data` (
  `seq` bigint(20) NOT NULL AUTO_INCREMENT,
  `type` tinyint(4) NOT NULL DEFAULT '0' COMMENT '0上报，1下发',
  `codec` tinyint(4) NOT NULL DEFAULT '0' COMMENT '0 origin,1 codec',
  `binary_data` longtext COLLATE utf8mb4_unicode_ci,
  `codec_data` longtext COLLATE utf8mb4_unicode_ci,
  `save_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`seq`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS `t_law_record`;
CREATE TABLE `t_law_record` (
  `r_id` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `r_version` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '版本号',
  `r_dataType` tinyint(4) NOT NULL DEFAULT '5' COMMENT '数据类型 1：表示epc码 2：表示温度 3：表示图片 4：表示心跳 5：其他',
  `r_mId` int(11) NOT NULL COMMENT '消息ID',
  `r_law_imei` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '设备内部集成的 通讯模块的 IMEI 号',
  `r_upTime` datetime NOT NULL COMMENT '数据产生的时间',
  `r_finish` tinyint(11) DEFAULT '0' COMMENT '0，没有后续数据  1，有后续数据',
  `r_data_len` int(11) NOT NULL DEFAULT '0' COMMENT '数据内容长度',
  `r_content` varbinary(10240) DEFAULT NULL COMMENT '数据体内容',
  `r_binary` varbinary(10240) NOT NULL COMMENT '元数据',
  `creation_time` datetime NOT NULL,
  PRIMARY KEY (`r_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


DROP TABLE IF EXISTS `t_law_record_detail`;
CREATE TABLE `t_law_record_detail` (
  `rd_id` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `r_id` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '记录id',
  `rd_desc` varchar(252) COLLATE utf8mb4_unicode_ci COMMENT '消息描述',
  `instrument_id` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '执法仪id',
  `device_id` bigint(11)  COMMENT '设备ID',
  `creation_time` datetime NOT NULL,
  PRIMARY KEY (`rd_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

 */
public class EnLawServerImpl implements EnLawService {

    @Value("${fastDfs.tracker.url}")
    private String trackerUrl;

    @Value("${fastDfs.prefix.url}")
    private String picPrefix;

    @Value("${send.sms.url}")
    private String smsUrl;

    @Value("${ecp.cache.sms.timeOut}")
    private int ecpCacheSmsTimeOut;

    @Value("${ecp.cache.device.timeOut}")
    private int ecpCacheDeviceTimeOut;


    @Autowired
    private TEnLawDataMapper enLawDataMapper;

    @Autowired
    private TLawEnforcementInstrumentMapper tLawEnforcementInstrumentMapper;

    @Autowired
    private TLawRecordDetailMapper tLawRecordDetailMapper;

    @Autowired
    private TDeviceMapper tDeviceMapper;

    @Autowired
    private TLawRecordMapper tLawRecordMapper;

    @Autowired
    private TPlaceMapper tPlaceMapper;

    /**
     * 测试用
     *
     * @param requests
     * @return
     */
    private boolean testHandle(List<BaseRequest> requests) {
        if (requests == null) {
            return false;
        }
        try {
            byte[] b = new byte[2];
            return requests.stream()
                    .map(x -> {
                        TEnLawData tEnLawData = new TEnLawData();
                        tEnLawData.setType(0);
                        tEnLawData.setCodec(0);
                        byte[] origin = x.getOrigin();
                        if (origin != null)
                            tEnLawData.setBinaryData(Arrays.toString(ByteUtils.byte2HexStr(origin, null)));
                        tEnLawData.setSaveTime(LocalDateTime.now());
                        if (x.getDataType() != null) {
                            tEnLawData.setCodec(1);
                            JSONObject json = JSON.parseObject(JSON.toJSONString(x));
                            if (x.getContent() != null)
                                json.put("content", Arrays.toString(ByteUtils.byte2HexStr(x.getContent(), null)));
                            json.put("origin", tEnLawData.getBinaryData());
                            ByteUtils.intTobyte2(x.getMark(), b, 0);
                            json.put("mark", ByteUtils.byte2HexStr(b, null));
                            tEnLawData.setCodecData(JSON.toJSONString(json, new SerializerFeature[]{SerializerFeature.UseSingleQuotes}));
                        }
                        return tEnLawData;
                    }).allMatch(x -> 1 == enLawDataMapper.insert(x));
        } catch (Exception e) {
            e.printStackTrace();
            log.warning("testHandle:" + e.getMessage());
        }
        return false;
    }

    @Override
    public List<TEnLawData> findTestData(ReqPage page, String deviceId, String dataTypeEm, Integer type) {
        return enLawDataMapper.datas(page, deviceId, dataTypeEm, type);
    }


    private Map<BizRequest, TLawEnforcementInstrument> dataTypeFilter(Map<BizRequest, TLawEnforcementInstrument> map, ReqDataType dataType) {
        if (map == null || map.size() > 0)
            return map.entrySet().stream()
                    .filter(Objects::nonNull)
                    .filter(x -> x.getKey() != null)
                    .filter(x -> x.getKey().getT() != null)
                    .filter(x -> x.getKey().getTLawRecord() != null)
                    .filter(x -> x.getValue() != null)
                    .filter(x -> ReqDataType.EPC.equality(x.getKey().getT().getDataType(), dataType))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        return null;
    }

    private void epcHandle(Map<BizRequest, TLawEnforcementInstrument> map) {
        map = dataTypeFilter(map, ReqDataType.EPC);
        if (map == null || map.size() == 0) return;

        long count = map.entrySet().stream()
                .map(x -> {
                    String imeiStr = new String(x.getKey().getT().getContent(), Charsets.UTF_8);
                    if (StringUtils.isBlank(imeiStr)) {
                        return 0;
                    }
                    String[] imeis = StringUtils.split(imeiStr, ServerConstant.SPLIT_IMEI_TOKEN);

                    if (imeis == null || imeis.length == 0) {
                        return 0;
                    }

                    TLawEnforcementInstrument oldEntity = x.getValue();
                    Integer record = Arrays.stream(imeis)
                            .map(y -> {
                                while (y.length() > 0 && y.indexOf("0") == 0) {
                                    y = y.substring(1);
                                }
                                return y.trim();
                            })
                            .map(y -> {
                                List<TDevice> tDevices = tDeviceMapper.selectList(Wrappers.<TDevice>lambdaQuery().eq(TDevice::getImei, y));
                                if (tDevices != null && tDevices.size() > 0) return tDevices.get(0);
                                return null;
                            }).filter(Objects::nonNull)
                            .filter(y -> {
                                //设备重传的抑制
                                LocalDateTime minusTime = LocalDateTime.now().minusSeconds(ecpCacheDeviceTimeOut / 1000);
                                return 0 == tLawRecordDetailMapper.selectCount(Wrappers.<TLawRecordDetail>lambdaQuery()
                                        .eq(TLawRecordDetail::getDeviceId, y.getDeviceId())
                                        .ge(TLawRecordDetail::getCreationTime, minusTime));
                            })
                            .map(y -> {
                                Long deviceId = y.getDeviceId();
                                TLawRecordDetail detail = new TLawRecordDetail();
                                detail.setCreationTime(LocalDateTime.now());
                                detail.setDeviceId(deviceId);
                                detail.setInstrumentId(oldEntity.getInstrumentId() + "");
                                detail.setRdDesc(y.getImei());
                                detail.setDeviceId(y.getDeviceId());
                                detail.setRId(x.getKey().getTLawRecord().getRId());

                                int jj = tLawRecordDetailMapper.insert(detail);
                                if (jj == 1) {
                                    sendWarnSms(detail, x.getKey().getTLawRecord());
                                }
                                return jj;
                            }).reduce(0, Integer::sum);

                    log.info("执法仪编号[" + oldEntity.getInstrumentCode() + "]，保存执法记录数：" + record);

                    return record != 0 ? 1 : record;
                }).reduce(0, Integer::sum);

        if (map.size() != count) {
            log.warning(map.size() - count + "执法仪记录插入失败！");
        }

        log.info(count + "：条epc数据处理成功");

    }

    private void sendWarnSms(TLawRecordDetail detail, TLawRecord tLawRecord) {
        try {
            if (detail == null || detail.getDeviceId() == null || detail.getInstrumentId() == null) {
                return;
            }

            RecordDetail recordDetail = enLawDataMapper.findRecordDetailByDeivceIdAndInsId(detail.getDeviceId(), detail.getInstrumentId());

            if (recordDetail == null) {
                return;
            }
            String smsCacheSuffix = "epc_smsCacheSuffix";
            String key = detail.getDeviceId() + smsCacheSuffix;

            if (null != CacheUtils.getCache(key)) {
                log.info("丢弃过于频繁的通知信息：" + key);
                return;
            }

            //发送短信犬主
            Map<String, String> params = new HashMap<>();
            params.put("type", "17");
            params.put("phone", recordDetail.getOwnerPhone());
            Map<String, String> template_param = new HashMap<>();

            template_param.put("uname", recordDetail.getDogOwnerName());
            template_param.put("dogName", recordDetail.getDogName());
            template_param.put("cTime", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(tLawRecord.getRUptime()));
            template_param.put("cLocation", recordDetail.getPlaceName());

            params.put("template_param", JSONObject.toJSONString(template_param));
            HttpResult httpResult = HttpClientUtil.executeHttpParams(smsUrl, "POST", null, params);
            log.info(ReflectionToStringBuilder.toString(httpResult, ToStringStyle.MULTI_LINE_STYLE));
            try {
                if (200 == JSON.parseObject(httpResult.getContent()).getInteger("status")) {
                    log.info("发送通知给犬主成功！" + recordDetail.getOwnerPhone());
                    CacheUtils.setCache(key, detail, ecpCacheSmsTimeOut);
                }
            } catch (Exception e) {
                log.info(e.getMessage());
            }

            //发送短信管理员
            params.clear();
            TPlace place = tPlaceMapper.findAdminInfoByInstrumentId(detail.getInstrumentId());
            params.put("type", "18");
            params.put("phone", place.getPlacePhone());

            template_param.clear();
            template_param.put("uname", place.getPlaceManagement());
            template_param.put("area", place.getPlaceName());
            template_param.put("owner", recordDetail.getDogOwnerName());
            template_param.put("phone", recordDetail.getOwnerPhone());

            params.put("template_param", JSONObject.toJSONString(template_param));
            httpResult = HttpClientUtil.executeHttpParams(smsUrl, "POST", null, params);
            log.info(ReflectionToStringBuilder.toString(httpResult, ToStringStyle.MULTI_LINE_STYLE));
            try {
                if (200 == JSON.parseObject(httpResult.getContent()).getInteger("status")) {
                    log.info("发送通知给管理员成功！" + place.getPlacePhone());
                }
            } catch (Exception e) {
                log.info(e.getMessage());
            }
        } catch (Exception e) {
            log.warning(e.getMessage());
        }
    }

    private void temperatureHandle(Map<BizRequest, TLawEnforcementInstrument> map) {
        map = dataTypeFilter(map, ReqDataType.TEMPERATURE);
        if (map == null || map.size() == 0) return;
        //nothing
    }

    private void imgHandle(Map<BizRequest, TLawEnforcementInstrument> map) {
        map = dataTypeFilter(map, ReqDataType.IMG);
        if (map == null || map.size() == 0) return;

        Integer count = map.entrySet().stream()
                .filter(x -> x.getKey().getT().getmId() != null && x.getKey().getT().getContent() != null && x.getKey().getT().getContent().length > 0)
                .map(x -> {
                    BaseRequest baseRequest = x.getKey().getT();
                    String mId = String.valueOf(baseRequest.getmId());
                    boolean flag = true;
                    try {

                        byte[] oldBinary = CacheUtils.isExist(mId) ? (byte[]) CacheUtils.getCache(mId) : new byte[0];
                        byte[] curBinary = baseRequest.getContent();
                        byte[] binary = new byte[oldBinary.length + curBinary.length];
                        ByteUtils.copyArrays(oldBinary, 0, oldBinary.length, binary, 0);
                        ByteUtils.copyArrays(curBinary, 0, curBinary.length, binary, oldBinary.length);

                        CacheUtils.setCache(mId, binary, ServerConstant.MAX_LEN);
                        flag = !(0 == baseRequest.getFinish() && binary.length > 0);
                    } catch (Exception e) {
                        log.warning(e.getMessage());
                    }
                    if (!flag) {
                        try {
                            TLawEnforcementInstrument oldEntity = x.getValue();
                            byte[] fileData = (byte[]) Objects.requireNonNull(CacheUtils.getCache(mId));

                            FastDFSClient fastDFSClient = new FastDFSClient(Arrays.asList(trackerUrl));
                            NameValuePair[] meta_list = new NameValuePair[4];

                            //固定仪编号_时间.png
                            //20198872_2019-09-11_14:22:21_111.png
                            String fileNeme = oldEntity.getInstrumentCode() + "_" + DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss_SSS").format(LocalDateTime.now()) + "." + ServerConstant.SAVE_IMG_SUFFIX;
                            meta_list[0] = new NameValuePair("fileName", fileNeme);
                            meta_list[1] = new NameValuePair("fileLength", String.valueOf(fileData.length));
                            meta_list[2] = new NameValuePair("fileExt", ServerConstant.SAVE_IMG_SUFFIX);
                            meta_list[3] = new NameValuePair("USERNAME", System.getenv().get("USERNAME"));
                            meta_list[3] = new NameValuePair("os.name", String.valueOf(System.getProperties().get("os.name")));

                            String url = fastDFSClient.uploadFile(fileData, ServerConstant.SAVE_IMG_SUFFIX, meta_list);

                            TLawRecordDetail detail = new TLawRecordDetail();
                            detail.setCreationTime(LocalDateTime.now());
                            detail.setInstrumentId(oldEntity.getInstrumentId() + "");
                            detail.setRdDesc(picPrefix + "/" + url);
                            detail.setRId(x.getKey().getTLawRecord().getRId());
                            flag = 1 == tLawRecordDetailMapper.insert(detail);
                        } catch (Exception e) {
                            log.warning(e.getMessage());
                        }
                    }
                    return flag ? 1 : 0;
                })
                .reduce(0, Integer::sum);

        log.info(count + "：条img数据处理成功");
    }

    private void heartbeatHandle(Map<BizRequest, TLawEnforcementInstrument> map) {
        map = dataTypeFilter(map, ReqDataType.HEARTBEAT);
        if (map == null || map.size() == 0) return;
        //nothing

    }

    private void otherHandle(Map<BizRequest, TLawEnforcementInstrument> map) {
        map = dataTypeFilter(map, ReqDataType.OTHER);
        if (map == null || map.size() == 0) return;
        //nothing
    }

    private Map<BizRequest, TLawEnforcementInstrument> recordSave(Map<BaseRequest, TLawEnforcementInstrument> map) {
        HashMap<BizRequest, TLawEnforcementInstrument> rMap = new HashMap<>();
        if (map == null || map.size() == 0) return rMap;
        Integer count = map.entrySet().stream()
                .map(x -> {
                    BaseRequest baseRequest = x.getKey();
                    BizRequest<BaseRequest> bizRequest = new BizRequest<>();
                    bizRequest.setT(baseRequest);

                    TLawEnforcementInstrument mValue = x.getValue();
                    if (baseRequest != null) {
                        TLawRecord record = new TLawRecord();
                        record.setRId(UUID.randomUUID().toString().replaceAll("-", ""));
                        record.setCreationTime(LocalDateTime.now());
                        record.setRBinary(baseRequest.getOrigin());
                        record.setRContent(baseRequest.getContent());
                        record.setRDataLen(baseRequest.getDataLength() != null ? baseRequest.getDataLength().intValue() : 0);
                        record.setRDatatype(baseRequest.getDataType() != null ? (int) baseRequest.getDataType().getCode() : -1);
                        record.setRFinish((int) baseRequest.getFinish());
                        record.setRLawImei(baseRequest.getDeviceId());
                        record.setRMid(baseRequest.getmId() != null ? baseRequest.getmId().intValue() : -1);
                        record.setRUptime(baseRequest.getUpTime() != null && baseRequest.getUpTime().length() == 16 ? LocalDateTime.parse(baseRequest.getUpTime().substring(0, 16 - 2), DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) : null);
                        record.setRVersion(baseRequest.getVersion() != null ? baseRequest.getVersion() + "" : "");

                        if (1 == tLawRecordMapper.insert(record)) {
                            bizRequest.setTLawRecord(record);
                        }

                        if (StringUtils.isNoneBlank(baseRequest.getDeviceId())) {
                            List<TLawEnforcementInstrument> maps = tLawEnforcementInstrumentMapper.selectList(Wrappers.<TLawEnforcementInstrument>lambdaQuery().eq(TLawEnforcementInstrument::getInstrumentImei, baseRequest.getDeviceId()));
                            if (maps != null && maps.size() > 0) {
                                mValue = maps.get(0);
                            }
                        }

                    }
                    rMap.put(bizRequest, mValue);
                    return bizRequest.getTLawRecord() != null ? 1 : 0;
                })
                .reduce(0, Integer::sum);
        log.info("save record count:" + count);
        return rMap;
    }

    @Override
    public void dataHandle(Map<BaseRequest, TLawEnforcementInstrument> map) {
        //记录保存
        Map<BizRequest, TLawEnforcementInstrument> rMap = recordSave(map);
        //EPC
        epcHandle(rMap);
        //温度
        temperatureHandle(rMap);
        //图片
        imgHandle(rMap);
        //心跳
        heartbeatHandle(rMap);
        //其他
        otherHandle(rMap);
    }


    @Override
    public Map<BaseRequest, TLawEnforcementInstrument> preproccess(List<BaseRequest> requests, boolean isTestRecord) {
        if (isTestRecord) testHandle(requests);

        Map<BaseRequest, TLawEnforcementInstrument> map = new HashMap<>();

        requests.forEach(x -> {
            TLawEnforcementInstrument tLawEnforcementInstrument = null;
            List<TLawEnforcementInstrument> lawEnforcementInstruments = tLawEnforcementInstrumentMapper.selectList(Wrappers.<TLawEnforcementInstrument>lambdaQuery().eq(TLawEnforcementInstrument::getInstrumentCode, x.getDeviceId()));
            if (lawEnforcementInstruments != null && lawEnforcementInstruments.size() > 0) {
                tLawEnforcementInstrument = lawEnforcementInstruments.get(0);
            } else {
                log.info("设备不存在：" + x.getDeviceId());
            }
            map.put(x, tLawEnforcementInstrument);
        });
        return map;
    }
}
