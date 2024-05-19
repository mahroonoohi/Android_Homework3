# Android_Homework3

Internet_Logger:
make sure notification inside setting of an app is on and Allowed 


If you want to see internet_status.txt go to :
*Open Android Studio.
*Connect your device to the computer.
*Go to View > Tool Windows > Device File Explorer.
*Navigate to /data/data/<your_app_package_name>/files/.
Here you should find the internet_status.txt file.
"/data/data/<your_app_package_name>/files/internet_status.txt"


or Using ADB (Android Debug Bridge):  In terminal type :"adb shell "run-as <your_app_package_name> cat files/internet_status.txt" > internet_status.txt"
