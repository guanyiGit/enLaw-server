package com.soholy.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.soholy.common.ReqPage;
import com.soholy.entity.*;
import com.soholy.mapper.*;
import com.soholy.model.req.BaseRequest;
import com.soholy.model.req.ReqDataType;
import com.soholy.service.EnLawService;
import com.soholy.service.server.ServerConstant;
import com.soholy.utils.ByteUtils;
import com.soholy.utils.CacheUtils;
import lombok.extern.java.Log;
import org.apache.commons.codec.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Log
public class EnLawServerImpl implements EnLawService {

    @Autowired
    private TEnLawDataMapper enLawDataMapper;

    @Autowired
    private TLawEnforcementInstrumentMapper tLawEnforcementInstrumentMapper;

    @Autowired
    private TInstrumentRecordMapper tInstrumentRecordMapper;

    @Autowired
    private TDeviceMapper tDeviceMapper;

    @Autowired
    private TLawRecordMapper tLawRecordMapper;

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


    private Map<BaseRequest, TLawEnforcementInstrument> dataTypeFilter(Map<BaseRequest, TLawEnforcementInstrument> map, ReqDataType dataType) {
        if (map == null || map.size() > 0)
            return map.entrySet().stream()
                    .filter(Objects::nonNull)
                    .filter(x -> x.getKey() != null)
                    .filter(x -> x.getValue() != null)
                    .filter(x -> ReqDataType.EPC.equality(x.getKey().getDataType(), dataType))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        return null;
    }

    private void epcHandle(Map<BaseRequest, TLawEnforcementInstrument> map) {
        map = dataTypeFilter(map, ReqDataType.EPC);
        if (map == null || map.size() == 0) return;

        long count = map.entrySet().stream()
                .map(x -> {
                    String imeiStr = new String(x.getKey().getContent(), Charsets.UTF_8);
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
                                List<TDevice> tDevices = tDeviceMapper.selectList(Wrappers.<TDevice>lambdaQuery().eq(TDevice::getImei, y));
                                if (tDevices != null && tDevices.size() > 0) return tDevices.get(0);
                                return null;
                            }).filter(Objects::nonNull)
                            .map(y -> {
                                Long deviceId = y.getDeviceId();
                                TInstrumentRecord entity = new TInstrumentRecord();
                                entity.setCreateTime(LocalDateTime.now());
                                entity.setRecordDeviceid(deviceId.intValue());
                                entity.setRecordInstrumentid(oldEntity.getInstrumentId());
                                entity.setRecordPlaceid(oldEntity.getInstrumentPlaceid());

                                return tInstrumentRecordMapper.insert(entity);
                            }).reduce(0, Integer::sum);

                    log.info("执法仪编号[" + oldEntity.getInstrumentCode() + "]，保存执法记录数：" + record);

                    return record != 0 ? 1 : record;
                }).reduce(0, Integer::sum);

        if (map.size() != count) {
            log.warning(map.size() - count + "执法仪记录插入失败！");
        }

    }

    private void temperatureHandle(Map<BaseRequest, TLawEnforcementInstrument> map) {
        map = dataTypeFilter(map, ReqDataType.TEMPERATURE);
        if (map == null || map.size() == 0) return;
        //nothing
    }

    private void imgHandle(Map<BaseRequest, TLawEnforcementInstrument> map) {
        map = dataTypeFilter(map, ReqDataType.IMG);
        if (map == null || map.size() == 0) return;

        String fileName = "./" + Calendar.getInstance().getTimeInMillis() + ".jpg";
        map.entrySet().stream()
                .filter(x -> x.getKey().getmId() != null && x.getKey().getContent() != null && x.getKey().getContent().length > 0)
                .forEach(x -> {
                    try {
                        BaseRequest key = x.getKey();
                        String mId = String.valueOf(key.getmId());

                        byte[] oldBinary = CacheUtils.isExist(mId) ? (byte[]) CacheUtils.getCache(mId) : new byte[0];
                        byte[] curBinary = key.getContent();
                        byte[] binary = new byte[oldBinary.length + curBinary.length];
                        ByteUtils.copyArrays(oldBinary, 0, oldBinary.length, binary, 0);
                        ByteUtils.copyArrays(curBinary, 0, curBinary.length, binary, oldBinary.length);

                        CacheUtils.setCache(mId, binary, ServerConstant.MAX_LEN);
                        if (0 == key.getFinish() && binary.length > 0) {

                            FileUtils.writeByteArrayToFile(new File(fileName), (byte[]) Objects.requireNonNull(CacheUtils.getCache(mId)));
                            CacheUtils.deleteCache(mId);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }

    private void heartbeatHandle(Map<BaseRequest, TLawEnforcementInstrument> map) {
        map = dataTypeFilter(map, ReqDataType.HEARTBEAT);
        if (map == null || map.size() == 0) return;

        Integer count = map.entrySet().stream()
                .map(x -> {
                    BaseRequest key = x.getKey();
                    TLawEnforcementInstrument entity = x.getValue();
                    return tLawEnforcementInstrumentMapper.update(null, Wrappers.<TLawEnforcementInstrument>lambdaUpdate().eq(TLawEnforcementInstrument::getInstrumentId, entity.getInstrumentId()).set(TLawEnforcementInstrument::getUpdateTime, entity.getUpdateTime()));
                }).reduce(0, Integer::sum);

        log.info("HEARTBEAT：" + count);
    }

    private void otherHandle(Map<BaseRequest, TLawEnforcementInstrument> map) {
        map = dataTypeFilter(map, ReqDataType.OTHER);
        if (map == null || map.size() == 0) return;
        //nothing
    }

    private void recordSave(Map<BaseRequest, TLawEnforcementInstrument> map) {
        if (map == null || map.size() == 0) return;
        Integer count = map.entrySet().stream()
                .map(x -> {
                    BaseRequest baseRequest = x.getKey();
                    TLawRecord record = null;
                    if (baseRequest != null) {
                        record = new TLawRecord();
                        record.setCreationTime(LocalDateTime.now());
                        record.setRBinary(baseRequest.getOrigin());
                        record.setRContent(baseRequest.getContent());
                        record.setRDataLen(baseRequest.getDataLength() != null ? baseRequest.getDataLength().intValue() : 0);
                        record.setRDatatype(baseRequest.getDataType() != null ? (int) baseRequest.getDataType().getCode() : -1);
                        record.setRFinish((int) baseRequest.getFinish());
                        record.setRLawImei(baseRequest.getDeviceId());
                        record.setRMid(baseRequest.getmId() != null ? baseRequest.getmId().intValue() : -1);
                        record.setRUptime(baseRequest.getUpTime() != null && baseRequest.getUpTime().length() == 16 ? LocalDateTime.parse(baseRequest.getUpTime().substring(0,16 - 2), DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) : null);
                        record.setRVersion(baseRequest.getVersion() != null ? baseRequest.getVersion() + "" : "");

                        TLawEnforcementInstrument instrument = x.getValue();
                        if (instrument != null) {
                            record.setInstrumentId(String.valueOf(instrument.getInstrumentId()));
                        }
                    }

                    return record;
                })
                .filter(Objects::nonNull)
                .map(tLawRecordMapper::insert)
                .reduce(0, Integer::sum);
        log.info("save record count:" + count);
    }

    @Override
    public void dataHandle(Map<BaseRequest, TLawEnforcementInstrument> map) {
        //EPC
        epcHandle(map);
        //温度
        temperatureHandle(map);
        //图片
        imgHandle(map);
        //心跳
        heartbeatHandle(map);
        //其他
        otherHandle(map);
        //记录保存
        recordSave(map);
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
