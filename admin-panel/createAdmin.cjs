const { initializeApp, cert } = require('firebase-admin/app');
const { getAuth } = require('firebase-admin/auth');
const { getFirestore, FieldValue } = require('firebase-admin/firestore');
const serviceAccount = require('../pmtv5464-firebase-adminsdk-fbsvc-a6e70daf45.json');

initializeApp({
  credential: cert(serviceAccount)
});

const auth = getAuth();
const db = getFirestore();

async function createSuperAdmin() {
  const email = 'admin@pmtv.com';
  const password = 'SuperSecretPassword123!';
  const displayName = 'PMTv Super Admin';

  try {
    console.log(`Checking if user ${email} already exists...`);
    let userRecord;
    try {
      userRecord = await auth.getUserByEmail(email);
      console.log('User already exists! UID:', userRecord.uid);
    } catch (error) {
      if (error.code === 'auth/user-not-found') {
        console.log('User not found. Creating new admin user...');
        userRecord = await auth.createUser({
          email: email,
          password: password,
          displayName: displayName,
        });
        console.log('Successfully created new user:', userRecord.uid);
      } else {
        throw error;
      }
    }

    // Add user to the "admins" collection
    console.log('Adding user to "admins" collection in Firestore...');
    await db.collection('admins').doc(userRecord.uid).set({
      email: email,
      role: 'Super Admin',
      createdAt: FieldValue.serverTimestamp()
    });

    console.log('\n✅ Super Admin created successfully!');
    console.log('--------------------------------------------------');
    console.log(`Email: ${email}`);
    console.log(`Password: ${password}`);
    console.log(`Role: Super Admin`);
    console.log('--------------------------------------------------');

  } catch (error) {
    console.error('Error creating super admin:', error);
  } finally {
    process.exit(0);
  }
}

createSuperAdmin();
