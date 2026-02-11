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
} from 'antd';
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  ReloadOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  TeamOutlined,
  UserAddOutlined,
  UserDeleteOutlined,
} from '@ant-design/icons';
import { gateway } from '../../services/api';

const { Text } = Typography;

interface ConsumerSummary {
  id: number;
  username: string;
  enabled: boolean;
}

interface ConsumerGroup {
  id: number;
  name: string;
  description?: string;
  rateLimit?: number;
  rateLimitWindowSeconds: number;
  enabled: boolean;
  memberCount: number;
  members: ConsumerSummary[];
  tags?: Record<string, string>;
  createdAt: string;
  updatedAt: string;
}

interface ConsumerGroupStats {
  totalGroups: number;
  enabledGroups: number;
  disabledGroups: number;
}

interface Consumer {
  id: number;
  username: string;
  enabled: boolean;
}

const ConsumerGroupsPage: React.FC = () => {
  const [groups, setGroups] = useState<ConsumerGroup[]>([]);
  const [consumers, setConsumers] = useState<Consumer[]>([]);
  const [stats, setStats] = useState<ConsumerGroupStats | null>(null);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [memberModalVisible, setMemberModalVisible] = useState(false);
  const [editingGroup, setEditingGroup] = useState<ConsumerGroup | null>(null);
  const [selectedGroup, setSelectedGroup] = useState<ConsumerGroup | null>(null);
  const [selectedConsumerId, setSelectedConsumerId] = useState<number | null>(null);
  const [form] = Form.useForm();

  useEffect(() => {
    loadGroups();
    loadStats();
    loadConsumers();
  }, []);

  const loadGroups = async () => {
    setLoading(true);
    try {
      const response = await gateway.getConsumerGroups();
      setGroups(response.data);
    } catch (error) {
      message.error('Failed to load consumer groups');
    } finally {
      setLoading(false);
    }
  };

  const loadStats = async () => {
    try {
      const response = await gateway.getConsumerGroupStats();
      setStats(response.data);
    } catch (error) {
      console.error('Failed to load stats:', error);
    }
  };

  const loadConsumers = async () => {
    try {
      const response = await gateway.getConsumers();
      setConsumers(response.data);
    } catch (error) {
      console.error('Failed to load consumers:', error);
    }
  };

  const handleCreate = () => {
    setEditingGroup(null);
    form.resetFields();
    form.setFieldsValue({ enabled: true, rateLimitWindowSeconds: 60 });
    setModalVisible(true);
  };

  const handleEdit = (record: ConsumerGroup) => {
    setEditingGroup(record);
    form.setFieldsValue({
      name: record.name,
      description: record.description,
      rateLimit: record.rateLimit,
      rateLimitWindowSeconds: record.rateLimitWindowSeconds,
      enabled: record.enabled,
    });
    setModalVisible(true);
  };

  const handleDelete = (record: ConsumerGroup) => {
    Modal.confirm({
      title: 'Delete Consumer Group',
      content: `Are you sure you want to delete group "${record.name}"?`,
      okText: 'Delete',
      okType: 'danger',
      onOk: async () => {
        try {
          await gateway.deleteConsumerGroup(record.id);
          message.success('Consumer group deleted successfully');
          loadGroups();
          loadStats();
        } catch (error) {
          message.error('Failed to delete consumer group');
        }
      },
    });
  };

  const handleToggle = async (record: ConsumerGroup) => {
    try {
      await gateway.toggleConsumerGroup(record.id);
      message.success(`Group ${record.enabled ? 'disabled' : 'enabled'} successfully`);
      loadGroups();
      loadStats();
    } catch (error) {
      message.error('Failed to toggle consumer group');
    }
  };

  const handleModalOk = async () => {
    try {
      const values = await form.validateFields();
      if (editingGroup) {
        await gateway.updateConsumerGroup(editingGroup.id, values);
        message.success('Consumer group updated successfully');
      } else {
        await gateway.createConsumerGroup(values);
        message.success('Consumer group created successfully');
      }
      setModalVisible(false);
      loadGroups();
      loadStats();
    } catch (error: any) {
      if (error.response?.data?.message) {
        message.error(error.response.data.message);
      }
    }
  };

  const handleManageMembers = (record: ConsumerGroup) => {
    setSelectedGroup(record);
    setSelectedConsumerId(null);
    setMemberModalVisible(true);
  };

  const handleAddConsumer = async () => {
    if (!selectedGroup || !selectedConsumerId) return;
    try {
      await gateway.addConsumerToGroup(selectedGroup.id, selectedConsumerId);
      message.success('Consumer added to group');
      setSelectedConsumerId(null);
      loadGroups();
      // Reload the selected group
      const response = await gateway.getConsumerGroup(selectedGroup.id);
      setSelectedGroup(response.data);
    } catch (error: any) {
      message.error(error.response?.data?.message || 'Failed to add consumer');
    }
  };

  const handleRemoveConsumer = async (consumerId: number) => {
    if (!selectedGroup) return;
    try {
      await gateway.removeConsumerFromGroup(selectedGroup.id, consumerId);
      message.success('Consumer removed from group');
      loadGroups();
      const response = await gateway.getConsumerGroup(selectedGroup.id);
      setSelectedGroup(response.data);
    } catch (error: any) {
      message.error(error.response?.data?.message || 'Failed to remove consumer');
    }
  };

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
      render: (name: string, record: ConsumerGroup) => (
        <Space direction="vertical" size={0}>
          <Text strong>{name}</Text>
          {record.description && (
            <Text type="secondary" style={{ fontSize: 12 }}>{record.description}</Text>
          )}
        </Space>
      ),
    },
    {
      title: 'Members',
      dataIndex: 'memberCount',
      key: 'memberCount',
      width: 100,
      align: 'center' as const,
      render: (count: number, record: ConsumerGroup) => (
        <Button type="link" onClick={() => handleManageMembers(record)}>
          <TeamOutlined /> {count}
        </Button>
      ),
    },
    {
      title: 'Rate Limit',
      dataIndex: 'rateLimit',
      key: 'rateLimit',
      width: 150,
      render: (rateLimit: number | undefined, record: ConsumerGroup) =>
        rateLimit ? (
          <Tag color="blue">{rateLimit} req/{record.rateLimitWindowSeconds}s</Tag>
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
          <Tag icon={<CheckCircleOutlined />} color="success">Enabled</Tag>
        ) : (
          <Tag icon={<CloseCircleOutlined />} color="default">Disabled</Tag>
        ),
    },
    {
      title: 'Created',
      dataIndex: 'createdAt',
      key: 'createdAt',
      width: 140,
      render: (date: string) => date ? date.substring(0, 10) : '-',
    },
    {
      title: 'Actions',
      key: 'actions',
      width: 220,
      render: (_: any, record: ConsumerGroup) => (
        <Space size="small">
          <Tooltip title="Manage Members">
            <Button type="text" icon={<TeamOutlined />} onClick={() => handleManageMembers(record)} />
          </Tooltip>
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

  // Filter consumers not already in the selected group
  const availableConsumers = selectedGroup
    ? consumers.filter(
        (c) => !selectedGroup.members?.some((m) => m.id === c.id)
      )
    : consumers;

  return (
    <div>
      {stats && (
        <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
          <Col span={8}>
            <Card><Statistic title="Total Groups" value={stats.totalGroups} prefix={<TeamOutlined />} /></Card>
          </Col>
          <Col span={8}>
            <Card><Statistic title="Enabled" value={stats.enabledGroups} prefix={<CheckCircleOutlined />} valueStyle={{ color: '#3f8600' }} /></Card>
          </Col>
          <Col span={8}>
            <Card><Statistic title="Disabled" value={stats.disabledGroups} prefix={<CloseCircleOutlined />} valueStyle={{ color: '#999' }} /></Card>
          </Col>
        </Row>
      )}

      <Card
        title={<Space><TeamOutlined /> Consumer Groups</Space>}
        extra={
          <Space>
            <Button icon={<ReloadOutlined />} onClick={loadGroups}>Refresh</Button>
            <Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>Create Group</Button>
          </Space>
        }
      >
        <Table
          columns={columns}
          dataSource={groups}
          rowKey="id"
          loading={loading}
          pagination={{ pageSize: 10 }}
          scroll={{ x: 1200 }}
        />
      </Card>

      {/* Create/Edit Group Modal */}
      <Modal
        title={editingGroup ? 'Edit Consumer Group' : 'Create Consumer Group'}
        open={modalVisible}
        onOk={handleModalOk}
        onCancel={() => setModalVisible(false)}
        width={600}
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="name"
            label="Name"
            rules={[
              { required: true, message: 'Please input name' },
              { pattern: /^[a-zA-Z0-9_-]{2,100}$/, message: '2-100 chars, alphanumeric with _/-' },
            ]}
          >
            <Input placeholder="e.g., premium-consumers" />
          </Form.Item>

          <Form.Item name="description" label="Description">
            <Input.TextArea rows={2} placeholder="Description of this group" />
          </Form.Item>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="rateLimit"
                label="Group Rate Limit"
                tooltip="Leave empty for unlimited"
              >
                <InputNumber min={1} style={{ width: '100%' }} placeholder="e.g., 5000" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="rateLimitWindowSeconds" label="Window (seconds)">
                <InputNumber min={1} style={{ width: '100%' }} placeholder="60" />
              </Form.Item>
            </Col>
          </Row>

          <Form.Item name="enabled" label="Enabled" valuePropName="checked">
            <Switch />
          </Form.Item>
        </Form>
      </Modal>

      {/* Manage Members Modal */}
      <Modal
        title={`Members of "${selectedGroup?.name || ''}"`}
        open={memberModalVisible}
        onCancel={() => setMemberModalVisible(false)}
        footer={null}
        width={600}
      >
        <div style={{ marginBottom: 16 }}>
          <Space.Compact style={{ width: '100%' }}>
            <Select
              style={{ width: '80%' }}
              placeholder="Select consumer to add"
              value={selectedConsumerId}
              onChange={setSelectedConsumerId}
              showSearch
              optionFilterProp="label"
              options={availableConsumers.map((c) => ({
                value: c.id,
                label: `${c.username} (ID: ${c.id})`,
              }))}
            />
            <Button
              type="primary"
              icon={<UserAddOutlined />}
              onClick={handleAddConsumer}
              disabled={!selectedConsumerId}
            >
              Add
            </Button>
          </Space.Compact>
        </div>

        <Table
          dataSource={selectedGroup?.members || []}
          rowKey="id"
          pagination={false}
          size="small"
          columns={[
            {
              title: 'ID',
              dataIndex: 'id',
              key: 'id',
              width: 60,
            },
            {
              title: 'Username',
              dataIndex: 'username',
              key: 'username',
            },
            {
              title: 'Status',
              dataIndex: 'enabled',
              key: 'enabled',
              width: 100,
              render: (enabled: boolean) =>
                enabled ? (
                  <Tag color="success">Enabled</Tag>
                ) : (
                  <Tag color="default">Disabled</Tag>
                ),
            },
            {
              title: 'Action',
              key: 'action',
              width: 80,
              render: (_: any, record: ConsumerSummary) => (
                <Tooltip title="Remove from group">
                  <Button
                    type="text"
                    danger
                    size="small"
                    icon={<UserDeleteOutlined />}
                    onClick={() => handleRemoveConsumer(record.id)}
                  />
                </Tooltip>
              ),
            },
          ]}
        />
      </Modal>
    </div>
  );
};

export default ConsumerGroupsPage;
