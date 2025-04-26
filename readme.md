
<img src="https://github.com/user-attachments/assets/9081bcfc-538e-4263-be43-10850603046f" width="200" />

# MomentReely - A Photo Sharing Application

It is a photo-sharing application designed to foster personal connections through visual storytelling. It enables users to create accounts, connect with friends, and share photos seamlessly within the platform. 

## Purpose
The resoning behind this project was not to create a photo sharing application, but rather learn AWS Cloud & Kotlin Mobile Application Development.

## Tech-Stack

### Front-End (Mobile Application)
- Kotlin
  - State Management - _MVVM_ Architecture.
- Jetpack Compose
- Retrofit & OkHttp

### Back-End
- Firebase
  - Firebase Authentication
  - Firebase Cloud Messaging (FCM)
- AWS Cloud <br>
  A two tier architecture with various AWS Services was leveraged for this section.
- Flask Application

## AWS Architecture
![CloudArch](https://github.com/user-attachments/assets/cad371f8-82a7-4dd2-89d8-22f5b661441d)
*AWS Architecture*

### Data Storage
- **S3 Bucket** <br> For storing user images, logs and Lambda code.
- **DynamoDB** <br> For user Data storage.
### Compute
- **Lambda** <br> For severless execution of user push notifications.
- **EC2 Instance** <br> Runs containerised Flask Application as backend in a private subnet.
- **Application Load Balancer** <br> Acts as a reverse proxy for the EC2 instance.
### Content Delivery Network
- **Amazon Simple Notification Service (SNS)** <br> For user push notifications (implicitly uses FCM)


## Security
- **Security Groups** <br> They ensure only cetrain protocol traffic on certain ports.
- **Load Balancer Proxy** <br> Prevents direct access to backend.
- **Firebase Authorization Headers** <br> All REST APIs need to be sent from authenticated users.

## Application Demo

https://github.com/user-attachments/assets/b6bab3ea-9880-4dc5-b794-69100c6ef362

