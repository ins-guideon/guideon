import { Modal, Descriptions, Tag, Card, Space, Typography } from 'antd';
import { FileTextOutlined } from '@ant-design/icons';
import { useQuery } from '@tanstack/react-query';
import { documentService } from '@/services/documentService';
import type { DocumentDetailResponse } from '@/types';
import dayjs from 'dayjs';
import { LoadingSpinner } from './LoadingSpinner';

const { Text } = Typography;

interface DocumentDetailModalProps {
  open: boolean;
  onClose: () => void;
  documentId: string | null;
}

export const DocumentDetailModal = ({ open, onClose, documentId }: DocumentDetailModalProps) => {
  const { data: documentDetail, isLoading } = useQuery({
    queryKey: ['document', documentId],
    queryFn: () => documentService.getDocumentDetail(documentId!),
    enabled: !!documentId && open,
  });

  const formatFileSize = (size: number): string => {
    if (size < 1024) return `${size} B`;
    if (size < 1024 * 1024) return `${(size / 1024).toFixed(2)} KB`;
    return `${(size / (1024 * 1024)).toFixed(2)} MB`;
  };

  const getStatusTag = (status: string) => {
    const statusConfig: Record<string, { color: string; text: string }> = {
      indexed: { color: 'success', text: '인덱싱 완료' },
      pending: { color: 'processing', text: '대기 중' },
      error: { color: 'error', text: '오류' },
    };
    const config = statusConfig[status] || { color: 'default', text: status };
    return <Tag color={config.color}>{config.text}</Tag>;
  };

  return (
    <Modal
      title={
        <Space>
          <FileTextOutlined />
          <span>문서 상세 정보</span>
        </Space>
      }
      open={open}
      onCancel={onClose}
      footer={null}
      width={1000}
      style={{ top: 20 }}
    >
      {isLoading ? (
        <LoadingSpinner />
      ) : documentDetail ? (
        <div>
          <Descriptions bordered column={2} style={{ marginBottom: 24 }}>
            <Descriptions.Item label="파일명">{documentDetail.fileName}</Descriptions.Item>
            <Descriptions.Item label="규정 유형">
              <Tag color="blue">{documentDetail.regulationType}</Tag>
            </Descriptions.Item>
            <Descriptions.Item label="업로드 시간">
              {dayjs(documentDetail.uploadTime).format('YYYY-MM-DD HH:mm:ss')}
            </Descriptions.Item>
            <Descriptions.Item label="작성자">{documentDetail.uploaderName}</Descriptions.Item>
            <Descriptions.Item label="파일 크기">{formatFileSize(documentDetail.fileSize)}</Descriptions.Item>
            <Descriptions.Item label="상태">{getStatusTag(documentDetail.status)}</Descriptions.Item>
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
                {documentDetail.content}
              </pre>
            </Card>
          </div>
        </div>
      ) : null}
    </Modal>
  );
};

