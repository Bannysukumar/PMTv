import React, { useEffect, useState } from 'react';
import { Box, Card, CardContent, Typography, TextField, Button, Grid, CircularProgress, Alert, List, ListItem, ListItemText, Divider, IconButton } from '@mui/material';
import { Delete as DeleteIcon } from '@mui/icons-material';
import { collection, addDoc, serverTimestamp, query, orderBy, onSnapshot, deleteDoc, doc } from 'firebase/firestore';
import { db } from '../firebase/config';

export default function PollManager() {
  const [loading, setLoading] = useState(false);
  const [polls, setPolls] = useState([]);
  
  const [newPoll, setNewPoll] = useState({
    question: '',
    option1: '',
    option2: '',
    option3: '',
  });

  useEffect(() => {
    const q = query(collection(db, 'polls'), orderBy('timestamp', 'desc'));
    const unsubscribe = onSnapshot(q, (snapshot) => {
      setPolls(snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() })));
    });
    return () => unsubscribe();
  }, []);

  const handleChange = (e) => {
    setNewPoll({ ...newPoll, [e.target.name]: e.target.value });
  };

  const handleCreatePoll = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      const optionsArray = [newPoll.option1, newPoll.option2];
      if (newPoll.option3) optionsArray.push(newPoll.option3);

      await addDoc(collection(db, 'polls'), {
        question: newPoll.question,
        options: optionsArray,
        votes: {}, // To track uid -> selectedOption
        isActive: true,
        timestamp: serverTimestamp(),
      });
      
      setNewPoll({ question: '', option1: '', option2: '', option3: '' });
    } catch (err) {
      console.error(err);
      alert("Failed to create poll");
    } finally {
      setLoading(false);
    }
  };

  const handleDeletePoll = async (id) => {
    if (window.confirm("Are you sure you want to delete this poll?")) {
      await deleteDoc(doc(db, 'polls', id));
    }
  };

  return (
    <Box>
      <Typography variant="h5" fontWeight="bold" gutterBottom>Poll Management System</Typography>
      
      <Grid container spacing={3}>
        {/* Create Poll Form */}
        <Grid item xs={12} md={5}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>Create New Poll</Typography>
              <form onSubmit={handleCreatePoll}>
                <TextField fullWidth label="Poll Question" name="question" value={newPoll.question} onChange={handleChange} required sx={{ mb: 2 }} />
                <TextField fullWidth label="Option 1" name="option1" value={newPoll.option1} onChange={handleChange} required sx={{ mb: 2 }} />
                <TextField fullWidth label="Option 2" name="option2" value={newPoll.option2} onChange={handleChange} required sx={{ mb: 2 }} />
                <TextField fullWidth label="Option 3 (Optional)" name="option3" value={newPoll.option3} onChange={handleChange} sx={{ mb: 3 }} />
                
                <Button type="submit" variant="contained" fullWidth disabled={loading}>
                  {loading ? <CircularProgress size={24} /> : 'Publish Poll'}
                </Button>
              </form>
            </CardContent>
          </Card>
        </Grid>

        {/* Active Polls List */}
        <Grid item xs={12} md={7}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>Active Polls</Typography>
              <List>
                {polls.map((poll) => {
                  const totalVotes = poll.votes ? Object.keys(poll.votes).length : 0;
                  return (
                    <React.Fragment key={poll.id}>
                      <ListItem
                        secondaryAction={
                          <IconButton edge="end" color="error" onClick={() => handleDeletePoll(poll.id)}>
                            <DeleteIcon />
                          </IconButton>
                        }
                      >
                        <ListItemText 
                          primary={<Typography fontWeight="bold">{poll.question}</Typography>} 
                          secondary={
                            <React.Fragment>
                              <Typography variant="body2" color="text.secondary">Total Votes: {totalVotes}</Typography>
                              {poll.options.map((opt, index) => (
                                <Typography key={index} variant="caption" display="block">- {opt}</Typography>
                              ))}
                            </React.Fragment>
                          }
                        />
                      </ListItem>
                      <Divider />
                    </React.Fragment>
                  );
                })}
                {polls.length === 0 && <Typography variant="body2" color="text.secondary">No active polls.</Typography>}
              </List>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
}
