import React, { useEffect, useState } from 'react';
import { Card, CardContent, Typography, Box, TextField, Button, Switch, FormControlLabel, CircularProgress, Grid, Paper } from '@mui/material';
import { doc, getDoc, setDoc } from 'firebase/firestore';
import { ref, onValue } from 'firebase/database';
import { db, rtdb } from '../firebase/config';
import PeopleIcon from '@mui/icons-material/People';
import ThumbUpIcon from '@mui/icons-material/ThumbUp';
import CommentIcon from '@mui/icons-material/Comment';

export default function LiveStreamManager() {
  const [streamData, setStreamData] = useState({ 
    rtmpUrl: '', 
    backupUrl: '', 
    quality: 'Auto',
    channelName: '',
    description: '',
    thumbnailUrl: '',
    isActive: false 
  });
  const [stats, setStats] = useState({
    viewers: 0,
    likes: 0,
    comments: 0
  });
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    const fetchStreamData = async () => {
      try {
        const docRef = doc(db, 'settings', 'livestream');
        const docSnap = await getDoc(docRef);
        if (docSnap.exists()) {
          setStreamData(docSnap.data());
        }
      } catch (error) {
        console.error("Error fetching stream data:", error);
      } finally {
        setLoading(false);
      }
    };
    fetchStreamData();

    // Setup Realtime Database Listeners
    const viewersRef = ref(rtdb, 'live_stream/viewers');
    const statsRef = ref(rtdb, 'live_stream/stats');
    const commentsRef = ref(rtdb, 'live_stream/comments');

    const unsubViewers = onValue(viewersRef, (snapshot) => {
      setStats(prev => ({ ...prev, viewers: snapshot.exists() ? snapshot.size : 0 }));
    });

    const unsubStats = onValue(statsRef, (snapshot) => {
      setStats(prev => ({ 
        ...prev, 
        likes: snapshot.child('likes').val() || 0 
      }));
    });

    const unsubComments = onValue(commentsRef, (snapshot) => {
      setStats(prev => ({ ...prev, comments: snapshot.exists() ? snapshot.size : 0 }));
    });

    return () => {
      unsubViewers();
      unsubStats();
      unsubComments();
    };
  }, []);

  const handleSave = async () => {
    setSaving(true);
    try {
      await setDoc(doc(db, 'settings', 'livestream'), streamData, { merge: true });
      alert("Live Stream settings updated successfully!");
    } catch (error) {
      console.error("Error saving stream data:", error);
      alert("Failed to update settings.");
    } finally {
      setSaving(false);
    }
  };

  if (loading) return <CircularProgress />;

  return (
    <Box sx={{ maxWidth: 600 }}>
      <Typography variant="h5" gutterBottom fontWeight="bold">Live Stream Manager</Typography>
      
      <Card sx={{ mt: 3 }}>
        <CardContent>
          <FormControlLabel
            control={
              <Switch 
                checked={streamData.isActive} 
                onChange={(e) => setStreamData({ ...streamData, isActive: e.target.checked })}
                color="primary" 
              />
            }
            label={streamData.isActive ? "Stream is Currently LIVE" : "Stream is OFFLINE"}
            sx={{ mb: 3 }}
          />
          
          <Box sx={{ mb: 4, p: 2, bgcolor: 'background.default', borderRadius: 1, border: '1px solid', borderColor: streamData.isActive ? 'success.main' : 'error.main' }}>
            <Typography variant="subtitle2" color="text.secondary">Currently Broadcasting URL:</Typography>
            <Typography variant="body1" fontWeight="bold" sx={{ wordBreak: 'break-all' }}>
              {streamData.rtmpUrl || "No URL Set"}
            </Typography>
          </Box>

          <Typography variant="h6" gutterBottom>Live Statistics</Typography>
          <Grid container spacing={2} sx={{ mb: 4 }}>
            <Grid item xs={12} sm={4}>
              <Paper elevation={0} sx={{ p: 2, textAlign: 'center', border: '1px solid #e0e0e0', bgcolor: '#f8f9fa' }}>
                <PeopleIcon color="primary" sx={{ fontSize: 40, mb: 1 }} />
                <Typography variant="h4" fontWeight="bold">{stats.viewers}</Typography>
                <Typography variant="body2" color="text.secondary">Active Viewers</Typography>
              </Paper>
            </Grid>
            <Grid item xs={12} sm={4}>
              <Paper elevation={0} sx={{ p: 2, textAlign: 'center', border: '1px solid #e0e0e0', bgcolor: '#f8f9fa' }}>
                <ThumbUpIcon color="info" sx={{ fontSize: 40, mb: 1 }} />
                <Typography variant="h4" fontWeight="bold">{stats.likes}</Typography>
                <Typography variant="body2" color="text.secondary">Total Likes</Typography>
              </Paper>
            </Grid>
            <Grid item xs={12} sm={4}>
              <Paper elevation={0} sx={{ p: 2, textAlign: 'center', border: '1px solid #e0e0e0', bgcolor: '#f8f9fa' }}>
                <CommentIcon color="secondary" sx={{ fontSize: 40, mb: 1 }} />
                <Typography variant="h4" fontWeight="bold">{stats.comments}</Typography>
                <Typography variant="body2" color="text.secondary">Live Comments</Typography>
              </Paper>
            </Grid>
          </Grid>
          
          <Typography variant="h6" gutterBottom>Update Stream Configuration</Typography>
          <TextField
            fullWidth
            label="RTMP Stream URL"
            variant="outlined"
            value={streamData.rtmpUrl || ''}
            onChange={(e) => setStreamData({ ...streamData, rtmpUrl: e.target.value })}
            sx={{ mb: 2 }}
            helperText="Enter a new RTMP or HLS (.m3u8) URL to replace the current broadcast."
          />

          <TextField
            fullWidth
            label="Backup Stream URL (Failover)"
            variant="outlined"
            value={streamData.backupUrl || ''}
            onChange={(e) => setStreamData({ ...streamData, backupUrl: e.target.value })}
            sx={{ mb: 2 }}
          />

          <TextField
            fullWidth
            label="Stream Quality (e.g. 1080p, 720p, Auto)"
            variant="outlined"
            value={streamData.quality || ''}
            onChange={(e) => setStreamData({ ...streamData, quality: e.target.value })}
            sx={{ mb: 2 }}
          />

          <TextField
            fullWidth
            label="Stream Title"
            variant="outlined"
            value={streamData.channelName || ''}
            onChange={(e) => setStreamData({ ...streamData, channelName: e.target.value })}
            sx={{ mb: 2 }}
          />

          <TextField
            fullWidth
            label="Stream Description (Time / Schedule)"
            variant="outlined"
            multiline
            rows={2}
            value={streamData.description || ''}
            onChange={(e) => setStreamData({ ...streamData, description: e.target.value })}
            sx={{ mb: 2 }}
          />

          <TextField
            fullWidth
            label="Stream Thumbnail Image URL"
            variant="outlined"
            value={streamData.thumbnailUrl || ''}
            onChange={(e) => setStreamData({ ...streamData, thumbnailUrl: e.target.value })}
            sx={{ mb: 3 }}
          />
          
          <Button 
            variant="contained" 
            size="large" 
            onClick={handleSave}
            disabled={saving}
          >
            {saving ? 'Saving...' : 'Save Settings'}
          </Button>
        </CardContent>
      </Card>
    </Box>
  );
}
