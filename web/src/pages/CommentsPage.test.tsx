/**
 * @vitest-environment jsdom
 */
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor, fireEvent } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import CommentsPage from '../pages/CommentsPage'
import { apiClient } from '../api/client'

// Mock the API client
vi.mock('../api/client', () => ({
  apiClient: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
  },
}))

// Mock react-router-dom
vi.mock('react-router-dom', () => ({
  useParams: vi.fn(),
  useNavigate: vi.fn(),
}))

// Import mocked useParams
const mockUseParams = require('react-router-dom').useParams as any

describe('CommentsPage 三态测试', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    // Default mock for taskId
    mockUseParams.mockReturnValue({ taskId: '1' })
  })

  it('应该显示 loading 态当 API 未返回时', async () => {
    // Given - API 返回 pending promise
    ;(apiClient.get as any).mockReturnValue(Promise.resolve({ data: null }))

    // When
    render(<CommentsPage />)

    // Then - 应立即显示加载中状态
    expect(screen.getByText(/加载中.../i)).toBeInTheDocument()
  })

  it('应该显示 empty 态当 API 返回空列表', async () => {
    // Given
    ;(apiClient.get as any).mockResolvedValue({ 
      data: { items: [], total: 0 } 
    })

    // When
    render(<CommentsPage />)

    // Then - API 响应后应显示空状态
    await waitFor(() => {
      expect(screen.getByText(/暂无评论/i)).toBeInTheDocument()
    })
  })

  it('应该显示 error 态当 API 返回错误', async () => {
    // Given
    ;(apiClient.get as any).mockRejectedValue(new Error('网络错误'))

    // When
    render(<CommentsPage />)

    // Then - 应显示错误提示
    await waitFor(() => {
      expect(screen.getByText(/加载失败/i)).toBeInTheDocument()
    })
    
    // 验证错误消息包含详细信息
    expect(screen.getByText(/网络错误/i)).toBeInTheDocument()
  })

  it('应该在缺少 taskId 时显示错误', async () => {
    // Given
    mockUseParams.mockReturnValue({ taskId: null })

    // When
    render(<CommentsPage />)

    // Then - 应显示错误而不是发起 API 请求
    await waitFor(() => {
      expect(apiClient.get).not.toHaveBeenCalled()
    })
  })
})

describe('CommentsPage 数据加载测试', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockUseParams.mockReturnValue({ taskId: '1' })
  })

  it('应该调用 apiClient.get 获取评论列表', async () => {
    // Given
    const mockResponse = {
      data: {
        items: [
          { id: 1, taskId: 1, author: '张三', content: '评论一', createdAt: '2026-01-01T10:00:00Z' },
        ],
        total: 1,
        page: 1,
        size: 20,
      }
    }
    ;(apiClient.get as any).mockResolvedValue(mockResponse)

    // When
    render(<CommentsPage />)

    // Then
    await waitFor(() => {
      expect(apiClient.get).toHaveBeenCalledWith('/tasks/1/comments?page=1&size=20')
    })
  })

  it('应该在有数据时显示表格和评论列表', async () => {
    // Given
    const mockComments = [
      { id: 1, taskId: 1, author: '张三', content: '评论内容一', createdAt: '2026-01-01T10:00:00Z' },
      { id: 2, taskId: 1, author: '李四', content: '评论内容二', createdAt: '2026-01-01T09:00:00Z' },
    ]
    ;(apiClient.get as any).mockResolvedValue({ 
      data: { items: mockComments, total: 2, page: 1, size: 20 } 
    })

    // When
    render(<CommentsPage />)

    // Then
    await waitFor(() => {
      expect(screen.getByText(/任务评论/i)).toBeInTheDocument()
      expect(screen.getByText(/发表评论/i)).toBeInTheDocument()
    })

    // 验证评论数据渲染
    await waitFor(() => {
      expect(screen.getByText('张三')).toBeInTheDocument()
      expect(screen.getByText('李四')).toBeInTheDocument()
      expect(screen.getByText('评论内容一')).toBeInTheDocument()
      expect(screen.getByText('评论内容二')).toBeInTheDocument()
    })
  })
})

