import React, { useEffect, useState } from 'react';
import { Box, Card, CardContent, Typography, TextField, Button, Grid, CircularProgress, List, ListItem, ListItemText, Divider, IconButton, Chip, MenuItem } from '@mui/material';
import { Delete as DeleteIcon } from '@mui/icons-material';
import { collection, addDoc, serverTimestamp, query, orderBy, onSnapshot, deleteDoc, doc } from 'firebase/firestore';
import { db } from '../firebase/config';

export default function AlertsManager() {
  const [loading, setLoading] = useState(false);
  const [alerts, setAlerts] = useState([]);
  
  const [newAlert, setNewAlert] = useState({
    message: '',
    type: 'Breaking News',
    priority: 'High',
  });

  useEffect(() => {
    const q = query(collection(db, 'announcements'), orderBy('timestamp', 'desc'));
    const unsubscribe = onSnapshot(q, (snapshot) => {
      setAlerts(snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() })));
    });
    return () => unsubscribe();
  }, []);

  const handleChange = (e) => {
    setNewAlert({ ...newAlert, [e.target.name]: e.target.value });
  };

  const handleCreateAlert = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      await addDoc(collection(db, 'announcements'), {
        ...newAlert,
        isActive: true,
        timestamp: serverTimestamp(),
      });
      setNewAlert({ message: '', type: 'Breaking News', priority: 'High' });
    } catch (err) {
      console.error(err);
      alert("Failed to publish alert");
    } finally {
      setLoading(false);
    }
  };

  const handleDeleteAlert = async (id) => {
    if (window.confirm("Delete this alert? It will immediately disappear from the app.")) {
      await deleteDoc(doc(db, 'announcements', id));
    }
  };

  return (
    <Box>
      <Typography variant="h5" fontWeight="bold" gutterBottom>Breaking News & Alerts Ticker</Typography>
      
      <Grid container spacing={3}>
        <Grid item xs={12} md={5}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>Broadcast Alert</Typography>
              <form onSubmit={handleCreateAlert}>
                <TextField fullWidth label="Alert Message / Ticker Text" name="message" value={newAlert.message} onChange={handleChange} required multiline rows={3} sx={{ mb: 2 }} />
                
                <Grid container spacing={2} sx={{ mb: 3 }}>
                  <Grid item xs={6}>
                    <TextField fullWidth select label="Alert Type" name="type" value={newAlert.type} onChange={handleChange}>
                      <MenuItem value="Breaking News">Breaking News</MenuItem>
                      <MenuItem value="Maintenance Notice">Maintenance Notice</MenuItem>
                      <MenuItem value="General Update">General Update</MenuItem>
                      <MenuItem value="Emergency">Emergency</MenuItem>
                    </TextField>
                  </Grid>
                  <Grid item xs={6}>
                    <TextField fullWidth select label="Priority" name="priority" value={newAlert.priority} onChange={handleChange}>
                      <MenuItem value="High">High</MenuItem>
                      <MenuItem value="Normal">Normal</MenuItem>
                    </TextField>
                  </Grid>
                </Grid>
                
                <Button type="submit" variant="contained" color="error" fullWidth disabled={loading}>
                  {loading ? <CircularProgress size={24} color="inherit" /> : 'Push Live Ticker'}
                </Button>
              </form>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} md={7}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>Active Tickers & Announcements</Typography>
              <List>
                {alerts.map((alert) => (
                  <React.Fragment key={alert.id}>
                    <ListItem
                      secondaryAction={
                        <IconButton edge="end" color="error" onClick={() => handleDeleteAlert(alert.id)}>
                          <DeleteIcon />
                        </IconButton>
                      }
                    >
                      <ListItemText 
                        primary={<Typography fontWeight="bold">{alert.message}</Typography>} 
                        secondary={
                          <Box sx={{ mt: 1 }}>
                            <Chip label={alert.type} size="small" color={alert.type === 'Emergency' || alert.type === 'Breaking News' ? 'error' : 'primary'} sx={{ mr: 1 }} />
                            <Chip label={`${alert.priority} Priority`} size="small" variant="outlined" />
                          </Box>
                        }
                      />
                    </ListItem>
                    <Divider />
                  </React.Fragment>
                ))}
                {alerts.length === 0 && <Typography variant="body2" color="text.secondary">No active alerts.</Typography>}
              </List>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
}
