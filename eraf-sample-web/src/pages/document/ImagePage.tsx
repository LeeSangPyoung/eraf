import React, { useState } from 'react';
import { Card, Button, Upload, InputNumber, message, Typography, Tabs, Space, Image, Spin, Input } from 'antd';
import { PictureOutlined, UploadOutlined, DownloadOutlined } from '@ant-design/icons';
import { documentApi } from '../../services/api';

const { Title, Text } = Typography;

const ImagePage: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [resizedImage, setResizedImage] = useState<string | null>(null);
  const [thumbnailImage, setThumbnailImage] = useState<string | null>(null);
  const [watermarkedImage, setWatermarkedImage] = useState<string | null>(null);
  const [resizeWidth, setResizeWidth] = useState(400);
  const [resizeHeight, setResizeHeight] = useState(300);
  const [thumbnailSize, setThumbnailSize] = useState(150);
  const [watermarkText, setWatermarkText] = useState('SAMPLE');

  const handleResize = async (file: File) => {
    setLoading(true);
    try {
      const response = await documentApi.resizeImage(file, resizeWidth, resizeHeight);
      // Backend returns JSON with base64 data URL in 'image' field
      setResizedImage(response.data.image);
      message.success('Image resized successfully');
    } catch (error) {
      message.error('Failed to resize image');
    } finally {
      setLoading(false);
    }
    return false;
  };

  const handleThumbnail = async (file: File) => {
    setLoading(true);
    try {
      const response = await documentApi.createThumbnail(file, thumbnailSize);
      // Backend returns JSON with base64 data URL in 'image' field
      setThumbnailImage(response.data.image);
      message.success('Thumbnail created successfully');
    } catch (error) {
      message.error('Failed to create thumbnail');
    } finally {
      setLoading(false);
    }
    return false;
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
      label: '#10 Resize',
      children: (
        <Card>
          <Title level={5}>Image Resize</Title>
          <Text type="secondary">Resize an image to specified dimensions.</Text>
          <Space direction="vertical" style={{ marginTop: 16 }}>
            <Space>
              <Text>Width:</Text>
              <InputNumber value={resizeWidth} onChange={(v) => setResizeWidth(v || 400)} min={50} max={2000} />
              <Text>Height:</Text>
              <InputNumber value={resizeHeight} onChange={(v) => setResizeHeight(v || 300)} min={50} max={2000} />
            </Space>
            <Upload accept="image/*" beforeUpload={handleResize} showUploadList={false}>
              <Button icon={<UploadOutlined />} loading={loading}>
                Upload & Resize
              </Button>
            </Upload>
          </Space>
          {resizedImage && (
            <div style={{ marginTop: 16 }}>
              <Image src={resizedImage} alt="Resized" style={{ maxWidth: 400 }} />
              <br />
              <Button
                icon={<DownloadOutlined />}
                onClick={() => downloadImage(resizedImage, 'resized.png')}
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
      label: '#11 Thumbnail',
      children: (
        <Card>
          <Title level={5}>Create Thumbnail</Title>
          <Text type="secondary">Generate a thumbnail from an image.</Text>
          <Space direction="vertical" style={{ marginTop: 16 }}>
            <Space>
              <Text>Size:</Text>
              <InputNumber value={thumbnailSize} onChange={(v) => setThumbnailSize(v || 150)} min={50} max={500} />
              <Text>px</Text>
            </Space>
            <Upload accept="image/*" beforeUpload={handleThumbnail} showUploadList={false}>
              <Button icon={<UploadOutlined />} loading={loading}>
                Upload & Create Thumbnail
              </Button>
            </Upload>
          </Space>
          {thumbnailImage && (
            <div style={{ marginTop: 16 }}>
              <Image src={thumbnailImage} alt="Thumbnail" style={{ maxWidth: 200 }} />
              <br />
              <Button
                icon={<DownloadOutlined />}
                onClick={() => downloadImage(thumbnailImage, 'thumbnail.png')}
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
      key: '3',
      label: '#12 Watermark',
      children: (
        <Card>
          <Title level={5}>Image Watermark</Title>
          <Text type="secondary">Add a text watermark to an image.</Text>
          <Space direction="vertical" style={{ marginTop: 16 }}>
            <Space>
              <Text>Watermark Text:</Text>
              <Input
                value={watermarkText}
                onChange={(e) => setWatermarkText(e.target.value)}
                style={{ width: 200 }}
              />
            </Space>
            <Upload accept="image/*" beforeUpload={() => false} showUploadList={false}>
              <Button icon={<UploadOutlined />} loading={loading}>
                Upload & Add Watermark
              </Button>
            </Upload>
          </Space>
          {watermarkedImage && (
            <div style={{ marginTop: 16 }}>
              <Image src={watermarkedImage} alt="Watermarked" style={{ maxWidth: 400 }} />
              <br />
              <Button
                icon={<DownloadOutlined />}
                onClick={() => downloadImage(watermarkedImage, 'watermarked.png')}
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
        <PictureOutlined /> Image Processing (#10~#12)
      </Title>
      <Tabs items={items} />
    </Spin>
  );
};

export default ImagePage;
