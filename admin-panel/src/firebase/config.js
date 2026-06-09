import { initializeApp } from "firebase/app";
import { getAuth } from "firebase/auth";
import { getFirestore } from "firebase/firestore";
import { getStorage } from "firebase/storage";
import { getDatabase } from "firebase/database";

const firebaseConfig = {
    apiKey: "AIzaSyCqdmwgbYiPV-ZIxVoK2BMZoQkjn5q4frk",
    authDomain: "pmtv5464.firebaseapp.com",
    projectId: "pmtv5464",
    storageBucket: "pmtv5464.firebasestorage.app",
    messagingSenderId: "950553095405",
    appId: "1:950553095405:web:564d3fd4f13add055db0e4",
    measurementId: "G-64JQ9KR1NC",
    databaseURL: "https://pmtv5464-default-rtdb.firebaseio.com"
};

const app = initializeApp(firebaseConfig);
export const auth = getAuth(app);
export const db = getFirestore(app);
export const storage = getStorage(app);
export const rtdb = getDatabase(app);
