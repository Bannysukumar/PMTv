import React, { useEffect, useState } from 'react';
import { Box, Card, CardContent, Typography, TextField, Button, Grid, CircularProgress } from '@mui/material';
import { doc, getDoc, setDoc } from 'firebase/firestore';
import { db } from '../firebase/config';

export default function SettingsManager() {
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  
  const [settings, setSettings] = useState({
    appName: 'PMTv',
    supportEmail: '',
    contactNumber: '',
    website: '',
    facebookUrl: '',
    twitterUrl: '',
    youtubeUrl: '',
    primaryColor: '#E50914',
    secondaryColor: '#121212',
  });

  useEffect(() => {
    const fetchSettings = async () => {
      try {
        const docRef = doc(db, 'settings', 'global');
        const docSnap = await getDoc(docRef);
        if (docSnap.exists()) {
          setSettings(prev => ({ ...prev, ...docSnap.data() }));
        }
      } catch (error) {
        console.error("Error fetching settings:", error);
      } finally {
        setLoading(false);
      }
    };
    fetchSettings();
  }, []);

  const handleChange = (e) => {
    setSettings({ ...settings, [e.target.name]: e.target.value });
  };

  const handleSave = async (e) => {
    e.preventDefault();
    setSaving(true);
    try {
      await setDoc(doc(db, 'settings', 'global'), settings, { merge: true });
      alert("App Settings updated successfully!");
    } catch (error) {
      console.error("Error saving settings:", error);
      alert("Failed to update settings.");
    } finally {
      setSaving(false);
    }
  };

  if (loading) return <CircularProgress />;

  return (
    <Box>
      <Typography variant="h5" fontWeight="bold" gutterBottom>App Settings & Theme Manager</Typography>
      
      <Card sx={{ mt: 3, maxWidth: 800 }}>
        <CardContent>
          <form onSubmit={handleSave}>
            <Grid container spacing={3}>
              <Grid item xs={12} md={6}>
                <TextField fullWidth label="App Name" name="appName" value={settings.appName} onChange={handleChange} required />
              </Grid>
              <Grid item xs={12} md={6}>
                <TextField fullWidth label="Support Email" name="supportEmail" value={settings.supportEmail} onChange={handleChange} type="email" />
              </Grid>
              
              <Grid item xs={12} md={6}>
                <TextField fullWidth label="Contact Number" name="contactNumber" value={settings.contactNumber} onChange={handleChange} />
              </Grid>
              <Grid item xs={12} md={6}>
                <TextField fullWidth label="Website URL" name="website" value={settings.website} onChange={handleChange} type="url" />
              </Grid>

              <Grid item xs={12}>
                <Typography variant="h6" sx={{ mt: 2, mb: 1 }}>Social Media Links</Typography>
              </Grid>

              <Grid item xs={12} md={4}>
                <TextField fullWidth label="Facebook URL" name="facebookUrl" value={settings.facebookUrl} onChange={handleChange} />
              </Grid>
              <Grid item xs={12} md={4}>
                <TextField fullWidth label="Twitter URL" name="twitterUrl" value={settings.twitterUrl} onChange={handleChange} />
              </Grid>
              <Grid item xs={12} md={4}>
                <TextField fullWidth label="YouTube URL" name="youtubeUrl" value={settings.youtubeUrl} onChange={handleChange} />
              </Grid>

              <Grid item xs={12}>
                <Typography variant="h6" sx={{ mt: 2, mb: 1 }}>Theme Manager (Hex Colors)</Typography>
              </Grid>

              <Grid item xs={12} md={6}>
                <TextField fullWidth label="Primary Color (e.g. #E50914)" name="primaryColor" value={settings.primaryColor} onChange={handleChange} />
                <Box sx={{ mt: 1, width: '100%', height: 20, bgcolor: settings.primaryColor, borderRadius: 1 }}></Box>
              </Grid>
              
              <Grid item xs={12} md={6}>
                <TextField fullWidth label="Secondary Color (e.g. #121212)" name="secondaryColor" value={settings.secondaryColor} onChange={handleChange} />
                <Box sx={{ mt: 1, width: '100%', height: 20, bgcolor: settings.secondaryColor, borderRadius: 1 }}></Box>
              </Grid>

              <Grid item xs={12} sx={{ mt: 2 }}>
                <Button type="submit" variant="contained" size="large" disabled={saving}>
                  {saving ? 'Saving...' : 'Save Global Settings'}
                </Button>
              </Grid>
            </Grid>
          </form>
        </CardContent>
      </Card>
    </Box>
  );
}
