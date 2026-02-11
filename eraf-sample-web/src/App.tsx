import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { ConfigProvider } from 'antd';
import Layout from './components/Layout';
import LoginPage from './pages/LoginPage';
import ProtectedRoute from './components/ProtectedRoute';
import Dashboard from './pages/Dashboard';
import DashboardPage from './pages/DashboardPage';
import RoutesPage from './pages/gateway/RoutesPage';
import ApisPage from './pages/gateway/ApisPage';
import ServicesPage from './pages/gateway/ServicesPage';
import TargetsPage from './pages/gateway/TargetsPage';
import PluginsPage from './pages/gateway/PluginsPage';
import RequestLogsPage from './pages/gateway/RequestLogsPage';
import ConsumersPage from './pages/gateway/ConsumersPage';
import HealthChecksPage from './pages/gateway/HealthChecksPage';
import UpstreamsPage from './pages/gateway/UpstreamsPage';
import CertificatesPage from './pages/gateway/CertificatesPage';
import ConsumerGroupsPage from './pages/gateway/ConsumerGroupsPage';
import ExcelPage from './pages/document/ExcelPage';
import PdfPage from './pages/document/PdfPage';
import BarcodePage from './pages/document/BarcodePage';
import ImagePage from './pages/document/ImagePage';
import JwtAuthPage from './pages/security/JwtAuthPage';
import BotDetectionPage from './pages/security/BotDetectionPage';
import SessionPage from './pages/security/SessionPage';
import AuditLogPage from './pages/security/AuditLogPage';
import ResiliencePage from './pages/resilience/ResiliencePage';
import StateMachinePage from './pages/statemachine/StateMachinePage';
import SagaPage from './pages/saga/SagaPage';
import UtilityPage from './pages/utility/UtilityPage';
import NotificationPage from './pages/notification/NotificationPage';
import SchedulerPage from './pages/scheduler/SchedulerPage';
import CachePage from './pages/cache/CachePage';
import HealthPage from './pages/monitoring/HealthPage';
import MetricsPage from './pages/monitoring/MetricsPage';
import TracingPage from './pages/monitoring/TracingPage';
import SwaggerPage from './pages/monitoring/SwaggerPage';

function App() {
  return (
    <ConfigProvider
      theme={{
        token: {
          colorPrimary: '#1890ff',
        },
      }}
    >
      <BrowserRouter>
        <Routes>
          {/* Public Routes */}
          <Route path="/login" element={<LoginPage />} />

          {/* Protected Routes */}
          <Route path="/" element={
            <ProtectedRoute>
              <Layout />
            </ProtectedRoute>
          }>
            <Route index element={<Dashboard />} />
            {/* API Gateway Management */}
            <Route path="gateway/analytics" element={<DashboardPage />} />
            <Route path="gateway/routes" element={<RoutesPage />} />
            <Route path="gateway/apis" element={<ApisPage />} />
            <Route path="gateway/services" element={<ServicesPage />} />
            <Route path="gateway/targets" element={<TargetsPage />} />
            <Route path="gateway/plugins" element={<PluginsPage />} />
            <Route path="gateway/request-logs" element={<RequestLogsPage />} />
            <Route path="gateway/consumers" element={<ConsumersPage />} />
            <Route path="gateway/health-checks" element={<HealthChecksPage />} />
            <Route path="gateway/upstreams" element={<UpstreamsPage />} />
            <Route path="gateway/certificates" element={<CertificatesPage />} />
            <Route path="gateway/consumer-groups" element={<ConsumerGroupsPage />} />
            {/* Document & Media */}
            <Route path="document/excel" element={<ExcelPage />} />
            <Route path="document/pdf" element={<PdfPage />} />
            <Route path="document/barcode" element={<BarcodePage />} />
            <Route path="document/image" element={<ImagePage />} />
            {/* Security */}
            <Route path="security/jwt" element={<JwtAuthPage />} />
            <Route path="security/bot" element={<BotDetectionPage />} />
            <Route path="security/session" element={<SessionPage />} />
            <Route path="security/audit" element={<AuditLogPage />} />
            {/* Resilience */}
            <Route path="resilience" element={<ResiliencePage />} />
            {/* State & Saga */}
            <Route path="statemachine" element={<StateMachinePage />} />
            <Route path="saga" element={<SagaPage />} />
            {/* Utility */}
            <Route path="utility" element={<UtilityPage />} />
            {/* Notification */}
            <Route path="notification" element={<NotificationPage />} />
            {/* Scheduler */}
            <Route path="scheduler" element={<SchedulerPage />} />
            {/* Cache */}
            <Route path="cache" element={<CachePage />} />
            {/* Monitoring */}
            <Route path="monitoring/health" element={<HealthPage />} />
            <Route path="monitoring/metrics" element={<MetricsPage />} />
            <Route path="monitoring/tracing" element={<TracingPage />} />
            <Route path="monitoring/swagger" element={<SwaggerPage />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </ConfigProvider>
  );
}

export default App;
