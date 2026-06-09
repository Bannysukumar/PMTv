import React, { useEffect, useState } from 'react';
import { Box, Card, Typography, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Paper } from '@mui/material';
import { collection, query, orderBy, onSnapshot, limit } from 'firebase/firestore';
import { db } from '../firebase/config';

export default function AuditLogs() {
  const [logs, setLogs] = useState([]);

  useEffect(() => {
    // Only fetching the last 100 logs for performance
    const q = query(collection(db, 'audit_logs'), orderBy('timestamp', 'desc'), limit(100));
    const unsubscribe = onSnapshot(q, (snapshot) => {
      setLogs(snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() })));
    });
    return () => unsubscribe();
  }, []);

  return (
    <Box>
      <Typography variant="h5" fontWeight="bold" gutterBottom>Security & Audit Logs</Typography>
      <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
        Tracking recent administrative actions performed across the Super Admin Dashboard.
      </Typography>

      <TableContainer component={Paper} elevation={1}>
        <Table>
          <TableHead sx={{ bgcolor: 'background.default' }}>
            <TableRow>
              <TableCell>Timestamp</TableCell>
              <TableCell>Admin Email</TableCell>
              <TableCell>Action Performed</TableCell>
              <TableCell>Module</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {logs.map((log) => (
              <TableRow key={log.id}>
                <TableCell>
                  {log.timestamp ? new Date(log.timestamp.toDate()).toLocaleString() : 'N/A'}
                </TableCell>
                <TableCell fontWeight="bold">{log.adminEmail}</TableCell>
                <TableCell>{log.action}</TableCell>
                <TableCell>{log.module}</TableCell>
              </TableRow>
            ))}
            {logs.length === 0 && (
              <TableRow>
                <TableCell colSpan={4} align="center" sx={{ py: 3 }}>
                  No audit logs found. Note: You must integrate the `logAdminAction` helper function across your components to populate this table.
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </TableContainer>
    </Box>
  );
}
