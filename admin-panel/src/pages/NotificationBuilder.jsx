import React, { useState } from 'react';
import { Box, Card, CardContent, Typography, TextField, Button, Grid, MenuItem, CircularProgress, Alert } from '@mui/material';
import { collection, addDoc, serverTimestamp } from 'firebase/firestore';
import { db } from '../firebase/config';

export default function NotificationBuilder() {
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState('');
  const [error, setError] = useState('');

  const [notification, setNotification] = useState({
    title: '',
    message: '',
    imageUrl: '',
    actionUrl: '',
    targetAudience: 'All Users',
  });

  const handleChange = (e) => {
    setNotification({ ...notification, [e.target.name]: e.target.value });
  };

  const handleSend = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    setSuccess('');

    try {
      // Here we just write to a 'notifications' collection.
      // A Firebase Cloud Function (or backend server) would need to be written later 
      // to listen to this collection and actually trigger the FCM API.
      await addDoc(collection(db, 'notifications'), {
        ...notification,
        timestamp: serverTimestamp(),
        status: 'pending' // pending -> sent
      });
      
      setSuccess("Notification payload queued successfully! (Requires FCM Cloud Function to deliver)");
      setNotification({ title: '', message: '', imageUrl: '', actionUrl: '', targetAudience: 'All Users' });
    } catch (err) {
      console.error(err);
      setError("Failed to queue notification.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box>
      <Typography variant="h5" fontWeight="bold" gutterBottom>Push Notification Builder</Typography>
      
      {success && <Alert severity="success" sx={{ mb: 2 }}>{success}</Alert>}
      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      <Card sx={{ mt: 3, maxWidth: 600 }}>
        <CardContent>
          <form onSubmit={handleSend}>
            <Grid container spacing={3}>
              <Grid item xs={12}>
                <TextField fullWidth label="Notification Title" name="title" value={notification.title} onChange={handleChange} required />
              </Grid>

              <Grid item xs={12}>
                <TextField fullWidth label="Notification Message" name="message" value={notification.message} onChange={handleChange} required multiline rows={3} />
              </Grid>

              <Grid item xs={12}>
                <TextField fullWidth select label="Target Audience" name="targetAudience" value={notification.targetAudience} onChange={handleChange}>
                  <MenuItem value="All Users">All Users</MenuItem>
                  <MenuItem value="Registered Users">Registered Users Only</MenuItem>
                  <MenuItem value="Guests">Guests Only</MenuItem>
                </TextField>
              </Grid>

              <Grid item xs={12}>
                <TextField fullWidth label="Image URL (Optional Big Picture)" name="imageUrl" value={notification.imageUrl} onChange={handleChange} />
              </Grid>

              <Grid item xs={12}>
                <TextField fullWidth label="Action URL (On Click)" name="actionUrl" value={notification.actionUrl} onChange={handleChange} helperText="Deep link to open specific app screen" />
              </Grid>

              <Grid item xs={12}>
                <Button type="submit" variant="contained" color="secondary" size="large" fullWidth disabled={loading}>
                  {loading ? <CircularProgress size={24} /> : 'Blast Notification'}
                </Button>
              </Grid>
            </Grid>
          </form>
        </CardContent>
      </Card>
    </Box>
  );
}
