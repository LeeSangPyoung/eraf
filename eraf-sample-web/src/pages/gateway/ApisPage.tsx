import React, { useState, useEffect, useMemo, useRef } from 'react';
import {
  Card,
  Button,
  Table,
  Form,
  Input,
  Modal,
  message,
  Space,
  Tag,
  Switch,
  Select,
  Typography,
  Tabs,
  InputNumber,
  Row,
  Col,
  Statistic,
  AutoComplete,
} from 'antd';
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  ReloadOutlined,
  ApiOutlined,
  CheckCircleOutlined,
  LockOutlined,
  SafetyOutlined,
} from '@ant-design/icons';
import { gateway } from '../../services/api';

const { Title, Text } = Typography;
const { TextArea } = Input;

interface Api {
  id: number;
  name: string;
  path: string;
  method: string;
  serviceId: number;
  serviceName?: string;
  routeId?: number;
  routeName?: string;
  description?: string;
  enabled: boolean;
  priority: number;
  authRequired: boolean;
  authType?: string;
  requiredRoles?: string[];
  validationEnabled: boolean;
  validateHeaders: boolean;
  validateQueryParams: boolean;
  validateBody: boolean;
  validateResponse: boolean;
  responseSchema?: string;
  openApiOperationId?: string;
  requiredHeaders?: Record<string, string>;
  requiredQueryParams?: string[];
  jsonSchema?: string;
  allowedContentTypes?: string[];
  requiredFields?: string[];
  maxBodySize?: number;
  rateLimitEnabled: boolean;
  rateLimitRequests?: number;
  rateLimitWindowSeconds?: number;
  rateLimitKey?: string;
  ipRestrictionEnabled: boolean;
  allowedIps?: string[];
  blockedIps?: string[];
  cacheEnabled: boolean;
  cacheTtlSeconds?: number;
  cacheKey?: string;
  timeoutMs?: number;
  tags?: string[];
  metadata?: Record<string, any>;
  createdAt?: string;
  updatedAt?: string;
}

interface ApiStats {
  totalApis: number;
  enabledApis: number;
  withAuth: number;
  withValidation: number;
  withRateLimit: number;
  withCache: number;
}

