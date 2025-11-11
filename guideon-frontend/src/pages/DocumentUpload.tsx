import { useState } from 'react';
import {
  Card,
  Upload,
  Button,
  Select,
  Table,
  Typography,
  Space,
  message,
  Tag,
  Popconfirm,
  Progress,
} from 'antd';
import {
  UploadOutlined,
  DeleteOutlined,
  FileTextOutlined,
  FilePdfOutlined,
  FileWordOutlined,
} from '@ant-design/icons';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { documentService } from '@/services/documentService';
import type { DocumentInfo } from '@/types';
import { REGULATION_TYPES } from '@/types';
import type { UploadFile } from 'antd';
import dayjs from 'dayjs';
import { useNavigate } from 'react-router-dom';

const { Title, Paragraph, Text } = Typography;
const { Option } = Select;

export const DocumentUpload = () => {
  const queryClient = useQueryClient();
  const navigate = useNavigate();
  const [selectedType, setSelectedType] = useState<string>('');
  const [fileList, setFileList] = useState<UploadFile[]>([]);

  // 문서 목록 조회
  const { data: documentList, isLoading } = useQuery({
    queryKey: ['documents'],
    queryFn: () => documentService.getDocuments(),
  });

  // 텍스트 추출(프리뷰 단계)
  const { mutate: extractText, isPending: isUploading } = useMutation({
    mutationFn: ({ file, type }: { file: File; type: string }) =>
      documentService.extractText(file, type),
    onSuccess: ({ uploadId, text }) => {
      message.success('텍스트를 추출했습니다. 확인 페이지로 이동합니다.');
      setFileList([]);
      navigate(`/documents/upload/${uploadId}`, { state: { text } });
    },
    onError: (error) => {
      message.error(error instanceof Error ? error.message : '텍스트 추출 중 오류가 발생했습니다.');
    },
  });

  // 문서 삭제
  const { mutate: deleteDocument } = useMutation({
    mutationFn: (id: string) => documentService.deleteDocument(id),
    onSuccess: () => {
      message.success('문서가 삭제되었습니다.');
      queryClient.invalidateQueries({ queryKey: ['documents'] });
    },
    onError: (error) => {
      message.error(error instanceof Error ? error.message : '삭제 중 오류가 발생했습니다.');
    },
  });

  const handleUpload = () => {
    if (fileList.length === 0) {
      message.warning('업로드할 파일을 선택해주세요.');
      return;
    }

    if (!selectedType) {
      message.warning('규정 유형을 선택해주세요.');
      return;
    }

    const file = fileList[0].originFileObj as File;
    extractText({ file, type: selectedType });
  };

  const beforeUpload = (file: File) => {
    const isValidType =
      file.type === 'application/pdf' ||
      file.type === 'application/msword' ||
      file.type === 'application/vnd.openxmlformats-officedocument.wordprocessingml.document' ||
      file.type === 'text/plain';

    if (!isValidType) {
      message.error('PDF, DOC, DOCX, TXT 파일만 업로드 가능합니다.');
      return false;
    }

    const isLt10M = file.size / 1024 / 1024 < 10;
    if (!isLt10M) {
      message.error('파일 크기는 10MB를 초과할 수 없습니다.');
      return false;
    }

    return false; // 자동 업로드 방지
  };

  const getFileIcon = (fileName: string) => {
    const extension = fileName.split('.').pop()?.toLowerCase();
    switch (extension) {
      case 'pdf':
        return <FilePdfOutlined style={{ fontSize: 20, color: '#ff4d4f' }} />;
      case 'doc':
      case 'docx':
        return <FileWordOutlined style={{ fontSize: 20, color: '#1890ff' }} />;
      case 'txt':
        return <FileTextOutlined style={{ fontSize: 20, color: '#52c41a' }} />;
      default:
        return <FileTextOutlined style={{ fontSize: 20 }} />;
    }
  };

  const formatFileSize = (bytes: number) => {
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
    return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
  };

  const columns = [
    {
      title: '파일명',
      dataIndex: 'fileName',
      key: 'fileName',
      render: (text: string) => (
        <Space>
          {getFileIcon(text)}
          <Text strong>{text}</Text>
        </Space>
      ),
    },
    {
      title: '규정 유형',
      dataIndex: 'regulationType',
      key: 'regulationType',
      render: (type: string) => <Tag color="blue">{type}</Tag>,
    },
    {
      title: '파일 크기',
      dataIndex: 'fileSize',
      key: 'fileSize',
      render: (size: number) => <Text>{formatFileSize(size)}</Text>,
    },
    {
      title: '업로드 시간',
      dataIndex: 'uploadTimestamp',
      key: 'uploadTimestamp',
      render: (timestamp: number) => (
        <Text>{dayjs(timestamp).format('YYYY-MM-DD HH:mm:ss')}</Text>
      ),
    },
    {
      title: '상태',
      dataIndex: 'status',
      key: 'status',
      render: (status: string) => {
        const statusConfig = {
          indexed: { color: 'success', text: '인덱싱 완료' },
          pending: { color: 'processing', text: '처리 중' },
          error: { color: 'error', text: '오류' },
        };
        const config = statusConfig[status as keyof typeof statusConfig] || statusConfig.pending;
        return <Tag color={config.color}>{config.text}</Tag>;
      },
    },
    {
      title: '작업',
      key: 'action',
      render: (_: unknown, record: DocumentInfo) => (
        <Popconfirm
          title="문서를 삭제하시겠습니까?"
          description="삭제된 문서는 복구할 수 없습니다."
          onConfirm={() => deleteDocument(record.id)}
          okText="삭제"
          cancelText="취소"
        >
          <Button type="link" danger icon={<DeleteOutlined />}>
            삭제
          </Button>
        </Popconfirm>
      ),
    },
  ];

  return (
    <div style={{ maxWidth: 1200, margin: '0 auto' }}>
      <div style={{ marginBottom: 32 }}>
        <Title level={2} style={{ marginBottom: 8 }}>
          문서 업로드
        </Title>
        <Paragraph type="secondary" style={{ fontSize: 15 }}>
          PDF, DOC, DOCX, TXT 파일을 업로드하여 벡터 데이터베이스에 인덱싱합니다.
        </Paragraph>
      </div>

      {/* 업로드 섹션 */}
      <Card
        title={<Text strong style={{ fontSize: 16 }}>파일 업로드</Text>}
        style={{
          marginBottom: 32,
          border: '1px solid #e8e8e8',
          borderRadius: 8,
        }}
        bodyStyle={{ padding: 24 }}
      >
        <Space direction="vertical" size="large" style={{ width: '100%' }}>
          <div>
            <Text strong style={{ display: 'block', marginBottom: 12 }}>
              규정 유형 선택 <span style={{ color: '#ff4d4f' }}>*</span>
            </Text>
            <Select
              style={{ width: '100%' }}
              placeholder="규정 유형을 선택하세요"
              value={selectedType || undefined}
              onChange={setSelectedType}
              size="large"
              showSearch
              filterOption={(input, option) =>
                (() => {
                  const child = option?.children;
                  const text = typeof child === 'string' ? child : '';
                  return text.toLowerCase().includes(input.toLowerCase());
                })()
              }
            >
              {REGULATION_TYPES.map((type) => (
                <Option key={type} value={type}>
                  {type}
                </Option>
              ))}
            </Select>
          </div>

          <div>
            <Text strong style={{ display: 'block', marginBottom: 12 }}>
              파일 선택 <span style={{ color: '#ff4d4f' }}>*</span>
            </Text>
            <Upload
              fileList={fileList}
              onChange={({ fileList: newFileList }) => setFileList(newFileList)}
              beforeUpload={beforeUpload}
              maxCount={1}
              accept=".pdf,.doc,.docx,.txt"
            >
              <Button icon={<UploadOutlined />} size="large">
                파일 선택
              </Button>
            </Upload>
            <Text type="secondary" style={{ display: 'block', marginTop: 8 }}>
              지원 형식: PDF, DOC, DOCX, TXT (최대 10MB)
            </Text>
          </div>

          <Button
            type="primary"
            size="large"
            onClick={handleUpload}
            loading={isUploading}
            disabled={fileList.length === 0 || !selectedType}
            style={{
              height: 48,
              fontSize: 16,
              fontWeight: 500,
            }}
            block
          >
            {isUploading ? '텍스트 추출 중...' : '업로드 후 텍스트 추출'}
          </Button>

          {isUploading && (
            <div>
              <Text type="secondary" style={{ display: 'block', marginBottom: 8 }}>
                문서를 파싱하여 본문 텍스트를 추출하는 중입니다...
              </Text>
              <Progress percent={100} status="active" showInfo={false} />
            </div>
          )}
        </Space>
      </Card>

      {/* 문서 목록 */}
      <Card
        title={
          <Space>
            <Text strong style={{ fontSize: 16 }}>인덱싱된 문서</Text>
            <Tag color="blue">{documentList?.totalCount || 0}개</Tag>
          </Space>
        }
        style={{
          border: '1px solid #e8e8e8',
          borderRadius: 8,
        }}
        bodyStyle={{ padding: 24 }}
      >
        <Table
          columns={columns}
          dataSource={documentList?.documents || []}
          rowKey="id"
          loading={isLoading}
          pagination={{
            pageSize: 10,
            showSizeChanger: true,
            showTotal: (total) => `총 ${total}개`,
          }}
        />
      </Card>
    </div>
  );
};
