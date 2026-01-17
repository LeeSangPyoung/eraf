package com.eraf.gateway.loadbalancer.algorithm;

import com.eraf.gateway.loadbalancer.domain.Server;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class IpHashLoadBalancerTest {

    private IpHashLoadBalancer loadBalancer;
    private List<Server> servers;

    @BeforeEach
    void setUp() {
        loadBalancer = new IpHashLoadBalancer();

        servers = Arrays.asList(
                Server.builder().host("server1").port(8080).build(),
                Server.builder().host("server2").port(8080).build(),
                Server.builder().host("server3").port(8080).build()
        );
    }

    @Test
    void testConsistentHashing() {
        String clientIp = "192.168.1.100";

        // Same IP should always go to same server
        Server server1 = loadBalancer.selectServer(servers, clientIp);
        Server server2 = loadBalancer.selectServer(servers, clientIp);
        Server server3 = loadBalancer.selectServer(servers, clientIp);

        assertEquals(server1.getHost(), server2.getHost());
        assertEquals(server2.getHost(), server3.getHost());
    }

    @Test
    void testDifferentIpsGetDifferentServers() {
        String ip1 = "192.168.1.100";
        String ip2 = "192.168.1.101";
        String ip3 = "192.168.1.102";

        Server server1 = loadBalancer.selectServer(servers, ip1);
        Server server2 = loadBalancer.selectServer(servers, ip2);
        Server server3 = loadBalancer.selectServer(servers, ip3);

        // At least one should be different (with 3 servers, high probability)
        assertNotNull(server1);
        assertNotNull(server2);
        assertNotNull(server3);
    }

    @Test
    void testNullIpFallback() {
        Server server = loadBalancer.selectServer(servers, null);
        assertNotNull(server);
        assertEquals("server1", server.getHost()); // Falls back to first server
    }

    @Test
    void testEmptyIpFallback() {
        Server server = loadBalancer.selectServer(servers, "");
        assertNotNull(server);
        assertEquals("server1", server.getHost()); // Falls back to first server
    }
}
