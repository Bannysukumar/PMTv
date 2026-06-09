import React, { useEffect, useState } from 'react';
import { Box, Card, CardContent, Typography, Grid, CircularProgress, Paper, useTheme } from '@mui/material';
import { People as PeopleIcon, Article as ArticleIcon, LiveTv as LiveTvIcon, HowToVote as PollIcon } from '@mui/icons-material';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Legend } from 'recharts';
import { collection, onSnapshot } from 'firebase/firestore';
import { db } from '../firebase/config';

export default function Analytics() {
  const [loading, setLoading] = useState(true);
  const [stats, setStats] = useState({
    users: 0,
    news: 0,
    channels: 0,
    polls: 0
  });
  
  const theme = useTheme();

  // Mock data for the chart to simulate growth until ExoPlayer tracking is added
  const chartData = [
    { name: 'Jan', WatchTime: 4000, ActiveUsers: 2400 },
    { name: 'Feb', WatchTime: 3000, ActiveUsers: 1398 },
    { name: 'Mar', WatchTime: 2000, ActiveUsers: 9800 },
    { name: 'Apr', WatchTime: 2780, ActiveUsers: 3908 },
    { name: 'May', WatchTime: 1890, ActiveUsers: 4800 },
    { name: 'Jun', WatchTime: 2390, ActiveUsers: 3800 },
    { name: 'Jul', WatchTime: 3490, ActiveUsers: 4300 },
  ];

  useEffect(() => {
    const unsubUsers = onSnapshot(collection(db, 'users'), (snap) => {
      setStats(prev => ({ ...prev, users: snap.size }));
      setLoading(false);
    });
    const unsubNews = onSnapshot(collection(db, 'news'), (snap) => {
      setStats(prev => ({ ...prev, news: snap.size }));
    });
    const unsubChannels = onSnapshot(collection(db, 'channels'), (snap) => {
      setStats(prev => ({ ...prev, channels: snap.size }));
    });
    const unsubPolls = onSnapshot(collection(db, 'polls'), (snap) => {
      setStats(prev => ({ ...prev, polls: snap.size }));
    });

    return () => {
      unsubUsers();
      unsubNews();
      unsubChannels();
      unsubPolls();
    };
  }, []);

  const StatCard = ({ title, count, icon, color }) => (
    <Card 
      elevation={0} 
      sx={{ 
        height: '100%',
        bgcolor: 'background.paper',
        border: '1px solid',
        borderColor: 'divider',
        transition: 'transform 0.2s, box-shadow 0.2s',
        '&:hover': {
          transform: 'translateY(-4px)',
          boxShadow: `0 8px 24px rgba(0,0,0,0.12)`,
          borderColor: `${color}.main`,
        }
      }}
    >
      <CardContent sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', p: 3 }}>
        <Box>
          <Typography variant="caption" color="text.secondary" fontWeight="bold" textTransform="uppercase" letterSpacing={1} gutterBottom>
            {title}
          </Typography>
          <Typography variant="h3" fontWeight="bold" sx={{ mt: 1 }}>
            {loading ? <CircularProgress size={30} /> : count}
          </Typography>
        </Box>
        <Box sx={{ p: 2, borderRadius: '16px', bgcolor: `${color}.dark`, color: `${color}.light`, display: 'flex' }}>
          {icon}
        </Box>
      </CardContent>
    </Card>
  );

  return (
    <Box>
      <Typography variant="h5" fontWeight="bold" gutterBottom>Analytics & Reports Center</Typography>
      
      <Grid container spacing={3} sx={{ mb: 4 }}>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard title="Total Users" count={stats.users} icon={<PeopleIcon fontSize="large" />} color="primary" />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard title="Total News Articles" count={stats.news} icon={<ArticleIcon fontSize="large" />} color="success" />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard title="Active Channels" count={stats.channels} icon={<LiveTvIcon fontSize="large" />} color="error" />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard title="Live Polls" count={stats.polls} icon={<PollIcon fontSize="large" />} color="warning" />
        </Grid>
      </Grid>

      <Grid container spacing={3}>
        <Grid item xs={12} md={8}>
          <Card elevation={0} sx={{ border: '1px solid', borderColor: 'divider' }}>
            <CardContent sx={{ p: 3 }}>
              <Typography variant="h6" fontWeight="bold" gutterBottom>System Growth & Engagement</Typography>
              <Typography variant="body2" color="text.secondary" gutterBottom>
                Simulated Watch Time & Active Users (Awaiting ExoPlayer Integration)
              </Typography>
              <Box sx={{ height: 350, mt: 4 }}>
                <ResponsiveContainer width="100%" height="100%">
                  <LineChart data={chartData} margin={{ top: 5, right: 30, left: 20, bottom: 5 }}>
                    <CartesianGrid strokeDasharray="3 3" stroke={theme.palette.divider} vertical={false} />
                    <XAxis dataKey="name" stroke={theme.palette.text.secondary} axisLine={false} tickLine={false} />
                    <YAxis stroke={theme.palette.text.secondary} axisLine={false} tickLine={false} />
                    <Tooltip 
                      contentStyle={{ backgroundColor: theme.palette.background.paper, borderColor: theme.palette.divider, borderRadius: '8px' }}
                      itemStyle={{ color: theme.palette.text.primary, fontWeight: 'bold' }}
                    />
                    <Legend iconType="circle" wrapperStyle={{ paddingTop: '20px' }} />
                    <Line type="monotone" dataKey="WatchTime" name="Watch Time (hrs)" stroke={theme.palette.primary.main} strokeWidth={3} activeDot={{ r: 8, strokeWidth: 0 }} dot={{ r: 0 }} />
                    <Line type="monotone" dataKey="ActiveUsers" name="Active Users" stroke={theme.palette.secondary.main} strokeWidth={3} dot={{ r: 0 }} activeDot={{ r: 8, strokeWidth: 0 }} />
                  </LineChart>
                </ResponsiveContainer>
              </Box>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} md={4}>
          <Card elevation={0} sx={{ height: '100%', border: '1px solid', borderColor: 'divider', background: `linear-gradient(145deg, ${theme.palette.background.paper} 0%, ${theme.palette.background.default} 100%)` }}>
            <CardContent sx={{ p: 3 }}>
              <Typography variant="h6" fontWeight="bold" gutterBottom>Real-Time Status</Typography>
              <Box sx={{ mt: 3 }}>
                <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                  <Box sx={{ width: 10, height: 10, borderRadius: '50%', bgcolor: 'success.main', mr: 2, boxShadow: '0 0 10px #22C55E' }} />
                  <Typography variant="body1">System Online</Typography>
                </Box>
                <Typography variant="body2" color="text.secondary" paragraph sx={{ mt: 2, lineHeight: 1.8 }}>
                  The platform is fully synchronized. Firestore listeners are active and pushing data directly to the Android clients.
                </Typography>
                <Typography variant="body2" color="text.secondary" paragraph sx={{ lineHeight: 1.8 }}>
                  Currently managing <strong>{stats.channels}</strong> active live streams with <strong>{stats.users}</strong> connected users and <strong>{stats.news}</strong> news documents.
                </Typography>
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
}
