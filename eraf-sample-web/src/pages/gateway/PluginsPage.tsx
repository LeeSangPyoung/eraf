import React, { useState, useEffect } from 'react';
import { Card, Button, Table, Form, Input, InputNumber, Modal, message, Space, Tag, Switch, Select, Typography } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, ReloadOutlined, ToolOutlined } from '@ant-design/icons';
import { gateway } from '../../services/api';

const { Title, Text } = Typography;
const { TextArea } = Input;

interface Plugin {
  id: number;
  name: string;
  scope: string;
  routeId: number | null;
  routeName?: string;
  serviceId: number | null;
  serviceName?: string;
  consumerGroupId: number | null;
  consumerGroupName?: string;
  config: any;
  priority: number;
  description: string | null;
  enabled: boolean;
}

const PLUGIN_TYPES = [
  'cors',
  'rate-limit',
  'jwt-auth',
  'api-key',
  'cache',
  'circuit-breaker',
  'ip-restriction',
  'bot-detection',
  'analytics',
  'request-transformer',
  'response-transformer',
  'logging',
  'retry',
  'timeout',
];

const PLUGIN_CONFIG_TEMPLATES: Record<string, any> = {
  'cors': {
    allowed_origins: ['*'],
    allowed_methods: ['GET', 'POST', 'PUT', 'DELETE', 'OPTIONS'],
    allowed_headers: ['*'],
    exposed_headers: ['X-Request-Id'],
    allow_credentials: true,
    max_age_seconds: 3600,
  },
  'rate-limit': {
    limit: 100,
    window_seconds: 60,
    key_prefix: 'rate_limit',
  },
  'jwt-auth': {
    secret_key: 'your-secret-key',
    exclude_paths: ['/api/login', '/api/register'],
    required: true,
  },
  'api-key': {
    header_name: 'X-API-Key',
    required: true,
  },
  'cache': {
    ttl_seconds: 300,
    methods: ['GET'],
    exclude_paths: [],
  },
  'circuit-breaker': {
    name: 'default-cb',
    failure_rate_threshold: 50,
    slow_call_rate_threshold: 100,
    slow_call_duration_seconds: 5,
    minimum_number_of_calls: 10,
    wait_duration_in_open_state_seconds: 60,
  },
  'ip-restriction': {
    policy: 'whitelist',
    ip_list: ['127.0.0.1', '192.168.*.*'],
  },
  'bot-detection': {
    action: 'block',
    allow_good_bots: true,
    custom_patterns: [],
  },
  'analytics': {
    collect_headers: false,
    collect_body: false,
  },
  'request-transformer': {
    add_headers: {},
    remove_headers: [],
    add_querystring: {},
    remove_querystring: [],
  },
  'response-transformer': {
    add_headers: {},
    remove_headers: [],
  },
  'logging': {
    log_body: false,
    log_headers: true,
    log_level: 'INFO',
  },
  'retry': {
    max_retries: 3,
    retry_on_status: [502, 503, 504],
    retry_delay_ms: 1000,
  },
  'timeout': {
    connect_timeout_ms: 5000,
    read_timeout_ms: 30000,
    write_timeout_ms: 30000,
  },
};

