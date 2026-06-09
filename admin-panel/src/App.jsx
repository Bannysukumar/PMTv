import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import AdminLogin from './pages/AdminLogin';
import DashboardLayout from './components/DashboardLayout';
import Overview from './pages/Overview';
import LiveStreamManager from './pages/LiveStreamManager';
import NewsManager from './pages/NewsManager';
import NewsForm from './pages/NewsForm';
import UserManager from './pages/UserManager';
import SettingsManager from './pages/SettingsManager';
import AdManager from './pages/AdManager';
import NotificationBuilder from './pages/NotificationBuilder';
import CommunityManager from './pages/CommunityManager';
import PollManager from './pages/PollManager';
import ScheduleManager from './pages/ScheduleManager';
import ChannelManager from './pages/ChannelManager';
import AlertsManager from './pages/AlertsManager';
import MediaLibrary from './pages/MediaLibrary';
import Analytics from './pages/Analytics';
import AuditLogs from './pages/AuditLogs';

// Protected Route Component
const ProtectedRoute = ({ children }) => {
  const { currentUser } = useAuth();
  if (!currentUser) return <Navigate to="/login" replace />;
  return children;
};

function App() {
  return (
    <AuthProvider>
      <Router>
        <Routes>
          <Route path="/login" element={<AdminLogin />} />
          
          <Route 
            path="/" 
            element={
              <ProtectedRoute>
                <DashboardLayout />
              </ProtectedRoute>
            }
          >
            <Route index element={<Overview />} />
            <Route path="livestream" element={<LiveStreamManager />} />
            <Route path="news" element={<NewsManager />} />
            <Route path="news/create" element={<NewsForm />} />
            <Route path="users" element={<UserManager />} />
            <Route path="community" element={<CommunityManager />} />
            <Route path="polls" element={<PollManager />} />
            <Route path="schedules" element={<ScheduleManager />} />
            <Route path="settings" element={<SettingsManager />} />
            <Route path="ads" element={<AdManager />} />
            <Route path="notifications" element={<NotificationBuilder />} />
            <Route path="channels" element={<ChannelManager />} />
            <Route path="alerts" element={<AlertsManager />} />
            <Route path="media" element={<MediaLibrary />} />
            <Route path="analytics" element={<Analytics />} />
            <Route path="audit" element={<AuditLogs />} />
          </Route>
        </Routes>
      </Router>
    </AuthProvider>
  );
}

export default App;
