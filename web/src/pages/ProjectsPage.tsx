import { useState, useEffect } from 'react'
import { 
  Button, 
  Table, 
  Modal, 
  Form, 
  Input, 
  Space, 
  Typography, 
  message,
  Popconfirm,
  Alert
} from 'antd'
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons'

interface ProjectItem {
  id: number
  name: string
  description: string
  createdAt: string
  updatedAt: string
}

function ProjectsPage() {
  const [projects, setProjects] = useState<ProjectItem[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [empty, setEmpty] = useState(false)
  const [modalVisible, setModalVisible] = useState(false)
  const [editingProject, setEditingProject] = useState<ProjectItem | null>(null)
  const [form] = Form.useForm()

  // Fetch projects
  const fetchProjects = async () => {
    setLoading(true)
    setError(null)
    setEmpty(false)
    try {
      const response = await fetch('/api/projects')
      const data = await response.json()
      if (data.code === 0) {
        const projectList = data.data || []
        setProjects(projectList)
        if (projectList.length === 0) {
          setEmpty(true)
        }
      } else {
        const errorMsg = data.message || '获取项目列表失败'
        setError(errorMsg)
        message.error(errorMsg)
      }
    } catch (error) {
      const errorMsg = '网络请求失败'
      setError(errorMsg)
      message.error(errorMsg)
    } finally {
      setLoading(false)
    }
  }

  // Load projects on mount
  useEffect(() => {
    fetchProjects()
  }, [])

  // Handle create project
  const handleCreate = () => {
    setEditingProject(null)
    form.resetFields()
    setModalVisible(true)
  }

  // Handle edit project
  const handleEdit = (record: ProjectItem) => {
    setEditingProject(record)
    form.setFieldsValue({
      name: record.name,
      description: record.description
    })
    setModalVisible(true)
  }

  // Handle delete project
  const handleDelete = async (id: number) => {
    try {
      const response = await fetch(`/api/projects/${id}`, {
        method: 'DELETE'
      })
      const data = await response.json()
      if (data.code === 0) {
        message.success('删除成功')
        fetchProjects()
      } else {
        message.error(data.message || '删除失败')
      }
    } catch (error) {
      message.error('网络请求失败')
    }
  }

  // Handle modal submit
  const handleSubmit = async () => {
    try {
      const values = await form.validateFields()
      
      let response
      if (editingProject) {
        // Update existing project
        response = await fetch(`/api/projects/${editingProject.id}`, {
          method: 'PUT',
          body: new URLSearchParams({
            name: values.name,
            description: values.description || ''
          })
        })
      } else {
        // Create new project
        response = await fetch('/api/projects', {
          method: 'POST',
          body: new URLSearchParams({
            name: values.name,
            description: values.description || ''
          })
        })
      }

      const data = await response.json()
      if (data.code === 0) {
        message.success(editingProject ? '更新成功' : '创建成功')
        setModalVisible(false)
        fetchProjects()
      } else {
        message.error(data.message || '操作失败')
      }
    } catch (error) {
      console.error('Validation failed', error)
    }
  }

  // Table columns
  const columns = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      width: 80
    },
    {
      title: '项目名称',
      dataIndex: 'name',
      key: 'name',
      ellipsis: true
    },
    {
      title: '描述',
      dataIndex: 'description',
      key: 'description',
      ellipsis: true,
      width: 300
    },
    {
      title: '创建时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
      width: 180
    },
    {
      title: '更新时间',
      dataIndex: 'updatedAt',
      key: 'updatedAt',
      width: 180
    },
    {
      title: '操作',
      key: 'action',
      width: 200,
      render: (_: unknown, record: ProjectItem) => (
        <Space>
          <Button 
            type="link" 
            icon={<EditOutlined />} 
            onClick={() => handleEdit(record)}
          >
            编辑
          </Button>
          <Popconfirm
            title="确定要删除这个项目吗？"
            description="删除后无法恢复"
            onConfirm={() => handleDelete(record.id)}
            okText="确定"
            cancelText="取消"
          >
            <Button type="link" danger icon={<DeleteOutlined />}>
              删除
            </Button>
          </Popconfirm>
        </Space>
      )
    }
  ]

  return (
    <div style={{ padding: '24px' }}>
      <Typography.Title level={2}>项目管理</Typography.Title>
      
      <div style={{ marginBottom: 16 }}>
        <Button 
          type="primary" 
          icon={<PlusOutlined />} 
          onClick={handleCreate}
        >
          新建项目
        </Button>
      </div>

      {error && (
        <Alert
          message="加载失败"
          description={error}
          type="error"
          showIcon
          closable
          style={{ marginBottom: 16 }}
          onClose={() => setError(null)}
        />
      )}

      {!loading && !error && empty && (
        <Alert
          message="暂无项目"
          description="还没有任何项目，点击下方按钮创建第一个项目。"
          type="info"
          showIcon
          style={{ marginBottom: 16 }}
        />
      )}

      <Table
        columns={columns}
        dataSource={projects}
        rowKey="id"
        loading={loading}
        pagination={{ pageSize: 10 }}
      />

      <Modal
        title={editingProject ? '编辑项目' : '新建项目'}
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        onOk={handleSubmit}
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="name"
            label="项目名称"
            rules={[
              { required: true, message: '请输入项目名称' },
              { max: 64, message: '项目名称不能超过64个字符' }
            ]}
          >
            <Input placeholder="请输入项目名称" />
          </Form.Item>

          <Form.Item
            name="description"
            label="项目描述"
            rules={[
              { max: 512, message: '描述不能超过512个字符' }
            ]}
          >
            <Input.TextArea 
              placeholder="请输入项目描述（可选）" 
              rows={4}
            />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}

export default ProjectsPage
