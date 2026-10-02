import { Typography } from 'antd'

function HomePage() {
  return (
    <div style={{
      display: 'flex',
      justifyContent: 'center',
      alignItems: 'center',
      minHeight: '60vh',
    }}>
      <Typography.Text type="secondary" style={{ fontSize: 18 }}>
        欢迎使用 TaskBoard 任务看板系统
      </Typography.Text>
    </div>
  )
}

export default HomePage
