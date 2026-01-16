package com.eraf.gateway.exception;

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
