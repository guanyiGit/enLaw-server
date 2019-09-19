package com.soholy.service.codec;

import com.soholy.model.req.BaseRequest;

public interface CodecService {

    BaseRequest decode(byte[] binary);

    <T extends BaseRequest> int decodeHeaders(T request, byte[] binary, int binaryIndex);

    <T extends BaseRequest> int decodeBody(T request, byte[] binary, int binaryIndex);

    <T extends BaseRequest> byte[] encode(T request);

    boolean decodeCheckd(byte[] binary);
}
