import React, { useState } from 'react';
import { Box, Card, CardContent, Typography, TextField, Button, Grid, FormControlLabel, Switch, MenuItem, CircularProgress, Alert } from '@mui/material';
import ReactQuill from 'react-quill';
import 'react-quill/dist/quill.snow.css';
import { useNavigate } from 'react-router-dom';
import { collection, addDoc, serverTimestamp } from 'firebase/firestore';
import { ref, uploadBytes, getDownloadURL } from 'firebase/storage';
import { db, storage } from '../firebase/config';
import { useAuth } from '../context/AuthContext';

const CATEGORIES = [
  'Politics', 'National', 'International', 'Technology', 'Sports', 
  'Business', 'Entertainment', 'Health', 'Education', 'Local News'
];

export default function NewsForm() {
  const navigate = useNavigate();
  const { currentUser } = useAuth();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  
  const [formData, setFormData] = useState({
    headline: '',
    shortDescription: '',
    fullContent: '',
    category: 'Politics',
    author: currentUser?.email || 'Admin',
    videoLink: '',
    externalLink: '',
    tags: '',
    isBreaking: false,
    isFeatured: false,
  });

  const [thumbnailFile, setThumbnailFile] = useState(null);

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData({
      ...formData,
      [name]: type === 'checkbox' ? checked : value
    });
  };

  const handleContentChange = (value) => {
    setFormData({
      ...formData,
      fullContent: value
    });
  };

  const handleFileChange = (e) => {
    if (e.target.files[0]) {
      setThumbnailFile(e.target.files[0]);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      let thumbnailUrl = '';
      
      // Upload thumbnail to Firebase Storage if selected
      if (thumbnailFile) {
        const fileRef = ref(storage, `news_thumbnails/${Date.now()}_${thumbnailFile.name}`);
        await uploadBytes(fileRef, thumbnailFile);
        thumbnailUrl = await getDownloadURL(fileRef);
      }

      // Prepare tags array
      const tagsArray = formData.tags.split(',').map(tag => tag.trim()).filter(tag => tag);

      // Add to Firestore
      await addDoc(collection(db, 'news'), {
        title: formData.headline,
        description: formData.shortDescription,
        content: formData.fullContent,
        category: formData.category,
        author: formData.author,
        videoLink: formData.videoLink,
        externalLink: formData.externalLink,
        tags: tagsArray,
        isBreaking: formData.isBreaking,
        isFeatured: formData.isFeatured,
        imageUrl: thumbnailUrl,
        timestamp: serverTimestamp(),
      });

      navigate('/news');
    } catch (err) {
      console.error(err);
      setError('Failed to create news article. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 3 }}>
        <Typography variant="h5" fontWeight="bold">Create News Article</Typography>
        <Button variant="outlined" onClick={() => navigate('/news')}>Cancel</Button>
      </Box>

      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      <Card>
        <CardContent>
          <form onSubmit={handleSubmit}>
            <Grid container spacing={3}>
              <Grid item xs={12}>
                <TextField fullWidth label="Headline" name="headline" value={formData.headline} onChange={handleChange} required />
              </Grid>
              
              <Grid item xs={12}>
                <TextField fullWidth label="Short Description" name="shortDescription" value={formData.shortDescription} onChange={handleChange} required multiline rows={2} />
              </Grid>

              <Grid item xs={12}>
                <Typography variant="body2" color="text.secondary" gutterBottom>Full Content</Typography>
                <Box sx={{ 
                  bgcolor: 'background.paper', 
                  '& .ql-toolbar': { borderColor: 'divider', bgcolor: 'background.default' },
                  '& .ql-container': { borderColor: 'divider', minHeight: 300 }
                }}>
                  <ReactQuill 
                    theme="snow" 
                    value={formData.fullContent} 
                    onChange={handleContentChange} 
                    style={{ height: '300px', marginBottom: '40px' }}
                  />
                </Box>
              </Grid>

              <Grid item xs={12} md={6}>
                <TextField fullWidth select label="Category" name="category" value={formData.category} onChange={handleChange}>
                  {CATEGORIES.map(option => (
                    <MenuItem key={option} value={option}>{option}</MenuItem>
                  ))}
                </TextField>
              </Grid>

              <Grid item xs={12} md={6}>
                <TextField fullWidth label="Author Name" name="author" value={formData.author} onChange={handleChange} required />
              </Grid>

              <Grid item xs={12} md={6}>
                <Typography variant="body2" color="text.secondary" gutterBottom>Thumbnail Image</Typography>
                <input type="file" accept="image/*" onChange={handleFileChange} />
              </Grid>

              <Grid item xs={12} md={6}>
                <TextField fullWidth label="Tags (comma separated)" name="tags" value={formData.tags} onChange={handleChange} placeholder="e.g. Politics, Election, Update" />
              </Grid>

              <Grid item xs={12} md={6}>
                <TextField fullWidth label="Video Link (Optional)" name="videoLink" value={formData.videoLink} onChange={handleChange} />
              </Grid>

              <Grid item xs={12} md={6}>
                <TextField fullWidth label="External Link (Optional)" name="externalLink" value={formData.externalLink} onChange={handleChange} />
              </Grid>

              <Grid item xs={12} md={6}>
                <FormControlLabel control={<Switch checked={formData.isBreaking} onChange={handleChange} name="isBreaking" color="error" />} label="Mark as Breaking News" />
              </Grid>

              <Grid item xs={12} md={6}>
                <FormControlLabel control={<Switch checked={formData.isFeatured} onChange={handleChange} name="isFeatured" color="primary" />} label="Mark as Featured (Top Story)" />
              </Grid>

              <Grid item xs={12}>
                <Button type="submit" variant="contained" size="large" fullWidth disabled={loading}>
                  {loading ? <CircularProgress size={24} color="inherit" /> : 'Publish News'}
                </Button>
              </Grid>
            </Grid>
          </form>
        </CardContent>
      </Card>
    </Box>
  );
}
