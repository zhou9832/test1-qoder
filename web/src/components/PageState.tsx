import { Spin, Alert, Empty } from 'antd'
import { ReactNode } from 'react'

interface PageStateProps {
  loading?: boolean
  error?: string | null
  empty?: boolean
  onRetry?: () => void
  children?: ReactNode
}

/**
 * Unified page state component for loading/empty/error states.
 */
export function PageState({ loading, error, empty, onRetry, children }: PageStateProps) {
  if (loading) {
    return (
      <div style={{ textAlign: 'center', padding: '48px 0' }}>
        <Spin size="large" tip="加载中..." />
      </div>
    )
  }

  if (error) {
    return (
      <Alert
        message="加载失败"
        description={error}
        type="error"
        closable
        action={
          onRetry && (
            <button onClick={onRetry}>重试</button>
          )
        }
        style={{ margin: '24px' }}
      />
    )
  }

  if (empty) {
    return (
      <Empty
        description={children || "暂无数据"}
        style={{ margin: '48px 0' }}
      />
    )
  }

  return <>{children}</>
}
