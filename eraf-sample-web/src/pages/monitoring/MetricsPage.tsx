import React, { useState, useEffect } from 'react';
import { Card, Button, Table, Typography, Space, Tag, Spin, Row, Col, Statistic, Input, Select } from 'antd';
import { LineChartOutlined, ReloadOutlined, RiseOutlined, FallOutlined } from '@ant-design/icons';
import { monitoringApi } from '../../services/api';

const { Title, Text } = Typography;
const { Search } = Input;

const MetricsPage: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [metrics, setMetrics] = useState<any>(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [categoryFilter, setCategoryFilter] = useState('');

  const loadMetrics = async () => {
    setLoading(true);
    try {
      const response = await monitoringApi.getMetrics();
      setMetrics(response.data);
    } catch (error) {
      console.error('Failed to load metrics');
    } finally {
      setLoading(false);
    }
  };

  const incrementRequest = async () => {
    try {
      await monitoringApi.incrementRequest();
      loadMetrics();
    } catch (error) {
      console.error('Failed to increment');
    }
  };

  useEffect(() => {
    loadMetrics();
    const interval = setInterval(loadMetrics, 10000); // Auto refresh every 10 seconds
    return () => clearInterval(interval);
  }, []);

  const filteredMetrics = metrics?.metrics?.filter((m: any) => {
    const matchesSearch = !searchTerm || m.name.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesCategory = !categoryFilter || m.category === categoryFilter;
    return matchesSearch && matchesCategory;
  }) || [];

  const categories = [...new Set(metrics?.metrics?.map((m: any) => m.category) || [])];

  const getMetricColor = (type: string) => {
    switch (type) {
      case 'COUNTER': return 'blue';
      case 'GAUGE': return 'green';
      case 'TIMER': return 'orange';
      case 'HISTOGRAM': return 'purple';
      default: return 'default';
    }
  };

  return (
    <Spin spinning={loading}>
      <Title level={3}>
        <LineChartOutlined /> Metrics (#65)
      </Title>

      <Card>
        <Space style={{ marginBottom: 16 }}>
          <Button icon={<ReloadOutlined />} onClick={loadMetrics}>Refresh</Button>
          <Button type="primary" onClick={incrementRequest}>Simulate Request</Button>
        </Space>

        {metrics && (
          <>
            <Row gutter={16} style={{ marginBottom: 24 }}>
              <Col span={6}>
                <Card>
                  <Statistic
                    title="Total Requests"
                    value={metrics.summary?.totalRequests || 0}
                    prefix={<RiseOutlined />}
                  />
                </Card>
              </Col>
              <Col span={6}>
                <Card>
                  <Statistic
                    title="Success Rate"
                    value={metrics.summary?.successRate || 0}
                    suffix="%"
                    valueStyle={{ color: '#52c41a' }}
                  />
                </Card>
              </Col>
              <Col span={6}>
                <Card>
                  <Statistic
                    title="Avg Response Time"
                    value={metrics.summary?.avgResponseTime || 0}
                    suffix="ms"
                  />
                </Card>
              </Col>
              <Col span={6}>
                <Card>
                  <Statistic
                    title="Error Count"
                    value={metrics.summary?.errorCount || 0}
                    valueStyle={{ color: metrics.summary?.errorCount > 0 ? '#ff4d4f' : '#52c41a' }}
                  />
                </Card>
              </Col>
            </Row>

            <Row gutter={16} style={{ marginBottom: 24 }}>
              <Col span={6}>
                <Card size="small">
                  <Statistic title="JVM Heap Used" value={metrics.jvm?.heapUsedFormatted || 'N/A'} />
                </Card>
              </Col>
              <Col span={6}>
                <Card size="small">
                  <Statistic title="JVM Threads" value={metrics.jvm?.threadCount || 0} />
                </Card>
              </Col>
              <Col span={6}>
                <Card size="small">
                  <Statistic title="CPU Usage" value={`${metrics.system?.cpuUsage || 0}%`} />
                </Card>
              </Col>
              <Col span={6}>
                <Card size="small">
                  <Statistic title="Uptime" value={metrics.system?.uptimeFormatted || 'N/A'} />
                </Card>
              </Col>
            </Row>

            <Title level={5}>All Metrics</Title>
            <Space style={{ marginBottom: 16 }}>
              <Search
                placeholder="Search metrics"
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                style={{ width: 250 }}
              />
              <Select
                value={categoryFilter}
                onChange={setCategoryFilter}
                style={{ width: 150 }}
                placeholder="Category"
                allowClear
              >
                {categories.map((c: string) => (
                  <Select.Option key={c} value={c}>{c}</Select.Option>
                ))}
              </Select>
            </Space>

            <Table
              dataSource={filteredMetrics}
              rowKey="name"
              size="small"
              columns={[
                { title: 'Name', dataIndex: 'name', key: 'name', ellipsis: true },
                { title: 'Type', dataIndex: 'type', key: 'type', render: (t: string) => <Tag color={getMetricColor(t)}>{t}</Tag> },
                { title: 'Category', dataIndex: 'category', key: 'category' },
                { title: 'Value', dataIndex: 'value', key: 'value', render: (v: number) => v?.toLocaleString() },
                { title: 'Unit', dataIndex: 'unit', key: 'unit' },
                { title: 'Description', dataIndex: 'description', key: 'description', ellipsis: true },
              ]}
              pagination={{ pageSize: 15 }}
            />
          </>
        )}
      </Card>
    </Spin>
  );
};

export default MetricsPage;
