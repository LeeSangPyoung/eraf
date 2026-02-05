import React from 'react';
import { Card, Row, Col, Statistic, Typography, List, Tag, Progress } from 'antd';
import {
  FileExcelOutlined,
  SafetyOutlined,
  ThunderboltOutlined,
  ApartmentOutlined,
  ToolOutlined,
  BellOutlined,
  ClockCircleOutlined,
  DatabaseOutlined,
  DashboardOutlined,
  CheckCircleOutlined,
} from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';

const { Title, Paragraph } = Typography;

const featureGroups = [
  {
    title: 'Document & Media',
    icon: <FileExcelOutlined />,
    color: '#52c41a',
    count: 12,
    range: '#1~#12',
    path: '/document/excel',
    features: ['Excel Download/Upload', 'PDF Generation', 'Barcode/QR', 'Image Processing'],
  },
  {
    title: 'Auth & Security',
    icon: <SafetyOutlined />,
    color: '#1890ff',
    count: 8,
    range: '#13~#20',
    path: '/security/jwt',
    features: ['JWT Login', 'Bot Detection', 'Session Management', 'Audit Log'],
  },
  {
    title: 'Resilience',
    icon: <ThunderboltOutlined />,
    color: '#faad14',
    count: 8,
    range: '#21~#28',
    path: '/resilience',
    features: ['Circuit Breaker', 'Rate Limiter', 'Retry', 'Timeout', 'Bulkhead'],
  },
  {
    title: 'State & Saga',
    icon: <ApartmentOutlined />,
    color: '#722ed1',
    count: 14,
    range: '#29~#42',
    path: '/statemachine',
    features: ['State Machine', 'Saga Orchestration', 'Compensation'],
  },
  {
    title: 'Utilities',
    icon: <ToolOutlined />,
    color: '#eb2f96',
    count: 7,
    range: '#43~#49',
    path: '/utility',
    features: ['Encryption', 'Hashing', 'Masking', 'ID Generation'],
  },
  {
    title: 'Notification & Kafka',
    icon: <BellOutlined />,
    color: '#13c2c2',
    count: 6,
    range: '#50~#55',
    path: '/notification',
    features: ['Email', 'SMS', 'Push', 'Kafka Messaging'],
  },
  {
    title: 'Scheduler & Batch',
    icon: <ClockCircleOutlined />,
    color: '#fa8c16',
    count: 4,
    range: '#56~#59',
    path: '/scheduler',
    features: ['Job Scheduling', 'Batch Processing', 'Monitoring'],
  },
  {
    title: 'Cache & Redis',
    icon: <DatabaseOutlined />,
    color: '#f5222d',
    count: 4,
    range: '#60~#63',
    path: '/cache',
    features: ['Cache Management', 'Redis Operations', 'Pub/Sub'],
  },
  {
    title: 'Monitoring & API',
    icon: <DashboardOutlined />,
    color: '#2f54eb',
    count: 4,
    range: '#64~#67',
    path: '/monitoring/health',
    features: ['Health Check', 'Metrics', 'Tracing', 'Swagger'],
  },
];

const Dashboard: React.FC = () => {
  const navigate = useNavigate();

  return (
    <div>
      <Title level={2}>
        <DashboardOutlined /> ERAF Sample Application
      </Title>
      <Paragraph type="secondary">
        Enterprise Reusable Asset Factory - 67 Features Demo
      </Paragraph>

      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        <Col span={6}>
          <Card>
            <Statistic
              title="Total Features"
              value={67}
              prefix={<CheckCircleOutlined />}
              valueStyle={{ color: '#52c41a' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic title="Modules" value={26} valueStyle={{ color: '#1890ff' }} />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic title="Feature Groups" value={9} valueStyle={{ color: '#722ed1' }} />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic title="Implementation" value="100%" valueStyle={{ color: '#52c41a' }} />
            <Progress percent={100} showInfo={false} status="success" />
          </Card>
        </Col>
      </Row>

      <Title level={4}>Feature Groups</Title>
      <Row gutter={[16, 16]}>
        {featureGroups.map((group) => (
          <Col span={8} key={group.title}>
            <Card
              hoverable
              onClick={() => navigate(group.path)}
              style={{ height: '100%' }}
            >
              <div style={{ display: 'flex', alignItems: 'center', marginBottom: 16 }}>
                <div
                  style={{
                    fontSize: 24,
                    color: group.color,
                    marginRight: 12,
                  }}
                >
                  {group.icon}
                </div>
                <div>
                  <Title level={5} style={{ margin: 0 }}>
                    {group.title}
                  </Title>
                  <Tag color={group.color}>{group.range}</Tag>
                </div>
              </div>
              <List
                size="small"
                dataSource={group.features}
                renderItem={(item) => (
                  <List.Item style={{ padding: '4px 0', border: 'none' }}>
                    <CheckCircleOutlined style={{ color: '#52c41a', marginRight: 8 }} />
                    {item}
                  </List.Item>
                )}
              />
            </Card>
          </Col>
        ))}
      </Row>
    </div>
  );
};

export default Dashboard;
