import React, { useEffect, useState } from 'react';
import { Box, Card, CardContent, Typography, TextField, Button, Grid, CircularProgress, List, ListItem, ListItemText, Divider, IconButton } from '@mui/material';
import { Delete as DeleteIcon } from '@mui/icons-material';
import { collection, addDoc, serverTimestamp, query, orderBy, onSnapshot, deleteDoc, doc } from 'firebase/firestore';
import { db } from '../firebase/config';

export default function ScheduleManager() {
  const [loading, setLoading] = useState(false);
  const [schedules, setSchedules] = useState([]);
  
  const [newProgram, setNewProgram] = useState({
    programName: '',
    hostName: '',
    startTime: '',
    endTime: '',
  });

  useEffect(() => {
    const q = query(collection(db, 'schedules'), orderBy('startTime', 'asc'));
    const unsubscribe = onSnapshot(q, (snapshot) => {
      setSchedules(snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() })));
    });
    return () => unsubscribe();
  }, []);

  const handleChange = (e) => {
    setNewProgram({ ...newProgram, [e.target.name]: e.target.value });
  };

  const handleAddProgram = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      await addDoc(collection(db, 'schedules'), {
        ...newProgram,
        timestamp: serverTimestamp(),
      });
      setNewProgram({ programName: '', hostName: '', startTime: '', endTime: '' });
    } catch (err) {
      console.error(err);
      alert("Failed to add program");
    } finally {
      setLoading(false);
    }
  };

  const handleDeleteProgram = async (id) => {
    if (window.confirm("Are you sure you want to delete this scheduled program?")) {
      await deleteDoc(doc(db, 'schedules', id));
    }
  };

  return (
    <Box>
      <Typography variant="h5" fontWeight="bold" gutterBottom>Program Schedule Manager</Typography>
      
      <Grid container spacing={3}>
        <Grid item xs={12} md={5}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>Add Daily Program</Typography>
              <form onSubmit={handleAddProgram}>
                <TextField fullWidth label="Program Name" name="programName" value={newProgram.programName} onChange={handleChange} required sx={{ mb: 2 }} />
                <TextField fullWidth label="Host Name (Optional)" name="hostName" value={newProgram.hostName} onChange={handleChange} sx={{ mb: 2 }} />
                
                <Grid container spacing={2} sx={{ mb: 3 }}>
                  <Grid item xs={6}>
                    <TextField fullWidth label="Start Time (e.g. 10:00 AM)" name="startTime" value={newProgram.startTime} onChange={handleChange} required />
                  </Grid>
                  <Grid item xs={6}>
                    <TextField fullWidth label="End Time (e.g. 11:30 AM)" name="endTime" value={newProgram.endTime} onChange={handleChange} required />
                  </Grid>
                </Grid>
                
                <Button type="submit" variant="contained" fullWidth disabled={loading}>
                  {loading ? <CircularProgress size={24} /> : 'Add Program'}
                </Button>
              </form>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} md={7}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>Today's TV Schedule</Typography>
              <List>
                {schedules.map((program) => (
                  <React.Fragment key={program.id}>
                    <ListItem
                      secondaryAction={
                        <IconButton edge="end" color="error" onClick={() => handleDeleteProgram(program.id)}>
                          <DeleteIcon />
                        </IconButton>
                      }
                    >
                      <ListItemText 
                        primary={<Typography fontWeight="bold">{program.programName}</Typography>} 
                        secondary={
                          <Typography variant="body2" color="text.secondary">
                            {program.startTime} - {program.endTime} {program.hostName ? `| Hosted by: ${program.hostName}` : ''}
                          </Typography>
                        }
                      />
                    </ListItem>
                    <Divider />
                  </React.Fragment>
                ))}
                {schedules.length === 0 && <Typography variant="body2" color="text.secondary">No programs scheduled.</Typography>}
              </List>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
}
