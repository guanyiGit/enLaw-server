package com.soholy.service.codec.impl;

import com.soholy.model.req.*;
import com.soholy.service.codec.CodecService;
import com.soholy.utils.ByteUtils;
import lombok.extern.java.Log;
import org.apache.commons.codec.Charsets;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@Log
public class CodecServiceImpl implements CodecService {

    /**
     * 最大保存一天
     */
    public static final long BUFFER_TIME_OUT = 1000 * 60 * 60 * 24;

    /**
     * 临时会话保存10分钟
     */
    public static final long BUFFER_TEM_TIME_OUT = 1000 * 60 * 10;

    public static final String BUFFER_PREFIX_KEY = "BUFFER_PREFIX_KEY";

    public static final String BUFFER_SUFFIX_KEY = "BUFFER_PREFIX_KEY";


    private static final int HEADER_LEN = 30;

    @Override
    public BaseRequest decode(byte[] binary) {
        log.info("[" + binary.length + "] decode input:" + Arrays.toString(ByteUtils.byte2HexStr(binary, null)));
        BaseRequest request = new BaseRequest();
        try {
            request.setOrigin(binary);
            if (decodeCheckd(binary)) {
                ReqDataType dataType = ReqDataType.EPC.format(ByteUtils.copyArrays(binary, 4, 1)[0]);
                switch (dataType) {
                    case EPC:
                        request = new EpcRequest();
                        break;
                    case TEMPERATURE:
                        request = new TemperatureRequest();
                        break;
                    case IMG:
                        request = new ImgRequest();
                        break;
                    case HEARTBEAT:
                        request = new HeartbeatRequest();
                        break;
                    case OTHER:
                        request = new OtherRequest();
                        break;
                    default:
                        break;
                }
                request.setOrigin(binary);

                int binaryIndex = 0;
                binaryIndex = decodeHeaders(request, binary, binaryIndex);

                decodeBody(request, binary, binaryIndex);

                log.info("[" + (request.getContent() != null ? request.getContent().length : 0) + "] decode output:" + ReflectionToStringBuilder.toString(request, ToStringStyle.MULTI_LINE_STYLE));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return request;
    }


    @Override
    public int decodeHeaders(BaseRequest headers, byte[] binary, int binaryIndex) {
        int mark = ByteUtils.byte2Toint(binary, binaryIndex);
        headers.setMark(mark);
        binaryIndex += 2;

        int versionPrefix = ByteUtils.byte1Toint(binary, binaryIndex++);
        int versionSuffix = ByteUtils.byte1Toint(binary, binaryIndex++);
        headers.setVersion(Float.valueOf(versionPrefix + "." + versionSuffix));

        byte type = binary[binaryIndex++];
        headers.setDataType(ReqDataType.EPC.format(type));

        long mId = ByteUtils.byte4Tolong(binary, binaryIndex);
        binaryIndex += 4;
        headers.setmId(mId);

        StringBuilder deviceIds = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            String temp = ByteUtils.byteTohex(binary, binaryIndex, 1);
            deviceIds.append(temp);
            binaryIndex++;
        }
        headers.setDeviceId(deviceIds.toString());

//        long upTime = ByteUtils.byte8ToLong(binary, binaryIndex);
        byte[] upTimeArr = ByteUtils.copyArrays(binary, binaryIndex, 8);
        StringBuffer sb = new StringBuffer();
        try {
            for (int i = 0; i < upTimeArr.length; i++) {
                String temp = Integer.toHexString(Integer.parseInt(upTimeArr[i] + "", 10));
                if (temp.length() == 1) {
                    temp = "0" + temp;
                }
                sb.append(temp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        headers.setUpTime(sb.toString());
        binaryIndex += 8;

        byte finish = binary[binaryIndex++];
        headers.setFinish(finish);

        long dataLength = ByteUtils.byte4Tolong(binary, binaryIndex);
        headers.setDataLength(dataLength);
        binaryIndex += 4;

        return binaryIndex;
    }

    @Override
    public int decodeBody(BaseRequest request, byte[] binary, int binaryIndex) {
        ReqDataType dataType = request.getDataType();
        if (request.getDataLength() > 0l && dataType != null) {
            int len = binary.length - binaryIndex;
            byte[] content = ByteUtils.copyArrays(binary, binaryIndex, len);
            request.setContent(content);
            binaryIndex += len;
            switch (dataType) {
                case EPC:
                    EpcRequest epcRequest = (EpcRequest) request;
                    String escStrs = new String(content, Charsets.UTF_8);
                    if (StringUtils.isNoneBlank(escStrs)) {
                        epcRequest.setEpcs(StringUtils.split(escStrs, ","));
                    }
                    break;
                case TEMPERATURE:
                    TemperatureRequest temperatureRequest = (TemperatureRequest) request;
                    temperatureRequest.setTemperatures(new Double[]{ByteUtils.byte8ToDouble(content, 0)});
                    break;
                case IMG:
                    ImgRequest imgRequest = (ImgRequest) request;
                    break;
                case HEARTBEAT:
                    HeartbeatRequest heartbeatRequest = (HeartbeatRequest) request;
                    break;
                case OTHER:
                    OtherRequest otherRequest = (OtherRequest) request;
                    break;
            }
        }
        return binaryIndex;
    }

    @Override
    public byte[] encode(BaseRequest request) {
        log.info("[" + (request.getContent() != null ? request.getContent().length : 0) + "] encode input:" + ReflectionToStringBuilder.toString(request, ToStringStyle.MULTI_LINE_STYLE));
        byte[] binary = new byte[0];
        try {
            binary = new byte[HEADER_LEN];
            int binaryIndex = 0;
            //起始位
            ByteUtils.intTobyte2(request.getMark(), binary, binaryIndex);
            binaryIndex += 2;

            //版本号
            Float version = request.getVersion();
            String[] split = StringUtils.split(String.valueOf(version), ".");
            ByteUtils.intTobyte1(Integer.valueOf(split[0]), binary, binaryIndex++);
            ByteUtils.intTobyte1(Integer.valueOf(split[1]), binary, binaryIndex++);

            //数据类型ID
            binary[binaryIndex++] = request.getDataType().getCode();

            //消息mid
            ByteUtils.longTobyte4(request.getmId(), binary, binaryIndex);
            binaryIndex += 4;

            //设备ID
            if(request.getDeviceId().length()%2 != 0){
                request.setDeviceId(request.getDeviceId()+"0");
            }
            byte[] src = ByteUtils.hex2byteWithBlank(request.getDeviceId());
            ByteUtils.copyArrays(src, 0, src.length, binary, binaryIndex);
            binaryIndex += src.length;

            //上传时间
//            src = ByteUtils.longToByte8(request.getUpTime());
            String s = request.getUpTime();
            StringBuffer sb2 = new StringBuffer();
            if (s.length() < 8 * 2) {
                for (int i = 0; i < 8 * 2 - s.length(); i++) {
                    sb2.append("0");
                }
                s += sb2.toString();
            }
            char[] chars = s.toCharArray();
            src = new byte[8];
            for (int i = 0; i < chars.length; i += 2) {
                int i1 = Integer.parseInt(chars[i] + "" + chars[i + 1] + "", 16);
                ByteUtils.intTobyte1(i1, src, i / 2);
            }
            ByteUtils.copyArrays(src, 0, src.length, binary, binaryIndex);

            binaryIndex += src.length;


            //数据结束指示符
            binary[binaryIndex++] = request.getFinish();

            //数据内容长度
            ByteUtils.longTobyte4(request.getDataLength(), binary, binaryIndex);
            binaryIndex += 4;

            //数据内容
            src = request.getContent();
            if (src != null && src.length > 0) {
                int contextLen = request.getDataLength().intValue();

                byte[] fullArr = new byte[binary.length + contextLen];
                ByteUtils.copyArrays(binary, 0, binary.length, fullArr, 0);
                ByteUtils.copyArrays(src, 0, contextLen, fullArr, binary.length);
                binary = fullArr;
                binaryIndex += contextLen;
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            log.warning(e.getMessage());
        }
        log.info("[" + binary.length + "] encode output:" + Arrays.toString(ByteUtils.byte2HexStr(binary, null)));
        request.setOrigin(binary);
        return binary;
    }


    @Override
    public boolean decodeCheckd(byte[] binary) {
        List<byte[]> rtBytes = new ArrayList<>();
        if (binary == null || binary.length == 0) {
            return false;
        }

        for (int i = 0; i < binary.length - 1; i++) {
            if (binary[i] == (byte) 0xA0 && binary[i + 1] == (byte) 0xA2) {
                byte[] data = ByteUtils.copyArrays(binary, i, binary.length - i);
                if (binary.length - i + 1 >= HEADER_LEN) {
                    if (binary.length - i + 1 > HEADER_LEN) {
                    }
                    binary = data;
                    return true;
                } else {
                }
            }
        }
        if (rtBytes.size() == 0) {
            log.info("discard datas ...");
        }
        return false;
    }

}
