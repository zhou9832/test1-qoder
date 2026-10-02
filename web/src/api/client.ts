/**
 * Generic HTTP client for TaskBoard API
 */

interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}

export class ApiClient {
  private baseURL: string;

  constructor(baseURL: string = '/api') {
    this.baseURL = baseURL;
  }

  private async request<T>(url: string, options: RequestInit = {}): Promise<T> {
    const response = await fetch(`${this.baseURL}${url}`, {
      headers: {
        'Content-Type': 'application/json',
        ...options.headers,
      },
      ...options,
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    return response.json();
  }

  async get<T>(endpoint: string): Promise<ApiResponse<T>> {
    return this.request<ApiResponse<T>>(endpoint, { method: 'GET' });
  }

  async post<T>(endpoint: string, body: Record<string, unknown>): Promise<ApiResponse<T>> {
    const filtered = Object.entries(body)
      .filter(([_, v]) => v !== undefined && v !== '')
      .map(([k, v]) => [k, String(v)]) as Array<[string, string]>;
    
    return this.request<ApiResponse<T>>(endpoint, {
      method: 'POST',
      body: new URLSearchParams(filtered).toString(),
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
      },
    });
  }

  async put<T>(endpoint: string, body: Record<string, unknown>): Promise<ApiResponse<T>> {
    const filtered = Object.entries(body)
      .filter(([_, v]) => v !== undefined && v !== null && v !== '')
      .map(([k, v]) => [k, String(v)]) as Array<[string, string]>;
    
    return this.request<ApiResponse<T>>(endpoint, {
      method: 'PUT',
      body: new URLSearchParams(filtered).toString(),
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
      },
    });
  }

  async delete(endpoint: string): Promise<void> {
    await this.request(endpoint, { method: 'DELETE' });
  }
}

export const apiClient = new ApiClient();
