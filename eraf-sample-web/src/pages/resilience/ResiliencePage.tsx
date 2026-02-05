import React, { useState, useEffect } from 'react';
import { Card, Button, Slider, Table, message, Typography, Tabs, Space, Tag, Progress, Statistic, Row, Col, Spin, InputNumber } from 'antd';
import { ThunderboltOutlined, ReloadOutlined, PlayCircleOutlined, StopOutlined, DashboardOutlined } from '@ant-design/icons';
import { resilienceApi } from '../../services/api';

const { Title, Text } = Typography;

const ResiliencePage: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [circuitBreakerStatus, setCircuitBreakerStatus] = useState<any>(null);
  const [rateLimiters, setRateLimiters] = useState<any[]>([]);
  const [testResult, setTestResult] = useState<any>(null);
  const [metrics, setMetrics] = useState<any>(null);

  const [successRate, setSuccessRate] = useState(50);
  const [failCount, setFailCount] = useState(3);
  const [timeoutDelay, setTimeoutDelay] = useState(5000);
  const [concurrentRequests, setConcurrentRequests] = useState(10);

  const loadCircuitBreakerStatus = async () => {
    try {
      const response = await resilienceApi.getCircuitBreakerStatus();
      setCircuitBreakerStatus(response.data);
    } catch (error) {
      console.error('Failed to load circuit breaker status');
    }
  };

  const loadRateLimiters = async () => {
    try {
      const response = await resilienceApi.getRateLimiters();
      setRateLimiters(response.data.limiters || []);
    } catch (error) {
      console.error('Failed to load rate limiters');
    }
  };

  const loadMetrics = async () => {
    try {
      const response = await resilienceApi.getMetrics();
      setMetrics(response.data);
    } catch (error) {
      console.error('Failed to load metrics');
    }
  };

  useEffect(() => {
    loadCircuitBreakerStatus();
    loadRateLimiters();
    loadMetrics();
  }, []);

  const testCircuitBreaker = async () => {
    setLoading(true);
    try {
      const response = await resilienceApi.testCircuitBreaker(successRate);
      setTestResult(response.data);
      loadCircuitBreakerStatus();
    } catch (error: any) {
      setTestResult(error.response?.data || { error: 'Request failed' });
    } finally {
      setLoading(false);
    }
  };

  const resetCircuitBreaker = async (name: string) => {
    try {
      await resilienceApi.resetCircuitBreaker(name);
      message.success('Circuit breaker reset');
      loadCircuitBreakerStatus();
    } catch (error) {
      message.error('Failed to reset');
    }
  };

  const testRateLimiter = async (name: string) => {
    setLoading(true);
    try {
      const response = await resilienceApi.testRateLimiter(name);
      setTestResult(response.data);
    } catch (error: any) {
      setTestResult(error.response?.data || { error: 'Rate limit exceeded' });
    } finally {
      setLoading(false);
    }
  };

  const testRetry = async () => {
    setLoading(true);
    try {
      const response = await resilienceApi.testRetry(failCount);
      setTestResult(response.data);
    } catch (error: any) {
      setTestResult(error.response?.data || { error: 'All retries failed' });
    } finally {
      setLoading(false);
    }
  };

  const testTimeout = async () => {
    setLoading(true);
    try {
      const response = await resilienceApi.testTimeout(timeoutDelay);
      setTestResult(response.data);
    } catch (error: any) {
      setTestResult(error.response?.data || { error: 'Timeout' });
    } finally {
      setLoading(false);
    }
  };

  const testBulkhead = async () => {
    setLoading(true);
    try {
      const response = await resilienceApi.testBulkhead(concurrentRequests);
      setTestResult(response.data);
    } catch (error: any) {
      setTestResult(error.response?.data || { error: 'Bulkhead rejected' });
    } finally {
      setLoading(false);
    }
  };

  const getStateColor = (state: string) => {
    switch (state) {
      case 'CLOSED': return 'green';
      case 'OPEN': return 'red';
      case 'HALF_OPEN': return 'orange';
      default: return 'default';
    }
  };

  const items = [
    {
      key: '1',
      label: '#21~#22 Circuit Breaker',
      children: (
        <Card>
          <Title level={5}>Circuit Breaker</Title>
          <Text type="secondary">Test circuit breaker with configurable failure rate.</Text>

          {circuitBreakerStatus?.circuitBreakers && (
            <Row gutter={16} style={{ margin: '16px 0' }}>
              {Object.entries(circuitBreakerStatus.circuitBreakers).map(([name, cb]: [string, any]) => (
                <Col span={8} key={name}>
                  <Card size="small">
                    <Statistic title={name} value={cb.state} valueStyle={{ color: getStateColor(cb.state) === 'green' ? '#52c41a' : getStateColor(cb.state) === 'red' ? '#ff4d4f' : '#faad14' }} />
                    <Text type="secondary">Failures: {cb.failureCount}</Text>
                    <br />
                    <Button size="small" onClick={() => resetCircuitBreaker(name)} style={{ marginTop: 8 }}>Reset</Button>
                  </Card>
                </Col>
              ))}
            </Row>
          )}

          <Space direction="vertical" style={{ width: '100%' }}>
            <Text>Success Rate: {successRate}%</Text>
            <Slider value={successRate} onChange={setSuccessRate} min={0} max={100} style={{ width: 300 }} />
            <Button type="primary" icon={<PlayCircleOutlined />} onClick={testCircuitBreaker} loading={loading}>
              Test Circuit Breaker
            </Button>
          </Space>
        </Card>
      ),
    },
    {
      key: '2',
      label: '#23~#24 Rate Limiter',
      children: (
        <Card>
          <Title level={5}>Rate Limiter</Title>
          <Text type="secondary">Test API rate limiting.</Text>

          <Table
            dataSource={rateLimiters}
            rowKey="name"
            size="small"
            style={{ marginTop: 16 }}
            columns={[
              { title: 'Name', dataIndex: 'name', key: 'name' },
              { title: 'Limit', dataIndex: 'limit', key: 'limit' },
              { title: 'Window', dataIndex: 'window', key: 'window' },
              { title: 'Available', dataIndex: 'available', key: 'available' },
              {
                title: 'Action',
                key: 'action',
                render: (_, record) => (
                  <Button size="small" onClick={() => testRateLimiter(record.name)}>Test</Button>
                ),
              },
            ]}
          />
        </Card>
      ),
    },
    {
      key: '3',
      label: '#25 Retry',
      children: (
        <Card>
          <Title level={5}>Retry Pattern</Title>
          <Text type="secondary">Test automatic retry with configurable failure count.</Text>
          <Space style={{ marginTop: 16 }}>
            <Text>Fail first N times:</Text>
            <InputNumber value={failCount} onChange={(v) => setFailCount(v || 0)} min={0} max={10} />
            <Button type="primary" onClick={testRetry} loading={loading}>Test Retry</Button>
          </Space>
        </Card>
      ),
    },
    {
      key: '4',
      label: '#26 Timeout',
      children: (
        <Card>
          <Title level={5}>Timeout</Title>
          <Text type="secondary">Test timeout handling (timeout is 3 seconds).</Text>
          <Space style={{ marginTop: 16 }}>
            <Text>Delay (ms):</Text>
            <InputNumber value={timeoutDelay} onChange={(v) => setTimeoutDelay(v || 1000)} min={100} max={10000} step={100} />
            <Button type="primary" onClick={testTimeout} loading={loading}>Test Timeout</Button>
          </Space>
        </Card>
      ),
    },
    {
      key: '5',
      label: '#27 Bulkhead',
      children: (
        <Card>
          <Title level={5}>Bulkhead Isolation</Title>
          <Text type="secondary">Test concurrent request limiting (max 5 concurrent).</Text>
          <Space style={{ marginTop: 16 }}>
            <Text>Concurrent Requests:</Text>
            <InputNumber value={concurrentRequests} onChange={(v) => setConcurrentRequests(v || 1)} min={1} max={20} />
            <Button type="primary" onClick={testBulkhead} loading={loading}>Test Bulkhead</Button>
          </Space>
        </Card>
      ),
    },
    {
      key: '6',
      label: '#28 Dashboard',
      children: (
        <Card>
          <Title level={5}>Resilience Dashboard</Title>
          <Button icon={<ReloadOutlined />} onClick={loadMetrics} style={{ marginBottom: 16 }}>Refresh</Button>
          {metrics && (
            <Row gutter={16}>
              <Col span={6}>
                <Statistic title="Total Requests" value={metrics.totalRequests || 0} />
              </Col>
              <Col span={6}>
                <Statistic title="Success Rate" value={`${metrics.successRate || 0}%`} valueStyle={{ color: '#52c41a' }} />
              </Col>
              <Col span={6}>
                <Statistic title="Failures" value={metrics.failures || 0} valueStyle={{ color: '#ff4d4f' }} />
              </Col>
              <Col span={6}>
                <Statistic title="Avg Response Time" value={`${metrics.avgResponseTime || 0}ms`} />
              </Col>
            </Row>
          )}
        </Card>
      ),
    },
  ];

  return (
    <Spin spinning={loading}>
      <Title level={3}>
        <ThunderboltOutlined /> Resilience Patterns (#21~#28)
      </Title>
      <Tabs items={items} />
      {testResult && (
        <Card title="Test Result" style={{ marginTop: 16 }}>
          <pre style={{ background: '#f5f5f5', padding: 16, borderRadius: 8, overflow: 'auto' }}>
            {JSON.stringify(testResult, null, 2)}
          </pre>
        </Card>
      )}
    </Spin>
  );
};

export default ResiliencePage;
