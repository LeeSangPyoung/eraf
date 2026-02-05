import React, { useState, useEffect } from 'react';
import { Card, Button, Typography, Spin, Descriptions, Tag, Space } from 'antd';
import { ApiOutlined, ReloadOutlined, LinkOutlined } from '@ant-design/icons';
import { monitoringApi } from '../../services/api';

const { Title, Text, Link } = Typography;

const SwaggerPage: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [swaggerInfo, setSwaggerInfo] = useState<any>(null);

  const loadSwaggerInfo = async () => {
    setLoading(true);
    try {
      const response = await monitoringApi.getSwaggerInfo();
      setSwaggerInfo(response.data);
    } catch (error) {
      console.error('Failed to load Swagger info');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadSwaggerInfo();
  }, []);

  const openSwaggerUI = () => {
    window.open('http://localhost:8080/swagger-ui.html', '_blank');
  };

  const openApiDocs = () => {
    window.open('http://localhost:8080/v3/api-docs', '_blank');
  };

  return (
    <Spin spinning={loading}>
      <Title level={3}>
        <ApiOutlined /> API Documentation (#67)
      </Title>

      <Card>
        <Space style={{ marginBottom: 24 }}>
          <Button type="primary" icon={<LinkOutlined />} onClick={openSwaggerUI} size="large">
            Open Swagger UI
          </Button>
          <Button icon={<ApiOutlined />} onClick={openApiDocs}>
            Open API Docs (JSON)
          </Button>
          <Button icon={<ReloadOutlined />} onClick={loadSwaggerInfo}>
            Refresh Info
          </Button>
        </Space>

        {swaggerInfo && (
          <>
            <Descriptions bordered column={2}>
              <Descriptions.Item label="Title">{swaggerInfo.title}</Descriptions.Item>
              <Descriptions.Item label="Version">{swaggerInfo.version}</Descriptions.Item>
              <Descriptions.Item label="Description" span={2}>{swaggerInfo.description}</Descriptions.Item>
              <Descriptions.Item label="Contact">{swaggerInfo.contact?.name}</Descriptions.Item>
              <Descriptions.Item label="Email">
                {swaggerInfo.contact?.email && (
                  <Link href={`mailto:${swaggerInfo.contact.email}`}>{swaggerInfo.contact.email}</Link>
                )}
              </Descriptions.Item>
              <Descriptions.Item label="License">{swaggerInfo.license?.name}</Descriptions.Item>
              <Descriptions.Item label="Terms of Service">
                {swaggerInfo.termsOfService && (
                  <Link href={swaggerInfo.termsOfService} target="_blank">{swaggerInfo.termsOfService}</Link>
                )}
              </Descriptions.Item>
            </Descriptions>

            {swaggerInfo.servers && swaggerInfo.servers.length > 0 && (
              <Card size="small" title="Servers" style={{ marginTop: 16 }}>
                {swaggerInfo.servers.map((server: any, idx: number) => (
                  <div key={idx} style={{ marginBottom: 8 }}>
                    <Tag color="blue">{server.url}</Tag>
                    <Text type="secondary">{server.description}</Text>
                  </div>
                ))}
              </Card>
            )}

            {swaggerInfo.tags && swaggerInfo.tags.length > 0 && (
              <Card size="small" title="API Tags" style={{ marginTop: 16 }}>
                <Space wrap>
                  {swaggerInfo.tags.map((tag: any) => (
                    <Tag key={tag.name} color="blue">
                      {tag.name}
                    </Tag>
                  ))}
                </Space>
              </Card>
            )}

            {swaggerInfo.paths && (
              <Card size="small" title="API Endpoints Summary" style={{ marginTop: 16 }}>
                <Descriptions bordered size="small" column={4}>
                  <Descriptions.Item label="Total Endpoints">{swaggerInfo.endpointCount || Object.keys(swaggerInfo.paths).length}</Descriptions.Item>
                  <Descriptions.Item label="GET">{swaggerInfo.methodCounts?.GET || 0}</Descriptions.Item>
                  <Descriptions.Item label="POST">{swaggerInfo.methodCounts?.POST || 0}</Descriptions.Item>
                  <Descriptions.Item label="PUT">{swaggerInfo.methodCounts?.PUT || 0}</Descriptions.Item>
                  <Descriptions.Item label="DELETE">{swaggerInfo.methodCounts?.DELETE || 0}</Descriptions.Item>
                  <Descriptions.Item label="PATCH">{swaggerInfo.methodCounts?.PATCH || 0}</Descriptions.Item>
                </Descriptions>
              </Card>
            )}
          </>
        )}

        <Card size="small" title="Quick Links" style={{ marginTop: 16 }}>
          <Space direction="vertical">
            <Link href="http://localhost:8080/swagger-ui.html" target="_blank">
              <LinkOutlined /> Swagger UI - Interactive API Documentation
            </Link>
            <Link href="http://localhost:8080/v3/api-docs" target="_blank">
              <ApiOutlined /> OpenAPI 3.0 Specification (JSON)
            </Link>
            <Link href="http://localhost:8080/v3/api-docs.yaml" target="_blank">
              <ApiOutlined /> OpenAPI 3.0 Specification (YAML)
            </Link>
          </Space>
        </Card>

        <Card size="small" title="Usage Guide" style={{ marginTop: 16, background: '#f5f5f5' }}>
          <Text>
            The Swagger UI provides interactive documentation for all ERAF APIs. You can:
          </Text>
          <ul style={{ marginTop: 8 }}>
            <li>Browse all available endpoints organized by tags</li>
            <li>View request/response schemas and examples</li>
            <li>Test APIs directly from the browser</li>
            <li>Generate client code using the OpenAPI specification</li>
            <li>Export API documentation in various formats</li>
          </ul>
        </Card>
      </Card>
    </Spin>
  );
};

export default SwaggerPage;
