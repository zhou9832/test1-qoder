import { useState } from 'react'
import { BrowserRouter, Routes, Route, useNavigate, useLocation } from 'react-router-dom'
import { ConfigProvider, Layout, Menu } from 'antd'
import {
  HomeOutlined,
  ProjectOutlined,
  UnorderedListOutlined,
  ClockCircleOutlined,
  BarChartOutlined,
} from '@ant-design/icons'
import zhCN from 'antd/locale/zh_CN'
import HomePage from './pages/HomePage'
import ProjectsPage from './pages/ProjectsPage'
import TasksPage from './pages/TasksPage'
import TagsPage from './pages/TagsPage'
import BoardPage from './pages/BoardPage'
import TimeLogsPage from './pages/TimeLogsPage'
import StatsPage from './pages/StatsPage'

const { Sider, Content } = Layout

const menuItems = [
  { key: '/', icon: <HomeOutlined />, label: '首页' },
  { key: '/projects', icon: <ProjectOutlined />, label: '项目管理' },
  { key: '/tasks', icon: <UnorderedListOutlined />, label: '任务列表' },
  { key: '/tags', icon: <UnorderedListOutlined />, label: '标签管理' },
  { key: '/timelogs', icon: <ClockCircleOutlined />, label: '工时填报' },
  { key: '/stats', icon: <BarChartOutlined />, label: '统计报表' },
]

function AppLayout() {
  const navigate = useNavigate()
  const location = useLocation()
  const [collapsed, setCollapsed] = useState(false)

  const selectedKey = menuItems
    .filter(item => item.key !== '/' && location.pathname.startsWith(item.key))
    .map(item => item.key)[0] || '/'

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider collapsible collapsed={collapsed} onCollapse={setCollapsed}>
        <div style={{
          height: 48,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          color: '#fff',
          fontWeight: 'bold',
          fontSize: collapsed ? 14 : 18,
          borderBottom: '1px solid rgba(255,255,255,0.1)',
        }}>
          {collapsed ? 'TB' : 'TaskBoard'}
        </div>
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={[selectedKey]}
          items={menuItems}
          onClick={({ key }) => navigate(key)}
        />
      </Sider>
      <Layout>
        <Content style={{ margin: '16px', minHeight: 280 }}>
          <Routes>
            <Route path="/" element={<HomePage />} />
            <Route path="/projects" element={<ProjectsPage />} />
            <Route path="/projects/:id/board" element={<BoardPage />} />
            <Route path="/tasks" element={<TasksPage />} />
            <Route path="/tags" element={<TagsPage />} />
            <Route path="/timelogs" element={<TimeLogsPage />} />
            <Route path="/stats" element={<StatsPage />} />
          </Routes>
        </Content>
      </Layout>
    </Layout>
  )
}

function App() {
  return (
    <ConfigProvider locale={zhCN}>
      <BrowserRouter>
        <AppLayout />
      </BrowserRouter>
    </ConfigProvider>
  )
}

export default App
