import React, { useEffect, useState } from 'react';
import { Card, Typography, Box, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Paper, Button, Chip } from '@mui/material';
import { collection, query, orderBy, onSnapshot, deleteDoc, doc } from 'firebase/firestore';
import { db } from '../firebase/config';
import { useNavigate } from 'react-router-dom';

export default function NewsManager() {
  const [news, setNews] = useState([]);
  const navigate = useNavigate();

  useEffect(() => {
    const q = query(collection(db, 'news'), orderBy('timestamp', 'desc'));
    const unsubscribe = onSnapshot(q, (snapshot) => {
      const newsData = snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));
      setNews(newsData);
    });

    return () => unsubscribe();
  }, []);

  const handleDelete = async (id) => {
    if (window.confirm("Are you sure you want to delete this news article?")) {
      await deleteDoc(doc(db, 'news', id));
    }
  };

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 3 }}>
        <Typography variant="h5" fontWeight="bold">News Management</Typography>
        <Button variant="contained" color="primary" onClick={() => navigate('/news/create')}>Create News</Button>
      </Box>

      <TableContainer component={Paper} elevation={1}>
        <Table>
          <TableHead sx={{ bgcolor: 'background.default' }}>
            <TableRow>
              <TableCell>Title</TableCell>
              <TableCell>Category</TableCell>
              <TableCell>Status</TableCell>
              <TableCell align="right">Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {news.map((item) => (
              <TableRow key={item.id}>
                <TableCell>{item.title || item.headline}</TableCell>
                <TableCell>{item.category}</TableCell>
                <TableCell>
                  <Chip label={item.isBreaking ? "Breaking" : "Standard"} color={item.isBreaking ? "error" : "default"} size="small" />
                </TableCell>
                <TableCell align="right">
                  <Button size="small" color="primary" sx={{ mr: 1 }}>Edit</Button>
                  <Button size="small" color="error" onClick={() => handleDelete(item.id)}>Delete</Button>
                </TableCell>
              </TableRow>
            ))}
            {news.length === 0 && (
              <TableRow>
                <TableCell colSpan={4} align="center" sx={{ py: 3 }}>
                  No news articles found.
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </TableContainer>
    </Box>
  );
}
