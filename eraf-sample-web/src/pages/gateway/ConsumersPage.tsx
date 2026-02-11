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
  message,
  Tooltip,
  Typography,
  Row,
  Col,
  Statistic,
} from 'antd';
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  KeyOutlined,
  CopyOutlined,
  ReloadOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
} from '@ant-design/icons';
import { gateway } from '../../services/api';

const { Text } = Typography;

interface Consumer {
  id: number;
  username: string;
  apiKey: string;
  description?: string;
  rateLimit?: number;
  rateLimitWindowSeconds: number;
  enabled: boolean;
  tags?: Record<string, string>;
  customId?: string;
  createdAt: string;
  updatedAt: string;
}

interface ConsumerStats {
  totalConsumers: number;
  enabledConsumers: number;
  disabledConsumers: number;
}

const ConsumersPage: React.FC = () => {
  const [consumers, setConsumers] = useState<Consumer[]>([]);
  const [stats, setStats] = useState<ConsumerStats | null>(null);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [editingConsumer, setEditingConsumer] = useState<Consumer | null>(null);
  const [form] = Form.useForm();

  useEffect(() => {
    loadConsumers();
    loadStats();
  }, []);

  const loadConsumers = async () => {
    setLoading(true);
    try {
      const response = await gateway.getConsumers();
      setConsumers(response.data);
    } catch (error) {
      message.error('Failed to load consumers');
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  const loadStats = async () => {
    try {
      const response = await gateway.getConsumerStats();
      setStats(response.data);
    } catch (error) {
      console.error('Failed to load stats:', error);
    }
  };

  const handleCreate = () => {
    setEditingConsumer(null);
    form.resetFields();
    form.setFieldsValue({ enabled: true, rateLimitWindowSeconds: 60 });
    setModalVisible(true);
  };

  const handleEdit = (consumer: Consumer) => {
    setEditingConsumer(consumer);
    form.setFieldsValue({
      username: consumer.username,
      description: consumer.description,
      rateLimit: consumer.rateLimit,
      rateLimitWindowSeconds: consumer.rateLimitWindowSeconds,
      enabled: consumer.enabled,
      customId: consumer.customId,
    });
    setModalVisible(true);
  };

  const handleDelete = (consumer: Consumer) => {
    Modal.confirm({
      title: 'Delete Consumer',
      content: `Are you sure you want to delete consumer "${consumer.username}"?`,
      okText: 'Delete',
      okType: 'danger',
      onOk: async () => {
        try {
          await gateway.deleteConsumer(consumer.id);
          message.success('Consumer deleted successfully');
          loadConsumers();
          loadStats();
        } catch (error) {
          message.error('Failed to delete consumer');
          console.error(error);
        }
      },
    });
  };

  const handleToggle = async (consumer: Consumer) => {
    try {
      await gateway.toggleConsumer(consumer.id);
      message.success(`Consumer ${consumer.enabled ? 'disabled' : 'enabled'} successfully`);
      loadConsumers();
      loadStats();
    } catch (error) {
      message.error('Failed to toggle consumer');
      console.error(error);
    }
  };

  const handleRegenerateApiKey = (consumer: Consumer) => {
    Modal.confirm({
      title: 'Regenerate API Key',
      content: `Are you sure you want to regenerate API key for "${consumer.username}"? The old key will be invalidated.`,
      okText: 'Regenerate',
      okType: 'danger',
      onOk: async () => {
        try {
          const response = await gateway.regenerateApiKey(consumer.id);
          message.success('API key regenerated successfully');
          const newApiKey = response.data.apiKey;
          Modal.info({
            title: 'New API Key Generated',
            content: (
              <div>
                <p>Please save this API key securely. You won't be able to see it again.</p>
                <Text code copyable style={{ fontSize: 12 }}>
                  {newApiKey}
                </Text>
              </div>
            ),
          });
          loadConsumers();
        } catch (error) {
          message.error('Failed to regenerate API key');
          console.error(error);
        }
      },
    });
  };

  const handleCopyApiKey = (apiKey: string) => {
    navigator.clipboard.writeText(apiKey);
    message.success('API key copied to clipboard');
  };

  const handleModalOk = async () => {
    try {
      const values = await form.validateFields();
      if (editingConsumer) {
        await gateway.updateConsumer(editingConsumer.id, values);
        message.success('Consumer updated successfully');
      } else {
        const response = await gateway.createConsumer(values);
        message.success('Consumer created successfully');
        const newApiKey = response.data.apiKey;
        Modal.info({
          title: 'Consumer Created',
          content: (
            <div>
              <p>Consumer created successfully! Please save this API key securely.</p>
              <Text code copyable style={{ fontSize: 12 }}>
                {newApiKey}
              </Text>
            </div>
          ),
        });
      }
      setModalVisible(false);
      loadConsumers();
      loadStats();
    } catch (error) {
      console.error('Validation failed:', error);
    }
  };

  const columns = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      width: 70,
    },
    {
      title: 'Username',
      dataIndex: 'username',
      key: 'username',
      width: 180,
      render: (username: string, record: Consumer) => (
        <Space direction="vertical" size={0}>
          <Text strong>{username}</Text>
          {record.customId && <Text type="secondary" style={{ fontSize: 12 }}>ID: {record.customId}</Text>}
        </Space>
      ),
    },
    {
      title: 'API Key',
      dataIndex: 'apiKey',
      key: 'apiKey',
      width: 200,
      render: (apiKey: string) => (
        <Space>
          <Text code style={{ fontSize: 11 }}>
            {apiKey.substring(0, 20)}...
          </Text>
          <Tooltip title="Copy API Key">
            <Button
              type="text"
              size="small"
              icon={<CopyOutlined />}
              onClick={() => handleCopyApiKey(apiKey)}
            />
          </Tooltip>
        </Space>
      ),
    },
    {
      title: 'Description',
      dataIndex: 'description',
      key: 'description',
      ellipsis: true,
    },
    {
      title: 'Rate Limit',
      dataIndex: 'rateLimit',
      key: 'rateLimit',
      width: 150,
      render: (rateLimit: number | undefined, record: Consumer) =>
        rateLimit ? (
          <Tag color="blue">
            {rateLimit} req/{record.rateLimitWindowSeconds}s
          </Tag>
        ) : (
          <Tag color="default">Unlimited</Tag>
        ),
    },
    {
      title: 'Status',
      dataIndex: 'enabled',
      key: 'enabled',
      width: 100,
      align: 'center' as const,
      render: (enabled: boolean) =>
        enabled ? (
          <Tag icon={<CheckCircleOutlined />} color="success">
            Enabled
          </Tag>
        ) : (
          <Tag icon={<CloseCircleOutlined />} color="default">
            Disabled
          </Tag>
        ),
    },
    {
      title: 'Actions',
      key: 'actions',
      width: 250,
      render: (_: any, record: Consumer) => (
        <Space size="small">
          <Tooltip title="Edit">
            <Button
              type="text"
              icon={<EditOutlined />}
              onClick={() => handleEdit(record)}
            />
          </Tooltip>
          <Tooltip title={record.enabled ? 'Disable' : 'Enable'}>
            <Switch
              size="small"
              checked={record.enabled}
              onChange={() => handleToggle(record)}
            />
          </Tooltip>
          <Tooltip title="Regenerate API Key">
            <Button
              type="text"
              icon={<KeyOutlined />}
              onClick={() => handleRegenerateApiKey(record)}
            />
          </Tooltip>
          <Tooltip title="Delete">
            <Button
              type="text"
              danger
              icon={<DeleteOutlined />}
              onClick={() => handleDelete(record)}
            />
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
            <Card>
              <Statistic
                title="Total Consumers"
                value={stats.totalConsumers}
                prefix={<KeyOutlined />}
              />
            </Card>
          </Col>
          <Col span={8}>
            <Card>
              <Statistic
                title="Enabled Consumers"
                value={stats.enabledConsumers}
                prefix={<CheckCircleOutlined />}
                valueStyle={{ color: '#3f8600' }}
              />
            </Card>
          </Col>
          <Col span={8}>
            <Card>
              <Statistic
                title="Disabled Consumers"
                value={stats.disabledConsumers}
                prefix={<CloseCircleOutlined />}
                valueStyle={{ color: '#999' }}
              />
            </Card>
          </Col>
        </Row>
      )}

      <Card
        title={
          <Space>
            <KeyOutlined />
            API Consumers
          </Space>
        }
        extra={
          <Space>
            <Button icon={<ReloadOutlined />} onClick={loadConsumers}>
              Refresh
            </Button>
            <Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>
              Create Consumer
            </Button>
          </Space>
        }
      >
        <Table
          columns={columns}
          dataSource={consumers}
          rowKey="id"
          loading={loading}
          pagination={{ pageSize: 10 }}
          scroll={{ x: 1200 }}
        />
      </Card>

      <Modal
        title={editingConsumer ? 'Edit Consumer' : 'Create Consumer'}
        open={modalVisible}
        onOk={handleModalOk}
        onCancel={() => setModalVisible(false)}
        width={600}
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="username"
            label="Username"
            rules={[
              { required: true, message: 'Please input username' },
              {
                pattern: /^[a-zA-Z0-9_-]{3,50}$/,
                message: 'Username must be 3-50 characters, alphanumeric with underscore/hyphen',
              },
            ]}
          >
            <Input placeholder="e.g., my-app-consumer" />
          </Form.Item>

          <Form.Item name="description" label="Description">
            <Input.TextArea rows={2} placeholder="Description of this consumer" />
          </Form.Item>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="rateLimit"
                label="Rate Limit (requests)"
                tooltip="Leave empty for unlimited"
              >
                <InputNumber
                  min={1}
                  style={{ width: '100%' }}
                  placeholder="e.g., 1000"
                />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="rateLimitWindowSeconds"
                label="Window (seconds)"
                initialValue={60}
              >
                <InputNumber
                  min={1}
                  style={{ width: '100%' }}
                  placeholder="e.g., 60"
                />
              </Form.Item>
            </Col>
          </Row>

          <Form.Item name="customId" label="Custom ID" tooltip="Optional external system ID">
            <Input placeholder="e.g., ext-12345" />
          </Form.Item>

          <Form.Item name="enabled" label="Enabled" valuePropName="checked" initialValue={true}>
            <Switch />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default ConsumersPage;
