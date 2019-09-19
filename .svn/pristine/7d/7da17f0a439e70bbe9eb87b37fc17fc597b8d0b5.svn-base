package com.soholy.model.req;

import java.util.Arrays;

public enum ReqDataType {
    /**
     * 表示epc码
     */
    EPC((byte) 0x01),
    /**
     * 表示温度
     */
    TEMPERATURE((byte) 0x2),
    /**
     * 表示图片
     */
    IMG((byte) 0x3),
    /**
     * 表示心跳
     */
    HEARTBEAT((byte) 0x4),
    /**
     * 其他
     */
    OTHER((byte) 0x5);

    private byte code;

    ReqDataType(byte code) {
        this.code = code;
    }

    public ReqDataType format(byte code) {
        return Arrays.stream(ReqDataType.values())
                .filter(x -> code == x.code)
                .findFirst().orElse(ReqDataType.OTHER);
    }

    public boolean equality(ReqDataType e1, ReqDataType e2) {
        if (e1 != null && e2 != null) {
            return e1.code == e2.code;
        }
        return false;
    }

    public byte getCode() {
        return code;
    }

    public void setCode(byte code) {
        this.code = code;
    }
}
