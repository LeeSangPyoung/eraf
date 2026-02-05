import React, { useState, useEffect } from 'react';
import { Card, Button, Table, message, Typography, Tabs, Space, Tag, Select, Input, Spin, Descriptions, Timeline, Form, Modal } from 'antd';
import { BranchesOutlined, ReloadOutlined, PlayCircleOutlined, StopOutlined, RetweetOutlined, EyeOutlined } from '@ant-design/icons';
import { sagaApi } from '../../services/api';

const { Title, Text } = Typography;

const SagaPage: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [definitions, setDefinitions] = useState<any[]>([]);
  const [executions, setExecutions] = useState<any[]>([]);
  const [selectedExecution, setSelectedExecution] = useState<any>(null);
  const [statusFilter, setStatusFilter] = useState<string>('');
  const [selectedSaga, setSelectedSaga] = useState<string>('');
  const [payload, setPayload] = useState<string>('{}');
  const [detailModal, setDetailModal] = useState(false);

  const loadDefinitions = async () => {
    try {
      const response = await sagaApi.getDefinitions();
      // Backend returns 'definitions' field
      const defs = response.data.definitions || [];
      setDefinitions(defs);
      if (defs.length > 0) {
        setSelectedSaga(defs[0].name);
      }
    } catch (error) {
      console.error('Failed to load definitions');
    }
  };

  const loadExecutions = async () => {
    setLoading(true);
    try {
      let response;
      if (statusFilter) {
        response = await sagaApi.getExecutionsByStatus(statusFilter);
      } else {
        response = await sagaApi.getExecutions(50);
      }
      setExecutions(response.data.executions || []);
    } catch (error) {
      console.error('Failed to load executions');
    } finally {
      setLoading(false);
    }
  };

  const loadExecutionDetail = async (sagaId: string) => {
    setLoading(true);
    try {
      const response = await sagaApi.getExecution(sagaId);
      setSelectedExecution(response.data);
      setDetailModal(true);
    } catch (error) {
      message.error('Failed to load execution detail');
    } finally {
      setLoading(false);
    }
  };

  const startSaga = async () => {
    if (!selectedSaga) return;
    setLoading(true);
    try {
      let parsedPayload = {};
      try {
        parsedPayload = JSON.parse(payload);
      } catch {
        message.error('Invalid JSON payload');
        setLoading(false);
        return;
      }
      // Use execute API instead of startSaga
      const response = await sagaApi.execute(selectedSaga, parsedPayload);
      message.success(`Saga started: ${response.data.executionId || response.data.traceId}`);
      loadExecutions();
    } catch (error: any) {
      message.error(error.response?.data?.message || 'Failed to start saga');
    } finally {
      setLoading(false);
    }
  };

  const retrySaga = async (sagaId: string) => {
    setLoading(true);
    try {
      await sagaApi.retrySaga(sagaId);
      message.success('Saga retry initiated');
      loadExecutions();
    } catch (error) {
      message.error('Failed to retry saga');
    } finally {
      setLoading(false);
    }
  };

  const cancelSaga = async (sagaId: string) => {
    // Cancel is not supported in this demo
    message.info('Cancel feature is not available in demo mode');
  };

  useEffect(() => {
    loadDefinitions();
    loadExecutions();
  }, []);

  useEffect(() => {
    loadExecutions();
  }, [statusFilter]);

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'COMPLETED': return 'green';
      case 'RUNNING': return 'blue';
      case 'FAILED': return 'red';
      case 'COMPENSATING': return 'orange';
      case 'COMPENSATED': return 'purple';
      case 'CANCELLED': return 'default';
      default: return 'default';
    }
  };

  const executionColumns = [
    { title: 'Saga ID', dataIndex: 'id', key: 'id', width: 150, ellipsis: true },
    { title: 'Saga Name', dataIndex: 'sagaName', key: 'sagaName' },
    { title: 'Status', dataIndex: 'status', key: 'status', render: (s: string) => <Tag color={getStatusColor(s)}>{s}</Tag> },
    { title: 'Current Step', dataIndex: 'currentStep', key: 'currentStep' },
    { title: 'Started', dataIndex: 'startedAt', key: 'startedAt' },
    { title: 'Completed', dataIndex: 'completedAt', key: 'completedAt' },
    {
      title: 'Actions',
      key: 'actions',
      render: (_: any, record: any) => (
        <Space>
          <Button size="small" icon={<EyeOutlined />} onClick={() => loadExecutionDetail(record.id)}>
            Detail
          </Button>
          {record.status === 'FAILED' && (
            <Button size="small" icon={<RetweetOutlined />} onClick={() => retrySaga(record.id)}>
              Retry
            </Button>
          )}
          {record.status === 'RUNNING' && (
            <Button size="small" danger icon={<StopOutlined />} onClick={() => cancelSaga(record.id)}>
              Cancel
            </Button>
          )}
        </Space>
      ),
    },
  ];

  const items = [
    {
      key: '1',
      label: '#35 Saga Definitions',
      children: (
        <Card>
          <Title level={5}>Saga Definitions</Title>
          <Text type="secondary">Available saga orchestration definitions.</Text>
          <Table
            dataSource={definitions}
            rowKey="name"
            size="small"
            style={{ marginTop: 16 }}
            columns={[
              { title: 'Name', dataIndex: 'name', key: 'name' },
              { title: 'Description', dataIndex: 'description', key: 'description' },
              { title: 'Steps', dataIndex: 'totalSteps', key: 'totalSteps' },
              {
                title: 'Steps Detail',
                dataIndex: 'steps',
                key: 'steps',
                render: (steps: any[]) => steps?.map((s, i) => <Tag key={i}>{s.name || s}</Tag>),
              },
            ]}
          />
        </Card>
      ),
    },
    {
      key: '2',
      label: '#36~#37 Start Saga',
      children: (
        <Card>
          <Title level={5}>Start New Saga</Title>
          <Text type="secondary">Select a saga and provide payload to start execution.</Text>
          <Form layout="vertical" style={{ marginTop: 16, maxWidth: 500 }}>
            <Form.Item label="Saga">
              <Select value={selectedSaga} onChange={setSelectedSaga} style={{ width: '100%' }}>
                {definitions.map((d) => (
                  <Select.Option key={d.name} value={d.name}>{d.name}</Select.Option>
                ))}
              </Select>
            </Form.Item>
            <Form.Item label="Payload (JSON)">
              <Input.TextArea
                value={payload}
                onChange={(e) => setPayload(e.target.value)}
                rows={4}
                placeholder='{"orderId": "ORD-001", "amount": 1000}'
              />
            </Form.Item>
            <Form.Item>
              <Button type="primary" icon={<PlayCircleOutlined />} onClick={startSaga} loading={loading}>
                Start Saga
              </Button>
            </Form.Item>
          </Form>
        </Card>
      ),
    },
    {
      key: '3',
      label: '#38~#40 Executions',
      children: (
        <Card>
          <Title level={5}>Saga Executions</Title>
          <Space style={{ marginBottom: 16 }}>
            <Select
              value={statusFilter}
              onChange={setStatusFilter}
              style={{ width: 150 }}
              placeholder="Filter by status"
              allowClear
            >
              <Select.Option value="">All</Select.Option>
              <Select.Option value="RUNNING">Running</Select.Option>
              <Select.Option value="COMPLETED">Completed</Select.Option>
              <Select.Option value="FAILED">Failed</Select.Option>
              <Select.Option value="COMPENSATING">Compensating</Select.Option>
              <Select.Option value="COMPENSATED">Compensated</Select.Option>
            </Select>
            <Button icon={<ReloadOutlined />} onClick={loadExecutions}>Refresh</Button>
          </Space>
          <Table
            dataSource={executions}
            columns={executionColumns}
            rowKey="id"
            size="small"
            loading={loading}
          />
        </Card>
      ),
    },
    {
      key: '4',
      label: '#41~#42 Compensation',
      children: (
        <Card>
          <Title level={5}>Saga Compensation</Title>
          <Text type="secondary">
            When a saga fails, compensation actions are automatically triggered to rollback completed steps.
          </Text>
          <div style={{ marginTop: 16 }}>
            <Text strong>Compensation States:</Text>
            <div style={{ marginTop: 8 }}>
              <Space direction="vertical">
                <div><Tag color="orange">COMPENSATING</Tag> Compensation in progress</div>
                <div><Tag color="purple">COMPENSATED</Tag> All steps have been compensated</div>
                <div><Tag color="red">FAILED</Tag> Saga failed, may need manual intervention</div>
              </Space>
            </div>
          </div>
          <div style={{ marginTop: 24 }}>
            <Text strong>Failed Sagas (requiring attention):</Text>
            <Table
              dataSource={executions.filter((e) => e.status === 'FAILED' || e.status === 'COMPENSATING')}
              columns={executionColumns}
              rowKey="id"
              size="small"
              style={{ marginTop: 8 }}
            />
          </div>
        </Card>
      ),
    },
  ];

  return (
    <Spin spinning={loading}>
      <Title level={3}>
        <BranchesOutlined /> Saga Pattern (#35~#42)
      </Title>
      <Tabs items={items} />

      <Modal
        title="Saga Execution Detail"
        open={detailModal}
        onCancel={() => setDetailModal(false)}
        footer={null}
        width={700}
      >
        {selectedExecution && (
          <>
            <Descriptions bordered size="small" column={2}>
              <Descriptions.Item label="Saga ID">{selectedExecution.id}</Descriptions.Item>
              <Descriptions.Item label="Saga Name">{selectedExecution.sagaName}</Descriptions.Item>
              <Descriptions.Item label="Status">
                <Tag color={getStatusColor(selectedExecution.status)}>{selectedExecution.status}</Tag>
              </Descriptions.Item>
              <Descriptions.Item label="Current Step">{selectedExecution.currentStep}</Descriptions.Item>
              <Descriptions.Item label="Started">{selectedExecution.startedAt}</Descriptions.Item>
              <Descriptions.Item label="Completed">{selectedExecution.completedAt || '-'}</Descriptions.Item>
            </Descriptions>
            <Title level={5} style={{ marginTop: 16 }}>Step History</Title>
            <Timeline>
              {selectedExecution.steps?.map((step: any, idx: number) => (
                <Timeline.Item
                  key={idx}
                  color={step.status === 'COMPLETED' ? 'green' : step.status === 'FAILED' ? 'red' : 'blue'}
                >
                  <Text strong>{step.name}</Text>
                  <br />
                  <Tag color={getStatusColor(step.status)}>{step.status}</Tag>
                  {step.error && <Text type="danger"> - {step.error}</Text>}
                  <br />
                  <Text type="secondary">{step.timestampFormatted}</Text>
                </Timeline.Item>
              ))}
            </Timeline>
          </>
        )}
      </Modal>
    </Spin>
  );
};

export default SagaPage;
