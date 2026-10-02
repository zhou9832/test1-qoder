import { useEffect, useState } from 'react'
import { Button, DatePicker, Form, Input, InputNumber, Modal, Select, Space, Table, Typography, message, Tag as AntTag } from 'antd'
import { PlusOutlined, DeleteOutlined } from '@ant-design/icons'
import type { ColumnsType } from 'antd/es/table'
import { apiClient } from '../api/client'
import { TimeLogDto, TaskDto, PageResult } from '../api/schema'
import { PageState } from '../components/PageState'
import styles from './TimeLogsPage.module.css'

const { confirm } = Modal
const { TextArea } = Input

interface TimeLogsPageState {
  loading: boolean
  timeLogs: TimeLogDto[]
  total: number
  page: number
  error: string | null
}

export default function TimeLogsPage() {
  const [state, setState] = useState<TimeLogsPageState>({
    loading: true,
    timeLogs: [],
    total: 0,
    page: 1,
    error: null,
  })
  const [tasks, setTasks] = useState<TaskDto[]>([])
  const [filterTaskId, setFilterTaskId] = useState<number | undefined>(undefined)
  const [createOpen, setCreateOpen] = useState(false)
  const [form] = Form.useForm()

  useEffect(() => {
    fetchTimeLogs()
    fetchTasks()
  }, [filterTaskId, state.page])

  const fetchTimeLogs = async () => {
    setState(prev => ({ ...prev, loading: true, error: null }))
    try {
      const params = new URLSearchParams()
      params.set('page', String(state.page))
      params.set('size', '20')
      if (filterTaskId) {
        params.set('taskId', String(filterTaskId))
      }
      const response = await apiClient.get<PageResult<TimeLogDto>>(`/timelogs?${params.toString()}`)
      setState(prev => ({
        ...prev,
        loading: false,
        timeLogs: response.data?.items || [],
        total: response.data?.total || 0,
        error: null,
      }))
    } catch (err) {
      setState(prev => ({
        ...prev,
        loading: false,
        timeLogs: [],
        error: err instanceof Error ? err.message : '获取工时列表失败',
      }))
    }
  }

  const fetchTasks = async () => {
    try {
      const response = await apiClient.get<TaskDto[]>('/tasks')
      setTasks(response.data || [])
    } catch {
      // Silently fail - tasks list is optional
    }
  }

  const handleCreate = async (values: { taskId: number; hours: number; workDate: string; note?: string }) => {
    try {
      await apiClient.post<TimeLogDto>('/timelogs', {
        taskId: values.taskId,
        hours: String(values.hours),
        workDate: values.workDate,
        note: values.note || '',
      })
      message.success('工时填报成功')
      setCreateOpen(false)
      form.resetFields()
      fetchTimeLogs()
    } catch (err) {
      message.error(err instanceof Error ? err.message : '填报失败')
    }
  }

  const handleDelete = (record: TimeLogDto) => {
    confirm({
      title: '确认删除',
      content: `确定要删除这条工时记录吗？此操作不可恢复。`,
      okButtonProps: { danger: true },
      onOk: async () => {
        try {
          await apiClient.delete(`/timelogs/${record.id}`)
          message.success('删除成功')
          fetchTimeLogs()
        } catch (err) {
          message.error(err instanceof Error ? err.message : '删除失败')
        }
      },
    })
  }

  const getTaskTitle = (taskId: number) => {
    const task = tasks.find(t => t.id === taskId)
    return task ? task.title : `任务 #${taskId}`
  }

  const columns: ColumnsType<TimeLogDto> = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      width: 80,
    },
    {
      title: '任务',
      dataIndex: 'taskId',
      key: 'taskId',
      render: (taskId: number) => <AntTag color="blue">{getTaskTitle(taskId)}</AntTag>,
    },
    {
      title: '工时（分钟）',
      dataIndex: 'hours',
      key: 'hours',
      width: 120,
      render: (hours: string) => <strong>{hours} 分钟</strong>,
    },
    {
      title: '工作日期',
      dataIndex: 'workDate',
      key: 'workDate',
      width: 140,
    },
    {
      title: '备注',
      dataIndex: 'note',
      key: 'note',
      ellipsis: true,
      render: (note: string | null) => note || '-',
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

  if (state.loading && state.timeLogs.length === 0) {
    return <PageState loading />
  }

  if (state.error) {
    return <PageState error={state.error} onRetry={fetchTimeLogs} />
  }

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <Typography.Title level={2}>工时填报</Typography.Title>
        <Space>
          <Select
            placeholder="按任务筛选"
            allowClear
            style={{ width: 200 }}
            value={filterTaskId}
            onChange={(val) => {
              setFilterTaskId(val)
              setState(prev => ({ ...prev, page: 1 }))
            }}
            options={tasks.map(t => ({ label: t.title, value: t.id }))}
          />
          <Button
            type="primary"
            icon={<PlusOutlined />}
            onClick={() => setCreateOpen(true)}
          >
            填报工时
          </Button>
        </Space>
      </div>

      {state.timeLogs.length === 0 ? (
        <PageState empty>
          <Typography.Text type="secondary">暂无工时记录</Typography.Text>
        </PageState>
      ) : (
        <Table
          columns={columns}
          dataSource={state.timeLogs}
          rowKey="id"
          pagination={{
            current: state.page,
            total: state.total,
            pageSize: 20,
            onChange: (page) => setState(prev => ({ ...prev, page })),
          }}
          size="middle"
        />
      )}

      <Modal
        title="填报工时"
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
            name="taskId"
            label="选择任务"
            rules={[{ required: true, message: '请选择任务' }]}
          >
            <Select
              placeholder="选择任务"
              showSearch
              optionFilterProp="label"
              options={tasks.map(t => ({ label: t.title, value: t.id }))}
            />
          </Form.Item>
          <Form.Item
            name="hours"
            label="工时（分钟）"
            rules={[{ required: true, message: '请输入工时' }]}
          >
            <InputNumber min={1} max={600} style={{ width: '100%' }} placeholder="输入整数分钟数" />
          </Form.Item>
          <Form.Item
            name="workDate"
            label="工作日期"
            rules={[{ required: true, message: '请选择工作日期' }]}
            getValueFromEvent={(_: unknown, dateString: string) => dateString}
            getValueProps={(value: string) => ({ value: value || undefined })}
          >
            <DatePicker style={{ width: '100%' }} format="YYYY-MM-DD" />
          </Form.Item>
          <Form.Item name="note" label="备注">
            <TextArea rows={3} maxLength={256} placeholder="可选备注信息" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}
