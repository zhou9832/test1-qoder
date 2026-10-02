import { useEffect, useState } from 'react'
import { Button, Modal, Space, Table, Typography, message } from 'antd'
import { PlusOutlined, EditOutlined, DeleteOutlined, DashboardOutlined } from '@ant-design/icons'
import type { ColumnsType } from 'antd/es/table'
import { useNavigate } from 'react-router-dom'
import { apiClient } from '../api/client'
import { ProjectDto } from '../api/schema'
import { PageState } from '../components/PageState'
import styles from './ProjectsPage.module.css'

const { confirm } = Modal

interface ProjectsPageState {
  loading: boolean
  projects: ProjectDto[]
  error: string | null
}

export default function ProjectsPage() {
  const navigate = useNavigate()
  const [state, setState] = useState<ProjectsPageState>({
    loading: true,
    projects: [],
    error: null,
  })

  useEffect(() => {
    fetchProjects()
  }, [])

  const fetchProjects = async () => {
    setState(prev => ({ ...prev, loading: true, error: null }))
    
    try {
      const response = await apiClient.get<ProjectDto[]>('/projects')
      setState({ loading: false, projects: response.data || [], error: null })
    } catch (err) {
      setState({
        loading: false,
        projects: [],
        error: err instanceof Error ? err.message : '获取项目列表失败',
      })
    }
  }

  const handleEdit = async (record: ProjectDto) => {
    let nameValue = ''
    
    confirm({
      title: '编辑项目',
      content: (
        <input
          defaultValue={record.name}
          placeholder="项目名称"
          style={{ width: '100%', padding: '8px', margin: '8px 0' }}
          onChange={(e) => { nameValue = e.target.value }}
        />
      ),
      okText: '保存',
      onOk: async () => {
        if (nameValue) {
          try {
            await apiClient.put<ProjectDto>(`/projects/${record.id}`, { name: nameValue })
            message.success('保存成功')
            fetchProjects()
          } catch (err) {
            message.error(err instanceof Error ? err.message : '保存失败')
          }
        }
      },
    })
  }

  const handleDelete = async (record: ProjectDto) => {
    confirm({
      title: '确认删除',
      content: `确定要删除项目“${record.name}”吗?此操作不可恢复。`,
      okButtonProps: { danger: true },
      onOk: async () => {
        try {
          await apiClient.delete(`/projects/${record.id}`)
          message.success('删除成功')
          fetchProjects()
        } catch (err) {
          message.error(err instanceof Error ? err.message : '删除失败')
        }
      },
    })
  }

  const columns: ColumnsType<ProjectDto> = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      width: 80,
    },
    {
      title: '项目名称',
      dataIndex: 'name',
      key: 'name',
      render: (text: string) => <strong>{text}</strong>,
    },
    {
      title: '描述',
      dataIndex: 'description',
      key: 'description',
      ellipsis: true,
    },
    {
      title: '创建时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
      width: 180,
      render: (value: string) => {
        // TODO: Use unified time formatting utility
        return value
      },
    },
    {
      title: '更新时间',
      dataIndex: 'updatedAt',
      key: 'updatedAt',
      width: 180,
      render: (value: string) => {
        // TODO: Use unified time formatting utility
        return value
      },
    },
    {
      title: '操作',
      key: 'action',
      width: 200,
      render: (_, record) => (
        <Space>
          <Button
            type="link"
            icon={<DashboardOutlined />}
            onClick={() => navigate(`/projects/${record.id}/board`)}
          >
            看板
          </Button>
          <Button
            type="link"
            icon={<EditOutlined />}
            onClick={() => handleEdit(record)}
          >
            编辑
          </Button>
          <Button
            type="link"
            danger
            icon={<DeleteOutlined />}
            onClick={() => handleDelete(record)}
          >
            删除
          </Button>
        </Space>
      ),
    },
  ]

  if (state.loading) {
    return <PageState loading />
  }

  if (state.error) {
    return <PageState error={state.error} onRetry={fetchProjects} />
  }

  if (state.projects.length === 0) {
    return (
      <PageState empty>
        <Typography.Text type="secondary">暂无项目数据</Typography.Text>
      </PageState>
    )
  }

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <Typography.Title level={2}>项目管理</Typography.Title>
        <Button
          type="primary"
          icon={<PlusOutlined />}
          onClick={async () => {
            let nameValue = ''
            let descValue = ''
            
            confirm({
              title: '新建项目',
              content: (
                <>
                  <input
                    placeholder="项目名称(必填)"
                    style={{ width: '100%', padding: '8px', margin: '8px 0' }}
                    onChange={(e) => { nameValue = e.target.value }}
                  />
                  <input
                    placeholder="项目描述(可选)"
                    style={{ width: '100%', padding: '8px', margin: '8px 0' }}
                    onChange={(e) => { descValue = e.target.value }}
                  />
                </>
              ),
              okText: '创建',
              onOk: async () => {
                if (nameValue) {
                  try {
                    await apiClient.post<ProjectDto>('/projects', { name: nameValue, description: descValue || '' })
                    message.success('创建成功')
                    fetchProjects()
                  } catch (err) {
                    message.error(err instanceof Error ? err.message : '创建失败')
                  }
                }
              },
            })
          }}
        >
          新建项目
        </Button>
      </div>

      <Table
        columns={columns}
        dataSource={state.projects}
        rowKey="id"
        pagination={{ pageSize: 20 }}
        size="middle"
      />
    </div>
  )
}
