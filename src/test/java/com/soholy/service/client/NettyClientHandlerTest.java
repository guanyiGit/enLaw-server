package com.soholy.service.client;

import com.alibaba.fastjson.JSONObject;
import com.soholy.utils.ByteUtils;
import com.soholy.utils.HttpClientUtil;
import com.soholy.utils.HttpResult;
import org.apache.commons.codec.Charsets;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class NettyClientHandlerTest {


    @Test
    public void test12() throws IOException, URISyntaxException {
        //发送短信犬主
        Map<String, String> params = new HashMap<>();
        params.put("type", "17");
        Map<String, String> template_param = new HashMap<>();

        template_param.put("cLocation", "cLocation");

        params.put("template_param", JSONObject.toJSONString(template_param));
        HttpResult httpResult = HttpClientUtil.executeHttpParams("http://localhost:8082", "POST", null, params);
    }

    @Test
    public void test1() {
        byte[] arr = new byte[]{
                0x20, 0x19, 0x09, 0x18, 0x10, 0x50, 0x15, 0x01
        };
//        parseHex(arr);

        String s = "2019091810501501";
        StringBuffer sb2 = new StringBuffer();
        if (s.length() < 8 * 2) {
            for (int i = 0; i < 8 * 2 - s.length(); i++) {
                sb2.append("0");
            }
            s += sb2.toString();
        }
        char[] chars = s.toCharArray();
        byte[] bytes = new byte[8];
        for (int i = 0; i < chars.length; i += 2) {
            int i1 = Integer.parseInt(chars[i] + "" + chars[i + 1] + "", 16);
            ByteUtils.intTobyte1(i1, bytes, i / 2);
        }
        parseHex(bytes);
    }
    //{'upTime':2312889882588561409,'epcs':['"""�'],'dataLength':4,'dataType':'EPC','origin':'[0xA0, 0xA2, 0x07, 0x03, 0x01, 0x00, 0x00, 0x0A, 0x6D, 0x86, 0x82, 0x21, 0x04, 0x53, 0x95, 0x00, 0x40, 0x20, 0x19, 0x09, 0x18, 0x10, 0x49, 0x40, 0x01, 0x00, 0x00, 0x00, 0x04, 0x22, 0x22, 0x22, 0x22, 0xA0]','mId':2669,'finish':34,'deviceId':'8682210453950040','version':7.3,'content':'[0x22, 0x22, 0x22, 0xA0]','mark':['0xA0','0xA2']}

    @Test
    public void test2() {
        byte[] bytes = new byte[]{
                0x31, 0x31, 0x31, 0x31, 0x31, 0x31, 0x31, 0x31, 0x31, 0x31, 0x36, (byte) 0xA0
        };
        bytes = new byte[]{
                0x22, 0x22, 0x22, (byte)0xA0
        };
        String str = new String(bytes, Charsets.US_ASCII);
        System.out.println(str);
        // 0xA0, 0xA2,  起始位
        // 0x07, 0x03,  版本号
        // 0x01,        数据类型
        // 0x00, 0x00, 0x00, 0x61,  消息ID
        // 0x86, 0x82, 0x21, 0x04, 0x53, 0x95, 0x00, 0x40,  设备ID
        // 0x20, 0x19, 0x09, 0x18, 0x11, 0x27, 0x44, 0x01,  时间
        // 0x00,        数据结束指示符
        // 0x00, 0x00, 0x0C, 0x31,  数据内容长度
        // 0x31, 0x31, 0x31, 0x31, 0x31, 0x31, 0x31, 0x31, 0x31, 0x31, 0x36, 0xA0   数据内容
    }

    private void parseHex(byte[] arr) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < arr.length; i++) {
            String temp = Integer.toHexString(Integer.parseInt(arr[i] + "", 10));
            if (temp.length() == 1) {
                temp = "0" + temp;
            }
            sb.append(temp);
        }
        System.err.println(sb);
    }
}