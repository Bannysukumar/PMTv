import React, { useEffect, useState } from 'react';
import { Card, Typography, Box, Paper, Button, Avatar, Chip } from '@mui/material';
import { DataGrid, GridToolbar } from '@mui/x-data-grid';
import { collection, query, orderBy, onSnapshot, deleteDoc, doc, updateDoc } from 'firebase/firestore';
import { db } from '../firebase/config';

export default function CommunityManager() {
  const [posts, setPosts] = useState([]);

  useEffect(() => {
    const q = query(collection(db, 'community_posts'));
    const unsubscribe = onSnapshot(q, (snapshot) => {
      let postsData = snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));
      // Sort client-side to avoid missing documents that lack a timestamp field
      postsData.sort((a, b) => {
        const timeA = a.timestamp ? a.timestamp.toMillis() : 0;
        const timeB = b.timestamp ? b.timestamp.toMillis() : 0;
        return timeB - timeA;
      });
      setPosts(postsData);
    });

    return () => unsubscribe();
  }, []);

  const handleDelete = async (id) => {
    if (window.confirm("Are you sure you want to completely delete this post? This cannot be undone.")) {
      try {
        await deleteDoc(doc(db, 'community_posts', id));
      } catch (err) {
        console.error(err);
        alert("Failed to delete post");
      }
    }
  };

  const handleApprove = async (id) => {
    try {
      await updateDoc(doc(db, 'community_posts', id), {
        isApproved: true
      });
    } catch (err) {
      console.error(err);
      alert("Failed to approve post");
    }
  };

  const columns = [
    {
      field: 'author',
      headerName: 'Author',
      width: 250,
      renderCell: (params) => (
        <Box sx={{ display: 'flex', alignItems: 'center', height: '100%' }}>
          <Avatar src={params.row.userProfileImage} sx={{ width: 32, height: 32, mr: 2 }} />
          <Box>
            <Typography variant="body2" fontWeight="bold" sx={{ lineHeight: 1.2 }}>{params.row.userName || 'Unknown'}</Typography>
            <Typography variant="caption" color="text.secondary">{params.row.userId}</Typography>
          </Box>
        </Box>
      )
    },
    { 
      field: 'content', 
      headerName: 'Content', 
      flex: 1,
      minWidth: 300,
      renderCell: (params) => (
        <Typography variant="body2" noWrap>{params.value}</Typography>
      )
    },
    { 
      field: 'status', 
      headerName: 'Status', 
      width: 130,
      renderCell: (params) => (
        <Chip 
          label={params.row.isApproved ? "Approved" : "Pending"} 
          color={params.row.isApproved ? "success" : "warning"} 
          size="small" 
        />
      )
    },
    { 
      field: 'date', 
      headerName: 'Date', 
      width: 180,
      valueGetter: (params, row) => row.timestamp ? row.timestamp.toDate() : new Date(),
      valueFormatter: (value) => value ? value.toLocaleString() : 'Just now'
    },
    { 
      field: 'actions', 
      headerName: 'Actions', 
      width: 180,
      sortable: false,
      renderCell: (params) => (
        <Box sx={{ display: 'flex', gap: 1, alignItems: 'center', height: '100%' }}>
          {!params.row.isApproved && (
            <Button size="small" color="primary" variant="outlined" onClick={() => handleApprove(params.row.id)}>
              Approve
            </Button>
          )}
          <Button size="small" color="error" variant="outlined" onClick={() => handleDelete(params.row.id)}>
            Delete
          </Button>
        </Box>
      )
    }
  ];

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 3 }}>
        <Typography variant="h5" fontWeight="bold">Community Moderation</Typography>
      </Box>

      <Paper elevation={1} sx={{ height: 600, width: '100%' }}>
        <DataGrid
          rows={posts}
          columns={columns}
          getRowId={(row) => row.id}
          pageSizeOptions={[10, 25, 50]}
          initialState={{
            pagination: { paginationModel: { pageSize: 25 } },
            sorting: {
              sortModel: [{ field: 'date', sort: 'desc' }],
            },
          }}
          slots={{ toolbar: GridToolbar }}
          slotProps={{
            toolbar: {
              showQuickFilter: true,
            },
          }}
          disableRowSelectionOnClick
        />
      </Paper>
    </Box>
  );
}
