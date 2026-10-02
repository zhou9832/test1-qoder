import { useEffect, useState, useMemo } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { Typography, Tag as AntTag, Space, Drawer, Form, Input, InputNumber, Button, message } from 'antd'
import {
  DndContext,
  DragOverlay,
  useDraggable,
  useDroppable,
  type DragStartEvent,
  type DragEndEvent,
  PointerSensor,
  useSensor,
  useSensors,
} from '@dnd-kit/core'
import { apiClient } from '../api/client'
import { TaskDto, TaskTransitionResult, TagDto } from '../api/schema'
import { PageState } from '../components/PageState'
import styles from './BoardPage.module.css'

type TaskStatus = 'TODO' | 'IN_PROGRESS' | 'DONE' | 'CLOSED'

interface StatusColumn {
  key: TaskStatus
  title: string
  color: string
}

const COLUMNS: StatusColumn[] = [
  { key: 'TODO', title: '待办', color: '#d9d9d9' },
  { key: 'IN_PROGRESS', title: '进行中', color: '#1890ff' },
  { key: 'DONE', title: '已完成', color: '#52c41a' },
  { key: 'CLOSED', title: '已关闭', color: '#8c8c8c' },
]

// State machine: valid transitions from each status
const VALID_TRANSITIONS: Record<TaskStatus, TaskStatus[]> = {
  TODO: ['IN_PROGRESS'],
  IN_PROGRESS: ['TODO', 'DONE'],
  DONE: ['IN_PROGRESS', 'CLOSED'],
  CLOSED: [], // terminal
}

const PRIORITY_COLORS: Record<number, string> = {
  0: 'default',
  1: 'blue',
  2: 'orange',
  3: 'red',
}

const PRIORITY_LABELS: Record<number, string> = {
  0: 'P0 低',
  1: 'P1',
  2: 'P2 高',
  3: 'P3 紧急',
}

function TaskCard({ task, onClick }: { task: TaskDto; onClick: () => void }) {
  const { attributes, listeners, setNodeRef, isDragging } = useDraggable({
    id: `task-${task.id}`,
    data: { task },
  })

  return (
    <div
      ref={setNodeRef}
      {...listeners}
      {...attributes}
      className={`${styles.card} ${isDragging ? styles.cardDragging : ''}`}
      onClick={onClick}
    >
      <div className={styles.cardTitle}>{task.title}</div>
      <Space size={4} wrap>
        <AntTag color={PRIORITY_COLORS[task.priority] || 'default'}>
          {PRIORITY_LABELS[task.priority] || `P${task.priority}`}
        </AntTag>
        {task.tags?.map((tag: TagDto) => (
          <AntTag key={tag.id} color="cyan">{tag.name}</AntTag>
        ))}
      </Space>
      {task.dueAt && (
        <div className={styles.cardDue}>
          截止: {task.dueAt.slice(0, 10)}
        </div>
      )}
    </div>
  )
}

function DroppableColumn({ column, children }: { column: StatusColumn; children: React.ReactNode }) {
  const { isOver, setNodeRef } = useDroppable({
    id: `column-${column.key}`,
    data: { status: column.key },
  })

  const isDisabled = column.key === 'CLOSED'

  return (
    <div
      ref={setNodeRef}
      className={`${styles.column} ${isOver ? styles.columnOver : ''} ${isDisabled ? styles.columnDisabled : ''}`}
    >
      <div className={styles.columnHeader} style={{ borderLeftColor: column.color }}>
        <span>{column.title}</span>
      </div>
      <div className={styles.columnBody}>
        {children}
      </div>
    </div>
  )
}

