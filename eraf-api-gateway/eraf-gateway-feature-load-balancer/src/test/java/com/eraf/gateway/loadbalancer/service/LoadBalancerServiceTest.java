package com.eraf.gateway.loadbalancer.service;

import com.eraf.gateway.loadbalancer.domain.LoadBalancerAlgorithm;
import com.eraf.gateway.loadbalancer.domain.Server;
import com.eraf.gateway.loadbalancer.domain.Upstream;
import com.eraf.gateway.loadbalancer.repository.UpstreamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoadBalancerServiceTest {

    @Mock
    private UpstreamRepository upstreamRepository;

    private LoadBalancerService loadBalancerService;

    @BeforeEach
    void setUp() {
        loadBalancerService = new LoadBalancerService(upstreamRepository);
        loadBalancerService.init();
    }

    @Test
    void testSelectServerRoundRobin() {
        Upstream upstream = Upstream.builder()
                .name("test-upstream")
                .algorithm(LoadBalancerAlgorithm.ROUND_ROBIN)
                .servers(Arrays.asList(
                        Server.builder().host("server1").port(8080).healthy(true).build(),
                        Server.builder().host("server2").port(8080).healthy(true).build()
                ))
                .build();

        when(upstreamRepository.findByName("test-upstream"))
                .thenReturn(Optional.of(upstream));

        Server server1 = loadBalancerService.selectServer("test-upstream", null);
        assertNotNull(server1);

        Server server2 = loadBalancerService.selectServer("test-upstream", null);
        assertNotNull(server2);
    }

    @Test
    void testSelectServerWithUnhealthyServers() {
        Upstream upstream = Upstream.builder()
                .name("test-upstream")
                .algorithm(LoadBalancerAlgorithm.ROUND_ROBIN)
                .servers(Arrays.asList(
                        Server.builder().host("server1").port(8080).healthy(false).build(),
                        Server.builder().host("server2").port(8080).healthy(true).build()
                ))
                .build();

        when(upstreamRepository.findByName("test-upstream"))
                .thenReturn(Optional.of(upstream));

        Server server = loadBalancerService.selectServer("test-upstream", null);
        assertNotNull(server);
        assertEquals("server2", server.getHost());
    }

    @Test
    void testSelectServerNoHealthyServers() {
        Upstream upstream = Upstream.builder()
                .name("test-upstream")
                .algorithm(LoadBalancerAlgorithm.ROUND_ROBIN)
                .servers(Arrays.asList(
                        Server.builder().host("server1").port(8080).healthy(false).build(),
                        Server.builder().host("server2").port(8080).healthy(false).build()
                ))
                .build();

        when(upstreamRepository.findByName("test-upstream"))
                .thenReturn(Optional.of(upstream));

        Server server = loadBalancerService.selectServer("test-upstream", null);
        assertNull(server);
    }

    @Test
    void testCanaryDeployment() {
        Upstream upstream = Upstream.builder()
                .name("test-upstream")
                .algorithm(LoadBalancerAlgorithm.ROUND_ROBIN)
                .canaryPercentage(50)
                .canaryVersion("v2")
                .servers(Arrays.asList(
                        Server.builder().host("server1").port(8080).healthy(true).version(null).build(),
                        Server.builder().host("server2").port(8080).healthy(true).version("v2").build()
                ))
                .build();

        when(upstreamRepository.findByName("test-upstream"))
                .thenReturn(Optional.of(upstream));

        // Should route to both stable and canary servers
        Server server = loadBalancerService.selectServer("test-upstream", null);
        assertNotNull(server);
    }

    @Test
    void testMarkServerDown() {
        Server server = Server.builder()
                .host("server1")
                .port(8080)
                .healthy(true)
                .build();

        Upstream upstream = Upstream.builder()
                .name("test-upstream")
                .algorithm(LoadBalancerAlgorithm.ROUND_ROBIN)
                .servers(Arrays.asList(server))
                .build();

        when(upstreamRepository.findByName("test-upstream"))
                .thenReturn(Optional.of(upstream));

        loadBalancerService.markServerDown("test-upstream", server);
        assertFalse(server.isHealthy());
    }

    @Test
    void testMarkServerUp() {
        Server server = Server.builder()
                .host("server1")
                .port(8080)
                .healthy(false)
                .build();

        Upstream upstream = Upstream.builder()
                .name("test-upstream")
                .algorithm(LoadBalancerAlgorithm.ROUND_ROBIN)
                .servers(Arrays.asList(server))
                .build();

        when(upstreamRepository.findByName("test-upstream"))
                .thenReturn(Optional.of(upstream));

        loadBalancerService.markServerUp("test-upstream", server);
        assertTrue(server.isHealthy());
    }

    @Test
    void testGetUpstreamStats() {
        Upstream upstream = Upstream.builder()
                .name("test-upstream")
                .algorithm(LoadBalancerAlgorithm.ROUND_ROBIN)
                .servers(Arrays.asList(
                        Server.builder().host("server1").port(8080).healthy(true).build(),
                        Server.builder().host("server2").port(8080).healthy(false).build()
                ))
                .build();

        when(upstreamRepository.findByName("test-upstream"))
                .thenReturn(Optional.of(upstream));

        UpstreamStats stats = loadBalancerService.getUpstreamStats("test-upstream");

        assertNotNull(stats);
        assertEquals("test-upstream", stats.getName());
        assertEquals(2, stats.getTotalServers());
        assertEquals(1, stats.getHealthyServers());
        assertEquals(LoadBalancerAlgorithm.ROUND_ROBIN, stats.getAlgorithm());
    }
}
