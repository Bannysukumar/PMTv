import React, { useEffect, useState } from 'react';
import { Card, Typography, Box, Paper, Button, Chip, Avatar, Select, MenuItem } from '@mui/material';
import { DataGrid, GridToolbar } from '@mui/x-data-grid';
import { collection, query, onSnapshot, doc, updateDoc, setDoc, deleteDoc } from 'firebase/firestore';
import { db } from '../firebase/config';

export default function UserManager() {
  const [users, setUsers] = useState([]);
  const [adminsMap, setAdminsMap] = useState({});

  useEffect(() => {
    const q = query(collection(db, 'users'));
    const unsubscribeUsers = onSnapshot(q, (snapshot) => {
      const usersData = snapshot.docs.map(doc => ({ uid: doc.id, ...doc.data() }));
      setUsers(usersData);
    });

    const unsubscribeAdmins = onSnapshot(collection(db, 'admins'), (snapshot) => {
      const adminData = {};
      snapshot.docs.forEach(doc => {
        adminData[doc.id] = doc.data().role;
      });
      setAdminsMap(adminData);
    });

    return () => {
      unsubscribeUsers();
      unsubscribeAdmins();
    };
  }, []);

  const handleToggleBan = async (uid, currentStatus) => {
    const action = currentStatus ? "unban" : "ban";
    if (window.confirm(`Are you sure you want to ${action} this user?`)) {
      await updateDoc(doc(db, 'users', uid), {
        isBanned: !currentStatus
      });
    }
  };

  const handleUpdateRole = async (uid, email, newRole) => {
    try {
      if (newRole === 'User') {
        if (window.confirm("Remove all admin privileges from this user?")) {
          await deleteDoc(doc(db, 'admins', uid));
        }
      } else {
        await setDoc(doc(db, 'admins', uid), {
          email: email || '',
          role: newRole,
        }, { merge: true });
      }
    } catch (err) {
      console.error(err);
      alert("Failed to update user role");
    }
  };

  const columns = [
    { 
      field: 'avatar', 
      headerName: 'Profile', 
      width: 80,
      renderCell: (params) => <Avatar src={params.row.photoUrl} alt={params.row.name} />
    },
    { field: 'uid', headerName: 'UID', width: 220 },
    { 
      field: 'name', 
      headerName: 'Name', 
      width: 200,
      renderCell: (params) => (
        <Box>
          <Typography variant="body2" fontWeight="bold">{params.row.name || 'Anonymous'}</Typography>
          <Typography variant="caption" color="text.secondary">{params.row.email || 'No email'}</Typography>
        </Box>
      )
    },
    { 
      field: 'status', 
      headerName: 'App Status', 
      width: 120,
      renderCell: (params) => (
        <Chip 
          label={params.row.isBanned ? "Banned" : "Active"} 
          color={params.row.isBanned ? "error" : "success"} 
          size="small" 
        />
      )
    },
    { 
      field: 'role', 
      headerName: 'Admin Role', 
      width: 200,
      renderCell: (params) => (
        <Select
          size="small"
          value={adminsMap[params.row.uid] || 'User'}
          onChange={(e) => handleUpdateRole(params.row.uid, params.row.email, e.target.value)}
          sx={{ minWidth: 140 }}
        >
          <MenuItem value="User">Standard User</MenuItem>
          <MenuItem value="Super Admin">Super Admin</MenuItem>
          <MenuItem value="Admin">Admin</MenuItem>
          <MenuItem value="Moderator">Moderator</MenuItem>
          <MenuItem value="Content Editor">Content Editor</MenuItem>
          <MenuItem value="News Publisher">News Publisher</MenuItem>
          <MenuItem value="Support Agent">Support Agent</MenuItem>
        </Select>
      )
    },
    { 
      field: 'actions', 
      headerName: 'Actions', 
      width: 150,
      sortable: false,
      renderCell: (params) => (
        <Button 
          size="small" 
          color={params.row.isBanned ? "success" : "error"} 
          onClick={() => handleToggleBan(params.row.uid, params.row.isBanned)}
          variant="outlined"
        >
          {params.row.isBanned ? "Unban" : "Ban"}
        </Button>
      )
    }
  ];

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 3 }}>
        <Typography variant="h5" fontWeight="bold">User Management</Typography>
      </Box>

      <Paper elevation={1} sx={{ height: 600, width: '100%' }}>
        <DataGrid
          rows={users}
          columns={columns}
          getRowId={(row) => row.uid}
          pageSizeOptions={[10, 25, 50, 100]}
          initialState={{
            pagination: { paginationModel: { pageSize: 25 } },
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
