import React, { useEffect, useState } from 'react';
import { Box, Card, CardContent, Typography, Button, Grid, CircularProgress, ImageList, ImageListItem, ImageListItemBar, IconButton, Alert } from '@mui/material';
import { Delete as DeleteIcon, ContentCopy as CopyIcon, CloudUpload as UploadIcon } from '@mui/icons-material';
import { ref, listAll, getDownloadURL, uploadBytes, deleteObject } from 'firebase/storage';
import { storage } from '../firebase/config';

export default function MediaLibrary() {
  const [loading, setLoading] = useState(true);
  const [uploading, setUploading] = useState(false);
  const [mediaFiles, setMediaFiles] = useState([]);
  const [error, setError] = useState('');

  const fetchMedia = async () => {
    setLoading(true);
    try {
      const listRef = ref(storage, 'media');
      const res = await listAll(listRef);
      
      const filePromises = res.items.map(async (itemRef) => {
        const url = await getDownloadURL(itemRef);
        return {
          name: itemRef.name,
          fullPath: itemRef.fullPath,
          url: url,
        };
      });

      const files = await Promise.all(filePromises);
      setMediaFiles(files);
    } catch (err) {
      console.error(err);
      setError("Failed to fetch media library.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchMedia();
  }, []);

  const handleUpload = async (e) => {
    const file = e.target.files[0];
    if (!file) return;

    setUploading(true);
    try {
      const fileRef = ref(storage, `media/${Date.now()}_${file.name}`);
      await uploadBytes(fileRef, file);
      await fetchMedia();
    } catch (err) {
      console.error(err);
      alert("Failed to upload file.");
    } finally {
      setUploading(false);
    }
  };

  const handleDelete = async (fullPath) => {
    if (window.confirm("Are you sure you want to permanently delete this file from storage?")) {
      try {
        const fileRef = ref(storage, fullPath);
        await deleteObject(fileRef);
        await fetchMedia();
      } catch (err) {
        console.error(err);
        alert("Failed to delete file.");
      }
    }
  };

  const handleCopyUrl = (url) => {
    navigator.clipboard.writeText(url);
    alert("Copied Download URL to clipboard!");
  };

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h5" fontWeight="bold">Media Library</Typography>
        <Button
          variant="contained"
          component="label"
          startIcon={uploading ? <CircularProgress size={20} color="inherit" /> : <UploadIcon />}
          disabled={uploading}
        >
          {uploading ? 'Uploading...' : 'Upload Media'}
          <input type="file" hidden accept="image/*,video/*" onChange={handleUpload} />
        </Button>
      </Box>

      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      <Card>
        <CardContent>
          {loading ? (
            <Box sx={{ display: 'flex', justifyContent: 'center', p: 5 }}>
              <CircularProgress />
            </Box>
          ) : mediaFiles.length === 0 ? (
            <Typography variant="body1" color="text.secondary" align="center" sx={{ py: 5 }}>
              No media files found in the 'media/' folder.
            </Typography>
          ) : (
            <ImageList sx={{ width: '100%', height: 'auto' }} cols={4} rowHeight={200} gap={16}>
              {mediaFiles.map((item) => (
                <ImageListItem key={item.fullPath}>
                  <img
                    src={item.url}
                    alt={item.name}
                    loading="lazy"
                    style={{ borderRadius: '8px', objectFit: 'cover', width: '100%', height: '100%' }}
                    onError={(e) => { e.target.src = 'https://via.placeholder.com/200?text=File'; }} // fallback for non-images
                  />
                  <ImageListItemBar
                    title={item.name}
                    sx={{ borderBottomLeftRadius: '8px', borderBottomRightRadius: '8px' }}
                    actionIcon={
                      <Box sx={{ display: 'flex' }}>
                        <IconButton sx={{ color: 'rgba(255, 255, 255, 0.8)' }} onClick={() => handleCopyUrl(item.url)}>
                          <CopyIcon />
                        </IconButton>
                        <IconButton sx={{ color: 'rgba(255, 0, 0, 0.8)' }} onClick={() => handleDelete(item.fullPath)}>
                          <DeleteIcon />
                        </IconButton>
                      </Box>
                    }
                  />
                </ImageListItem>
              ))}
            </ImageList>
          )}
        </CardContent>
      </Card>
    </Box>
  );
}
