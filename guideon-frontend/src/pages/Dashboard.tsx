import { Row, Col, Card, Statistic, List, Typography, Button } from 'antd';
import {
  QuestionCircleOutlined,
  FileTextOutlined,
  StarOutlined,
  RiseOutlined,
} from '@ant-design/icons';
import { useQuery } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { regulationService } from '@/services/regulationService';
import { LoadingSpinner } from '@/components/common/LoadingSpinner';

const { Title, Paragraph } = Typography;

export const Dashboard = () => {
  const navigate = useNavigate();

  // 통계 데이터 조회
  const { data: stats, isLoading: statsLoading } = useQuery({
    queryKey: ['statistics'],
    queryFn: () => regulationService.getStatistics(),
    retry: false, // API 없으면 재시도 안함
    enabled: false, // 임시로 비활성화 (백엔드 API 구현 필요)
  });

  // 최근 이력 조회
  const { data: historyData, isLoading: historyLoading } = useQuery({
    queryKey: ['history', { page: 1, pageSize: 5 }],
    queryFn: () => regulationService.getHistory({ page: 1, pageSize: 5 }),
    retry: false, // API 없으면 재시도 안함
    enabled: false, // 임시로 비활성화 (백엔드 API 구현 필요)
  });

  if (statsLoading || historyLoading) {
    return <LoadingSpinner />;
  }

  return (
    <div>
      <Title level={2}>대시보드</Title>
      <Paragraph type="secondary">
        규정 Q&A 시스템의 전체 현황을 한눈에 확인하세요.
      </Paragraph>

      {/* 통계 카드 */}
      <Row gutter={[16, 16]} style={{ marginTop: 24 }}>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="전체 질문"
              value={stats?.totalQuestions || 0}
              prefix={<QuestionCircleOutlined />}
              valueStyle={{ color: '#3f8600' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="등록된 규정"
              value={stats?.totalRegulations || 0}
              prefix={<FileTextOutlined />}
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="평균 신뢰도"
              value={(stats?.averageConfidence || 0) * 100}
              suffix="%"
              prefix={<StarOutlined />}
              valueStyle={{ color: '#faad14' }}
              precision={1}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="만족도"
              value={(stats?.satisfactionRate || 0) * 100}
              suffix="%"
              prefix={<RiseOutlined />}
              valueStyle={{ color: '#cf1322' }}
              precision={1}
            />
          </Card>
        </Col>
      </Row>

      {/* 빠른 시작 */}
      <Row gutter={[16, 16]} style={{ marginTop: 24 }}>
        <Col xs={24} lg={12}>
          <Card
            title="빠른 시작"
            extra={<Button type="link" onClick={() => navigate('/qa')}>더보기</Button>}
          >
            <Paragraph>규정에 대해 궁금한 점이 있으신가요?</Paragraph>
            <Button
              type="primary"
              size="large"
              icon={<QuestionCircleOutlined />}
              onClick={() => navigate('/qa')}
              block
            >
              질문하기
            </Button>
          </Card>
        </Col>

        <Col xs={24} lg={12}>
          <Card
            title="최근 질문 이력"
            extra={
              <Button type="link" onClick={() => navigate('/history')}>
                전체보기
              </Button>
            }
          >
            <List
              dataSource={historyData?.items || []}
              locale={{ emptyText: '질문 이력이 없습니다.' }}
              renderItem={(item) => (
                <List.Item
                  style={{ cursor: 'pointer' }}
                  onClick={() => navigate(`/history?id=${item.id}`)}
                >
                  <List.Item.Meta
                    title={item.question}
                    description={`신뢰도: ${(
                      item.result.confidenceScore * 100
                    ).toFixed(0)}% | ${new Date(
                      item.createdAt
                    ).toLocaleString()}`}
                  />
                </List.Item>
              )}
            />
          </Card>
        </Col>
      </Row>

      {/* 인기 키워드 */}
      {stats?.popularKeywords && stats.popularKeywords.length > 0 && (
        <Row gutter={[16, 16]} style={{ marginTop: 24 }}>
          <Col xs={24}>
            <Card title="인기 키워드">
              <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
                {stats.popularKeywords.slice(0, 10).map((item) => (
                  <Button key={item.keyword} size="small">
                    {item.keyword} ({item.count})
                  </Button>
                ))}
              </div>
            </Card>
          </Col>
        </Row>
      )}
    </div>
  );
};
