package com.soholy.model.req;

public class EpcRequest extends BaseRequest {

    /**
     * epc场景：
     * 支持多个epc码，每个epc码之间通过逗号分开。
     * 数据编码方式utf8数据
     */
    private String[] epcs;

    public String[] getEpcs() {
        return epcs;
    }

    public void setEpcs(String[] epcs) {
        this.epcs = epcs;
    }
}
