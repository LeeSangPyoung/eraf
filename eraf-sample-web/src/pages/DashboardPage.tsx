import React, { useState, useEffect } from 'react';
import { Card, Row, Col, Statistic, Table, Tag, Spin, message, Tabs, Progress, Empty } from 'antd';
import type { TabsProps } from 'antd';
import {
  ApiOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  ClockCircleOutlined,
  ThunderboltOutlined,
  BarChartOutlined,
  ReloadOutlined,
  FireOutlined,
  BugOutlined,
  UnorderedListOutlined,
} from '@ant-design/icons';
import { gateway } from '../services/api';

interface ApiStat {
  method: string;
  path: string;
  requestCount: number;
  errorCount: number;
  errorRate: number;
  avgResponseTime: number;
}

interface DashboardOverview {
  totalRequests: number;
  successRate: number;
  errorRate: number;
  avgResponseTime: number;
  recentRequestsPerHour: number;
  topApis: ApiStat[];
  slowestApis: ApiStat[];
}

interface HourlyData {
  hour: string;
  requestCount: number;
  successCount: number;
  errorCount: number;
  avgResponseTime: number;
}

interface TrafficStats {
  hourlyData: HourlyData[];
  methodDistribution: Record<string, number>;
  statusDistribution: Record<string, number>;
}

const METHOD_COLORS: Record<string, string> = {
  GET: 'blue',
  POST: 'green',
  PUT: 'orange',
  DELETE: 'red',
  PATCH: 'purple',
  ALL: 'default',
};

const STATUS_COLORS: Record<string, string> = {
  '2xx': '#52c41a',
  '3xx': '#1890ff',
  '4xx': '#faad14',
  '5xx': '#f5222d',
};

