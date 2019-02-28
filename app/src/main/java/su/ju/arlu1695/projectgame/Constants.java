package su.ju.arlu1695.projectgame;

import java.util.ArrayList;

public class Constants {

    public static int SCREEN_WIDTH;
    public static int SCREEN_HEIGHT;

    public static int SCREEN_WIDTH_COEFFICIENT;
    public static int SCREEN_HEIGHT_COEFFICIENT;

    public static int LEVEL_SELECTED;

    // http address for notifications request from backend.
    public static final String FIREBASE_CLOUD_FUNCTIONS_BASE = "https://us-central1-icy-slide.cloudfunctions.net";

    // Channel constants needed for Notification channel on API 26+
    public static final String CHANNEL_ID = "icy_slide";
    public static final String CHANNEL_NAME = "Icy Slide";
    public static final String CHANNEL_DESC = "Icy Slide Notifications";

    // Local tracking of current user. Used for e.g. sending notifications
    public static User thisUser = new User();

    public static boolean ALLOW_INVITES;
    public static boolean ALLOW_MUSIC;
    public static boolean ALLOW_SOUND;


    // Level select array list.
    public static ArrayList<levelName> levels = new ArrayList<>();
    static {
        levels.add(new levelName("Level 1"));
        levels.add(new levelName("Level 2"));
        levels.add(new levelName("Level 3"));
        levels.add(new levelName("Level 4"));
        levels.add(new levelName("Level 5"));
    }


    public static class levelName {
        public String title;
        public levelName(String title) { this.title = title; }
        @Override
        public String toString() {
            return title;
        }
    }


    public static void setDefaultSettings() {
        ALLOW_INVITES = true;
        ALLOW_MUSIC = true;
        ALLOW_SOUND  = true;
    }
}
