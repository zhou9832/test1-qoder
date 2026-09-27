import { Layout, Typography } from 'antd'
const { Header, Content, Footer } = Layout

function HomePage() {
  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Header style={{ 
        display: 'flex', 
        alignItems: 'center', 
        background: '#001529',
        padding: '0 48px'
      }}>
        <Typography.Title level={3} style={{ 
          color: 'white', 
          margin: 0,
          lineHeight: '32px'
        }}>
          TaskBoard
        </Typography.Title>
      </Header>
      
      <Content style={{ 
        display: 'flex', 
        justifyContent: 'center', 
        alignItems: 'center',
        maxWidth: '100%',
        minHeight: '200px'
      }}>
        <Typography.Text type="secondary">
          Welcome to TaskBoard
        </Typography.Text>
      </Content>
      
      <Footer style={{ textAlign: 'center' }}>
        TaskBoard ©{new Date().getFullYear()}
      </Footer>
    </Layout>
  )
}

export default HomePage
