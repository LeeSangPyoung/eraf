import React, { useState } from 'react';
import { Card, Button, Upload, Table, message, Typography, Space, InputNumber, Tabs, Spin, Row, Col } from 'antd';
import { DownloadOutlined, UploadOutlined, FileExcelOutlined } from '@ant-design/icons';
import { documentApi } from '../../services/api';

const { Title, Text } = Typography;

const ExcelPage: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [parsedData, setParsedData] = useState<any[]>([]);
  const [columns, setColumns] = useState<any[]>([]);
  const [streamingRows, setStreamingRows] = useState(1000);

  const handleDownload = async () => {
    setLoading(true);
    try {
      const response = await documentApi.downloadExcel();
      const blob = new Blob([response.data], {
        type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
      });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = 'sample.xlsx';
      link.click();
      window.URL.revokeObjectURL(url);
      message.success('Excel downloaded successfully');
    } catch (error) {
      message.error('Failed to download Excel');
    } finally {
      setLoading(false);
    }
  };

  const handleUpload = async (file: File) => {
    setLoading(true);
    try {
      const response = await documentApi.uploadExcel(file);
      const data = response.data;

      if (data.rows && data.rows.length > 0) {
        const cols = Object.keys(data.rows[0]).map((key) => ({
          title: key,
          dataIndex: key,
          key: key,
        }));
        setColumns(cols);
        setParsedData(data.rows.map((row: any, idx: number) => ({ ...row, key: idx })));
        message.success(`Parsed ${data.rows.length} rows`);
      }
    } catch (error) {
      message.error('Failed to parse Excel');
    } finally {
      setLoading(false);
    }
    return false;
  };

  const handleStreamingDownload = async () => {
    setLoading(true);
    try {
      const response = await documentApi.downloadStreamingExcel(streamingRows);
      const blob = new Blob([response.data], {
        type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
      });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `streaming_${streamingRows}.xlsx`;
      link.click();
      window.URL.revokeObjectURL(url);
      message.success(`Streaming Excel with ${streamingRows} rows downloaded`);
    } catch (error) {
      message.error('Failed to download streaming Excel');
    } finally {
      setLoading(false);
    }
  };

  const items = [
    {
      key: '1',
      label: '#1 Excel Download',
      children: (
        <Card>
          <Title level={5}>Download Sample Excel</Title>
          <Text type="secondary">Generate and download an Excel file with sample data.</Text>
          <div style={{ marginTop: 16 }}>
            <Button
              type="primary"
              icon={<DownloadOutlined />}
              onClick={handleDownload}
              loading={loading}
              size="large"
            >
              Download Excel
            </Button>
          </div>
        </Card>
      ),
    },
    {
      key: '2',
      label: '#2 Excel Upload',
      children: (
        <Card>
          <Title level={5}>Upload & Parse Excel</Title>
          <Text type="secondary">Upload an Excel file to parse and display its contents.</Text>
          <div style={{ marginTop: 16 }}>
            <Upload
              accept=".xlsx,.xls"
              beforeUpload={handleUpload}
              showUploadList={false}
            >
              <Button icon={<UploadOutlined />} loading={loading} size="large">
                Upload Excel
              </Button>
            </Upload>
          </div>
          {parsedData.length > 0 && (
            <div style={{ marginTop: 16 }}>
              <Title level={5}>Parsed Data ({parsedData.length} rows)</Title>
              <Table
                columns={columns}
                dataSource={parsedData}
                size="small"
                scroll={{ x: true, y: 300 }}
                pagination={{ pageSize: 10 }}
              />
            </div>
          )}
        </Card>
      ),
    },
    {
      key: '3',
      label: '#3 Template Based',
      children: (
        <Card>
          <Title level={5}>Template-based Excel Generation</Title>
          <Text type="secondary">Generate Excel using a template with data binding.</Text>
          <div style={{ marginTop: 16 }}>
            <Button type="primary" icon={<FileExcelOutlined />} disabled>
              Coming Soon
            </Button>
          </div>
        </Card>
      ),
    },
    {
      key: '4',
      label: '#4 Streaming (Large)',
      children: (
        <Card>
          <Title level={5}>Large Excel Streaming (SXSSF)</Title>
          <Text type="secondary">Generate large Excel files using streaming API.</Text>
          <Row gutter={16} style={{ marginTop: 16 }}>
            <Col>
              <Space>
                <Text>Rows:</Text>
                <InputNumber
                  min={100}
                  max={100000}
                  value={streamingRows}
                  onChange={(v) => setStreamingRows(v || 1000)}
                />
                <Button
                  type="primary"
                  icon={<DownloadOutlined />}
                  onClick={handleStreamingDownload}
                  loading={loading}
                >
                  Download Streaming Excel
                </Button>
              </Space>
            </Col>
          </Row>
        </Card>
      ),
    },
  ];

  return (
    <Spin spinning={loading}>
      <Title level={3}>
        <FileExcelOutlined /> Excel Features (#1~#4)
      </Title>
      <Tabs items={items} />
    </Spin>
  );
};

export default ExcelPage;
