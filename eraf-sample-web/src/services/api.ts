import axios from 'axios';

// Temporarily connect directly to sample-biz until Gateway dynamic routing is implemented
const API_BASE_URL = 'http://localhost:8080';
const GATEWAY_ADMIN_URL = 'http://localhost:9000/admin';

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

// Gateway API instance (port 9000)
const gatewayApi = axios.create({
  baseURL: 'http://localhost:9000/admin',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Gateway API request interceptor (add JWT token)
gatewayApi.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('accessToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Gateway API response interceptor - unwrap ApiResponse wrapper
gatewayApi.interceptors.response.use(
  (response) => {
    // Admin controllers wrap responses in { success, data, message }
    // Unwrap automatically so pages can use response.data directly
    if (response.data && typeof response.data === 'object' && 'success' in response.data && 'data' in response.data) {
      response.data = response.data.data;
    }
    return response;
  },
  (error) => {
    console.error('Gateway API Error:', error);
    return Promise.reject(error);
  }
);

// Gateway APIs
export const gateway = {
  // Routes
  getRoutes: (enabledOnly?: boolean) =>
    gatewayApi.get(`/routes${enabledOnly ? '?enabledOnly=true' : ''}`),
  getRoute: (id: number) => gatewayApi.get(`/routes/${id}`),
  getRoutesByService: (serviceId: number) => gatewayApi.get(`/routes/service/${serviceId}`),
  createRoute: (data: any) => gatewayApi.post('/routes', data),
  updateRoute: (id: number, data: any) => gatewayApi.put(`/routes/${id}`, data),
  deleteRoute: (id: number) => gatewayApi.delete(`/routes/${id}`),
  toggleRoute: (id: number) => gatewayApi.patch(`/routes/${id}/toggle`),
  getRouteStats: () => gatewayApi.get('/routes/stats'),

  // Services
  getServices: (enabledOnly?: boolean) =>
    gatewayApi.get(`/services${enabledOnly ? '?enabledOnly=true' : ''}`),
  getService: (id: number) => gatewayApi.get(`/services/${id}`),
  createService: (data: any) => gatewayApi.post('/services', data),
  updateService: (id: number, data: any) => gatewayApi.put(`/services/${id}`, data),
  deleteService: (id: number) => gatewayApi.delete(`/services/${id}`),
  toggleService: (id: number) => gatewayApi.patch(`/services/${id}/toggle`),
  getServiceStats: () => gatewayApi.get('/services/stats'),

  // Targets
  getTargets: (enabledOnly?: boolean) =>
    gatewayApi.get(`/targets${enabledOnly ? '?enabledOnly=true' : ''}`),
  getTarget: (id: number) => gatewayApi.get(`/targets/${id}`),
  getTargetsByService: (serviceId: number) => gatewayApi.get(`/targets/service/${serviceId}`),
  createTarget: (data: any) => gatewayApi.post('/targets', data),
  updateTarget: (id: number, data: any) => gatewayApi.put(`/targets/${id}`, data),
  deleteTarget: (id: number) => gatewayApi.delete(`/targets/${id}`),
  toggleTarget: (id: number) => gatewayApi.patch(`/targets/${id}/toggle`),
  getTargetStats: () => gatewayApi.get('/targets/stats'),

  // Plugins
  getPlugins: (enabledOnly?: boolean) =>
    gatewayApi.get(`/plugins${enabledOnly ? '?enabledOnly=true' : ''}`),
  getPlugin: (id: number) => gatewayApi.get(`/plugins/${id}`),
  getPluginsByRoute: (routeId: number) => gatewayApi.get(`/plugins/route/${routeId}`),
  getPluginsByService: (serviceId: number) => gatewayApi.get(`/plugins/service/${serviceId}`),
  createPlugin: (data: any) => gatewayApi.post('/plugins', data),
  updatePlugin: (id: number, data: any) => gatewayApi.put(`/plugins/${id}`, data),
  deletePlugin: (id: number) => gatewayApi.delete(`/plugins/${id}`),
  togglePlugin: (id: number) => gatewayApi.patch(`/plugins/${id}/toggle`),
  getPluginStats: () => gatewayApi.get('/plugins/stats'),

  // APIs
  getApis: (params?: { enabledOnly?: boolean; serviceId?: number; routeId?: number }) => {
    const queryParams = new URLSearchParams();
    if (params?.enabledOnly) queryParams.append('enabledOnly', 'true');
    if (params?.serviceId) queryParams.append('serviceId', params.serviceId.toString());
    if (params?.routeId) queryParams.append('routeId', params.routeId.toString());
    const queryString = queryParams.toString();
    return gatewayApi.get(`/apis${queryString ? `?${queryString}` : ''}`);
  },
  getApi: (id: number) => gatewayApi.get(`/apis/${id}`),
  getApisByService: (serviceId: number) => gatewayApi.get(`/apis/service/${serviceId}`),
  getApisByRoute: (routeId: number) => gatewayApi.get(`/apis/route/${routeId}`),
  lookupApi: (path: string, method: string) =>
    gatewayApi.get(`/apis/lookup?path=${encodeURIComponent(path)}&method=${method}`),
  getApiStats: () => gatewayApi.get('/apis/stats'),
  createApi: (data: any) => gatewayApi.post('/apis', data),
  updateApi: (id: number, data: any) => gatewayApi.put(`/apis/${id}`, data),
  deleteApi: (id: number) => gatewayApi.delete(`/apis/${id}`),
  toggleApi: (id: number) => gatewayApi.patch(`/apis/${id}/toggle`),

  // Request Logs
  getRequestLogs: (limit: number = 100) => gatewayApi.get(`/request-logs?limit=${limit}`),
  getRequestLogsStats: () => gatewayApi.get('/request-logs/stats'),
  clearRequestLogs: () => gatewayApi.delete('/request-logs/clear'),

  // Consumers
  getConsumers: (enabledOnly?: boolean) =>
    gatewayApi.get(`/consumers${enabledOnly ? '?enabledOnly=true' : ''}`),
  getConsumer: (id: number) => gatewayApi.get(`/consumers/${id}`),
  getConsumerByUsername: (username: string) => gatewayApi.get(`/consumers/username/${username}`),
  createConsumer: (data: any) => gatewayApi.post('/consumers', data),
  updateConsumer: (id: number, data: any) => gatewayApi.put(`/consumers/${id}`, data),
  deleteConsumer: (id: number) => gatewayApi.delete(`/consumers/${id}`),
  toggleConsumer: (id: number) => gatewayApi.patch(`/consumers/${id}/toggle`),
  regenerateApiKey: (id: number) => gatewayApi.post(`/consumers/${id}/regenerate-key`),
  getConsumerStats: () => gatewayApi.get('/consumers/stats'),

  // Health Checks
  getHealthChecks: () => gatewayApi.get('/health-checks'),
  getHealthChecksByService: (serviceId: number) => gatewayApi.get(`/health-checks/service/${serviceId}`),
  performHealthCheck: (targetId: number) => gatewayApi.post(`/health-checks/check/${targetId}`),
  getHealthCheckStats: () => gatewayApi.get('/health-checks/stats'),

  // Dashboard / Analytics
  getDashboardOverview: () => gatewayApi.get('/dashboard/overview'),
  getDashboardTraffic: () => gatewayApi.get('/dashboard/traffic'),
  getDashboardTopApis: (limit: number = 10) => gatewayApi.get(`/dashboard/top-apis?limit=${limit}`),
  getDashboardErrorProneApis: (limit: number = 10) => gatewayApi.get(`/dashboard/error-prone-apis?limit=${limit}`),
  getDashboardApiStats: () => gatewayApi.get('/dashboard/api-stats'),

  // Upstreams
  getUpstreams: (enabledOnly?: boolean) =>
    gatewayApi.get(`/upstreams${enabledOnly ? '?enabledOnly=true' : ''}`),
  getUpstream: (id: number) => gatewayApi.get(`/upstreams/${id}`),
  getUpstreamByName: (name: string) => gatewayApi.get(`/upstreams/name/${encodeURIComponent(name)}`),
  createUpstream: (data: any) => gatewayApi.post('/upstreams', data),
  updateUpstream: (id: number, data: any) => gatewayApi.put(`/upstreams/${id}`, data),
  deleteUpstream: (id: number) => gatewayApi.delete(`/upstreams/${id}`),
  toggleUpstream: (id: number) => gatewayApi.patch(`/upstreams/${id}/toggle`),
  getUpstreamStats: () => gatewayApi.get('/upstreams/stats'),

  // Certificates
  getCertificates: (enabledOnly?: boolean) =>
    gatewayApi.get(`/certificates${enabledOnly ? '?enabledOnly=true' : ''}`),
  getCertificate: (id: number) => gatewayApi.get(`/certificates/${id}`),
  getCertificateByName: (name: string) => gatewayApi.get(`/certificates/name/${encodeURIComponent(name)}`),
  createCertificate: (data: any) => gatewayApi.post('/certificates', data),
  updateCertificate: (id: number, data: any) => gatewayApi.put(`/certificates/${id}`, data),
  deleteCertificate: (id: number) => gatewayApi.delete(`/certificates/${id}`),
  toggleCertificate: (id: number) => gatewayApi.patch(`/certificates/${id}/toggle`),
  getExpiringSoonCertificates: () => gatewayApi.get('/certificates/expiring-soon'),
  getExpiredCertificates: () => gatewayApi.get('/certificates/expired'),
  getCertificateStats: () => gatewayApi.get('/certificates/stats'),

  // Consumer Groups
  getConsumerGroups: (enabledOnly?: boolean) =>
    gatewayApi.get(`/consumer-groups${enabledOnly ? '?enabledOnly=true' : ''}`),
  getConsumerGroup: (id: number) => gatewayApi.get(`/consumer-groups/${id}`),
  getConsumerGroupByName: (name: string) => gatewayApi.get(`/consumer-groups/name/${encodeURIComponent(name)}`),
  createConsumerGroup: (data: any) => gatewayApi.post('/consumer-groups', data),
  updateConsumerGroup: (id: number, data: any) => gatewayApi.put(`/consumer-groups/${id}`, data),
  deleteConsumerGroup: (id: number) => gatewayApi.delete(`/consumer-groups/${id}`),
  toggleConsumerGroup: (id: number) => gatewayApi.patch(`/consumer-groups/${id}/toggle`),
  addConsumerToGroup: (groupId: number, consumerId: number) =>
    gatewayApi.post(`/consumer-groups/${groupId}/consumers/${consumerId}`),
  removeConsumerFromGroup: (groupId: number, consumerId: number) =>
    gatewayApi.delete(`/consumer-groups/${groupId}/consumers/${consumerId}`),
  getConsumerGroupsByConsumer: (consumerId: number) =>
    gatewayApi.get(`/consumer-groups/by-consumer/${consumerId}`),
  getConsumerGroupStats: () => gatewayApi.get('/consumer-groups/stats'),

  // Additional endpoints
  getGlobalPlugins: () => gatewayApi.get('/plugins/global'),
  getPluginByName: (name: string) => gatewayApi.get(`/plugins/name/${encodeURIComponent(name)}`),
  getServiceByName: (name: string) => gatewayApi.get(`/services/name/${encodeURIComponent(name)}`),
};