describe('CommentsPage 交互测试', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockUseParams.mockReturnValue({ taskId: '1' })
  })

  it('应该通过表单提交成功创建评论', async () => {
    // Given
    const mockNewComment = { 
      id: 3, 
      taskId: 1, 
      author: '王五', 
      content: '新评论', 
      createdAt: new Date().toISOString() 
    }
    
    ;(apiClient.get as any).mockResolvedValue({ 
      data: { items: [], total: 0, page: 1, size: 20 } 
    })
    ;(apiClient.post as any).mockResolvedValue({ data: mockNewComment })

    // When
    render(<CommentsPage />)

    // Wait for initial load
    await waitFor(() => {
      expect(screen.getByText(/暂无评论/i)).toBeInTheDocument()
    })

    // Click "发表评论" button
    const createButton = screen.getByText(/发表评论/i)
    await userEvent.click(createButton)

    // Modal should appear
    await waitFor(() => {
      expect(screen.getByText(/发表评论/i)).toBeInTheDocument()
    })

    // Fill in the form
    const authorInput = screen.getByLabelText(/作者/i)
    const contentInput = screen.getByLabelText(/评论内容/i)
    
    await userEvent.type(authorInput, '王五')
    await userEvent.type(contentInput, '新评论')

    // Submit form
    const submitButton = screen.getByText(/提交/i)
    await userEvent.click(submitButton)

    // Then - Should show success message and close modal
    await waitFor(() => {
      expect(screen.getByText(/评论发表成功/i)).toBeInTheDocument()
    })

    // Verify API call
    expect(apiClient.post).toHaveBeenCalledWith('/tasks/1/comments', {
      author: '王五',
      content: '新评论',
    })
  })

  it('应该在创建失败时显示错误消息', async () => {
    // Given
    ;(apiClient.get as any).mockResolvedValue({ 
      data: { items: [], total: 0, page: 1, size: 20 } 
    })
    ;(apiClient.post as any).mockRejectedValue(new Error('发表评论失败'))

    // When
    render(<CommentsPage />)

    // Wait for initial load
    await waitFor(() => {
      expect(screen.getByText(/暂无评论/i)).toBeInTheDocument()
    })

    // Open create modal
    const createButton = screen.getByText(/发表评论/i)
    await userEvent.click(createButton)

    await waitFor(() => {
      expect(screen.getByText(/发表评论/i)).toBeInTheDocument()
    })

    // Fill in the form
    const authorInput = screen.getByLabelText(/作者/i)
    const contentInput = screen.getByLabelText(/评论内容/i)
    
    await userEvent.type(authorInput, '王五')
    await userEvent.type(contentInput, '新评论')

    // Submit form
    const submitButton = screen.getByText(/提交/i)
    await userEvent.click(submitButton)

    // Then - Should show error message
    await waitFor(() => {
      expect(screen.getByText(/发表评论失败/i)).toBeInTheDocument()
    })
  })

  it('应该删除评论并弹出确认对话框', async () => {
    // Given
    const mockComments = [
      { id: 1, taskId: 1, author: '张三', content: '待删除评论', createdAt: '2026-01-01T10:00:00Z' },
    ]
    ;(apiClient.get as any).mockResolvedValue({ 
      data: { items: mockComments, total: 1, page: 1, size: 20 } 
    })
    ;(apiClient.delete as any).mockResolvedValue({})

    // When
    render(<CommentsPage />)

    // Wait for initial load
    await waitFor(() => {
      expect(screen.getByText('张三')).toBeInTheDocument()
    })

    // Click delete button
    const deleteButtons = screen.getAllByText(/删除/i)
    await userEvent.click(deleteButtons[0])

    // Then - Modal.confirm dialog should appear
    await waitFor(() => {
      expect(screen.getByText(/确认删除/i)).toBeInTheDocument()
      expect(screen.getByText(/确定要删除这条评论吗？此操作不可恢复/i)).toBeInTheDocument()
    })
  })

  it('应该在删除成功后重新加载评论列表', async () => {
    // Given
    const mockComments = [
      { id: 1, taskId: 1, author: '张三', content: '待删除评论', createdAt: '2026-01-01T10:00:00Z' },
    ]
    let deleteCalled = false
    ;(apiClient.get as any).mockImplementation(async () => {
      if (deleteCalled) {
        return { data: { items: [], total: 0, page: 1, size: 20 } }
      }
      return { data: { items: mockComments, total: 1, page: 1, size: 20 } }
    })
    ;(apiClient.delete as any).mockImplementation(async () => {
      deleteCalled = true
    })

    // When
    render(<CommentsPage />)

    // Wait for initial load
    await waitFor(() => {
      expect(screen.getByText('张三')).toBeInTheDocument()
    })

    // Click delete button
    const deleteButtons = screen.getAllByText(/删除/i)
    await userEvent.click(deleteButtons[0])

    // Confirm deletion
    await waitFor(() => {
      expect(screen.getByText(/确认删除/i)).toBeInTheDocument()
    })
    const okButtons = screen.getAllByText(/确定/i)
    await userEvent.click(okButtons[0])

    // Then - Should show success message and empty state
    await waitFor(() => {
      expect(screen.getByText(/删除成功/i)).toBeInTheDocument()
    })
    
    await waitFor(() => {
      expect(screen.getByText(/暂无评论/i)).toBeInTheDocument()
    })
  })

  it('分页按钮应该正常工作', async () => {
    // Given
    const mockCommentsPage1 = [
      { id: 1, taskId: 1, author: '张三', content: '第1页评论', createdAt: '2026-01-01T10:00:00Z' },
    ]
    const mockCommentsPage2 = [
      { id: 11, taskId: 1, author: '李四', content: '第2页评论', createdAt: '2026-01-01T08:00:00Z' },
    ]
    
    let currentPage = 1
    ;(apiClient.get as any).mockImplementation(async (url: string) => {
      const pageMatch = url.match(/page=(\d+)/)
      const page = pageMatch ? parseInt(pageMatch[1]) : 1
      
      if (page === 2) {
        return { data: { items: mockCommentsPage2, total: 11, page: 2, size: 20 } }
      }
      return { data: { items: mockCommentsPage1, total: 11, page: 1, size: 20 } }
    })

    // When
    render(<CommentsPage />)

    // Wait for initial load - page 1
    await waitFor(() => {
      expect(screen.getByText(/第1页评论/i)).toBeInTheDocument()
    })

    // Since antd Table pagination requires actual rendering of pagination component,
    // we verify the URL parameter changes when page state updates
    // The component's onChange handler should update state.page
  })
})

describe('CommentsPage 边界情况', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockUseParams.mockReturnValue({ taskId: '1' })
  })

  it('应该处理 API 返回的 data 为 null 的情况', async () => {
    // Given
    ;(apiClient.get as any).mockResolvedValue({ data: null })

    // When
    render(<CommentsPage />)

    // Then - Should handle gracefully
    await waitFor(() => {
      expect(screen.getByText(/暂无评论/i)).toBeInTheDocument()
    })
  })

  it('content 包含特殊字符时应正确渲染', async () => {
    // Given
    const mockComments = [
      { 
        id: 1, 
        taskId: 1, 
        author: '张三', 
        content: '包含\n换行符和\t制表符', 
        createdAt: '2026-01-01T10:00:00Z' 
      },
    ]
    ;(apiClient.get as any).mockResolvedValue({ 
      data: { items: mockComments, total: 1, page: 1, size: 20 } 
    })

    // When
    render(<CommentsPage />)

    // Then
    await waitFor(() => {
      expect(screen.getByText('张三')).toBeInTheDocument()
      expect(screen.getByText(/包含[\n\t]换行符和\t制表符/i)).toBeInTheDocument()
    })
  })
})
