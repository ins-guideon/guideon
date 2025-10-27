import { Card, Row, Col, Typography } from 'antd';
import { useQuery } from '@tanstack/react-query';
import {
  LineChart,
  Line,
  BarChart,
  Bar,
  PieChart,
  Pie,
  Cell,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from 'recharts';
import { regulationService } from '@/services/regulationService';
import { LoadingSpinner } from '@/components/common/LoadingSpinner';

const { Title, Paragraph } = Typography;

const COLORS = ['#0088FE', '#00C49F', '#FFBB28', '#FF8042', '#8884D8'];

export const Analytics = () => {
  // 통계 데이터 조회
  const { data: stats, isLoading } = useQuery({
    queryKey: ['statistics'],
    queryFn: () => regulationService.getStatistics(),
  });

  if (isLoading) {
    return <LoadingSpinner />;
  }

  return (
    <div>
      <Title level={2}>통계 및 분석</Title>
      <Paragraph type="secondary">
        시스템 사용 현황과 규정 활용도를 분석합니다.
      </Paragraph>

      {/* 일별 질문 추이 */}
      <Card title="일별 질문 추이" style={{ marginTop: 24 }}>
        <ResponsiveContainer width="100%" height={300}>
          <LineChart data={stats?.questionsByDate || []}>
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis
              dataKey="date"
              tickFormatter={(date) => {
                const d = new Date(date);
                return `${d.getMonth() + 1}/${d.getDate()}`;
              }}
            />
            <YAxis />
            <Tooltip
              labelFormatter={(date) => new Date(date).toLocaleDateString()}
            />
            <Legend />
            <Line
              type="monotone"
              dataKey="count"
              name="질문 수"
              stroke="#8884d8"
              strokeWidth={2}
              activeDot={{ r: 8 }}
            />
          </LineChart>
        </ResponsiveContainer>
      </Card>

      <Row gutter={[16, 16]} style={{ marginTop: 24 }}>
        {/* 규정별 활용도 */}
        <Col xs={24} lg={12}>
          <Card title="규정별 활용도 (Top 10)">
            <ResponsiveContainer width="100%" height={400}>
              <BarChart
                data={(stats?.regulationUsage || []).slice(0, 10)}
                layout="horizontal"
              >
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis type="number" />
                <YAxis
                  type="category"
                  dataKey="type"
                  width={150}
                  tick={{ fontSize: 12 }}
                />
                <Tooltip />
                <Legend />
                <Bar dataKey="count" name="사용 횟수" fill="#8884d8" />
              </BarChart>
            </ResponsiveContainer>
          </Card>
        </Col>

        {/* 신뢰도 분포 */}
        <Col xs={24} lg={12}>
          <Card title="신뢰도 분포">
            <ResponsiveContainer width="100%" height={400}>
              <PieChart>
                <Pie
                  data={[
                    {
                      name: '높음 (80% 이상)',
                      value: Math.round((stats?.averageConfidence || 0) * 100),
                    },
                    {
                      name: '보통 (60-80%)',
                      value: 20,
                    },
                    {
                      name: '낮음 (60% 미만)',
                      value: 10,
                    },
                  ]}
                  cx="50%"
                  cy="50%"
                  labelLine={false}
                  label={({ name, value }) => `${name}: ${value}%`}
                  outerRadius={120}
                  fill="#8884d8"
                  dataKey="value"
                >
                  {[0, 1, 2].map((index) => (
                    <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                  ))}
                </Pie>
                <Tooltip />
                <Legend />
              </PieChart>
            </ResponsiveContainer>
          </Card>
        </Col>
      </Row>

      {/* 인기 키워드 Top 20 */}
      {stats?.popularKeywords && stats.popularKeywords.length > 0 && (
        <Card title="인기 키워드 Top 20" style={{ marginTop: 24 }}>
          <ResponsiveContainer width="100%" height={400}>
            <BarChart data={stats.popularKeywords.slice(0, 20)}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis
                dataKey="keyword"
                angle={-45}
                textAnchor="end"
                height={100}
                tick={{ fontSize: 11 }}
              />
              <YAxis />
              <Tooltip />
              <Legend />
              <Bar dataKey="count" name="검색 횟수" fill="#82ca9d" />
            </BarChart>
          </ResponsiveContainer>
        </Card>
      )}
    </div>
  );
};
