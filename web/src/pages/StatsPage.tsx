import { useEffect, useState } from 'react'
import { Card, Typography, Row, Col, Statistic } from 'antd'
import { Column, Line, Pie } from '@ant-design/charts'
import { apiClient } from '../api/client'
import { StatsByProjectItem, StatsByWeekItem, StatsByStatusItem } from '../api/schema'
import { PageState } from '../components/PageState'
import styles from './StatsPage.module.css'

const STATUS_LABELS: Record<string, string> = {
  TODO: '待办',
  IN_PROGRESS: '进行中',
  DONE: '已完成',
  CLOSED: '已关闭',
}

interface StatsState {
  loading: boolean
  error: string | null
  byProject: StatsByProjectItem[]
  byWeek: StatsByWeekItem[]
  byStatus: StatsByStatusItem[]
}

export default function StatsPage() {
  const [state, setState] = useState<StatsState>({
    loading: true,
    error: null,
    byProject: [],
    byWeek: [],
    byStatus: [],
  })

  useEffect(() => {
    fetchStats()
  }, [])

  const fetchStats = async () => {
    setState(prev => ({ ...prev, loading: true, error: null }))
    try {
      const [projRes, weekRes, statusRes] = await Promise.all([
        apiClient.get<StatsByProjectItem[]>('/stats/by-assignee'),
        apiClient.get<StatsByWeekItem[]>('/stats/by-week'),
        apiClient.get<StatsByStatusItem[]>('/stats/by-status'),
      ])
      setState({
        loading: false,
        error: null,
        byProject: projRes.data || [],
        byWeek: weekRes.data || [],
        byStatus: statusRes.data || [],
      })
    } catch (err) {
      setState(prev => ({
        ...prev,
        loading: false,
        error: err instanceof Error ? err.message : '获取统计数据失败',
      }))
    }
  }

  if (state.loading) {
    return <PageState loading />
  }

  if (state.error) {
    return <PageState error={state.error} onRetry={fetchStats} />
  }

  // Summary totals
  const totalTasks = state.byStatus.reduce((sum, s) => sum + s.count, 0)
  const totalHours = state.byStatus.reduce((sum, s) => sum + s.totalHours, 0)
  const totalProjects = state.byProject.length

  // Chart data preparations
  const projectChartData = state.byProject.flatMap(p => [
    { project: p.projectName, type: '任务数', value: p.taskCount },
    { project: p.projectName, type: '工时(分钟)', value: p.totalHours },
  ])

  const weekChartData = state.byWeek.map(w => ({
    week: w.week,
    totalHours: w.totalHours,
    taskCount: w.taskCount,
  }))

  const statusChartData = state.byStatus
    .filter(s => s.count > 0)
    .map(s => ({
      status: STATUS_LABELS[s.status] || s.status,
      count: s.count,
      totalHours: s.totalHours,
    }))

  return (
    <div className={styles.container}>
      <Typography.Title level={2}>统计报表</Typography.Title>

      {/* Summary Cards */}
      <Row gutter={16} className={styles.summaryRow}>
        <Col span={8}>
          <Card>
            <Statistic title="项目总数" value={totalProjects} />
          </Card>
        </Col>
        <Col span={8}>
          <Card>
            <Statistic title="任务总数" value={totalTasks} />
          </Card>
        </Col>
        <Col span={8}>
          <Card>
            <Statistic title="累计工时（分钟）" value={totalHours} />
          </Card>
        </Col>
      </Row>

      {/* Charts */}
      <Row gutter={[16, 16]} className={styles.chartsRow}>
        <Col span={12}>
          <Card title="按项目统计" className={styles.chartCard}>
            {projectChartData.length > 0 ? (
              <Column
                data={projectChartData}
                xField="project"
                yField="value"
                colorField="type"
                group
                axis={{
                  x: { title: '项目' },
                  y: { title: '数值' },
                }}
                style={{ radiusTopLeft: 4, radiusTopRight: 4 }}
              />
            ) : (
              <div className={styles.emptyChart}>暂无数据</div>
            )}
          </Card>
        </Col>
        <Col span={12}>
          <Card title="按状态统计" className={styles.chartCard}>
            {statusChartData.length > 0 ? (
              <Pie
                data={statusChartData}
                angleField="count"
                colorField="status"
                innerRadius={0.6}
                label={{
                  text: 'status',
                  position: 'outside',
                }}
                legend={{ color: { position: 'bottom' } }}
                style={{ stroke: '#fff', lineWidth: 2 }}
              />
            ) : (
              <div className={styles.emptyChart}>暂无数据</div>
            )}
          </Card>
        </Col>
        <Col span={24}>
          <Card title="按周工时趋势" className={styles.chartCard}>
            {weekChartData.length > 0 ? (
              <Line
                data={weekChartData}
                xField="week"
                yField="totalHours"
                axis={{
                  x: { title: '周次' },
                  y: { title: '工时（分钟）' },
                }}
                style={{ lineWidth: 2 }}
                point={{ shapeField: 'circle', sizeField: 4 }}
              />
            ) : (
              <div className={styles.emptyChart}>暂无数据</div>
            )}
          </Card>
        </Col>
      </Row>
    </div>
  )
}
