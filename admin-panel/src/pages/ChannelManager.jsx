import React, { useEffect, useState } from 'react';
import { Box, Card, CardContent, Typography, TextField, Button, Grid, CircularProgress, List, ListItem, ListItemText, Divider, IconButton, Avatar, Chip, MenuItem } from '@mui/material';
import { Delete as DeleteIcon } from '@mui/icons-material';
import { collection, addDoc, serverTimestamp, query, orderBy, onSnapshot, deleteDoc, doc } from 'firebase/firestore';
import { ref, uploadBytes, getDownloadURL } from 'firebase/storage';
import { db, storage } from '../firebase/config';

export default function ChannelManager() {
  const [loading, setLoading] = useState(false);
  const [channels, setChannels] = useState([]);
  
  const [newChannel, setNewChannel] = useState({
    name: '',
    category: 'News',
    description: '',
    streamUrl: '',
    status: 'Online',
  });
  
  const [logoFile, setLogoFile] = useState(null);

  useEffect(() => {
    const q = query(collection(db, 'channels'), orderBy('timestamp', 'desc'));
    const unsubscribe = onSnapshot(q, (snapshot) => {
      setChannels(snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() })));
    });
    return () => unsubscribe();
  }, []);

  const handleChange = (e) => {
    setNewChannel({ ...newChannel, [e.target.name]: e.target.value });
  };

  const handleFileChange = (e) => {
    if (e.target.files[0]) {
      setLogoFile(e.target.files[0]);
    }
  };

  const handleCreateChannel = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      let logoUrl = '';
      if (logoFile) {
        const fileRef = ref(storage, `channel_logos/${Date.now()}_${logoFile.name}`);
        await uploadBytes(fileRef, logoFile);
        logoUrl = await getDownloadURL(fileRef);
      }

      await addDoc(collection(db, 'channels'), {
        ...newChannel,
        logoUrl: logoUrl,
        timestamp: serverTimestamp(),
      });
      
      setNewChannel({ name: '', category: 'News', description: '', streamUrl: '', status: 'Online' });
      setLogoFile(null);
    } catch (err) {
      console.error(err);
      alert("Failed to create channel");
    } finally {
      setLoading(false);
    }
  };

  const handleDeleteChannel = async (id) => {
    if (window.confirm("Are you sure you want to delete this channel?")) {
      await deleteDoc(doc(db, 'channels', id));
    }
  };

  return (
    <Box>
      <Typography variant="h5" fontWeight="bold" gutterBottom>Multi-Channel Network Manager</Typography>
      
      <Grid container spacing={3}>
        <Grid item xs={12} md={5}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>Create New Channel</Typography>
              <form onSubmit={handleCreateChannel}>
                <TextField fullWidth label="Channel Name" name="name" value={newChannel.name} onChange={handleChange} required sx={{ mb: 2 }} />
                
                <Grid container spacing={2} sx={{ mb: 2 }}>
                  <Grid item xs={6}>
                    <TextField fullWidth select label="Category" name="category" value={newChannel.category} onChange={handleChange}>
                      <MenuItem value="News">News</MenuItem>
                      <MenuItem value="Entertainment">Entertainment</MenuItem>
                      <MenuItem value="Sports">Sports</MenuItem>
                      <MenuItem value="Movies">Movies</MenuItem>
                      <MenuItem value="Education">Education</MenuItem>
                    </TextField>
                  </Grid>
                  <Grid item xs={6}>
                    <TextField fullWidth select label="Status" name="status" value={newChannel.status} onChange={handleChange}>
                      <MenuItem value="Online">Online</MenuItem>
                      <MenuItem value="Offline">Offline</MenuItem>
                      <MenuItem value="Maintenance">Maintenance</MenuItem>
                    </TextField>
                  </Grid>
                </Grid>

                <TextField fullWidth label="Description" name="description" value={newChannel.description} onChange={handleChange} multiline rows={2} sx={{ mb: 2 }} />
                <TextField fullWidth label="RTMP / HLS Stream URL" name="streamUrl" value={newChannel.streamUrl} onChange={handleChange} required sx={{ mb: 2 }} />
                
                <Typography variant="body2" color="text.secondary" gutterBottom>Channel Logo</Typography>
                <input type="file" accept="image/*" onChange={handleFileChange} style={{ marginBottom: '16px', display: 'block' }} />

                <Button type="submit" variant="contained" fullWidth disabled={loading}>
                  {loading ? <CircularProgress size={24} /> : 'Publish Channel'}
                </Button>
              </form>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} md={7}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>Active Channels Database</Typography>
              <List>
                {channels.map((channel) => (
                  <React.Fragment key={channel.id}>
                    <ListItem
                      secondaryAction={
                        <IconButton edge="end" color="error" onClick={() => handleDeleteChannel(channel.id)}>
                          <DeleteIcon />
                        </IconButton>
                      }
                    >
                      <Avatar src={channel.logoUrl} sx={{ width: 48, height: 48, mr: 2 }} variant="rounded" />
                      <ListItemText 
                        primary={<Typography fontWeight="bold">{channel.name}</Typography>} 
                        secondary={
                          <React.Fragment>
                            <Typography variant="body2" color="text.secondary" noWrap sx={{ maxWidth: 300 }}>
                              {channel.streamUrl}
                            </Typography>
                            <Chip label={channel.category} size="small" sx={{ mt: 0.5, mr: 1 }} />
                            <Chip label={channel.status} size="small" color={channel.status === 'Online' ? 'success' : 'error'} sx={{ mt: 0.5 }} />
                          </React.Fragment>
                        }
                      />
                    </ListItem>
                    <Divider />
                  </React.Fragment>
                ))}
                {channels.length === 0 && <Typography variant="body2" color="text.secondary">No channels found.</Typography>}
              </List>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
}
