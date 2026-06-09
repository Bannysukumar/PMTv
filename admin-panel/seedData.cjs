const admin = require('firebase-admin');
const serviceAccount = require('../pmtv5464-firebase-adminsdk-fbsvc-a6e70daf45.json');

if (!admin.apps.length) {
  admin.initializeApp({
    credential: admin.credential.cert(serviceAccount)
  });
}

const db = admin.firestore();

async function seedData() {
  console.log("Seeding dummy community posts...");

  const posts = [
    {
      userId: "user123",
      userName: "Rahul Sharma",
      userProfileImage: "https://i.pravatar.cc/150?u=rahul",
      content: "This is a great news channel! Loving the live updates.",
      isApproved: true,
      timestamp: admin.firestore.FieldValue.serverTimestamp()
    },
    {
      userId: "user456",
      userName: "Priya Patel",
      userProfileImage: "https://i.pravatar.cc/150?u=priya",
      content: "Can you guys cover the local elections more?",
      isApproved: false,
      timestamp: admin.firestore.FieldValue.serverTimestamp()
    },
    {
      userId: "user789",
      userName: "Spam Bot",
      userProfileImage: "https://i.pravatar.cc/150?u=bot",
      content: "Click here to win a free iPhone! http://spam-link.com",
      isApproved: false,
      timestamp: admin.firestore.FieldValue.serverTimestamp()
    }
  ];

  for (const post of posts) {
    await db.collection('community_posts').add(post);
  }

  console.log("Successfully added 3 dummy community posts!");
}

seedData().catch(console.error);
