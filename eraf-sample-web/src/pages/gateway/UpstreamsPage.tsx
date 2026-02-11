import React, { useState, useEffect } from 'react';
import {
  Card,
  Table,
  Button,
  Space,
  Tag,
  Modal,
  Form,
  Input,
  InputNumber,
  Switch,
  Select,
  message,
  Tooltip,
  Typography,
  Row,
  Col,
  Statistic,
  Tabs,
} from 'antd';
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  ReloadOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  CloudServerOutlined,
} from '@ant-design/icons';
import { gateway } from '../../services/api';

const { Text } = Typography;

interface Upstream {
  id: number;
  name: string;
  description?: string;
  algorithm: string;
  hashOn: string;
  hashOnHeader?: string;
  hashFallback: string;
  slots: number;
  healthcheckActiveEnabled: boolean;
  healthcheckActivePath: string;
  healthcheckActiveInterval: number;
  healthcheckActiveTimeout: number;
  healthcheckActiveHealthySuccesses: number;
  healthcheckActiveUnhealthyFailures: number;
  healthcheckActiveHealthyStatuses?: number[];
  healthcheckActiveUnhealthyStatuses?: number[];
  healthcheckPassiveEnabled: boolean;
  healthcheckPassiveUnhealthyFailures: number;
  healthcheckPassiveUnhealthyTimeouts: number;
  enabled: boolean;
  targetCount: number;
  tags?: Record<string, string>;
  createdAt: string;
  updatedAt: string;
}

interface UpstreamStats {
  totalUpstreams: number;
  enabledUpstreams: number;
  disabledUpstreams: number;
}

const ALGORITHMS = [
  { value: 'ROUND_ROBIN', label: 'Round Robin' },
  { value: 'LEAST_CONNECTIONS', label: 'Least Connections' },
  { value: 'IP_HASH', label: 'IP Hash' },
  { value: 'CONSISTENT_HASH', label: 'Consistent Hash' },
  { value: 'WEIGHTED', label: 'Weighted' },
];

const HASH_ON_OPTIONS = [
  { value: 'NONE', label: 'None' },
  { value: 'CONSUMER', label: 'Consumer' },
  { value: 'IP', label: 'IP' },
  { value: 'HEADER', label: 'Header' },
  { value: 'COOKIE', label: 'Cookie' },
];

