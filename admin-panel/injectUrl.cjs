const { initializeApp, cert } = require('firebase-admin/app');
const { getFirestore } = require('firebase-admin/firestore');
const serviceAccount = require('../pmtv5464-firebase-adminsdk-fbsvc-a6e70daf45.json');

try {
  initializeApp({
    credential: cert(serviceAccount)
  });
} catch (e) {
  // Already initialized
}

const db = getFirestore();

async function run() {
  await db.collection('settings').doc('livestream').set({
    rtmpUrl: 'rtmp://a1tvlive.online:1935/pmt/pmt',
    isActive: true,
    streamTitle: 'PMTv Live',
    streamDescription: 'Currently broadcasting live.',
    quality: 'Auto'
  }, { merge: true });
  console.log('Successfully injected hardcoded Android Stream URL into Firestore!');
}

run();