export default function BoardPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const [tasks, setTasks] = useState<TaskDto[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [activeTask, setActiveTask] = useState<TaskDto | null>(null)
  const [selectedTask, setSelectedTask] = useState<TaskDto | null>(null)
  const [drawerOpen, setDrawerOpen] = useState(false)
  const [form] = Form.useForm()

  const sensors = useSensors(
    useSensor(PointerSensor, { activationConstraint: { distance: 8 } })
  )

  const projectId = Number(id)

  useEffect(() => {
    fetchTasks()
  }, [projectId])

  const fetchTasks = async () => {
    setLoading(true)
    setError(null)
    try {
      const response = await apiClient.get<TaskDto[]>('/tasks')
      const allTasks = response.data || []
      // Filter by project if id is provided
      const filtered = id ? allTasks.filter((t: TaskDto) => t.projectId === projectId) : allTasks
      setTasks(filtered)
    } catch (err) {
      setError(err instanceof Error ? err.message : '获取任务列表失败')
    } finally {
      setLoading(false)
    }
  }

  const tasksByStatus = useMemo(() => {
    const grouped: Record<TaskStatus, TaskDto[]> = {
      TODO: [],
      IN_PROGRESS: [],
      DONE: [],
      CLOSED: [],
    }
    tasks.forEach((task: TaskDto) => {
      if (grouped[task.status]) {
        grouped[task.status].push(task)
      }
    })
    return grouped
  }, [tasks])

  const canDrop = (fromStatus: TaskStatus, toStatus: TaskStatus): boolean => {
    if (fromStatus === toStatus) return false
    return VALID_TRANSITIONS[fromStatus]?.includes(toStatus) ?? false
  }

  const handleDragStart = (event: DragStartEvent) => {
    const task = event.active.data.current?.task as TaskDto
    setActiveTask(task)
  }

  const handleDragEnd = async (event: DragEndEvent) => {
    setActiveTask(null)
    const task = event.active.data.current?.task as TaskDto
    const overData = event.over?.data.current
    if (!task || !overData) return

    const targetStatus = overData.status as TaskStatus
    if (!canDrop(task.status, targetStatus)) {
      if (task.status === targetStatus) return
      message.warning(`不允许从"${COLUMNS.find(c => c.key === task.status)?.title}"拖拽到"${COLUMNS.find(c => c.key === targetStatus)?.title}"`)
      return
    }

    try {
      await apiClient.patch<TaskTransitionResult>(`/tasks/${task.id}/transitions`, { to: targetStatus })
      message.success('状态流转成功')
      fetchTasks()
    } catch (err) {
      message.error(err instanceof Error ? err.message : '状态流转失败')
    }
  }

  const handleCardClick = (task: TaskDto) => {
    setSelectedTask(task)
    setDrawerOpen(true)
    form.setFieldsValue({
      title: task.title,
      description: task.description,
      priority: task.priority,
    })
  }

  const handleEdit = async (values: { title: string; description?: string; priority: number }) => {
    if (!selectedTask) return
    try {
      await apiClient.put<TaskDto>(`/tasks/${selectedTask.id}`, {
        title: values.title,
        description: values.description || '',
        priority: values.priority,
      })
      message.success('保存成功')
      setDrawerOpen(false)
      fetchTasks()
    } catch (err) {
      message.error(err instanceof Error ? err.message : '保存失败')
    }
  }

  if (loading) {
    return <PageState loading />
  }

  if (error) {
    return <PageState error={error} onRetry={fetchTasks} />
  }

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <Typography.Title level={2}>任务看板</Typography.Title>
        <Button onClick={() => navigate('/projects')}>返回项目列表</Button>
      </div>

      <DndContext
        sensors={sensors}
        onDragStart={handleDragStart}
        onDragEnd={handleDragEnd}
      >
        <div className={styles.board}>
          {COLUMNS.map(column => (
            <DroppableColumn key={column.key} column={column}>
              {tasksByStatus[column.key].map(task => (
                <TaskCard
                  key={task.id}
                  task={task}
                  onClick={() => handleCardClick(task)}
                />
              ))}
              {tasksByStatus[column.key].length === 0 && (
                <div className={styles.emptyColumn}>暂无任务</div>
              )}
            </DroppableColumn>
          ))}
        </div>

        <DragOverlay>
          {activeTask ? (
            <div className={styles.cardOverlay}>
              <div className={styles.cardTitle}>{activeTask.title}</div>
              <Space size={4}>
                <AntTag color={PRIORITY_COLORS[activeTask.priority]}>
                  {PRIORITY_LABELS[activeTask.priority]}
                </AntTag>
              </Space>
            </div>
          ) : null}
        </DragOverlay>
      </DndContext>

      <Drawer
        title="任务详情"
        open={drawerOpen}
        onClose={() => setDrawerOpen(false)}
        width={480}
      >
        {selectedTask && (
          <Form form={form} layout="vertical" onFinish={handleEdit}>
            <Form.Item name="title" label="标题" rules={[{ required: true }]}>
              <Input />
            </Form.Item>
            <Form.Item name="description" label="描述">
              <Input.TextArea rows={4} />
            </Form.Item>
            <Form.Item name="priority" label="优先级">
              <InputNumber min={0} max={3} />
            </Form.Item>
            <div className={styles.drawerMeta}>
              <p><strong>状态：</strong>
                <AntTag color={COLUMNS.find(c => c.key === selectedTask.status)?.color}>
                  {COLUMNS.find(c => c.key === selectedTask.status)?.title}
                </AntTag>
              </p>
              <p><strong>到期时间：</strong>{selectedTask.dueAt?.slice(0, 10) || '无'}</p>
              <p><strong>标签：</strong>
                {selectedTask.tags?.map((tag: TagDto) => (
                  <AntTag key={tag.id} color="cyan">{tag.name}</AntTag>
                ))}
              </p>
            </div>
            <Form.Item>
              <Button type="primary" htmlType="submit">保存修改</Button>
            </Form.Item>
          </Form>
        )}
      </Drawer>
    </div>
  )
}
