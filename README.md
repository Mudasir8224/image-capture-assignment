# image-capture-assignment
# Running the App:
Open the project in Android Studio.
Sync the Gradle files.
Connect your Android device or start an emulator.
Build and run the app.

# Coroutines for Asynchronous Tasks
The app uses Kotlin Coroutines to handle asynchronous tasks efficiently without blocking the main thread. In this app, there are three primary tasks that are managed asynchronously using coroutines:

# Continuous Image Capture: 
Images are captured at a regular interval (e.g., 1 frame per second) using CameraX APIs, and this process runs in a coroutine.
# Image Upload: 
Each captured image is uploaded to the server using a non-blocking coroutine.
# Error Handling and Retries:
In case of network failures, retries are performed using coroutines with backoff strategies to handle failures gracefully.
