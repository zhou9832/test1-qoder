import { useEffect, useState } from 'react'
import { Button, Modal, Space, Table, Typography, message } from 'antd'
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons'
import type { ColumnsType } from 'antd/es/table'
import { apiClient } from '../api/client'
import { TagDto } from '../api/schema'
import { PageState } from '../components/PageState'
import styles from './TagsPage.module.css'

const { confirm } = Modal

interface TagsPageState {
  loading: boolean
  tags: TagDto[]
  error: string | null
}

export default function TagsPage() {
  const [state, setState] = useState<TagsPageState>({
    loading: true,
    tags: [],
    error: null,
  })

  useEffect(() => {
    fetchTags()
  }, [])

  const fetchTags = async () => {
    setState(prev => ({ ...prev, loading: true, error: null }))
    
    try {
      const response = await apiClient.get<TagDto[]>('/tags')
      setState({ loading: false, tags: response.data || [], error: null })
    } catch (err) {
      setState({
        loading: false,
        tags: [],
        error: err instanceof Error ? err.message : '获取标签列表失败',
      })
    }
  }

  const handleEdit = async (record: TagDto) => {
    let nameValue = ''

    confirm({
      title: '编辑标签',
      content: (
        <input
          defaultValue={record.name}
          placeholder="标签名称"
          style={{ width: '100%', padding: '8px', margin: '8px 0' }}
          onChange={(e) => { nameValue = e.target.value }}
        />
      ),
      okText: '保存',
      onOk: async () => {
        if (!nameValue) {
          message.error('标签名称不能为空')
          return
        }
        try {
          await apiClient.put<TagDto>(`/tags/${record.id}`, { name: nameValue })
          message.success('保存成功')
          fetchTags()
        } catch (err) {
          message.error(err instanceof Error ? err.message : '保存失败')
        }
      },
    })
  }

  const handleDelete = async (record: TagDto) => {
    confirm({
      title: '确认删除',
      content: `确定要删除标签"${record.name}"吗?此操作会解除所有关联。`,
      okButtonProps: { danger: true },
      onOk: async () => {
        try {
          await apiClient.delete(`/tags/${record.id}`)
          message.success('删除成功')
          fetchTags()
        } catch (err) {
          message.error(err instanceof Error ? err.message : '删除失败')
        }
      },
    })
  }

  const handleCreate = async () => {
    let nameValue = ''

    confirm({
      title: '新建标签',
      content: (
        <input
          placeholder="标签名称(必填)"
          style={{ width: '100%', padding: '8px', margin: '8px 0' }}
          onChange={(e) => { nameValue = e.target.value }}
        />
      ),
      okText: '创建',
      onOk: async () => {
        if (!nameValue) {
          message.error('标签名称为必填项')
          return
        }
        try {
          await apiClient.post<TagDto>('/tags', { name: nameValue })
          message.success('创建成功')
          fetchTags()
        } catch (err) {
          message.error(err instanceof Error ? err.message : '创建失败')
        }
      },
    })
  }

  const columns: ColumnsType<TagDto> = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      width: 80,
    },
    {
      title: '标签名称',
      dataIndex: 'name',
      key: 'name',
      render: (text: string) => <strong>{text}</strong>,
    },
    {
      title: '创建时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
      width: 200,
    },
    {
      title: '更新时间',
      dataIndex: 'updatedAt',
      key: 'updatedAt',
      width: 200,
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
    return <PageState error={state.error} onRetry={fetchTags} />
  }

  if (state.tags.length === 0) {
    return (
      <PageState empty>
        <Typography.Text type="secondary">暂无标签数据</Typography.Text>
      </PageState>
    )
  }

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <Typography.Title level={2}>标签管理</Typography.Title>
        <Button
          type="primary"
          icon={<PlusOutlined />}
          onClick={handleCreate}
        >
          新建标签
        </Button>
      </div>

      <Table
        columns={columns}
        dataSource={state.tags}
        rowKey="id"
        pagination={false}
        size="middle"
      />
    </div>
  )
}