const DashboardPage: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [overview, setOverview] = useState<DashboardOverview | null>(null);
  const [traffic, setTraffic] = useState<TrafficStats | null>(null);
  const [topApis, setTopApis] = useState<ApiStat[]>([]);
  const [errorProneApis, setErrorProneApis] = useState<ApiStat[]>([]);
  const [apiStats, setApiStats] = useState<ApiStat[]>([]);

  useEffect(() => {
    loadAllData();
    const interval = setInterval(loadAllData, 30000);
    return () => clearInterval(interval);
  }, []);

  const loadAllData = async () => {
    setLoading(true);
    try {
      const [overviewRes, trafficRes, topApisRes, errorApisRes, apiStatsRes] = await Promise.allSettled([
        gateway.getDashboardOverview(),
        gateway.getDashboardTraffic(),
        gateway.getDashboardTopApis(10),
        gateway.getDashboardErrorProneApis(10),
        gateway.getDashboardApiStats(),
      ]);

      if (overviewRes.status === 'fulfilled') setOverview(overviewRes.value.data);
      if (trafficRes.status === 'fulfilled') setTraffic(trafficRes.value.data);
      if (topApisRes.status === 'fulfilled') setTopApis(topApisRes.value.data);
      if (errorApisRes.status === 'fulfilled') setErrorProneApis(errorApisRes.value.data);
      if (apiStatsRes.status === 'fulfilled') setApiStats(apiStatsRes.value.data);
    } catch (error) {
      message.error('Failed to load dashboard data');
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  if (loading && !overview) {
    return (
      <div style={{ textAlign: 'center', padding: '100px' }}>
        <Spin size="large" />
      </div>
    );
  }

  // ===== Column Definitions =====

  const topApisColumns = [
    {
      title: 'Rank',
      key: 'rank',
      width: 60,
      render: (_: any, __: any, index: number) => index + 1,
    },
    {
      title: 'Method',
      dataIndex: 'method',
      key: 'method',
      width: 80,
      render: (method: string) => <Tag color={METHOD_COLORS[method] || 'default'}>{method}</Tag>,
    },
    {
      title: 'Path',
      dataIndex: 'path',
      key: 'path',
      ellipsis: true,
    },
    {
      title: 'Requests',
      dataIndex: 'requestCount',
      key: 'requestCount',
      width: 100,
      align: 'right' as const,
      sorter: (a: ApiStat, b: ApiStat) => a.requestCount - b.requestCount,
    },
    {
      title: 'Avg Time (ms)',
      dataIndex: 'avgResponseTime',
      key: 'avgResponseTime',
      width: 120,
      align: 'right' as const,
      render: (time: number) => (
        <Tag color={time > 1000 ? 'red' : time > 500 ? 'orange' : 'green'}>
          {Math.round(time)} ms
        </Tag>
      ),
    },
  ];

  const errorApisColumns = [
    {
      title: 'Rank',
      key: 'rank',
      width: 60,
      render: (_: any, __: any, index: number) => index + 1,
    },
    {
      title: 'Path',
      dataIndex: 'path',
      key: 'path',
      ellipsis: true,
    },
    {
      title: 'Requests',
      dataIndex: 'requestCount',
      key: 'requestCount',
      width: 100,
      align: 'right' as const,
    },
    {
      title: 'Errors',
      dataIndex: 'errorCount',
      key: 'errorCount',
      width: 80,
      align: 'right' as const,
      render: (count: number) => <span style={{ color: count > 0 ? '#f5222d' : '#52c41a' }}>{count}</span>,
    },
    {
      title: 'Error Rate',
      dataIndex: 'errorRate',
      key: 'errorRate',
      width: 120,
      align: 'right' as const,
      render: (rate: number) => (
        <Tag color={rate > 50 ? 'red' : rate > 20 ? 'orange' : rate > 0 ? 'gold' : 'green'}>
          {rate.toFixed(1)}%
        </Tag>
      ),
    },
  ];

  const apiStatsColumns = [
    {
      title: 'Method',
      dataIndex: 'method',
      key: 'method',
      width: 90,
      render: (method: string) => <Tag color={METHOD_COLORS[method] || 'default'}>{method}</Tag>,
      filters: [...new Set(apiStats.map(s => s.method))].map(m => ({ text: m, value: m })),
      onFilter: (value: any, record: ApiStat) => record.method === value,
    },
    {
      title: 'Path',
      dataIndex: 'path',
      key: 'path',
      ellipsis: true,
    },
    {
      title: 'Requests',
      dataIndex: 'requestCount',
      key: 'requestCount',
      width: 100,
      align: 'right' as const,
      sorter: (a: ApiStat, b: ApiStat) => a.requestCount - b.requestCount,
      defaultSortOrder: 'descend' as const,
    },
    {
      title: 'Errors',
      dataIndex: 'errorCount',
      key: 'errorCount',
      width: 80,
      align: 'right' as const,
      render: (count: number) => <span style={{ color: count > 0 ? '#f5222d' : undefined }}>{count}</span>,
    },
    {
      title: 'Error Rate',
      dataIndex: 'errorRate',
      key: 'errorRate',
      width: 110,
      align: 'right' as const,
      render: (rate: number) => (
        <Progress
          percent={Number(rate.toFixed(1))}
          size="small"
          status={rate > 50 ? 'exception' : rate > 20 ? 'active' : 'success'}
          format={(p) => `${p}%`}
        />
      ),
      sorter: (a: ApiStat, b: ApiStat) => a.errorRate - b.errorRate,
    },
    {
      title: 'Avg Time (ms)',
      dataIndex: 'avgResponseTime',
      key: 'avgResponseTime',
      width: 130,
      align: 'right' as const,
      render: (time: number) => (
        <Tag color={time > 1000 ? 'red' : time > 500 ? 'orange' : 'green'}>
          {Math.round(time)} ms
        </Tag>
      ),
      sorter: (a: ApiStat, b: ApiStat) => a.avgResponseTime - b.avgResponseTime,
    },
  ];

  // ===== Traffic Rendering =====

  const renderHourlyTraffic = () => {
    if (!traffic?.hourlyData || traffic.hourlyData.length === 0) {
      return <Empty description="No traffic data in the last 24 hours" />;
    }

    const maxCount = Math.max(...traffic.hourlyData.map(d => d.requestCount), 1);

    return (
      <div>
        <div style={{ display: 'flex', gap: 4, alignItems: 'flex-end', height: 200, padding: '0 8px' }}>
          {traffic.hourlyData.map((data, idx) => {
            const height = (data.requestCount / maxCount) * 160;
            const errorHeight = data.errorCount > 0 ? (data.errorCount / maxCount) * 160 : 0;
            const successHeight = height - errorHeight;

            return (
              <div
                key={idx}
                style={{
                  flex: 1,
                  display: 'flex',
                  flexDirection: 'column',
                  alignItems: 'center',
                  justifyContent: 'flex-end',
                }}
                title={`${data.hour}\nRequests: ${data.requestCount}\nSuccess: ${data.successCount}\nErrors: ${data.errorCount}\nAvg: ${Math.round(data.avgResponseTime)}ms`}
              >
                <div style={{ fontSize: 10, marginBottom: 4, color: '#666' }}>
                  {data.requestCount}
                </div>
                <div style={{ width: '100%', display: 'flex', flexDirection: 'column' }}>
                  {errorHeight > 0 && (
                    <div
                      style={{
                        height: errorHeight,
                        backgroundColor: '#ff4d4f',
                        borderRadius: '2px 2px 0 0',
                        minHeight: 2,
                      }}
                    />
                  )}
                  <div
                    style={{
                      height: Math.max(successHeight, 2),
                      backgroundColor: '#52c41a',
                      borderRadius: errorHeight > 0 ? '0' : '2px 2px 0 0',
                    }}
                  />
                </div>
                <div style={{ fontSize: 10, marginTop: 4, color: '#999' }}>{data.hour}</div>
              </div>
            );
          })}
        </div>
        <div style={{ display: 'flex', justifyContent: 'center', gap: 16, marginTop: 16 }}>
          <span><span style={{ display: 'inline-block', width: 12, height: 12, backgroundColor: '#52c41a', marginRight: 4 }} />Success</span>
          <span><span style={{ display: 'inline-block', width: 12, height: 12, backgroundColor: '#ff4d4f', marginRight: 4 }} />Error</span>
        </div>
      </div>
    );
  };

  const renderDistribution = (data: Record<string, number> | undefined, colorMap: Record<string, string>) => {
    if (!data || Object.keys(data).length === 0) {
      return <Empty description="No data" />;
    }

    const total = Object.values(data).reduce((a, b) => a + b, 0);
    const sortedEntries = Object.entries(data).sort(([, a], [, b]) => b - a);

    return (
      <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
        {sortedEntries.map(([key, value]) => {
          const percent = total > 0 ? (value / total) * 100 : 0;
          return (
            <div key={key}>
              <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 4 }}>
                <Tag color={colorMap[key] || 'default'}>{key}</Tag>
                <span>{value} ({percent.toFixed(1)}%)</span>
              </div>
              <Progress
                percent={Number(percent.toFixed(1))}
                showInfo={false}
                strokeColor={colorMap[key] || '#1890ff'}
                size="small"
              />
            </div>
          );
        })}
      </div>
    );
  };

  // ===== Tabs =====

  const tabItems: TabsProps['items'] = [
    {
      key: 'traffic',
      label: <span><BarChartOutlined /> Traffic (24h)</span>,
      children: (
        <Row gutter={[16, 16]}>
          <Col span={24}>
            <Card title="Hourly Traffic" size="small">
              {renderHourlyTraffic()}
            </Card>
          </Col>
          <Col xs={24} lg={12}>
            <Card title="HTTP Method Distribution" size="small">
              {renderDistribution(traffic?.methodDistribution, METHOD_COLORS)}
            </Card>
          </Col>
          <Col xs={24} lg={12}>
            <Card title="Status Code Distribution" size="small">
              {renderDistribution(traffic?.statusDistribution, STATUS_COLORS)}
            </Card>
          </Col>
        </Row>
      ),
    },
    {
      key: 'topApis',
      label: <span><FireOutlined /> Top APIs</span>,
      children: (
        <Card title="Top 10 Most Called APIs" size="small">
          <Table
            dataSource={topApis}
            columns={topApisColumns}
            pagination={false}
            size="small"
            rowKey={(record) => `${record.method}-${record.path}`}
          />
        </Card>
      ),
    },
    {
      key: 'errorApis',
      label: <span><BugOutlined /> Error-Prone APIs</span>,
      children: (
        <Card title="APIs with Highest Error Rates" size="small">
          {errorProneApis.length > 0 ? (
            <Table
              dataSource={errorProneApis}
              columns={errorApisColumns}
              pagination={false}
              size="small"
              rowKey={(record) => `${record.method}-${record.path}`}
            />
          ) : (
            <Empty description="No error-prone APIs detected" />
          )}
        </Card>
      ),
    },
    {
      key: 'apiStats',
      label: <span><UnorderedListOutlined /> API Detail Stats</span>,
      children: (
        <Card title="All API Statistics (Method + Path)" size="small">
          <Table
            dataSource={apiStats}
            columns={apiStatsColumns}
            pagination={{ pageSize: 15, showSizeChanger: true, showTotal: (total) => `Total: ${total} APIs` }}
            size="small"
            rowKey={(record) => `${record.method}-${record.path}`}
          />
        </Card>
      ),
    },
  ];

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 24 }}>
        <h2 style={{ margin: 0 }}>
          <ApiOutlined /> Dashboard & Analytics
        </h2>
        <Tag
          icon={<ReloadOutlined spin={loading} />}
          color="processing"
          style={{ cursor: 'pointer' }}
          onClick={loadAllData}
        >
          {loading ? 'Refreshing...' : 'Refresh (auto: 30s)'}
        </Tag>
      </div>

      {/* Overview Statistics */}
      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        <Col xs={24} sm={12} lg={8}>
          <Card>
            <Statistic
              title="Total Requests"
              value={overview?.totalRequests || 0}
              prefix={<ApiOutlined />}
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={8}>
          <Card>
            <Statistic
              title="Success Rate"
              value={overview?.successRate || 0}
              precision={1}
              suffix="%"
              prefix={<CheckCircleOutlined />}
              valueStyle={{ color: overview && overview.successRate >= 95 ? '#3f8600' : '#cf1322' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={8}>
          <Card>
            <Statistic
              title="Error Rate"
              value={overview?.errorRate || 0}
              precision={1}
              suffix="%"
              prefix={<CloseCircleOutlined />}
              valueStyle={{ color: overview && overview.errorRate > 5 ? '#cf1322' : '#3f8600' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={8}>
          <Card>
            <Statistic
              title="Avg Response Time"
              value={overview?.avgResponseTime || 0}
              precision={0}
              suffix="ms"
              prefix={<ClockCircleOutlined />}
              valueStyle={{
                color: overview && overview.avgResponseTime > 1000 ? '#cf1322' : '#3f8600',
              }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={8}>
          <Card>
            <Statistic
              title="Recent Requests (1 hour)"
              value={overview?.recentRequestsPerHour || 0}
              prefix={<ThunderboltOutlined />}
              valueStyle={{ color: '#722ed1' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={8}>
          <Card>
            <Statistic
              title="Last Updated"
              value={new Date().toLocaleTimeString()}
              prefix={<ClockCircleOutlined />}
            />
          </Card>
        </Col>
      </Row>

      {/* Analytics Tabs */}
      <Card>
        <Tabs items={tabItems} defaultActiveKey="traffic" />
      </Card>
    </div>
  );
};

export default DashboardPage;
