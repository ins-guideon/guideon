import { useState } from 'react';
import {
  Card,
  Table,
  Typography,
  Space,
  Tag,
  Modal,
  Descriptions,
  message,
} from 'antd';
import {
  FileTextOutlined,
  EyeOutlined,
} from '@ant-design/icons';
import { useQuery } from '@tanstack/react-query';
import { documentService } from '@/services/documentService';
import type { DocumentInfo, DocumentDetailResponse } from '@/types';
import dayjs from 'dayjs';
import { LoadingSpinner } from '@/components/common/LoadingSpinner';

const { Title, Text } = Typography;

export const DocumentView = () => {
  const [selectedDocument, setSelectedDocument] = useState<DocumentDetailResponse | null>(null);
  const [modalVisible, setModalVisible] = useState(false);
  const [isDetailLoading, setIsDetailLoading] = useState(false);

  // 문서 목록 조회
  const { data: documentList, isLoading } = useQuery({
    queryKey: ['documents-view'],
    queryFn: () => documentService.getDocumentsForView(),
  });

  const handleFileNameClick = async (record: DocumentInfo) => {
    setIsDetailLoading(true);
    try {
      const detail = await documentService.getDocumentDetail(record.id);
      setSelectedDocument(detail);
      setModalVisible(true);
    } catch (error) {
      message.error('문서 상세 정보를 불러오는 중 오류가 발생했습니다.');
    } finally {
      setIsDetailLoading(false);
    }
  };

  const handleCloseModal = () => {
    setModalVisible(false);
    setSelectedDocument(null);
  };

  const columns = [
    {
      title: '파일명',
      dataIndex: 'fileName',
      key: 'fileName',
      render: (text: string, record: DocumentInfo) => (
        <a
          onClick={() => handleFileNameClick(record)}
          style={{ cursor: 'pointer', color: '#1890ff' }}
        >
          <FileTextOutlined style={{ marginRight: 8 }} />
          {text}
        </a>
      ),
    },
    {
      title: '규정 유형',
      dataIndex: 'regulationType',
      key: 'regulationType',
      render: (type: string) => (
        <Tag color="blue">{type}</Tag>
      ),
    },
    {
      title: '업로드 시간',
      dataIndex: 'uploadTimestamp',
      key: 'uploadTimestamp',
      render: (timestamp: number) => dayjs(timestamp).format('YYYY-MM-DD HH:mm:ss'),
      sorter: (a: DocumentInfo, b: DocumentInfo) => a.uploadTimestamp - b.uploadTimestamp,
    },
    {
      title: '파일 크기',
      dataIndex: 'fileSize',
      key: 'fileSize',
      render: (size: number) => {
        if (size < 1024) return `${size} B`;
        if (size < 1024 * 1024) return `${(size / 1024).toFixed(2)} KB`;
        return `${(size / (1024 * 1024)).toFixed(2)} MB`;
      },
    },
    {
      title: '상태',
      dataIndex: 'status',
      key: 'status',
      render: (status: string) => {
        const statusConfig: Record<string, { color: string; text: string }> = {
          indexed: { color: 'success', text: '인덱싱 완료' },
          pending: { color: 'processing', text: '대기 중' },
          error: { color: 'error', text: '오류' },
        };
        const config = statusConfig[status] || { color: 'default', text: status };
        return <Tag color={config.color}>{config.text}</Tag>;
      },
    },
  ];

  if (isLoading) {
    return <LoadingSpinner />;
  }

  return (
    <div style={{ padding: 24 }}>
      <Card
        style={{
          border: '1px solid #e8e8e8',
          borderRadius: 8,
        }}
      >
        <Space direction="vertical" size="large" style={{ width: '100%' }}>
          <div>
            <Title level={2} style={{ marginBottom: 8 }}>
              <EyeOutlined style={{ marginRight: 12 }} />
              문서 조회
            </Title>
            <Text type="secondary">
              업로드된 문서 목록을 조회하고 상세 정보를 확인할 수 있습니다.
            </Text>
          </div>

          <Table
            columns={columns}
            dataSource={documentList?.documents || []}
            rowKey="id"
            pagination={{
              pageSize: 10,
              showSizeChanger: true,
              showTotal: (total) => `총 ${total}건`,
            }}
            style={{ marginTop: 24 }}
          />
        </Space>
      </Card>

      <Modal
        title={
          <Space>
            <FileTextOutlined />
            <span>문서 상세 정보</span>
          </Space>
        }
        open={modalVisible}
        onCancel={handleCloseModal}
        footer={null}
        width={1000}
        style={{ top: 20 }}
      >
        {isDetailLoading ? (
          <LoadingSpinner />
        ) : selectedDocument ? (
          <div>
            <Descriptions bordered column={2} style={{ marginBottom: 24 }}>
              <Descriptions.Item label="파일명">
                {selectedDocument.fileName}
              </Descriptions.Item>
              <Descriptions.Item label="규정 유형">
                <Tag color="blue">{selectedDocument.regulationType}</Tag>
              </Descriptions.Item>
              <Descriptions.Item label="업로드 시간">
                {dayjs(selectedDocument.uploadTime).format('YYYY-MM-DD HH:mm:ss')}
              </Descriptions.Item>
              <Descriptions.Item label="작성자">
                {selectedDocument.uploaderName}
              </Descriptions.Item>
              <Descriptions.Item label="파일 크기">
                {selectedDocument.fileSize < 1024
                  ? `${selectedDocument.fileSize} B`
                  : selectedDocument.fileSize < 1024 * 1024
                    ? `${(selectedDocument.fileSize / 1024).toFixed(2)} KB`
                    : `${(selectedDocument.fileSize / (1024 * 1024)).toFixed(2)} MB`}
              </Descriptions.Item>
              <Descriptions.Item label="상태">
                <Tag
                  color={
                    selectedDocument.status === 'indexed'
                      ? 'success'
                      : selectedDocument.status === 'pending'
                        ? 'processing'
                        : 'error'
                  }
                >
                  {selectedDocument.status === 'indexed'
                    ? '인덱싱 완료'
                    : selectedDocument.status === 'pending'
                      ? '대기 중'
                      : '오류'}
                </Tag>
              </Descriptions.Item>
            </Descriptions>

            <div>
              <Text strong style={{ display: 'block', marginBottom: 12 }}>
                내용
              </Text>
              <Card
                style={{
                  backgroundColor: '#fafafa',
                  maxHeight: 400,
                  overflowY: 'auto',
                }}
              >
                <pre
                  style={{
                    whiteSpace: 'pre-wrap',
                    wordBreak: 'break-word',
                    margin: 0,
                    fontFamily: 'inherit',
                  }}
                >
                  {selectedDocument.content}
                </pre>
              </Card>
            </div>
          </div>
        ) : null}
      </Modal>
    </div>
  );
};

