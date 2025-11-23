import { useState } from 'react';
import {
  Card,
  Table,
  Typography,
  Space,
  Tag,
  message,
} from 'antd';
import {
  FileTextOutlined,
  EyeOutlined,
} from '@ant-design/icons';
import { useQuery } from '@tanstack/react-query';
import { documentService } from '@/services/documentService';
import type { DocumentInfo } from '@/types';
import dayjs from 'dayjs';
import { LoadingSpinner } from '@/components/common/LoadingSpinner';
import { DocumentDetailModal } from '@/components/common/DocumentDetailModal';

const { Title, Text } = Typography;

export const DocumentView = () => {
  const [selectedDocumentId, setSelectedDocumentId] = useState<string | null>(null);
  const [modalVisible, setModalVisible] = useState(false);

  // 문서 목록 조회 - 페이지가 열릴 때마다 항상 최신 데이터를 가져옴
  const { data: documentList, isLoading } = useQuery({
    queryKey: ['documents-view'],
    queryFn: () => documentService.getDocumentsForView(),
    refetchOnMount: true, // 컴포넌트 마운트 시 항상 재요청
    staleTime: 0, // 데이터를 즉시 stale로 표시하여 항상 재요청
  });

  const handleFileNameClick = (record: DocumentInfo) => {
    setSelectedDocumentId(record.id);
    setModalVisible(true);
  };

  const handleCloseModal = () => {
    setModalVisible(false);
    setSelectedDocumentId(null);
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

      <DocumentDetailModal
        open={modalVisible}
        onClose={handleCloseModal}
        documentId={selectedDocumentId}
      />
    </div>
  );
};

