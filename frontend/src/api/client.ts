import axios, {
  AxiosError,
  AxiosHeaders,
  type AxiosInstance,
  type InternalAxiosRequestConfig,
} from 'axios';
import type { ApiResponse } from './types';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '/api/v1';

const TOKEN_KEY = 'jala.accessToken';

export const tokenStore = {
  get: (): string | null => localStorage.getItem(TOKEN_KEY),
  set: (token: string) => localStorage.setItem(TOKEN_KEY, token),
  clear: () => localStorage.removeItem(TOKEN_KEY),
};

/**
 * A parsed API error the UI can render. `status` is 0 for network failures.
 */
export class ApiError extends Error {
  status: number;
  constructor(message: string, status: number) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
  }
}

/** Called by the auth layer to react to an expired/invalid session. */
let onUnauthorized: (() => void) | null = null;
export function setUnauthorizedHandler(handler: () => void) {
  onUnauthorized = handler;
}

export const http: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  headers: { 'Content-Type': 'application/json' },
});

http.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = tokenStore.get();
  if (token) {
    const headers = AxiosHeaders.from(config.headers);
    headers.set('Authorization', `Bearer ${token}`);
    config.headers = headers;
  }
  return config;
});

http.interceptors.response.use(
  (response) => response,
  (error: AxiosError<ApiResponse<unknown>>) => {
    const status = error.response?.status ?? 0;

    if (status === 401) {
      tokenStore.clear();
      onUnauthorized?.();
    }

    const message =
      error.response?.data?.message ??
      (status === 0
        ? 'Network error — could not reach the server.'
        : error.message);

    return Promise.reject(new ApiError(message, status));
  },
);

/** GET returning the unwrapped ApiResponse.data payload. */
export async function get<T>(
  url: string,
  params?: Record<string, unknown>,
): Promise<T> {
  const res = await http.get<ApiResponse<T>>(url, { params });
  return res.data.data;
}

export async function post<T>(url: string, body?: unknown): Promise<T> {
  const res = await http.post<ApiResponse<T>>(url, body);
  return res.data.data;
}

export async function patch<T>(url: string, body?: unknown): Promise<T> {
  const res = await http.patch<ApiResponse<T>>(url, body);
  return res.data.data;
}

export async function del<T>(url: string): Promise<T> {
  const res = await http.delete<ApiResponse<T>>(url);
  return res.data.data;
}

/** POST multipart/form-data (file uploads); lets axios set the boundary. */
export async function postForm<T>(url: string, form: FormData): Promise<T> {
  const res = await http.post<ApiResponse<T>>(url, form, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
  return res.data.data;
}

/** POST expecting a binary blob back (Excel/PDF exports). */
export async function postBlob(url: string, body?: unknown): Promise<Blob> {
  const res = await http.post(url, body, { responseType: 'blob' });
  return res.data as Blob;
}

export { API_BASE_URL };
