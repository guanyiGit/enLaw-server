package com.soholy.service;

import com.soholy.common.ReqPage;
import com.soholy.entity.TEnLawData;
import com.soholy.entity.TLawEnforcementInstrument;
import com.soholy.model.req.BaseRequest;

import java.util.List;
import java.util.Map;

public interface EnLawService {

    List<TEnLawData> findTestData(ReqPage page, String deviceId, String dataTypeEm, Integer type);

    void dataHandle(Map<BaseRequest, TLawEnforcementInstrument> map);

    Map<BaseRequest, TLawEnforcementInstrument> preproccess(List<BaseRequest> asList, boolean isTestRecord);
}
