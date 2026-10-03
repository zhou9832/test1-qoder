import { useEffect, useState } from 'react'
import { Button, Form, Input, Modal, Table, Typography, message } from 'antd'
import { PlusOutlined, DeleteOutlined } from '@ant-design/icons'
import type { ColumnsType } from 'antd/es/table'
import { useParams } from 'react-router-dom'
import { apiClient } from '../api/client'
import { CommentDto, PageResult } from '../api/schema'
import { PageState } from '../components/PageState'
import styles from './CommentsPage.module.css'

const { confirm } = Modal
const { TextArea } = Input

interface CommentsPageState {
  loading: boolean
  comments: CommentDto[]
  total: number
  page: number
  size: number
  error: string | null
}

export default function CommentsPage() {
  const { taskId } = useParams<{ taskId: string }>()
  const [state, setState] = useState<CommentsPageState>({
    loading: true,
    comments: [],
    total: 0,
    page: 1,
    size: 20,
    error: null,
  })
  const [createOpen, setCreateOpen] = useState(false)
  const [form] = Form.useForm()

  useEffect(() => {
    if (taskId) {
      fetchComments()
    } else {
      setState(prev => ({
        ...prev,
        loading: false,
        error: '缺少任务 ID',
      }))
    }
  }, [taskId, state.page])

  const fetchComments = async () => {
    if (!taskId) return
    setState(prev => ({ ...prev, loading: true, error: null }))
    try {
      const params = new URLSearchParams()
      params.set('page', String(state.page))
      params.set('size', String(state.size))
      const response = await apiClient.get<PageResult<CommentDto>>(
        `/tasks/${taskId}/comments?${params.toString()}`
      )
      setState(prev => ({
        ...prev,
        loading: false,
        comments: response.data?.items || [],
        total: response.data?.total || 0,
        error: null,
      }))
    } catch (err) {
      setState(prev => ({
        ...prev,
        loading: false,
        comments: [],
        total: 0,
        error: err instanceof Error ? err.message : '获取评论列表失败',
      }))
    }
  }

  const handleCreate = async (values: { author: string; content: string }) => {
    if (!taskId) return
    try {
      await apiClient.post<CommentDto>('/tasks/' + taskId + '/comments', {
        author: values.author,
        content: values.content,
      })
      message.success('评论发表成功')
      setCreateOpen(false)
      form.resetFields()
      fetchComments()
    } catch (err) {
      message.error(err instanceof Error ? err.message : '发表评论失败')
    }
  }

  const handleDelete = (record: CommentDto) => {
    confirm({
      title: '确认删除',
      content: `确定要删除这条评论吗？此操作不可恢复。`,
      okButtonProps: { danger: true },
      onOk: async () => {
        try {
          await apiClient.delete(`/comments/${record.id}`)
          message.success('删除成功')
          fetchComments()
        } catch (err) {
          message.error(err instanceof Error ? err.message : '删除失败')
        }
      },
    })
  }

  const columns: ColumnsType<CommentDto> = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      width: 80,
    },
    {
      title: '作者',
      dataIndex: 'author',
      key: 'author',
      width: 120,
    },
    {
      title: '评论内容',
      dataIndex: 'content',
      key: 'content',
      ellipsis: true,
      render: (content: string) => <span style={{ whiteSpace: 'pre-wrap' }}>{content}</span>,
    },
    {
      title: '发表时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
      width: 180,
    },
    {
      title: '操作',
      key: 'action',
      width: 100,
      render: (_, record) => (
        <Button
          type="link"
          danger
          icon={<DeleteOutlined />}
          onClick={() => handleDelete(record)}
        >
          删除
        </Button>
      ),
    },
  ]

  if (state.loading && state.comments.length === 0) {
    return <PageState loading />
  }

  if (state.error) {
    return <PageState error={state.error} onRetry={fetchComments} />
  }

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <Typography.Title level={2} style={{ margin: 0 }}>
          任务评论
        </Typography.Title>
        <Button
          type="primary"
          icon={<PlusOutlined />}
          onClick={() => setCreateOpen(true)}
        >
          发表评论
        </Button>
      </div>

      {state.comments.length === 0 ? (
        <PageState empty>
          <Typography.Text type="secondary">暂无评论</Typography.Text>
        </PageState>
      ) : (
        <Table
          columns={columns}
          dataSource={state.comments}
          rowKey="id"
          pagination={{
            current: state.page,
            total: state.total,
            pageSize: state.size,
            onChange: (page) => setState(prev => ({ ...prev, page })),
          }}
          size="middle"
        />
      )}

      <Modal
        title="发表评论"
        open={createOpen}
        onCancel={() => {
          setCreateOpen(false)
          form.resetFields()
        }}
        onOk={() => form.submit()}
        okText="提交"
      >
        <Form
          form={form}
          layout="vertical"
          onFinish={handleCreate}
        >
          <Form.Item
            name="author"
            label="作者"
            rules={[
              { required: true, message: '请输入作者名称' },
              { max: 64, message: '作者名称最多 64 个字符' },
            ]}
          >
            <Input placeholder="请输入作者名称" maxLength={64} />
          </Form.Item>
          <Form.Item
            name="content"
            label="评论内容"
            rules={[
              { required: true, message: '请输入评论内容' },
              { max: 2048, message: '评论内容最多 2048 个字符' },
            ]}
          >
            <TextArea rows={5} placeholder="请输入评论内容" maxLength={2048} showCount />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}
