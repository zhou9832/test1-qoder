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
