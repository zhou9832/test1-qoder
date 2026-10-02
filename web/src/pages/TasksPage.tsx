import { useEffect, useState } from 'react'
import { Button, Modal, Space, Table, Typography, message, Tag as AntTag } from 'antd'
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons'
import type { ColumnsType } from 'antd/es/table'
import { apiClient } from '../api/client'
import { TaskDto, TagDto } from '../api/schema'
import { PageState } from '../components/PageState'
import styles from './TasksPage.module.css'

const { confirm } = Modal

interface TasksPageState {
  loading: boolean
  tasks: TaskDto[]
  error: string | null
}

const statusColorMap: Record<string, string> = {
  TODO: 'default',
  IN_PROGRESS: 'processing',
  DONE: 'success',
  CLOSED: 'muted',
}

export default function TasksPage() {
  const [state, setState] = useState<TasksPageState>({
    loading: true,
    tasks: [],
    error: null,
  })

  useEffect(() => {
    fetchTasks()
  }, [])

  const fetchTasks = async () => {
    setState(prev => ({ ...prev, loading: true, error: null }))
    
    try {
      const response = await apiClient.get<TaskDto[]>('/tasks')
      setState({ loading: false, tasks: response.data || [], error: null })
    } catch (err) {
      setState({
        loading: false,
        tasks: [],
        error: err instanceof Error ? err.message : '获取任务列表失败',
      })
    }
  }

  const handleEdit = async (record: TaskDto) => {
    let titleValue = ''
    let descriptionValue = ''

    confirm({
      title: '编辑任务',
      content: (
        <div>
          <input
            defaultValue={record.title}
            placeholder="任务标题(必填)"
            style={{ width: '100%', padding: '8px', margin: '8px 0' }}
            onChange={(e) => { titleValue = e.target.value }}
          />
          <textarea
            defaultValue={record.description || ''}
            placeholder="任务描述(可选)"
            rows={3}
            style={{ width: '100%', padding: '8px', margin: '8px 0' }}
            onChange={(e) => { descriptionValue = e.target.value }}
          />
        </div>
      ),
      okText: '保存',
      onOk: async () => {
        if (!titleValue) {
          message.error('任务标题不能为空')
          return
        }
        try {
          await apiClient.put<TaskDto>(`/tasks/${record.id}`, {
            title: titleValue,
            description: descriptionValue || '',
          })
          message.success('保存成功')
          fetchTasks()
        } catch (err) {
          message.error(err instanceof Error ? err.message : '保存失败')
        }
      },
    })
  }

  const handleDelete = async (record: TaskDto) => {
    confirm({
      title: '确认删除',
      content: `确定要删除任务"${record.title}"吗?此操作不可恢复。`,
      okButtonProps: { danger: true },
      onOk: async () => {
        try {
          await apiClient.delete(`/tasks/${record.id}`)
          message.success('删除成功')
          fetchTasks()
        } catch (err) {
          message.error(err instanceof Error ? err.message : '删除失败')
        }
      },
    })
  }

  const handleCreate = async () => {
    let titleValue = ''
    let descriptionValue = ''
    let projectIdValue = ''

    confirm({
      title: '新建任务',
      content: (
        <div>
          <input
            placeholder="所属项目ID(必填)"
            style={{ width: '100%', padding: '8px', margin: '8px 0' }}
            onChange={(e) => { projectIdValue = e.target.value }}
          />
          <input
            placeholder="任务标题(必填)"
            style={{ width: '100%', padding: '8px', margin: '8px 0' }}
            onChange={(e) => { titleValue = e.target.value }}
          />
          <textarea
            placeholder="任务描述(可选)"
            rows={3}
            style={{ width: '100%', padding: '8px', margin: '8px 0' }}
            onChange={(e) => { descriptionValue = e.target.value }}
          />
        </div>
      ),
      okText: '创建',
      onOk: async () => {
        if (!projectIdValue || !titleValue) {
          message.error('项目ID和标题为必填项')
          return
        }
        try {
          await apiClient.post<TaskDto>('/tasks', {
            projectId: Number(projectIdValue),
            title: titleValue,
            description: descriptionValue || '',
            status: 'TODO',
            priority: 0,
          })
          message.success('创建成功')
          fetchTasks()
        } catch (err) {
          message.error(err instanceof Error ? err.message : '创建失败')
        }
      },
    })
  }

  const columns: ColumnsType<TaskDto> = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      width: 80,
    },
    {
      title: '标题',
      dataIndex: 'title',
      key: 'title',
      render: (text: string) => <strong>{text}</strong>,
    },
    {
      title: '描述',
      dataIndex: 'description',
      key: 'description',
      ellipsis: true,
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 120,
      render: (status: string) => (
        <AntTag color={statusColorMap[status] || 'default'}>
          {status}
        </AntTag>
      ),
    },
    {
      title: '优先级',
      dataIndex: 'priority',
      key: 'priority',
      width: 100,
      render: (priority: number) => (
        <span style={{ color: priority > 1 ? '#ff4d4f' : '#69c0ff' }}>
          P{priority}
        </span>
      ),
    },
    {
      title: '标签',
      dataIndex: 'tags',
      key: 'tags',
      width: 200,
      render: (tags: TagDto[] | undefined) => (
        <Space wrap>
          {tags?.map(tag => (
            <AntTag key={tag.id} color="blue">
              {tag.name}
            </AntTag>
          ))}
        </Space>
      ),
    },
    {
      title: '到期时间',
      dataIndex: 'dueAt',
      key: 'dueAt',
      width: 180,
      render: (value: string | null) => value || '-',
    },
    {
      title: '创建时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
      width: 180,
    },
    {
      title: '操作',
      key: 'action',
      width: 150,
      render: (_, record) => (
        <Space>
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
    return <PageState error={state.error} onRetry={fetchTasks} />
  }

  if (state.tasks.length === 0) {
    return (
      <PageState empty>
        <Typography.Text type="secondary">暂无任务数据</Typography.Text>
      </PageState>
    )
  }

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <Typography.Title level={2}>任务管理</Typography.Title>
        <Button
          type="primary"
          icon={<PlusOutlined />}
          onClick={handleCreate}
        >
          新建任务
        </Button>
      </div>

      <Table
        columns={columns}
        dataSource={state.tasks}
        rowKey="id"
        pagination={{ pageSize: 20 }}
        size="middle"
      />
    </div>
  )
}
