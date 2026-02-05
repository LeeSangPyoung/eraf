import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080';

const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('accessToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor
api.interceptors.response.use(
  (response) => response,
  (error) => {
    console.error('API Error:', error);
    return Promise.reject(error);
  }
);

export default api;

// Document APIs
export const documentApi = {
  // Excel
  downloadExcel: () => api.get('/document/excel/download', { responseType: 'blob' }),
  uploadExcel: (file: File) => {
    const formData = new FormData();
    formData.append('file', file);
    return api.post('/document/excel/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  },
  downloadStreamingExcel: (rows: number) =>
    api.get(`/document/excel/streaming?rows=${rows}`, { responseType: 'blob' }),

  // PDF
  generatePdf: (data: any) => api.post('/document/pdf/generate', data, { responseType: 'blob' }),
  addWatermark: (file: File, text: string) => {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('text', text);
    return api.post('/document/pdf/watermark', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      responseType: 'blob',
    });
  },

  // Barcode
  generateBarcode: (data: any) => api.post('/document/barcode/generate', data, { responseType: 'blob' }),
  generateQrCode: (data: any) => api.post('/document/barcode/qrcode', data, { responseType: 'blob' }),

  // Image (returns JSON with base64 image)
  resizeImage: (file: File, width: number, height: number) => {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('width', width.toString());
    formData.append('height', height.toString());
    return api.post('/document/image/resize', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  },
  createThumbnail: (file: File, size: number) => {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('size', size.toString());
    return api.post('/document/image/thumbnail', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  },
};

// Security APIs
export const securityApi = {
  login: (username: string, password: string) =>
    api.post('/auth/login', { username, password }),
  refresh: (refreshToken: string) =>
    api.post('/auth/refresh', { refreshToken }),
  validateToken: (token: string) =>
    api.post('/auth/validate', { token }),
  getRoleMenu: (role: string) =>
    api.get(`/auth/menu?role=${role}`),
  detectBot: (userAgent?: string) =>
    api.post('/auth/bot/detect', { userAgent }),
  createSession: (userId: string) =>
    api.post('/auth/session/create', { userId }),
  getSessions: (userId: string) =>
    api.get(`/auth/session/list?userId=${userId}`),
  terminateSession: (sessionId: string) =>
    api.delete(`/auth/session/${sessionId}`),
  getAuditLogs: (userId?: string) =>
    api.get(`/auth/audit/logs${userId ? `?userId=${userId}` : ''}`),
};

// Resilience APIs
export const resilienceApi = {
  getCircuitBreakerStatus: () => api.get('/resilience/circuit-breakers'),
  testCircuitBreaker: (name: string, successRate: number) =>
    api.post(`/resilience/circuit-breaker/${name}/test`, { successRate }),
  resetCircuitBreaker: (name: string) =>
    api.post(`/resilience/circuit-breaker/${name}/reset`),
  tripCircuitBreaker: (name: string) =>
    api.post(`/resilience/circuit-breaker/${name}/trip`),
  getRateLimiters: () => api.get('/resilience/rate-limiters'),
  testRateLimiter: (name: string, requestCount: number, permitsPerSecond: number, maxBurstSize: number) =>
    api.post(`/resilience/rate-limiter/${name}/acquire`, { requestCount, permitsPerSecond, maxBurstSize }),
  testRetry: (maxAttempts: number, initialDelay: number, multiplier: number, maxDelay: number, successRate: number) =>
    api.post('/resilience/retry/test', { maxAttempts, initialDelay, multiplier, maxDelay, successRate }),
  testTimeout: (timeout: number, processingTime: number) =>
    api.post('/resilience/timeout/test', { timeout, processingTime }),
  testBulkhead: (name: string, maxConcurrent: number, processingTime: number) =>
    api.post(`/resilience/bulkhead/${name}/execute`, { maxConcurrent, processingTime }),
  testCombined: (circuitBreakerName: string, maxRetries: number, retryDelay: number, successRate: number) =>
    api.post('/resilience/combined/test', { circuitBreakerName, maxRetries, retryDelay, successRate }),
  getMetrics: () => api.get('/resilience/metrics'),
};

// State Machine APIs
export const stateMachineApi = {
  getDefinitions: () => api.get('/statemachine/definitions'),
  getDefinition: (machineId: string) => api.get(`/statemachine/definition/${machineId}`),
  getDiagram: (machineId: string) => api.get(`/statemachine/diagram/${machineId}`),
  getState: (machineId: string, entityId: string) =>
    api.get(`/statemachine/state/${machineId}/${entityId}`),
  initialize: (machineId: string, entityId: string, type?: string) =>
    api.post('/statemachine/initialize', { machineId, entityId, type }),
  sendEvent: (machineId: string, entityId: string, event: string) =>
    api.post('/statemachine/send-event', { machineId, entityId, event }),
  getHistory: (machineId: string, entityId: string) =>
    api.get(`/statemachine/history/${machineId}/${entityId}`),
  getAllHistory: () => api.get('/statemachine/history/all'),
  forceState: (machineId: string, entityId: string, newState: string, reason?: string) =>
    api.post('/statemachine/force-state', { machineId, entityId, newState, reason }),
  getEntities: (machineId: string) => api.get(`/statemachine/entities/${machineId}`),
};

// Saga APIs
export const sagaApi = {
  getDefinitions: () => api.get('/saga/definitions'),
  getExecutions: (limit?: number) =>
    api.get(`/saga/executions${limit ? `?limit=${limit}` : ''}`),
  getExecutionsByStatus: (status: string) =>
    api.get(`/saga/executions/by-status/${status}`),
  getExecution: (executionId: string) => api.get(`/saga/execution/${executionId}`),
  execute: (sagaName: string, input: any, failureRates?: Record<string, number>) =>
    api.post('/saga/execute', { sagaName, input, failureRates }),
  executeWithTimeout: (sagaName: string, input: any, timeout: number) =>
    api.post('/saga/execute-with-timeout', { sagaName, input, timeout }),
  retrySaga: (executionId: string) => api.post(`/saga/retry/${executionId}`),
  setFailureRate: (stepName: string, failureRate: number) =>
    api.post('/saga/failure-rate', { stepName, failureRate }),
  getFailureRates: () => api.get('/saga/failure-rates'),
  resetFailureRates: () => api.delete('/saga/failure-rates'),
  distributedSimulation: (sagaName: string, count: number) =>
    api.post('/saga/distributed-simulation', { sagaName, count }),
};

// Utility APIs
export const utilityApi = {
  encrypt: (plainText: string) => api.post('/utility/crypto/encrypt', { plainText }),
  decrypt: (encryptedText: string) => api.post('/utility/crypto/decrypt', { encryptedText }),
  hash: (text: string) => api.post('/utility/hash', { text }),
  encodePassword: (password: string) => api.post('/utility/password/encode', { password }),
  verifyPassword: (rawPassword: string, encodedPassword: string) =>
    api.post('/utility/password/verify', { rawPassword, encodedPassword }),
  mask: (value: string, pattern: string) => api.post('/utility/mask', { value, pattern }),
  getMaskExamples: () => api.get('/utility/mask/examples'),
  jsonToObject: (json: string) => api.post('/utility/json/to-object', { json }),
  objectToJson: (obj: any) => api.post('/utility/json/to-json', obj),
  formatDate: (date: string, outputFormat: string) =>
    api.post('/utility/date/format', { date, outputFormat }),
  getCurrentDate: () => api.get('/utility/date/now'),
  generateIds: () => api.get('/utility/id/generate'),
  generateUuid: () => api.get('/utility/id/uuid'),
  generateUlid: () => api.get('/utility/id/ulid'),
  generateCustomId: (prefix?: string, suffix?: string, type?: string, length?: number) =>
    api.post('/utility/id/custom', { prefix, suffix, type, length }),
};

// Notification APIs
export const notificationApi = {
  sendEmail: (to: string[], subject: string, body: string, html?: boolean) =>
    api.post('/notification/email/send', { to, subject, body, html }),
  sendSms: (to: string, message: string) =>
    api.post('/notification/sms/send', { to, message }),
  sendPush: (tokens: string[], title: string, body: string, platform?: string) =>
    api.post('/notification/push/send', { tokens, title, body, platform }),
  getHistory: (type?: string, limit?: number) =>
    api.get(`/notification/history?type=${type || 'ALL'}&limit=${limit || 20}`),
  clearHistory: () => api.delete('/notification/history/clear'),
  publishKafka: (topic: string, eventType: string, key: string, payload: any) =>
    api.post('/notification/kafka/publish', { topic, eventType, key, payload }),
  getKafkaMessages: (topic?: string, direction?: string) =>
    api.get(`/notification/kafka/messages?topic=${topic || ''}&direction=${direction || 'ALL'}`),
  subscribeKafka: (topic: string) =>
    api.post('/notification/kafka/subscribe', { topic }),
  getKafkaSubscriptions: () => api.get('/notification/kafka/subscriptions'),
};

// Scheduler APIs
export const schedulerApi = {
  getJobs: (group?: string, status?: string) =>
    api.get(`/scheduler/jobs?group=${group || ''}&status=${status || ''}`),
  getJob: (name: string) => api.get(`/scheduler/jobs/${name}`),
  executeJob: (name: string) => api.post(`/scheduler/jobs/${name}/execute`),
  pauseJob: (name: string) => api.post(`/scheduler/jobs/${name}/pause`),
  resumeJob: (name: string) => api.post(`/scheduler/jobs/${name}/resume`),
  getBatchJobs: () => api.get('/scheduler/batch/jobs'),
  getBatchJob: (name: string) => api.get(`/scheduler/batch/jobs/${name}`),
  executeBatchJob: (name: string) => api.post(`/scheduler/batch/jobs/${name}/execute`),
  getBatchProgress: (name: string) => api.get(`/scheduler/batch/jobs/${name}/progress`),
  stopBatchJob: (name: string) => api.post(`/scheduler/batch/jobs/${name}/stop`),
  getExecutions: (type?: string, limit?: number) =>
    api.get(`/scheduler/executions?type=${type || ''}&limit=${limit || 20}`),
};

// Cache APIs
export const cacheApi = {
  getCaches: () => api.get('/cache/list'),
  getCacheEntries: (cacheName: string) => api.get(`/cache/${cacheName}/entries`),
  putCache: (cacheName: string, key: string, value: string, ttlSeconds?: number) =>
    api.post('/cache/put', { cacheName, key, value, ttlSeconds }),
  evictCache: (cacheName: string, key: string) =>
    api.delete(`/cache/${cacheName}/entries/${encodeURIComponent(key)}`),
  clearCache: (cacheName: string) => api.delete(`/cache/${cacheName}/clear`),
  getRedisKeys: (pattern?: string) =>
    api.get(`/cache/redis/keys?pattern=${pattern || '*'}`),
  getRedisValue: (key: string) => api.get(`/cache/redis/get?key=${encodeURIComponent(key)}`),
  setRedisValue: (key: string, value: string, ttlSeconds?: number) =>
    api.post('/cache/redis/set', { key, value, ttlSeconds }),
  deleteRedisKey: (key: string) =>
    api.delete(`/cache/redis/delete?key=${encodeURIComponent(key)}`),
  subscribePubSub: (channel: string) =>
    api.post('/cache/pubsub/subscribe', { channel }),
  publishPubSub: (channel: string, message: string) =>
    api.post('/cache/pubsub/publish', { channel, message }),
  getPubSubChannels: () => api.get('/cache/pubsub/channels'),
  getPubSubMessages: (channel?: string) =>
    api.get(`/cache/pubsub/messages${channel ? `?channel=${channel}` : ''}`),
  clearPubSub: () => api.delete('/cache/pubsub/clear'),
};

// Monitoring APIs
export const monitoringApi = {
  getHealth: () => api.get('/monitoring/health'),
  getComponentHealth: (component: string) => api.get(`/monitoring/health/${component}`),
  toggleHealth: (component: string) => api.post(`/monitoring/health/${component}/toggle`),
  getMetrics: () => api.get('/monitoring/metrics'),
  getMetric: (name: string) => api.get(`/monitoring/metrics/${name}`),
  incrementRequest: () => api.post('/monitoring/metrics/increment'),
  getTraces: (status?: string, limit?: number) =>
    api.get(`/monitoring/traces?status=${status || ''}&limit=${limit || 20}`),
  getTrace: (traceId: string) => api.get(`/monitoring/traces/${traceId}`),
  generateTrace: (operation: string, durationMs: number, status: string) =>
    api.post('/monitoring/traces/generate', { operation, durationMs, status }),
  clearTraces: () => api.delete('/monitoring/traces/clear'),
  getSwaggerInfo: () => api.get('/monitoring/swagger/info'),
};