const UpstreamsPage: React.FC = () => {
  const [upstreams, setUpstreams] = useState<Upstream[]>([]);
  const [stats, setStats] = useState<UpstreamStats | null>(null);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [editingUpstream, setEditingUpstream] = useState<Upstream | null>(null);
  const [form] = Form.useForm();

  useEffect(() => {
    loadUpstreams();
    loadStats();
  }, []);

  const loadUpstreams = async () => {
    setLoading(true);
    try {
      const response = await gateway.getUpstreams();
      setUpstreams(response.data);
    } catch (error) {
      message.error('Failed to load upstreams');
    } finally {
      setLoading(false);
    }
  };

  const loadStats = async () => {
    try {
      const response = await gateway.getUpstreamStats();
      setStats(response.data);
    } catch (error) {
      console.error('Failed to load stats:', error);
    }
  };

  const handleCreate = () => {
    setEditingUpstream(null);
    form.resetFields();
    form.setFieldsValue({
      algorithm: 'ROUND_ROBIN',
      hashOn: 'NONE',
      hashFallback: 'NONE',
      slots: 10000,
      healthcheckActiveEnabled: true,
      healthcheckActivePath: '/',
      healthcheckActiveInterval: 30,
      healthcheckActiveTimeout: 5,
      healthcheckActiveHealthySuccesses: 2,
      healthcheckActiveUnhealthyFailures: 3,
      healthcheckPassiveEnabled: true,
      healthcheckPassiveUnhealthyFailures: 5,
      healthcheckPassiveUnhealthyTimeouts: 3,
      enabled: true,
    });
    setModalVisible(true);
  };

  const handleEdit = (record: Upstream) => {
    setEditingUpstream(record);
    form.setFieldsValue({ ...record });
    setModalVisible(true);
  };

  const handleDelete = (record: Upstream) => {
    Modal.confirm({
      title: 'Delete Upstream',
      content: `Are you sure you want to delete upstream "${record.name}"?`,
      okText: 'Delete',
      okType: 'danger',
      onOk: async () => {
        try {
          await gateway.deleteUpstream(record.id);
          message.success('Upstream deleted successfully');
          loadUpstreams();
          loadStats();
        } catch (error) {
          message.error('Failed to delete upstream');
        }
      },
    });
  };

  const handleToggle = async (record: Upstream) => {
    try {
      await gateway.toggleUpstream(record.id);
      message.success(`Upstream ${record.enabled ? 'disabled' : 'enabled'} successfully`);
      loadUpstreams();
      loadStats();
    } catch (error) {
      message.error('Failed to toggle upstream');
    }
  };

  const handleModalOk = async () => {
    try {
      const values = await form.validateFields();
      if (editingUpstream) {
        await gateway.updateUpstream(editingUpstream.id, values);
        message.success('Upstream updated successfully');
      } else {
        await gateway.createUpstream(values);
        message.success('Upstream created successfully');
      }
      setModalVisible(false);
      loadUpstreams();
      loadStats();
    } catch (error: any) {
      if (error.response?.data?.message) {
        message.error(error.response.data.message);
      }
    }
  };

  const algorithmHashOn = Form.useWatch('hashOn', form);

  const columns = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      width: 60,
    },
    {
      title: 'Name',
      dataIndex: 'name',
      key: 'name',
      width: 180,
      render: (name: string, record: Upstream) => (
        <Space direction="vertical" size={0}>
          <Text strong>{name}</Text>
          {record.description && (
            <Text type="secondary" style={{ fontSize: 12 }}>{record.description}</Text>
          )}
        </Space>
      ),
    },
    {
      title: 'Algorithm',
      dataIndex: 'algorithm',
      key: 'algorithm',
      width: 150,
      render: (algorithm: string) => {
        const colorMap: Record<string, string> = {
          ROUND_ROBIN: 'blue',
          LEAST_CONNECTIONS: 'cyan',
          IP_HASH: 'purple',
          CONSISTENT_HASH: 'magenta',
          WEIGHTED: 'orange',
        };
        return <Tag color={colorMap[algorithm] || 'default'}>{algorithm}</Tag>;
      },
    },
    {
      title: 'Targets',
      dataIndex: 'targetCount',
      key: 'targetCount',
      width: 80,
      align: 'center' as const,
      render: (count: number) => <Tag>{count}</Tag>,
    },
    {
      title: 'Health Check',
      key: 'healthcheck',
      width: 120,
      render: (_: any, record: Upstream) => (
        <Space direction="vertical" size={0}>
          <Tag color={record.healthcheckActiveEnabled ? 'green' : 'default'}>
            Active: {record.healthcheckActiveEnabled ? 'ON' : 'OFF'}
          </Tag>
          <Tag color={record.healthcheckPassiveEnabled ? 'green' : 'default'}>
            Passive: {record.healthcheckPassiveEnabled ? 'ON' : 'OFF'}
          </Tag>
        </Space>
      ),
    },
    {
      title: 'Slots',
      dataIndex: 'slots',
      key: 'slots',
      width: 80,
      align: 'center' as const,
    },
    {
      title: 'Status',
      dataIndex: 'enabled',
      key: 'enabled',
      width: 100,
      align: 'center' as const,
      render: (enabled: boolean) =>
        enabled ? (
          <Tag icon={<CheckCircleOutlined />} color="success">Enabled</Tag>
        ) : (
          <Tag icon={<CloseCircleOutlined />} color="default">Disabled</Tag>
        ),
    },
    {
      title: 'Actions',
      key: 'actions',
      width: 180,
      render: (_: any, record: Upstream) => (
        <Space size="small">
          <Tooltip title="Edit">
            <Button type="text" icon={<EditOutlined />} onClick={() => handleEdit(record)} />
          </Tooltip>
          <Tooltip title={record.enabled ? 'Disable' : 'Enable'}>
            <Switch size="small" checked={record.enabled} onChange={() => handleToggle(record)} />
          </Tooltip>
          <Tooltip title="Delete">
            <Button type="text" danger icon={<DeleteOutlined />} onClick={() => handleDelete(record)} />
          </Tooltip>
        </Space>
      ),
    },
  ];

  return (
    <div>
      {stats && (
        <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
          <Col span={8}>
            <Card><Statistic title="Total Upstreams" value={stats.totalUpstreams} prefix={<CloudServerOutlined />} /></Card>
          </Col>
          <Col span={8}>
            <Card><Statistic title="Enabled" value={stats.enabledUpstreams} prefix={<CheckCircleOutlined />} valueStyle={{ color: '#3f8600' }} /></Card>
          </Col>
          <Col span={8}>
            <Card><Statistic title="Disabled" value={stats.disabledUpstreams} prefix={<CloseCircleOutlined />} valueStyle={{ color: '#999' }} /></Card>
          </Col>
        </Row>
      )}

      <Card
        title={<Space><CloudServerOutlined /> Upstreams</Space>}
        extra={
          <Space>
            <Button icon={<ReloadOutlined />} onClick={loadUpstreams}>Refresh</Button>
            <Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>Create Upstream</Button>
          </Space>
        }
      >
        <Table
          columns={columns}
          dataSource={upstreams}
          rowKey="id"
          loading={loading}
          pagination={{ pageSize: 10 }}
          scroll={{ x: 1200 }}
        />
      </Card>

      <Modal
        title={editingUpstream ? 'Edit Upstream' : 'Create Upstream'}
        open={modalVisible}
        onOk={handleModalOk}
        onCancel={() => setModalVisible(false)}
        width={700}
      >
        <Form form={form} layout="vertical">
          <Tabs
            items={[
              {
                key: 'basic',
                label: 'Basic',
                children: (
                  <>
                    <Form.Item
                      name="name"
                      label="Name"
                      rules={[
                        { required: true, message: 'Please input name' },
                        { pattern: /^[a-zA-Z0-9_-]{2,100}$/, message: '2-100 chars, alphanumeric with _/-' },
                      ]}
                    >
                      <Input placeholder="e.g., my-upstream" />
                    </Form.Item>
                    <Form.Item name="description" label="Description">
                      <Input.TextArea rows={2} placeholder="Description" />
                    </Form.Item>
                    <Row gutter={16}>
                      <Col span={12}>
                        <Form.Item name="algorithm" label="Algorithm" rules={[{ required: true }]}>
                          <Select options={ALGORITHMS} />
                        </Form.Item>
                      </Col>
                      <Col span={12}>
                        <Form.Item name="slots" label="Slots">
                          <InputNumber min={10} max={65536} style={{ width: '100%' }} />
                        </Form.Item>
                      </Col>
                    </Row>
                    <Row gutter={16}>
                      <Col span={8}>
                        <Form.Item name="hashOn" label="Hash On">
                          <Select options={HASH_ON_OPTIONS} />
                        </Form.Item>
                      </Col>
                      <Col span={8}>
                        <Form.Item name="hashFallback" label="Hash Fallback">
                          <Select options={HASH_ON_OPTIONS} />
                        </Form.Item>
                      </Col>
                      {algorithmHashOn === 'HEADER' && (
                        <Col span={8}>
                          <Form.Item name="hashOnHeader" label="Hash Header">
                            <Input placeholder="X-Custom-Header" />
                          </Form.Item>
                        </Col>
                      )}
                    </Row>
                    <Form.Item name="enabled" label="Enabled" valuePropName="checked">
                      <Switch />
                    </Form.Item>
                  </>
                ),
              },
              {
                key: 'active',
                label: 'Active Health Check',
                children: (
                  <>
                    <Form.Item name="healthcheckActiveEnabled" label="Enabled" valuePropName="checked">
                      <Switch />
                    </Form.Item>
                    <Row gutter={16}>
                      <Col span={12}>
                        <Form.Item name="healthcheckActivePath" label="Path">
                          <Input placeholder="/" />
                        </Form.Item>
                      </Col>
                      <Col span={12}>
                        <Form.Item name="healthcheckActiveInterval" label="Interval (sec)">
                          <InputNumber min={1} style={{ width: '100%' }} />
                        </Form.Item>
                      </Col>
                    </Row>
                    <Row gutter={16}>
                      <Col span={12}>
                        <Form.Item name="healthcheckActiveTimeout" label="Timeout (sec)">
                          <InputNumber min={1} style={{ width: '100%' }} />
                        </Form.Item>
                      </Col>
                      <Col span={12}>
                        <Form.Item name="healthcheckActiveHealthySuccesses" label="Healthy Successes">
                          <InputNumber min={1} style={{ width: '100%' }} />
                        </Form.Item>
                      </Col>
                    </Row>
                    <Form.Item name="healthcheckActiveUnhealthyFailures" label="Unhealthy Failures">
                      <InputNumber min={1} style={{ width: '100%' }} />
                    </Form.Item>
                  </>
                ),
              },
              {
                key: 'passive',
                label: 'Passive Health Check',
                children: (
                  <>
                    <Form.Item name="healthcheckPassiveEnabled" label="Enabled" valuePropName="checked">
                      <Switch />
                    </Form.Item>
                    <Row gutter={16}>
                      <Col span={12}>
                        <Form.Item name="healthcheckPassiveUnhealthyFailures" label="Unhealthy Failures">
                          <InputNumber min={1} style={{ width: '100%' }} />
                        </Form.Item>
                      </Col>
                      <Col span={12}>
                        <Form.Item name="healthcheckPassiveUnhealthyTimeouts" label="Unhealthy Timeouts">
                          <InputNumber min={1} style={{ width: '100%' }} />
                        </Form.Item>
                      </Col>
                    </Row>
                  </>
                ),
              },
            ]}
          />
        </Form>
      </Modal>
    </div>
  );
};

export default UpstreamsPage;
