import React, { useEffect, useState } from 'react';
import { Grid, Card, CardContent, Typography, Box } from '@mui/material';
import { People as PeopleIcon, Article as ArticleIcon, Forum as ForumIcon, Visibility as ViewsIcon, HowToVote as PollIcon, Report as ReportIcon, WifiTethering as OnlineIcon } from '@mui/icons-material';
import { collection, onSnapshot } from 'firebase/firestore';
import { db } from '../firebase/config';

export default function Overview() {
  const [stats, setStats] = useState({ users: 0, news: 0, posts: 0, polls: 0, reports: 0 });

  useEffect(() => {
    // Realtime listeners for overview stats
    const unsubUsers = onSnapshot(collection(db, 'users'), (snap) => {
      setStats(prev => ({ ...prev, users: snap.size }));
    });
    
    const unsubNews = onSnapshot(collection(db, 'news'), (snap) => {
      setStats(prev => ({ ...prev, news: snap.size }));
    });
    
    const unsubPosts = onSnapshot(collection(db, 'community_posts'), (snap) => {
      setStats(prev => ({ ...prev, posts: snap.size }));
    });
    
    const unsubPolls = onSnapshot(collection(db, 'polls'), (snap) => {
      setStats(prev => ({ ...prev, polls: snap.size }));
    });

    const unsubReports = onSnapshot(collection(db, 'reports'), (snap) => {
      setStats(prev => ({ ...prev, reports: snap.size }));
    });

    return () => {
      unsubUsers();
      unsubNews();
      unsubPosts();
      unsubPolls();
      unsubReports();
    };
  }, []);

  const statCards = [
    { title: 'Total Registered Users', value: stats.users, icon: <PeopleIcon color="primary" sx={{ fontSize: 40 }} /> },
    { title: 'Online Users (Live)', value: 'N/A*', icon: <OnlineIcon color="success" sx={{ fontSize: 40 }} /> },
    { title: 'Total Stream Views', value: 'N/A*', icon: <ViewsIcon color="info" sx={{ fontSize: 40 }} /> },
    { title: 'Published News', value: stats.news, icon: <ArticleIcon color="secondary" sx={{ fontSize: 40 }} /> },
    { title: 'Community Comments', value: stats.posts, icon: <ForumIcon color="action" sx={{ fontSize: 40 }} /> },
    { title: 'Active Polls', value: stats.polls, icon: <PollIcon color="warning" sx={{ fontSize: 40 }} /> },
    { title: 'Pending Reports', value: stats.reports, icon: <ReportIcon color="error" sx={{ fontSize: 40 }} /> },
  ];

  return (
    <Box>
      <Typography variant="h5" gutterBottom fontWeight="bold">Platform Overview</Typography>
      <Grid container spacing={3} sx={{ mt: 1 }}>
        {statCards.map((stat, i) => (
          <Grid item xs={12} sm={6} md={4} key={i}>
            <Card>
              <CardContent sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                <Box>
                  <Typography color="text.secondary" gutterBottom>
                    {stat.title}
                  </Typography>
                  <Typography variant="h4" fontWeight="bold">
                    {stat.value}
                  </Typography>
                </Box>
                {stat.icon}
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>
      <Box sx={{ mt: 3 }}>
        <Typography variant="body2" color="text.secondary">
          * Note: Live Online Users and Total Stream Views will display as N/A until we implement the tracking code inside your Android mobile app. Once the Android app sends this data to Firebase, these numbers will automatically go live.
        </Typography>
      </Box>
    </Box>
  );
}
