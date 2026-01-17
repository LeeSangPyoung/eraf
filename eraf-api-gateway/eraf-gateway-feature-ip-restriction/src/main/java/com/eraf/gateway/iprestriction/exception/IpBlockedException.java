package com.eraf.gateway.iprestriction.exception;

import com.eraf.gateway.common.exception.GatewayErrorCode;
import com.eraf.gateway.common.exception.GatewayException;
import lombok.Getter;

/**
 * IP 차단 예외
 */
@Getter
public class IpBlockedException extends GatewayException {

    private final String blockedIp;

    public IpBlockedException(String blockedIp) {
        super(GatewayErrorCode.IP_BLOCKED);
        this.blockedIp = blockedIp;
    }

    public String getBlockedIp() {
        return blockedIp;
    }
}
