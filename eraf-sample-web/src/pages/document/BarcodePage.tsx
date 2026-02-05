import React, { useState } from 'react';
import { Card, Button, Input, Select, message, Typography, Tabs, Form, Space, Image, Spin } from 'antd';
import { BarcodeOutlined, QrcodeOutlined, DownloadOutlined } from '@ant-design/icons';
import { documentApi } from '../../services/api';

const { Title, Text } = Typography;

const BarcodePage: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [barcodeImage, setBarcodeImage] = useState<string | null>(null);
  const [qrImage, setQrImage] = useState<string | null>(null);

  const handleGenerateBarcode = async (values: any) => {
    setLoading(true);
    try {
      const response = await documentApi.generateBarcode(values);
      const blob = new Blob([response.data], { type: 'image/png' });
      const url = window.URL.createObjectURL(blob);
      setBarcodeImage(url);
      message.success('Barcode generated');
    } catch (error) {
      message.error('Failed to generate barcode');
    } finally {
      setLoading(false);
    }
  };

  const handleGenerateQr = async (values: any) => {
    setLoading(true);
    try {
      const response = await documentApi.generateQrCode(values);
      const blob = new Blob([response.data], { type: 'image/png' });
      const url = window.URL.createObjectURL(blob);
      setQrImage(url);
      message.success('QR Code generated');
    } catch (error) {
      message.error('Failed to generate QR Code');
    } finally {
      setLoading(false);
    }
  };

  const downloadImage = (url: string, filename: string) => {
    const link = document.createElement('a');
    link.href = url;
    link.download = filename;
    link.click();
  };

  const items = [
    {
      key: '1',
      label: '#8 Barcode',
      children: (
        <Card>
          <Title level={5}>Generate Barcode</Title>
          <Text type="secondary">Generate various barcode formats (Code128, EAN, UPC, etc.)</Text>
          <Form
            layout="vertical"
            onFinish={handleGenerateBarcode}
            style={{ marginTop: 16, maxWidth: 400 }}
            initialValues={{ content: '1234567890', format: 'CODE_128', width: 200, height: 80 }}
          >
            <Form.Item name="content" label="Content" rules={[{ required: true }]}>
              <Input placeholder="Barcode content" />
            </Form.Item>
            <Form.Item name="format" label="Format">
              <Select>
                <Select.Option value="CODE_128">Code 128</Select.Option>
                <Select.Option value="CODE_39">Code 39</Select.Option>
                <Select.Option value="EAN_13">EAN-13</Select.Option>
                <Select.Option value="EAN_8">EAN-8</Select.Option>
                <Select.Option value="UPC_A">UPC-A</Select.Option>
              </Select>
            </Form.Item>
            <Form.Item>
              <Button type="primary" htmlType="submit" icon={<BarcodeOutlined />} loading={loading}>
                Generate Barcode
              </Button>
            </Form.Item>
          </Form>
          {barcodeImage && (
            <div style={{ marginTop: 16 }}>
              <Image src={barcodeImage} alt="Barcode" style={{ maxWidth: 300 }} />
              <br />
              <Button
                icon={<DownloadOutlined />}
                onClick={() => downloadImage(barcodeImage, 'barcode.png')}
                style={{ marginTop: 8 }}
              >
                Download
              </Button>
            </div>
          )}
        </Card>
      ),
    },
    {
      key: '2',
      label: '#9 QR Code',
      children: (
        <Card>
          <Title level={5}>Generate QR Code</Title>
          <Text type="secondary">Generate QR codes for URLs, text, or any data.</Text>
          <Form
            layout="vertical"
            onFinish={handleGenerateQr}
            style={{ marginTop: 16, maxWidth: 400 }}
            initialValues={{ content: 'https://example.com', size: 200 }}
          >
            <Form.Item name="content" label="Content" rules={[{ required: true }]}>
              <Input placeholder="URL or text" />
            </Form.Item>
            <Form.Item name="size" label="Size (px)">
              <Select>
                <Select.Option value={150}>150x150</Select.Option>
                <Select.Option value={200}>200x200</Select.Option>
                <Select.Option value={300}>300x300</Select.Option>
                <Select.Option value={400}>400x400</Select.Option>
              </Select>
            </Form.Item>
            <Form.Item>
              <Button type="primary" htmlType="submit" icon={<QrcodeOutlined />} loading={loading}>
                Generate QR Code
              </Button>
            </Form.Item>
          </Form>
          {qrImage && (
            <div style={{ marginTop: 16 }}>
              <Image src={qrImage} alt="QR Code" style={{ maxWidth: 300 }} />
              <br />
              <Button
                icon={<DownloadOutlined />}
                onClick={() => downloadImage(qrImage, 'qrcode.png')}
                style={{ marginTop: 8 }}
              >
                Download
              </Button>
            </div>
          )}
        </Card>
      ),
    },
  ];

  return (
    <Spin spinning={loading}>
      <Title level={3}>
        <BarcodeOutlined /> Barcode & QR Code (#8~#9)
      </Title>
      <Tabs items={items} />
    </Spin>
  );
};

export default BarcodePage;
