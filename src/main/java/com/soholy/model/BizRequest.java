package com.soholy.model;

import com.soholy.entity.TLawRecord;
import com.soholy.model.req.BaseRequest;
import lombok.Data;

@Data
public class BizRequest<T extends BaseRequest> {

    private T t;

    private TLawRecord tLawRecord;

}
