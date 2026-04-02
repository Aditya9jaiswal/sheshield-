const functions = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();

// Firebase Realtime Database Trigger
exports.sendEmergencyNotification = functions.database
    .ref("/emergencies/{emergencyId}")
    .onCreate(async (snapshot, context) => {
        const emergency = snapshot.val();
        const userId = emergency.userId;

        // Get family members
        const familySnap = await admin.database().ref(`/users/${userId}/family`).once("value");
        const family = familySnap.val();

        if (!family) return null;

        const promises = [];

        for (const memberId in family) {
            const token = family[memberId].fcmToken;
            if (token) {
                const message = {
                    token: token,
                    notification: {
                        title: "Emergency Alert!",
                        body: "Your family member is in danger. Open SheShield app."
                    },
                    data: {
                        latitude: emergency.latitude.toString(),
                        longitude: emergency.longitude.toString(),
                        videoUrl: emergency.videoUrl
                    }
                };
                promises.push(admin.messaging().send(message));
            }
        }

        await Promise.all(promises);

        // Mark familyAlerted = true
        return snapshot.ref.child("familyAlerted").set(true);
    });
