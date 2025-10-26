import { useState } from 'react';
import {
  Card,
  Table,
  Button,
  Space,
  DatePicker,
  Select,
  Tag,
  Modal,
  Typography,
  Divider,
  List,
} from 'antd';
import {
  StarOutlined,
  StarFilled,
  EyeOutlined,
  DeleteOutlined,
} from '@ant-design/icons';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import dayjs from 'dayjs';
import { regulationService } from '@/services/regulationService';
import { REGULATION_TYPES, type QuestionHistory } from '@/types';
import { LoadingSpinner } from '@/components/common/LoadingSpinner';

const { RangePicker } = DatePicker;
const { Title, Paragraph, Text } = Typography;

export const History = () => {
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [dateRange, setDateRange] = useState<[string, string] | null>(null);
  const [selectedType, setSelectedType] = useState<string>('');
  const [selectedHistory, setSelectedHistory] = useState<QuestionHistory | null>(null);
  const [detailModalVisible, setDetailModalVisible] = useState(false);
  const queryClient = useQueryClient();

  // 이력 조회
  const { data, isLoading } = useQuery({
    queryKey: ['history', page, pageSize, dateRange, selectedType],
    queryFn: () =>
      regulationService.getHistory({
        page,
        pageSize,
        startDate: dateRange?.[0],
        endDate: dateRange?.[1],
        regulationType: selectedType || undefined,
      }),
  });

  // 즐겨찾기 토글
  const favoriteMutation = useMutation({
    mutationFn: (historyId: string) => regulationService.toggleFavorite(historyId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['history'] });
    },
  });

  const handleViewDetail = (record: QuestionHistory) => {
    setSelectedHistory(record);
    setDetailModalVisible(true);
  };

  const columns = [
    {
      title: '질문',
      dataIndex: 'question',
      key: 'question',
      width: 300,
      ellipsis: true,
    },
    {
      title: '신뢰도',
      key: 'confidence',
      width: 120,
      render: (_: unknown, record: QuestionHistory) => {
        const confidence = record.result.confidenceScore * 100;
        let color = 'success';
        if (confidence < 60) color = 'error';
        else if (confidence < 80) color = 'warning';

        return <Tag color={color}>{confidence.toFixed(0)}%</Tag>;
      },
    },
    {
      title: '규정 유형',
      key: 'regulationTypes',
      width: 200,
      render: (_: unknown, record: QuestionHistory) => (
        <>
          {record.result.analysis.regulationTypes.slice(0, 2).map((type) => (
            <Tag key={type} color="blue">
              {type}
            </Tag>
          ))}
          {record.result.analysis.regulationTypes.length > 2 && (
            <Tag>+{record.result.analysis.regulationTypes.length - 2}</Tag>
          )}
        </>
      ),
    },
    {
      title: '질문 시각',
      dataIndex: 'createdAt',
      key: 'createdAt',
      width: 180,
      render: (date: string) => new Date(date).toLocaleString(),
    },
    {
      title: '작업',
      key: 'action',
      width: 150,
      render: (_: unknown, record: QuestionHistory) => (
        <Space>
          <Button
            icon={record.isFavorite ? <StarFilled /> : <StarOutlined />}
            size="small"
            type={record.isFavorite ? 'primary' : 'default'}
            onClick={() => favoriteMutation.mutate(record.id)}
          />
          <Button
            icon={<EyeOutlined />}
            size="small"
            onClick={() => handleViewDetail(record)}
          >
            보기
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
      <Title level={2}>검색 이력</Title>
      <Paragraph type="secondary">
        과거에 질문했던 내역을 확인할 수 있습니다.
      </Paragraph>

      <Card style={{ marginTop: 24 }}>
        <Space style={{ marginBottom: 16 }} wrap>
          <RangePicker
            onChange={(dates) => {
              if (dates) {
                setDateRange([
                  dates[0]!.format('YYYY-MM-DD'),
                  dates[1]!.format('YYYY-MM-DD'),
                ]);
              } else {
                setDateRange(null);
              }
            }}
          />
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
        </Space>

        <Table
          dataSource={data?.items || []}
          columns={columns}
          rowKey="id"
          pagination={{
            total: data?.pagination.total,
            pageSize,
            current: page,
            showSizeChanger: true,
            showTotal: (total) => `총 ${total}개`,
            onChange: (newPage, newPageSize) => {
              setPage(newPage);
              setPageSize(newPageSize);
            },
          }}
        />
      </Card>

      {/* 상세 모달 */}
      <Modal
        title="질문 상세"
        open={detailModalVisible}
        onCancel={() => setDetailModalVisible(false)}
        footer={null}
        width={800}
      >
        {selectedHistory && (
          <div>
            <Title level={4}>질문</Title>
            <Paragraph>{selectedHistory.question}</Paragraph>

            <Divider />

            <Title level={4}>분석</Title>
            <Space direction="vertical" size="small" style={{ width: '100%' }}>
              <div>
                <Text strong>키워드: </Text>
                {selectedHistory.result.analysis.keywords.map((keyword) => (
                  <Tag key={keyword} color="blue">
                    {keyword}
                  </Tag>
                ))}
              </div>
              <div>
                <Text strong>질문 의도: </Text>
                <Tag color="purple">
                  {selectedHistory.result.analysis.questionIntent}
                </Tag>
              </div>
              <div>
                <Text strong>신뢰도: </Text>
                <Tag
                  color={
                    selectedHistory.result.confidenceScore >= 0.8
                      ? 'success'
                      : selectedHistory.result.confidenceScore >= 0.6
                      ? 'warning'
                      : 'error'
                  }
                >
                  {(selectedHistory.result.confidenceScore * 100).toFixed(0)}%
                </Tag>
              </div>
            </Space>

            <Divider />

            <Title level={4}>답변</Title>
            <Paragraph style={{ fontSize: 16, lineHeight: 1.8 }}>
              {selectedHistory.result.answer}
            </Paragraph>

            <Divider />

            <Title level={4}>근거 규정</Title>
            <List
              dataSource={selectedHistory.result.references}
              renderItem={(ref, index) => (
                <List.Item>
                  <List.Item.Meta
                    avatar={
                      <div
                        style={{
                          width: 32,
                          height: 32,
                          borderRadius: '50%',
                          background: '#1890ff',
                          color: 'white',
                          display: 'flex',
                          alignItems: 'center',
                          justifyContent: 'center',
                          fontWeight: 'bold',
                        }}
                      >
                        {index + 1}
                      </div>
                    }
                    title={
                      <Space>
                        <Text strong>{ref.documentName}</Text>
                        <Tag color="orange">
                          {(ref.relevanceScore * 100).toFixed(0)}%
                        </Tag>
                      </Space>
                    }
                    description={ref.content}
                  />
                </List.Item>
              )}
            />
          </div>
        )}
      </Modal>
    </div>
  );
};