const PluginsPage: React.FC = () => {
  const [plugins, setPlugins] = useState<Plugin[]>([]);
  const [routes, setRoutes] = useState<any[]>([]);
  const [services, setServices] = useState<any[]>([]);
  const [consumerGroups, setConsumerGroups] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [editingPlugin, setEditingPlugin] = useState<Plugin | null>(null);
  const [form] = Form.useForm();

  useEffect(() => {
    loadPlugins();
    loadRoutes();
    loadServices();
    loadConsumerGroups();
  }, []);

  const loadPlugins = async () => {
    setLoading(true);
    try {
      const response = await gateway.getPlugins();
      setPlugins(response.data);
    } catch (error) {
      message.error('Failed to load plugins');
    } finally {
      setLoading(false);
    }
  };

  const loadRoutes = async () => {
    try {
      const response = await gateway.getRoutes();
      setRoutes(response.data);
    } catch (error) {
      console.error('Failed to load routes');
    }
  };

  const loadServices = async () => {
    try {
      const response = await gateway.getServices();
      setServices(response.data);
    } catch (error) {
      console.error('Failed to load services');
    }
  };

  const loadConsumerGroups = async () => {
    try {
      const response = await gateway.getConsumerGroups();
      setConsumerGroups(response.data);
    } catch (error) {
      console.error('Failed to load consumer groups');
    }
  };

  const handleCreate = () => {
    setEditingPlugin(null);
    form.resetFields();
    form.setFieldsValue({
      scope: 'GLOBAL',
      priority: 100,
      enabled: true,
      config: '{}',
    });
    setModalVisible(true);
  };

  const handleEdit = (record: Plugin) => {
    setEditingPlugin(record);
    form.setFieldsValue({
      ...record,
      config: JSON.stringify(record.config, null, 2),
    });
    setModalVisible(true);
  };

  const handleDelete = async (id: number) => {
    Modal.confirm({
      title: 'Delete Plugin',
      content: 'Are you sure you want to delete this plugin?',
      okText: 'Delete',
      okType: 'danger',
      onOk: async () => {
        try {
          await gateway.deletePlugin(id);
          message.success('Plugin deleted successfully');
          loadPlugins();
        } catch (error) {
          message.error('Failed to delete plugin');
        }
      },
    });
  };

  const handleToggle = async (id: number) => {
    try {
      await gateway.togglePlugin(id);
      message.success('Plugin toggled successfully');
      loadPlugins();
    } catch (error) {
      message.error('Failed to toggle plugin');
    }
  };

  const handlePluginTypeChange = (pluginName: string) => {
    const template = PLUGIN_CONFIG_TEMPLATES[pluginName];
    if (template) {
      form.setFieldsValue({
        config: JSON.stringify(template, null, 2),
      });
    }
  };

  const handleScopeChange = (scope: string) => {
    if (scope === 'GLOBAL') {
      form.setFieldsValue({ routeId: null, serviceId: null, consumerGroupId: null });
    } else if (scope === 'ROUTE') {
      form.setFieldsValue({ serviceId: null, consumerGroupId: null });
    } else if (scope === 'SERVICE') {
      form.setFieldsValue({ routeId: null, consumerGroupId: null });
    } else if (scope === 'CONSUMER_GROUP') {
      form.setFieldsValue({ routeId: null, serviceId: null });
    }
  };

  const handleSubmit = async (values: any) => {
    try {
      const data = {
        ...values,
        config: JSON.parse(values.config),
      };

      if (editingPlugin) {
        await gateway.updatePlugin(editingPlugin.id, data);
        message.success('Plugin updated successfully');
      } else {
        await gateway.createPlugin(data);
        message.success('Plugin created successfully');
      }

      setModalVisible(false);
      loadPlugins();
    } catch (error: any) {
      message.error(error.response?.data?.message || 'Operation failed');
    }
  };

  const getScopeColor = (scope: string) => {
    switch (scope) {
      case 'GLOBAL':
        return 'purple';
      case 'SERVICE':
        return 'blue';
      case 'ROUTE':
        return 'green';
      case 'CONSUMER_GROUP':
        return 'orange';
      default:
        return 'default';
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
      width: 150,
      render: (name: string) => <Tag color="cyan">{name}</Tag>,
    },
    {
      title: 'Scope',
      dataIndex: 'scope',
      key: 'scope',
      width: 100,
      align: 'center' as const,
      render: (scope: string) => <Tag color={getScopeColor(scope)}>{scope}</Tag>,
    },
    {
      title: 'Applied To',
      key: 'appliedTo',
      width: 200,
      render: (_: any, record: Plugin) => {
        if (record.scope === 'GLOBAL') return <Text type="secondary">All requests</Text>;
        if (record.scope === 'ROUTE') return <Tag>{record.routeName || `Route #${record.routeId}`}</Tag>;
        if (record.scope === 'SERVICE') return <Tag>{record.serviceName || `Service #${record.serviceId}`}</Tag>;
        if (record.scope === 'CONSUMER_GROUP') return <Tag color="orange">{record.consumerGroupName || `Group #${record.consumerGroupId}`}</Tag>;
        return '-';
      },
    },
    {
      title: 'Priority',
      dataIndex: 'priority',
      key: 'priority',
      width: 80,
      align: 'center' as const,
    },
    {
      title: 'Config',
      dataIndex: 'config',
      key: 'config',
      width: 200,
      render: (config: any) => (
        <Text code ellipsis style={{ maxWidth: 200 }}>
          {JSON.stringify(config)}
        </Text>
      ),
    },
    {
      title: 'Status',
      dataIndex: 'enabled',
      key: 'enabled',
      width: 100,
      align: 'center' as const,
      render: (enabled: boolean, record: Plugin) => (
        <Switch checked={enabled} onChange={() => handleToggle(record.id)} />
      ),
    },
    {
      title: 'Actions',
      key: 'actions',
      width: 120,
      align: 'center' as const,
      render: (_: any, record: Plugin) => (
        <Space>
          <Button type="link" icon={<EditOutlined />} onClick={() => handleEdit(record)} />
          <Button type="link" danger icon={<DeleteOutlined />} onClick={() => handleDelete(record.id)} />
        </Space>
      ),
    },
  ];

  return (
    <div>
      <Card>
        <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <div>
            <Title level={4} style={{ margin: 0 }}>
              <ToolOutlined /> Plugins Management
            </Title>
            <Text type="secondary">Manage gateway plugins for routes and services</Text>
          </div>
          <Space>
            <Button icon={<ReloadOutlined />} onClick={loadPlugins}>
              Refresh
            </Button>
            <Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>
              Create Plugin
            </Button>
          </Space>
        </div>

        <Table
          columns={columns}
          dataSource={plugins}
          rowKey="id"
          loading={loading}
          pagination={{ pageSize: 10 }}
          scroll={{ x: 1200 }}
        />
      </Card>

      <Modal
        title={editingPlugin ? 'Edit Plugin' : 'Create Plugin'}
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        onOk={() => form.submit()}
        width={700}
      >
        <Form form={form} layout="vertical" onFinish={handleSubmit}>
          <Form.Item name="name" label="Plugin Type" rules={[{ required: true, message: 'Please select plugin type' }]}>
            <Select placeholder="Select plugin type" onChange={handlePluginTypeChange}>
              {PLUGIN_TYPES.map((type) => (
                <Select.Option key={type} value={type}>
                  {type}
                </Select.Option>
              ))}
            </Select>
          </Form.Item>

          <Form.Item name="scope" label="Scope" rules={[{ required: true }]}>
            <Select placeholder="Select scope" onChange={handleScopeChange}>
              <Select.Option value="GLOBAL">Global (All Requests)</Select.Option>
              <Select.Option value="SERVICE">Service Level</Select.Option>
              <Select.Option value="ROUTE">Route Level</Select.Option>
              <Select.Option value="CONSUMER_GROUP">Consumer Group Level</Select.Option>
            </Select>
          </Form.Item>

          <Form.Item noStyle shouldUpdate={(prev, curr) => prev.scope !== curr.scope}>
            {({ getFieldValue }) => {
              const scope = getFieldValue('scope');
              if (scope === 'ROUTE') {
                return (
                  <Form.Item name="routeId" label="Route" rules={[{ required: true, message: 'Please select a route' }]}>
                    <Select placeholder="Select route">
                      {routes.map((r) => (
                        <Select.Option key={r.id} value={r.id}>
                          {r.name}
                        </Select.Option>
                      ))}
                    </Select>
                  </Form.Item>
                );
              } else if (scope === 'SERVICE') {
                return (
                  <Form.Item name="serviceId" label="Service" rules={[{ required: true, message: 'Please select a service' }]}>
                    <Select placeholder="Select service">
                      {services.map((s) => (
                        <Select.Option key={s.id} value={s.id}>
                          {s.name}
                        </Select.Option>
                      ))}
                    </Select>
                  </Form.Item>
                );
              } else if (scope === 'CONSUMER_GROUP') {
                return (
                  <Form.Item name="consumerGroupId" label="Consumer Group" rules={[{ required: true, message: 'Please select a consumer group' }]}>
                    <Select placeholder="Select consumer group">
                      {consumerGroups.map((g) => (
                        <Select.Option key={g.id} value={g.id}>
                          {g.name}
                        </Select.Option>
                      ))}
                    </Select>
                  </Form.Item>
                );
              }
              return null;
            }}
          </Form.Item>

          <Form.Item name="priority" label="Priority">
            <InputNumber min={0} placeholder="100" style={{ width: '100%' }} />
          </Form.Item>

          <Form.Item
            name="config"
            label="Configuration (JSON)"
            rules={[
              { required: true, message: 'Please enter configuration' },
              {
                validator: (_, value) => {
                  try {
                    JSON.parse(value);
                    return Promise.resolve();
                  } catch (e) {
                    return Promise.reject(new Error('Invalid JSON format'));
                  }
                },
              },
            ]}
          >
            <TextArea rows={10} placeholder='{"key": "value"}' style={{ fontFamily: 'monospace' }} />
          </Form.Item>

          <Form.Item name="description" label="Description">
            <TextArea rows={2} placeholder="Description" />
          </Form.Item>

          <Form.Item name="enabled" label="Enabled" valuePropName="checked">
            <Switch />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default PluginsPage;
