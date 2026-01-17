package com.eraf.gateway.loadbalancer.algorithm;

import com.eraf.gateway.loadbalancer.domain.Server;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RoundRobinLoadBalancerTest {

    private RoundRobinLoadBalancer loadBalancer;
    private List<Server> servers;

    @BeforeEach
    void setUp() {
        loadBalancer = new RoundRobinLoadBalancer();

        servers = Arrays.asList(
                Server.builder().host("server1").port(8080).build(),
                Server.builder().host("server2").port(8080).build(),
                Server.builder().host("server3").port(8080).build()
        );
    }

    @Test
    void testRoundRobinSelection() {
        // Should cycle through servers in order
        Server server1 = loadBalancer.selectServer(servers, null);
        assertEquals("server1", server1.getHost());

        Server server2 = loadBalancer.selectServer(servers, null);
        assertEquals("server2", server2.getHost());

        Server server3 = loadBalancer.selectServer(servers, null);
        assertEquals("server3", server3.getHost());

        // Should cycle back to first server
        Server server4 = loadBalancer.selectServer(servers, null);
        assertEquals("server1", server4.getHost());
    }

    @Test
    void testEmptyServerList() {
        Server server = loadBalancer.selectServer(Arrays.asList(), null);
        assertNull(server);
    }

    @Test
    void testSingleServer() {
        List<Server> singleServer = Arrays.asList(
                Server.builder().host("server1").port(8080).build()
        );

        Server server1 = loadBalancer.selectServer(singleServer, null);
        assertEquals("server1", server1.getHost());

        Server server2 = loadBalancer.selectServer(singleServer, null);
        assertEquals("server1", server2.getHost());
    }

    @Test
    void testReset() {
        loadBalancer.selectServer(servers, null);
        loadBalancer.selectServer(servers, null);

        loadBalancer.reset();

        // Should start from beginning after reset
        Server server = loadBalancer.selectServer(servers, null);
        assertEquals("server1", server.getHost());
    }
}