const ApisPage: React.FC = () => {
  const [apis, setApis] = useState<Api[]>([]);
  const [stats, setStats] = useState<ApiStats | null>(null);
  const [services, setServices] = useState<any[]>([]);
  const [routes, setRoutes] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [editingApi, setEditingApi] = useState<Api | null>(null);
  const [filterMethod, setFilterMethod] = useState<string | undefined>(undefined);
  const [filterService, setFilterService] = useState<number | undefined>(undefined);
  const [filterEnabled, setFilterEnabled] = useState<boolean | undefined>(undefined);
  const [appliedFilterName, setAppliedFilterName] = useState<string>('');
  const searchInputRef = useRef<any>(null);
  const [form] = Form.useForm();

  useEffect(() => {
    loadApis();
    loadStats();
    loadServices();
    loadRoutes();
  }, []);

  const loadApis = async () => {
    setLoading(true);
    try {
      const response = await gateway.getApis();
      setApis(response.data);
    } catch (error) {
      message.error('Failed to load APIs');
    } finally {
      setLoading(false);
    }
  };

  const loadStats = async () => {
    try {
      const response = await gateway.getApiStats();
      setStats(response.data);
    } catch (error) {
      console.error('Failed to load stats');
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

  const loadRoutes = async () => {
    try {
      const response = await gateway.getRoutes();
      setRoutes(response.data);
    } catch (error) {
      console.error('Failed to load routes');
    }
  };

  const handleCreate = () => {
    setEditingApi(null);
    form.resetFields();
    form.setFieldsValue({
      enabled: true,
      priority: 0,
      authRequired: false,
      validationEnabled: false,
      validateHeaders: true,
      validateQueryParams: true,
      validateBody: true,
      validateResponse: false,
      rateLimitEnabled: false,
      ipRestrictionEnabled: false,
      cacheEnabled: false,
    });
    setModalVisible(true);
  };

  const handleEdit = (record: Api) => {
    setEditingApi(record);
    form.setFieldsValue({
      ...record,
      requiredHeaders: record.requiredHeaders
        ? Object.entries(record.requiredHeaders)
            .map(([k, v]) => `${k}=${v}`)
            .join('\n')
        : '',
      requiredQueryParams: record.requiredQueryParams?.join(', ') || '',
      allowedContentTypes: record.allowedContentTypes?.join(', ') || '',
      requiredFields: record.requiredFields?.join(', ') || '',
      requiredRoles: record.requiredRoles?.join(', ') || '',
      allowedIps: record.allowedIps?.join('\n') || '',
      blockedIps: record.blockedIps?.join('\n') || '',
      tags: record.tags?.join(', ') || '',
    });
    setModalVisible(true);
  };

  const handleDelete = async (id: number) => {
    Modal.confirm({
      title: 'Delete API',
      content: 'Are you sure you want to delete this API?',
      okText: 'Delete',
      okType: 'danger',
      onOk: async () => {
        try {
          await gateway.deleteApi(id);
          message.success('API deleted successfully');
          loadApis();
          loadStats();
        } catch (error) {
          message.error('Failed to delete API');
        }
      },
    });
  };

  const handleToggle = async (id: number) => {
    try {
      await gateway.toggleApi(id);
      message.success('API toggled successfully');
      loadApis();
      loadStats();
    } catch (error) {
      message.error('Failed to toggle API');
    }
  };

  const handleSubmit = async (values: any) => {
    try {
      const data = {
        ...values,
        requiredHeaders: values.requiredHeaders
          ? Object.fromEntries(
              values.requiredHeaders
                .split('\n')
                .map((line: string) => line.trim())
                .filter(Boolean)
                .map((line: string) => {
                  const [key, val] = line.split('=');
                  return [key.trim(), val?.trim() || '*'];
                })
            )
          : null,
        requiredQueryParams: values.requiredQueryParams
          ? values.requiredQueryParams.split(',').map((p: string) => p.trim()).filter(Boolean)
          : null,
        allowedContentTypes: values.allowedContentTypes
          ? values.allowedContentTypes.split(',').map((t: string) => t.trim()).filter(Boolean)
          : null,
        requiredFields: values.requiredFields
          ? values.requiredFields.split(',').map((f: string) => f.trim()).filter(Boolean)
          : null,
        requiredRoles: values.requiredRoles
          ? values.requiredRoles.split(',').map((r: string) => r.trim()).filter(Boolean)
          : null,
        allowedIps: values.allowedIps
          ? values.allowedIps.split('\n').map((ip: string) => ip.trim()).filter(Boolean)
          : null,
        blockedIps: values.blockedIps
          ? values.blockedIps.split('\n').map((ip: string) => ip.trim()).filter(Boolean)
          : null,
        tags: values.tags
          ? values.tags.split(',').map((t: string) => t.trim()).filter(Boolean)
          : null,
      };

      if (editingApi) {
        await gateway.updateApi(editingApi.id, data);
        message.success('API updated successfully');
      } else {
        await gateway.createApi(data);
        message.success('API created successfully');
      }

      setModalVisible(false);
      loadApis();
      loadStats();
    } catch (error: any) {
      message.error(error.response?.data?.message || 'Operation failed');
    }
  };

  const getMethodColor = (method: string) => {
    const colors: Record<string, string> = {
      GET: 'blue',
      POST: 'green',
      PUT: 'orange',
      DELETE: 'red',
      PATCH: 'purple',
      HEAD: 'cyan',
      OPTIONS: 'default',
    };
    return colors[method] || 'default';
  };

  const filteredApis = useMemo(() => apis.filter((api) => {
    if (appliedFilterName && !api.name.toLowerCase().includes(appliedFilterName.toLowerCase())) return false;
    if (filterMethod && api.method !== filterMethod) return false;
    if (filterService && api.serviceId !== filterService) return false;
    if (filterEnabled !== undefined && api.enabled !== filterEnabled) return false;
    return true;
  }), [apis, appliedFilterName, filterMethod, filterService, filterEnabled]);

  const columns = useMemo(() => [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      width: 60,
    },
    {
      title: 'Method',
      dataIndex: 'method',
      key: 'method',
      width: 100,
      render: (method: string) => <Tag color={getMethodColor(method)}>{method}</Tag>,
    },
    {
      title: 'Path',
      dataIndex: 'path',
      key: 'path',
      width: 250,
      render: (path: string) => <Text code>{path}</Text>,
    },
    {
      title: 'Name',
      dataIndex: 'name',
      key: 'name',
      width: 150,
    },
    {
      title: 'Service',
      dataIndex: 'serviceName',
      key: 'serviceName',
      width: 120,
      render: (name: string) => <Tag color="blue">{name}</Tag>,
    },
    {
      title: 'Features',
      key: 'features',
      width: 150,
      render: (record: Api) => (
        <Space size="small" wrap>
          {record.authRequired && <Tag color="gold" icon={<LockOutlined />}>Auth</Tag>}
          {record.validationEnabled && <Tag color="green" icon={<CheckCircleOutlined />}>Valid</Tag>}
          {record.rateLimitEnabled && <Tag color="orange" icon={<SafetyOutlined />}>Rate</Tag>}
          {record.cacheEnabled && <Tag color="purple">Cache</Tag>}
        </Space>
      ),
    },
    {
      title: 'Priority',
      dataIndex: 'priority',
      key: 'priority',
      width: 80,
      align: 'center' as const,
    },
    {
      title: 'Status',
      key: 'enabled',
      width: 90,
      align: 'center' as const,
      render: (record: Api) => (
        <Switch checked={record.enabled} onChange={() => handleToggle(record.id)} />
      ),
    },
    {
      title: 'Actions',
      key: 'actions',
      width: 120,
      fixed: 'right' as const,
      render: (record: Api) => (
        <Space>
          <Button type="link" size="small" icon={<EditOutlined />} onClick={() => handleEdit(record)} />
          <Button
            type="link"
            size="small"
            danger
            icon={<DeleteOutlined />}
            onClick={() => handleDelete(record.id)}
          />
        </Space>
      ),
    },
  ], []); // 의존성 없음 - 핸들러들은 함수 참조만 사용

  return (
    <div>
      {/* Statistics */}
      {stats && (
        <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
          <Col span={4}>
            <Card>
              <Statistic title="Total APIs" value={stats.totalApis} prefix={<ApiOutlined />} />
            </Card>
          </Col>
          <Col span={4}>
            <Card>
              <Statistic
                title="Enabled"
                value={stats.enabledApis}
                prefix={<CheckCircleOutlined />}
                valueStyle={{ color: '#3f8600' }}
              />
            </Card>
          </Col>
          <Col span={4}>
            <Card>
              <Statistic title="With Auth" value={stats.withAuth} prefix={<LockOutlined />} />
            </Card>
          </Col>
          <Col span={4}>
            <Card>
              <Statistic title="With Validation" value={stats.withValidation} />
            </Card>
          </Col>
          <Col span={4}>
            <Card>
              <Statistic title="With Rate Limit" value={stats.withRateLimit} />
            </Card>
          </Col>
          <Col span={4}>
            <Card>
              <Statistic title="With Cache" value={stats.withCache} />
            </Card>
          </Col>
        </Row>
      )}

      {/* Main Table */}
      <Card
        title={
          <Space>
            <ApiOutlined />
            <span>API Registry</span>
          </Space>
        }
        extra={
          <Space>
            <Input
              ref={searchInputRef}
              placeholder="Search by name (press Enter)..."
              style={{ width: 250 }}
              onPressEnter={() => {
                const value = searchInputRef.current?.input?.value || '';
                setAppliedFilterName(value);
              }}
              onChange={(e) => {
                if (!e.target.value) setAppliedFilterName(''); // Clear 버튼 클릭 시
              }}
              allowClear
            />
            <Select
              placeholder="Method"
              allowClear
              style={{ width: 120 }}
              onChange={setFilterMethod}
              value={filterMethod}
            >
              <Select.Option value="GET">GET</Select.Option>
              <Select.Option value="POST">POST</Select.Option>
              <Select.Option value="PUT">PUT</Select.Option>
              <Select.Option value="DELETE">DELETE</Select.Option>
              <Select.Option value="PATCH">PATCH</Select.Option>
            </Select>
            <Select
              placeholder="Service"
              allowClear
              style={{ width: 150 }}
              onChange={setFilterService}
              value={filterService}
            >
              {services.map((s) => (
                <Select.Option key={s.id} value={s.id}>
                  {s.name}
                </Select.Option>
              ))}
            </Select>
            <Select
              placeholder="Status"
              allowClear
              style={{ width: 120 }}
              onChange={setFilterEnabled}
              value={filterEnabled}
            >
              <Select.Option value={true}>Enabled</Select.Option>
              <Select.Option value={false}>Disabled</Select.Option>
            </Select>
            <Button icon={<ReloadOutlined />} onClick={() => { loadApis(); loadStats(); }}>
              Refresh
            </Button>
            <Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>
              Create API
            </Button>
          </Space>
        }
      >
        <Table
          columns={columns}
          dataSource={filteredApis}
          rowKey="id"
          loading={loading}
          pagination={{ pageSize: 20 }}
          scroll={{ x: 1400 }}
        />
      </Card>

      {/* Create/Edit Modal */}
      <Modal
        title={editingApi ? 'Edit API' : 'Create API'}
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        onOk={() => form.submit()}
        width={800}
        destroyOnClose
      >
        <Form form={form} layout="vertical" onFinish={handleSubmit}>
          <Tabs
            items={[
              {
                key: 'basic',
                label: 'Basic',
                children: (
                  <>
                    <Form.Item name="name" label="Name" rules={[{ required: true }]}>
                      <Input placeholder="user-list-api" />
                    </Form.Item>
                    <Row gutter={16}>
                      <Col span={8}>
                        <Form.Item name="method" label="Method" rules={[{ required: true }]}>
                          <Select>
                            <Select.Option value="GET">GET</Select.Option>
                            <Select.Option value="POST">POST</Select.Option>
                            <Select.Option value="PUT">PUT</Select.Option>
                            <Select.Option value="DELETE">DELETE</Select.Option>
                            <Select.Option value="PATCH">PATCH</Select.Option>
                            <Select.Option value="HEAD">HEAD</Select.Option>
                            <Select.Option value="OPTIONS">OPTIONS</Select.Option>
                          </Select>
                        </Form.Item>
                      </Col>
                      <Col span={16}>
                        <Form.Item name="path" label="Path" rules={[{ required: true }]}>
                          <Input placeholder="/api/users or /api/users/*" />
                        </Form.Item>
                      </Col>
                    </Row>
                    <Row gutter={16}>
                      <Col span={12}>
                        <Form.Item name="serviceId" label="Service" rules={[{ required: true }]}>
                          <Select placeholder="Select service">
                            {services.map((s) => (
                              <Select.Option key={s.id} value={s.id}>
                                {s.name}
                              </Select.Option>
                            ))}
                          </Select>
                        </Form.Item>
                      </Col>
                      <Col span={12}>
                        <Form.Item name="routeId" label="Route (Optional)">
                          <Select placeholder="Select route (optional)" allowClear>
                            {routes.map((r) => (
                              <Select.Option key={r.id} value={r.id}>
                                {r.name}
                              </Select.Option>
                            ))}
                          </Select>
                        </Form.Item>
                      </Col>
                    </Row>
                    <Form.Item name="description" label="Description">
                      <TextArea rows={2} placeholder="API description" />
                    </Form.Item>
                    <Row gutter={16}>
                      <Col span={8}>
                        <Form.Item name="priority" label="Priority">
                          <InputNumber min={0} style={{ width: '100%' }} />
                        </Form.Item>
                      </Col>
                      <Col span={8}>
                        <Form.Item name="timeoutMs" label="Timeout (ms)">
                          <InputNumber min={0} style={{ width: '100%' }} placeholder="30000" />
                        </Form.Item>
                      </Col>
                      <Col span={8}>
                        <Form.Item name="enabled" label="Enabled" valuePropName="checked">
                          <Switch />
                        </Form.Item>
                      </Col>
                    </Row>
                    <Form.Item name="openApiOperationId" label="OpenAPI Operation ID">
                      <Input placeholder="getUserById" />
                    </Form.Item>
                    <Form.Item name="tags" label="Tags">
                      <Input placeholder="public, v1, beta (comma-separated)" />
                    </Form.Item>
                  </>
                ),
              },
              {
                key: 'auth',
                label: 'Authentication',
                children: (
                  <>
                    <Form.Item name="authRequired" label="Auth Required" valuePropName="checked">
                      <Switch />
                    </Form.Item>
                    <Form.Item name="authType" label="Auth Type">
                      <Select placeholder="Select auth type" allowClear>
                        <Select.Option value="JWT">JWT</Select.Option>
                        <Select.Option value="API_KEY">API Key</Select.Option>
                        <Select.Option value="BASIC">Basic Auth</Select.Option>
                        <Select.Option value="OAUTH2">OAuth2</Select.Option>
                      </Select>
                    </Form.Item>
                    <Form.Item name="requiredRoles" label="Required Roles">
                      <Input placeholder="ADMIN, USER (comma-separated)" />
                    </Form.Item>
                  </>
                ),
              },
              {
                key: 'validation',
                label: 'Validation',
                children: (
                  <>
                    <Form.Item name="validationEnabled" label="Validation Enabled" valuePropName="checked">
                      <Switch />
                    </Form.Item>
                    <Row gutter={16}>
                      <Col span={6}>
                        <Form.Item name="validateHeaders" label="Headers" valuePropName="checked">
                          <Switch />
                        </Form.Item>
                      </Col>
                      <Col span={6}>
                        <Form.Item name="validateQueryParams" label="Query Params" valuePropName="checked">
                          <Switch />
                        </Form.Item>
                      </Col>
                      <Col span={6}>
                        <Form.Item name="validateBody" label="Body" valuePropName="checked">
                          <Switch />
                        </Form.Item>
                      </Col>
                      <Col span={6}>
                        <Form.Item name="validateResponse" label="Response" valuePropName="checked">
                          <Switch />
                        </Form.Item>
                      </Col>
                    </Row>
                    <Form.Item name="requiredHeaders" label="Required Headers">
                      <TextArea
                        rows={3}
                        placeholder={'X-API-Key=*\nContent-Type=application/json\n(one per line, key=value)'}
                      />
                    </Form.Item>
                    <Form.Item name="requiredQueryParams" label="Required Query Params">
                      <Input placeholder="userId, page (comma-separated)" />
                    </Form.Item>
                    <Form.Item name="allowedContentTypes" label="Allowed Content-Types">
                      <Input placeholder="application/json, application/xml (comma-separated)" />
                    </Form.Item>
                    <Form.Item name="requiredFields" label="Required Fields">
                      <Input placeholder="name, email (comma-separated)" />
                    </Form.Item>
                    <Form.Item name="maxBodySize" label="Max Body Size (bytes)">
                      <InputNumber min={0} style={{ width: '100%' }} placeholder="1048576" />
                    </Form.Item>
                    <Form.Item name="jsonSchema" label="JSON Schema (Request)">
                      <TextArea rows={4} placeholder='{"type": "object", "required": ["name"]}' />
                    </Form.Item>
                    <Form.Item name="responseSchema" label="JSON Schema (Response)">
                      <TextArea rows={4} placeholder='{"type": "object", "properties": {...}}' />
                    </Form.Item>
                  </>
                ),
              },
              {
                key: 'advanced',
                label: 'Advanced',
                children: (
                  <>
                    <Title level={5}>Rate Limiting</Title>
                    <Form.Item name="rateLimitEnabled" valuePropName="checked">
                      <Switch /> Enable Rate Limiting
                    </Form.Item>
                    <Row gutter={16}>
                      <Col span={12}>
                        <Form.Item name="rateLimitRequests" label="Requests">
                          <InputNumber min={1} style={{ width: '100%' }} placeholder="100" />
                        </Form.Item>
                      </Col>
                      <Col span={12}>
                        <Form.Item name="rateLimitWindowSeconds" label="Window (seconds)">
                          <InputNumber min={1} style={{ width: '100%' }} placeholder="60" />
                        </Form.Item>
                      </Col>
                    </Row>
                    <Form.Item name="rateLimitKey" label="Rate Limit Key">
                      <Select placeholder="Select key" allowClear>
                        <Select.Option value="ip">IP Address</Select.Option>
                        <Select.Option value="user">User ID</Select.Option>
                        <Select.Option value="api_key">API Key</Select.Option>
                      </Select>
                    </Form.Item>

                    <Title level={5} style={{ marginTop: 24 }}>
                      Cache
                    </Title>
                    <Form.Item name="cacheEnabled" valuePropName="checked">
                      <Switch /> Enable Cache
                    </Form.Item>
                    <Row gutter={16}>
                      <Col span={12}>
                        <Form.Item name="cacheTtlSeconds" label="TTL (seconds)">
                          <InputNumber min={0} style={{ width: '100%' }} placeholder="300" />
                        </Form.Item>
                      </Col>
                      <Col span={12}>
                        <Form.Item name="cacheKey" label="Cache Key Pattern">
                          <Input placeholder="{method}:{path}:{query}" />
                        </Form.Item>
                      </Col>
                    </Row>

                    <Title level={5} style={{ marginTop: 24 }}>
                      IP Restriction
                    </Title>
                    <Form.Item name="ipRestrictionEnabled" valuePropName="checked">
                      <Switch /> Enable IP Restriction
                    </Form.Item>
                    <Row gutter={16}>
                      <Col span={12}>
                        <Form.Item name="allowedIps" label="Allowed IPs">
                          <TextArea rows={3} placeholder="192.168.1.1\n10.0.0.0/8 (one per line)" />
                        </Form.Item>
                      </Col>
                      <Col span={12}>
                        <Form.Item name="blockedIps" label="Blocked IPs">
                          <TextArea rows={3} placeholder="1.2.3.4\n5.6.7.0/24 (one per line)" />
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

export default ApisPage;
