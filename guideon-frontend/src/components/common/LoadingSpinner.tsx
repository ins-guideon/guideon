import { Spin } from 'antd';

interface LoadingSpinnerProps {
  tip?: string;
  size?: 'small' | 'default' | 'large';
}

export const LoadingSpinner = ({ tip = '로딩 중...', size = 'large' }: LoadingSpinnerProps) => {
  return (
    <div
      style={{
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        minHeight: '200px',
      }}
    >
      <Spin size={size} tip={tip} />
    </div>
  );
};
