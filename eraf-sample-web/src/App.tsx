import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { ConfigProvider } from 'antd';
import Layout from './components/Layout';
import Dashboard from './pages/Dashboard';
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
          <Route path="/" element={<Layout />}>
            <Route index element={<Dashboard />} />
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
