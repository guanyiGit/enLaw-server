package com.soholy.model.req;

public class TemperatureRequest extends BaseRequest {

    /**
     * 温度场景：
     * double类型
     */
    private Double[] temperatures;

    public Double[] getTemperatures() {
        return temperatures;
    }

    public void setTemperatures(Double[] temperatures) {
        this.temperatures = temperatures;
    }
}
