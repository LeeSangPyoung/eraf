import React, { useState, useEffect } from 'react';
import { Card, Button, Table, message, Typography, Tabs, Space, Tag, Spin, Progress, Modal, Descriptions, Select } from 'antd';
import { ScheduleOutlined, PlayCircleOutlined, PauseCircleOutlined, ReloadOutlined, DatabaseOutlined, EyeOutlined } from '@ant-design/icons';
import { schedulerApi } from '../../services/api';

const { Title, Text } = Typography;

const SchedulerPage: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [jobs, setJobs] = useState<any[]>([]);
  const [batchJobs, setBatchJobs] = useState<any[]>([]);
  const [executions, setExecutions] = useState<any[]>([]);
  const [selectedJob, setSelectedJob] = useState<any>(null);
  const [detailModal, setDetailModal] = useState(false);
  const [batchProgress, setBatchProgress] = useState<any>(null);
  const [progressModal, setProgressModal] = useState(false);
  const [statusFilter, setStatusFilter] = useState('');
  const [executionType, setExecutionType] = useState('');

  const loadJobs = async () => {
    setLoading(true);
    try {
      const response = await schedulerApi.getJobs('', statusFilter);
      setJobs(response.data.jobs || []);
    } catch (error) {
      console.error('Failed to load jobs');
    } finally {
      setLoading(false);
    }
  };

  const loadBatchJobs = async () => {
    try {
      const response = await schedulerApi.getBatchJobs();
      setBatchJobs(response.data.jobs || []);
    } catch (error) {
      console.error('Failed to load batch jobs');
    }
  };

  const loadExecutions = async () => {
    try {
      const response = await schedulerApi.getExecutions(executionType, 50);
      setExecutions(response.data.executions || []);
    } catch (error) {
      console.error('Failed to load executions');
    }
  };

  const loadJobDetail = async (name: string) => {
    setLoading(true);
    try {
      const response = await schedulerApi.getJob(name);
      setSelectedJob(response.data);
      setDetailModal(true);
    } catch (error) {
      message.error('Failed to load job detail');
    } finally {
      setLoading(false);
    }
  };

  const executeJob = async (name: string) => {
    setLoading(true);
    try {
      await schedulerApi.executeJob(name);
      message.success(`Job ${name} executed`);
      loadJobs();
      loadExecutions();
    } catch (error) {
      message.error('Failed to execute job');
    } finally {
      setLoading(false);
    }
  };

  const pauseJob = async (name: string) => {
    setLoading(true);
    try {
      await schedulerApi.pauseJob(name);
      message.success(`Job ${name} paused`);
      loadJobs();
    } catch (error) {
      message.error('Failed to pause job');
    } finally {
      setLoading(false);
    }
  };

  const resumeJob = async (name: string) => {
    setLoading(true);
    try {
      await schedulerApi.resumeJob(name);
      message.success(`Job ${name} resumed`);
      loadJobs();
    } catch (error) {
      message.error('Failed to resume job');
    } finally {
      setLoading(false);
    }
  };

  const executeBatchJob = async (name: string) => {
    setLoading(true);
    try {
      await schedulerApi.executeBatchJob(name);
      message.success(`Batch job ${name} started`);
      loadBatchJobs();
      loadExecutions();
    } catch (error) {
      message.error('Failed to start batch job');
    } finally {
      setLoading(false);
    }
  };

  const loadBatchProgress = async (name: string) => {
    try {
      const response = await schedulerApi.getBatchProgress(name);
      setBatchProgress(response.data);
      setProgressModal(true);
    } catch (error) {
      message.error('Failed to load progress');
    }
  };

  const stopBatchJob = async (name: string) => {
    setLoading(true);
    try {
      await schedulerApi.stopBatchJob(name);
      message.success('Batch job stopped');
      loadBatchJobs();
    } catch (error) {
      message.error('Failed to stop batch job');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadJobs();
    loadBatchJobs();
    loadExecutions();
  }, []);

  useEffect(() => {
    loadJobs();
  }, [statusFilter]);

  useEffect(() => {
    loadExecutions();
  }, [executionType]);

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'RUNNING': return 'blue';
      case 'SCHEDULED': return 'green';
      case 'PAUSED': return 'orange';
      case 'COMPLETED': return 'green';
      case 'FAILED': return 'red';
      case 'STOPPED': return 'default';
      default: return 'default';
    }
  };

  const jobColumns = [
    { title: 'Name', dataIndex: 'name', key: 'name' },
    { title: 'Group', dataIndex: 'group', key: 'group' },
    { title: 'Cron', dataIndex: 'cronExpression', key: 'cron' },
    { title: 'Status', dataIndex: 'status', key: 'status', render: (s: string) => <Tag color={getStatusColor(s)}>{s}</Tag> },
    { title: 'Next Fire', dataIndex: 'nextFireTimeFormatted', key: 'nextFire' },
    { title: 'Last Fire', dataIndex: 'lastFireTimeFormatted', key: 'lastFire' },
    {
      title: 'Actions',
      key: 'actions',
      render: (_: any, record: any) => (
        <Space>
          <Button size="small" icon={<EyeOutlined />} onClick={() => loadJobDetail(record.name)}>Detail</Button>
          <Button size="small" icon={<PlayCircleOutlined />} onClick={() => executeJob(record.name)}>Run</Button>
          {record.status === 'SCHEDULED' && (
            <Button size="small" icon={<PauseCircleOutlined />} onClick={() => pauseJob(record.name)}>Pause</Button>
          )}
          {record.status === 'PAUSED' && (
            <Button size="small" icon={<PlayCircleOutlined />} onClick={() => resumeJob(record.name)}>Resume</Button>
          )}
        </Space>
      ),
    },
  ];

  const batchColumns = [
    { title: 'Name', dataIndex: 'name', key: 'name' },
    { title: 'Description', dataIndex: 'description', key: 'description' },
    { title: 'Status', dataIndex: 'status', key: 'status', render: (s: string) => <Tag color={getStatusColor(s)}>{s}</Tag> },
    { title: 'Last Run', dataIndex: 'lastRunFormatted', key: 'lastRun' },
    {
      title: 'Actions',
      key: 'actions',
      render: (_: any, record: any) => (
        <Space>
          <Button size="small" icon={<PlayCircleOutlined />} onClick={() => executeBatchJob(record.name)} disabled={record.status === 'RUNNING'}>
            Start
          </Button>
          {record.status === 'RUNNING' && (
            <>
              <Button size="small" onClick={() => loadBatchProgress(record.name)}>Progress</Button>
              <Button size="small" danger onClick={() => stopBatchJob(record.name)}>Stop</Button>
            </>
          )}
        </Space>
      ),
    },
  ];

  const executionColumns = [
    { title: 'Job Name', dataIndex: 'jobName', key: 'jobName' },
    { title: 'Type', dataIndex: 'type', key: 'type', render: (t: string) => <Tag color={t === 'BATCH' ? 'purple' : 'blue'}>{t}</Tag> },
    { title: 'Status', dataIndex: 'status', key: 'status', render: (s: string) => <Tag color={getStatusColor(s)}>{s}</Tag> },
    { title: 'Started', dataIndex: 'startedAtFormatted', key: 'startedAt' },
    { title: 'Completed', dataIndex: 'completedAtFormatted', key: 'completedAt' },
    { title: 'Duration', dataIndex: 'durationFormatted', key: 'duration' },
    { title: 'Result', dataIndex: 'result', key: 'result', ellipsis: true },
  ];

  const items = [
    {
      key: '1',
      label: '#56 Scheduled Jobs',
      children: (
        <Card>
          <Title level={5}><ScheduleOutlined /> Scheduled Jobs</Title>
          <Space style={{ marginBottom: 16 }}>
            <Select value={statusFilter} onChange={setStatusFilter} style={{ width: 150 }} placeholder="Filter by status" allowClear>
              <Select.Option value="">All</Select.Option>
              <Select.Option value="SCHEDULED">Scheduled</Select.Option>
              <Select.Option value="PAUSED">Paused</Select.Option>
              <Select.Option value="RUNNING">Running</Select.Option>
            </Select>
            <Button icon={<ReloadOutlined />} onClick={loadJobs}>Refresh</Button>
          </Space>
          <Table dataSource={jobs} columns={jobColumns} rowKey="name" size="small" loading={loading} />
        </Card>
      ),
    },
    {
      key: '2',
      label: '#57 Batch Jobs',
      children: (
        <Card>
          <Title level={5}><DatabaseOutlined /> Batch Jobs</Title>
          <Text type="secondary">Long-running batch processing jobs with progress tracking.</Text>
          <Button icon={<ReloadOutlined />} onClick={loadBatchJobs} style={{ marginBottom: 16, marginLeft: 16 }}>Refresh</Button>
          <Table dataSource={batchJobs} columns={batchColumns} rowKey="name" size="small" />
        </Card>
      ),
    },
    {
      key: '3',
      label: '#58 Job Control',
      children: (
        <Card>
          <Title level={5}>Job Control</Title>
          <Text type="secondary">Manage job lifecycle: execute, pause, resume, stop.</Text>
          <div style={{ marginTop: 16 }}>
            <Text strong>Quick Actions:</Text>
            <Table
              dataSource={jobs}
              rowKey="name"
              size="small"
              style={{ marginTop: 8 }}
              columns={[
                { title: 'Job', dataIndex: 'name', key: 'name' },
                { title: 'Status', dataIndex: 'status', key: 'status', render: (s: string) => <Tag color={getStatusColor(s)}>{s}</Tag> },
                {
                  title: 'Actions',
                  key: 'actions',
                  render: (_: any, record: any) => (
                    <Space>
                      <Button size="small" type="primary" onClick={() => executeJob(record.name)}>Execute Now</Button>
                      {record.status === 'SCHEDULED' && (
                        <Button size="small" onClick={() => pauseJob(record.name)}>Pause</Button>
                      )}
                      {record.status === 'PAUSED' && (
                        <Button size="small" onClick={() => resumeJob(record.name)}>Resume</Button>
                      )}
                    </Space>
                  ),
                },
              ]}
            />
          </div>
        </Card>
      ),
    },
    {
      key: '4',
      label: '#59 Execution History',
      children: (
        <Card>
          <Title level={5}>Execution History</Title>
          <Space style={{ marginBottom: 16 }}>
            <Select value={executionType} onChange={setExecutionType} style={{ width: 150 }} placeholder="Filter by type" allowClear>
              <Select.Option value="">All</Select.Option>
              <Select.Option value="SCHEDULED">Scheduled</Select.Option>
              <Select.Option value="BATCH">Batch</Select.Option>
            </Select>
            <Button icon={<ReloadOutlined />} onClick={loadExecutions}>Refresh</Button>
          </Space>
          <Table dataSource={executions} columns={executionColumns} rowKey="id" size="small" />
        </Card>
      ),
    },
  ];

  return (
    <Spin spinning={loading}>
      <Title level={3}>
        <ScheduleOutlined /> Scheduler (#56~#59)
      </Title>
      <Tabs items={items} />

      <Modal
        title="Job Detail"
        open={detailModal}
        onCancel={() => setDetailModal(false)}
        footer={null}
        width={600}
      >
        {selectedJob && (
          <Descriptions bordered size="small" column={2}>
            <Descriptions.Item label="Name">{selectedJob.name}</Descriptions.Item>
            <Descriptions.Item label="Group">{selectedJob.group}</Descriptions.Item>
            <Descriptions.Item label="Status">
              <Tag color={getStatusColor(selectedJob.status)}>{selectedJob.status}</Tag>
            </Descriptions.Item>
            <Descriptions.Item label="Cron">{selectedJob.cronExpression}</Descriptions.Item>
            <Descriptions.Item label="Description" span={2}>{selectedJob.description}</Descriptions.Item>
            <Descriptions.Item label="Next Fire">{selectedJob.nextFireTimeFormatted}</Descriptions.Item>
            <Descriptions.Item label="Last Fire">{selectedJob.lastFireTimeFormatted}</Descriptions.Item>
            <Descriptions.Item label="Job Class" span={2}>{selectedJob.jobClass}</Descriptions.Item>
          </Descriptions>
        )}
      </Modal>

      <Modal
        title="Batch Progress"
        open={progressModal}
        onCancel={() => setProgressModal(false)}
        footer={null}
        width={500}
      >
        {batchProgress && (
          <>
            <Descriptions bordered size="small" column={2}>
              <Descriptions.Item label="Job">{batchProgress.jobName}</Descriptions.Item>
              <Descriptions.Item label="Status">
                <Tag color={getStatusColor(batchProgress.status)}>{batchProgress.status}</Tag>
              </Descriptions.Item>
              <Descriptions.Item label="Processed">{batchProgress.processedCount}</Descriptions.Item>
              <Descriptions.Item label="Total">{batchProgress.totalCount}</Descriptions.Item>
            </Descriptions>
            <div style={{ marginTop: 16 }}>
              <Progress
                percent={batchProgress.totalCount > 0 ? Math.round((batchProgress.processedCount / batchProgress.totalCount) * 100) : 0}
                status={batchProgress.status === 'RUNNING' ? 'active' : batchProgress.status === 'COMPLETED' ? 'success' : 'exception'}
              />
            </div>
            {batchProgress.currentItem && (
              <div style={{ marginTop: 8 }}>
                <Text type="secondary">Current: {batchProgress.currentItem}</Text>
              </div>
            )}
          </>
        )}
      </Modal>
    </Spin>
  );
};

export default SchedulerPage;
