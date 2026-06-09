import React, { useEffect, useState } from 'react';
import { Box, Card, CardContent, Typography, TextField, Button, Grid, FormControlLabel, Switch, CircularProgress } from '@mui/material';
import { doc, getDoc, setDoc } from 'firebase/firestore';
import { db } from '../firebase/config';

export default function AdManager() {
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  
  const [ads, setAds] = useState({
    adsEnabled: true,
    adMobAppId: '',
    bannerAdUnitId: '',
    interstitialAdUnitId: '',
    nativeAdUnitId: '',
    rewardedAdUnitId: '',
  });

  useEffect(() => {
    const fetchAds = async () => {
      try {
        const docRef = doc(db, 'settings', 'ads');
        const docSnap = await getDoc(docRef);
        if (docSnap.exists()) {
          setAds(prev => ({ ...prev, ...docSnap.data() }));
        }
      } catch (error) {
        console.error("Error fetching ad settings:", error);
      } finally {
        setLoading(false);
      }
    };
    fetchAds();
  }, []);

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setAds({ ...ads, [name]: type === 'checkbox' ? checked : value });
  };

  const handleSave = async (e) => {
    e.preventDefault();
    setSaving(true);
    try {
      await setDoc(doc(db, 'settings', 'ads'), ads, { merge: true });
      alert("Advertisement Settings updated successfully!");
    } catch (error) {
      console.error("Error saving ad settings:", error);
      alert("Failed to update settings.");
    } finally {
      setSaving(false);
    }
  };

  if (loading) return <CircularProgress />;

  return (
    <Box>
      <Typography variant="h5" fontWeight="bold" gutterBottom>Advertisement Manager</Typography>
      
      <Card sx={{ mt: 3, maxWidth: 600 }}>
        <CardContent>
          <form onSubmit={handleSave}>
            <Grid container spacing={3}>
              
              <Grid item xs={12}>
                <Card variant="outlined" sx={{ bgcolor: ads.adsEnabled ? 'rgba(76, 175, 80, 0.1)' : 'rgba(244, 67, 54, 0.1)' }}>
                  <CardContent>
                    <FormControlLabel 
                      control={<Switch checked={ads.adsEnabled} onChange={handleChange} name="adsEnabled" color="success" />} 
                      label={
                        <Typography variant="h6" color={ads.adsEnabled ? "success.main" : "error.main"}>
                          {ads.adsEnabled ? "Global Ads are ENABLED" : "Global Ads are DISABLED"}
                        </Typography>
                      } 
                    />
                    <Typography variant="body2" color="text.secondary">
                      Toggle this switch to instantly turn all ads on or off across the entire PMTv Android app.
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>

              <Grid item xs={12}>
                <TextField fullWidth label="AdMob App ID" name="adMobAppId" value={ads.adMobAppId} onChange={handleChange} helperText="Required for initialization" />
              </Grid>

              <Grid item xs={12}>
                <TextField fullWidth label="Banner Ad Unit ID" name="bannerAdUnitId" value={ads.bannerAdUnitId} onChange={handleChange} />
              </Grid>
              
              <Grid item xs={12}>
                <TextField fullWidth label="Interstitial Ad Unit ID" name="interstitialAdUnitId" value={ads.interstitialAdUnitId} onChange={handleChange} />
              </Grid>

              <Grid item xs={12}>
                <TextField fullWidth label="Native Ad Unit ID" name="nativeAdUnitId" value={ads.nativeAdUnitId} onChange={handleChange} />
              </Grid>

              <Grid item xs={12}>
                <TextField fullWidth label="Rewarded Ad Unit ID" name="rewardedAdUnitId" value={ads.rewardedAdUnitId} onChange={handleChange} />
              </Grid>

              <Grid item xs={12}>
                <Button type="submit" variant="contained" size="large" disabled={saving}>
                  {saving ? 'Saving...' : 'Save Ad Settings'}
                </Button>
              </Grid>
            </Grid>
          </form>
        </CardContent>
      </Card>
    </Box>
  );
}
