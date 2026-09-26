import { ExpoConfig } from "expo/config";

const IS_DEV = process.env.APP_VARIANT === "development";

const config: ExpoConfig = {
  name: IS_DEV ? "Tide Times (Dev)" : "Tide Times",
  slug: "tide-times",
  icon: "./assets/icon.png",
  ios: {
    bundleIdentifier: IS_DEV ? "com.example.tidetimes.dev" : "com.example.tidetimes",
  },
  android: {
    package: IS_DEV ? "com.example.tidetimes.dev" : "com.example.tidetimes",
  },
  plugins: [
    "expo-router",
    [
      "expo-location",
      { locationWhenInUsePermission: "Shows tides for the beach you are on." },
    ],
    "@sentry/react-native/expo",
  ],
};

export default config;
