/**
 * Unified API response wrapper
 */
export interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}

/**
 * Project DTO from API contract
 */
export interface ProjectDto {
  id: number;
  name: string;
  description?: string | null;
  createdAt: string;
  updatedAt: string;
}

/**
 * Task DTO from API contract
 */
export interface TaskDto {
  id: number;
  projectId: number;
  title: string;
  description?: string | null;
  status: 'TODO' | 'IN_PROGRESS' | 'DONE' | 'CLOSED';
  priority: number;
  dueAt?: string | null;
  tags?: TagDto[];
  createdAt: string;
  updatedAt: string;
}

/**
 * Tag DTO from API contract
 */
export interface TagDto {
  id: number;
  name: string;
  createdAt: string;
  updatedAt: string;
}

/**
 * TimeLog DTO from API contract
 */
export interface TimeLogDto {
  id: number;
  taskId: number;
  hours: string;
  workDate: string;
  note?: string | null;
  createdAt: string;
}

/**
 * Task transition result from API contract
 */
export interface TaskTransitionResult {
  taskId: number;
  previousStatus: 'TODO' | 'IN_PROGRESS' | 'DONE' | 'CLOSED';
  currentStatus: 'TODO' | 'IN_PROGRESS' | 'DONE' | 'CLOSED';
  transitionedAt: string;
}

/**
 * Stats by project (by-assignee endpoint, no assignee field)
 */
export interface StatsByProjectItem {
  projectId: number;
  projectName: string;
  taskCount: number;
  totalHours: number;
}

/**
 * Stats by week
 */
export interface StatsByWeekItem {
  week: string;
  totalHours: number;
  taskCount: number;
}

/**
 * Stats by status
 */
export interface StatsByStatusItem {
  status: 'TODO' | 'IN_PROGRESS' | 'DONE' | 'CLOSED';
  count: number;
  totalHours: number;
}

/**
 * Page result for paginated responses
 */
export interface PageResult<T> {
  items: T[];
  total: number;
  page: number;
  size: number;
}

/**
 * Error item for validation failures
 */
export interface ErrorItem {
  code: number;
  field?: string | null;
  message: string;
}
