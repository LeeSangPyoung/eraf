import React, { useState } from 'react';
import { Card, Button, Input, message, Typography, Space, Tag, Alert, Spin, Descriptions } from 'antd';
import { RobotOutlined, SafetyOutlined, CheckCircleOutlined, CloseCircleOutlined } from '@ant-design/icons';
import { securityApi } from '../../services/api';

const { Title, Text } = Typography;

const sampleUserAgents = [
  { label: 'Chrome Browser', value: 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36' },
  { label: 'Firefox Browser', value: 'Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:121.0) Gecko/20100101 Firefox/121.0' },
  { label: 'Googlebot', value: 'Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)' },
  { label: 'Bingbot', value: 'Mozilla/5.0 (compatible; bingbot/2.0; +http://www.bing.com/bingbot.htm)' },
  { label: 'curl', value: 'curl/7.88.1' },
  { label: 'Python Requests', value: 'python-requests/2.31.0' },
  { label: 'Wget', value: 'Wget/1.21.3' },
];

const BotDetectionPage: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [userAgent, setUserAgent] = useState(navigator.userAgent);
  const [result, setResult] = useState<any>(null);

  const handleDetect = async () => {
    setLoading(true);
    try {
      const response = await securityApi.detectBot(userAgent);
      setResult(response.data);
      if (response.data.isBot) {
        message.warning('Bot detected!');
      } else {
        message.success('Not a bot');
      }
    } catch (error) {
      message.error('Detection failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Spin spinning={loading}>
      <Title level={3}>
        <RobotOutlined /> Bot Detection (#16)
      </Title>

      <Card>
        <Title level={5}>Bot Detection</Title>
        <Text type="secondary">Analyze User-Agent to detect bots and crawlers.</Text>

        <div style={{ marginTop: 16 }}>
          <Text strong>Sample User-Agents:</Text>
          <div style={{ marginTop: 8 }}>
            <Space wrap>
              {sampleUserAgents.map((ua) => (
                <Tag
                  key={ua.label}
                  style={{ cursor: 'pointer' }}
                  onClick={() => setUserAgent(ua.value)}
                  color={userAgent === ua.value ? 'blue' : 'default'}
                >
                  {ua.label}
                </Tag>
              ))}
            </Space>
          </div>
        </div>

        <div style={{ marginTop: 16 }}>
          <Text strong>User-Agent:</Text>
          <Input.TextArea
            value={userAgent}
            onChange={(e) => setUserAgent(e.target.value)}
            rows={3}
            style={{ marginTop: 8 }}
          />
        </div>

        <div style={{ marginTop: 16 }}>
          <Button type="primary" icon={<SafetyOutlined />} onClick={handleDetect} loading={loading}>
            Detect Bot
          </Button>
        </div>

        {result && (
          <div style={{ marginTop: 24 }}>
            <Alert
              type={result.isBot ? 'warning' : 'success'}
              message={result.isBot ? 'Bot Detected' : 'Not a Bot'}
              icon={result.isBot ? <RobotOutlined /> : <CheckCircleOutlined />}
              showIcon
            />
            <Descriptions bordered column={1} style={{ marginTop: 16 }}>
              <Descriptions.Item label="Is Bot">
                {result.isBot ? (
                  <Tag icon={<CloseCircleOutlined />} color="error">Yes</Tag>
                ) : (
                  <Tag icon={<CheckCircleOutlined />} color="success">No</Tag>
                )}
              </Descriptions.Item>
              <Descriptions.Item label="Bot Type">
                <Tag color={result.botType === 'HUMAN' ? 'green' : 'orange'}>{result.botType || 'N/A'}</Tag>
              </Descriptions.Item>
              <Descriptions.Item label="Detection Method">{result.detectionMethod || 'N/A'}</Descriptions.Item>
              <Descriptions.Item label="Confidence">{result.confidence ? `${(result.confidence * 100).toFixed(1)}%` : 'N/A'}</Descriptions.Item>
            </Descriptions>
          </div>
        )}
      </Card>
    </Spin>
  );
};

export default BotDetectionPage;
