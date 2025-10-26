import { useState } from 'react';
import {
  Card,
  Table,
  Button,
  Upload,
  Select,
  Modal,
  message,
  Tag,
  Space,
  Typography,
} from 'antd';
import {
  UploadOutlined,
  DeleteOutlined,
  EyeOutlined,
  CheckCircleOutlined,
  ClockCircleOutlined,
  CloseCircleOutlined,
} from '@ant-design/icons';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import type { UploadFile } from 'antd/es/upload/interface';
import { regulationService } from '@/services/regulationService';
import { REGULATION_TYPES, type RegulationDocument } from '@/types';
import { LoadingSpinner } from '@/components/common/LoadingSpinner';

const { Title, Paragraph } = Typography;

export const Regulations = () => {
  const [selectedType, setSelectedType] = useState<string>('');
  const [uploadModalVisible, setUploadModalVisible] = useState(false);
  const [fileList, setFileList] = useState<UploadFile[]>([]);
  const [uploadType, setUploadType] = useState<string>('');
  const queryClient = useQueryClient();

  // 규정 목록 조회
  const { data, isLoading } = useQuery({
    queryKey: ['regulations', selectedType],
    queryFn: () =>
      regulationService.getRegulations({
        type: selectedType || undefined,
        page: 1,
        pageSize: 50,
      }),
  });

  // 규정 업로드
  const uploadMutation = useMutation({
    mutationFn: ({ file, type }: { file: File; type: string }) =>
      regulationService.uploadRegulation(file, type),
    onSuccess: () => {
      message.success('규정이 업로드되었습니다.');
      queryClient.invalidateQueries({ queryKey: ['regulations'] });
      setUploadModalVisible(false);
      setFileList([]);
      setUploadType('');
    },
    onError: (error) => {
      message.error(
        error instanceof Error ? error.message : '업로드에 실패했습니다.'
      );
    },
  });

  // 규정 삭제
  const deleteMutation = useMutation({
    mutationFn: (id: string) => regulationService.deleteRegulation(id),
    onSuccess: () => {
      message.success('규정이 삭제되었습니다.');
      queryClient.invalidateQueries({ queryKey: ['regulations'] });
    },
    onError: (error) => {
      message.error(
        error instanceof Error ? error.message : '삭제에 실패했습니다.'
      );
    },
  });

  const handleUpload = () => {
    if (fileList.length === 0) {
      message.warning('파일을 선택해주세요.');
      return;
    }
    if (!uploadType) {
      message.warning('규정 유형을 선택해주세요.');
      return;
    }

    const file = fileList[0].originFileObj as File;
    uploadMutation.mutate({ file, type: uploadType });
  };

  const handleDelete = (id: string, name: string) => {
    Modal.confirm({
      title: '규정 삭제',
      content: `"${name}" 규정을 삭제하시겠습니까?`,
      okText: '삭제',
      okType: 'danger',
      cancelText: '취소',
      onOk: () => deleteMutation.mutate(id),
    });
  };

  const getStatusTag = (status: RegulationDocument['status']) => {
    switch (status) {
      case 'indexed':
        return <Tag icon={<CheckCircleOutlined />} color="success">인덱싱 완료</Tag>;
      case 'pending':
        return <Tag icon={<ClockCircleOutlined />} color="processing">대기중</Tag>;
      case 'error':
        return <Tag icon={<CloseCircleOutlined />} color="error">오류</Tag>;
      default:
        return <Tag>{status}</Tag>;
    }
  };

  const columns = [
    {
      title: '규정명',
      dataIndex: 'name',
      key: 'name',
      width: 250,
    },
    {
      title: '유형',
      dataIndex: 'type',
      key: 'type',
      width: 150,
    },
    {
      title: '상태',
      dataIndex: 'status',
      key: 'status',
      width: 120,
      render: (status: RegulationDocument['status']) => getStatusTag(status),
    },
    {
      title: '파일 크기',
      dataIndex: 'fileSize',
      key: 'fileSize',
      width: 120,
      render: (size: number) => `${(size / 1024).toFixed(1)} KB`,
    },
    {
      title: '업로드 일시',
      dataIndex: 'uploadedAt',
      key: 'uploadedAt',
      width: 180,
      render: (date: string) => new Date(date).toLocaleString(),
    },
    {
      title: '작업',
      key: 'action',
      width: 150,
      render: (_: unknown, record: RegulationDocument) => (
        <Space>
          <Button
            icon={<EyeOutlined />}
            size="small"
            onClick={() => message.info('미리보기 기능은 준비중입니다.')}
          >
            보기
          </Button>
          <Button
            icon={<DeleteOutlined />}
            size="small"
            danger
            onClick={() => handleDelete(record.id, record.name)}
          >
            삭제
          </Button>
        </Space>
      ),
    },
  ];

  if (isLoading) {
    return <LoadingSpinner />;
  }

  return (
    <div>
      <Title level={2}>규정 관리</Title>
      <Paragraph type="secondary">
        규정 문서를 업로드하고 관리합니다.
      </Paragraph>

      <Card style={{ marginTop: 24 }}>
        <Space style={{ marginBottom: 16, width: '100%', justifyContent: 'space-between' }}>
          <Select
            placeholder="규정 유형 필터"
            style={{ width: 200 }}
            allowClear
            value={selectedType || undefined}
            onChange={setSelectedType}
            options={[
              { label: '전체', value: '' },
              ...REGULATION_TYPES.map((type) => ({ label: type, value: type })),
            ]}
          />
          <Button
            type="primary"
            icon={<UploadOutlined />}
            onClick={() => setUploadModalVisible(true)}
          >
            규정 업로드
          </Button>
        </Space>

        <Table
          dataSource={data?.items || []}
          columns={columns}
          rowKey="id"
          pagination={{
            total: data?.pagination.total,
            pageSize: data?.pagination.pageSize,
            current: data?.pagination.page,
            showSizeChanger: true,
            showTotal: (total) => `총 ${total}개`,
          }}
        />
      </Card>

      {/* 업로드 모달 */}
      <Modal
        title="규정 업로드"
        open={uploadModalVisible}
        onOk={handleUpload}
        onCancel={() => {
          setUploadModalVisible(false);
          setFileList([]);
          setUploadType('');
        }}
        confirmLoading={uploadMutation.isPending}
      >
        <Space direction="vertical" style={{ width: '100%' }} size="large">
          <div>
            <Paragraph>규정 유형을 선택하세요:</Paragraph>
            <Select
              placeholder="규정 유형 선택"
              style={{ width: '100%' }}
              value={uploadType || undefined}
              onChange={setUploadType}
              options={REGULATION_TYPES.map((type) => ({
                label: type,
                value: type,
              }))}
            />
          </div>

          <div>
            <Paragraph>파일을 선택하세요 (TXT 형식):</Paragraph>
            <Upload
              fileList={fileList}
              onChange={({ fileList }) => setFileList(fileList)}
              beforeUpload={() => false}
              maxCount={1}
              accept=".txt"
            >
              <Button icon={<UploadOutlined />}>파일 선택</Button>
            </Upload>
          </div>
        </Space>
      </Modal>
    </div>
  );
};
