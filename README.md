# 📱 Android-Based Industrial Object Counting

This project is a mobile image processing system developed to **automatically count industrial materials** such as timber and pallets. It offers a **modular, low-cost, and portable** solution powered by AI.

---

## 🧠 How It Works

1. 📸 **Take or select an image** via the Android app  
2. 🔘 **Choose object type** (Timber or Pallet)  
3. ☁️ The image is sent to a **Raspberry Pi server** over the internet (via DuckDNS)  
4. 🕓 **FastAPI backend** adds it to a **RabbitMQ queue**  
5. 🤖 Images are processed with **Roboflow object detection models**  
6. ✅ **Results and marked images** are returned to the app  
7. 📂 User history is saved with image, object type, date, and result

---

## 🔐 Authentication

- 🔑 JSON Web Tokens (JWT) + OAuth2  
- 🔒 Passwords hashed securely with **bcrypt + Passlib**  
- 📦 All user data stored in a **PostgreSQL** database on Raspberry Pi

---

## 💡 Technologies Used

| Component        | Technology                  |
|------------------|-----------------------------|
| 📱 Mobile App    | Android (Kotlin + Jetpack Compose) |
| ☁️ Backend       | FastAPI (Python)            |
| 📩 Queue         | RabbitMQ                    |
| 🧠 AI Detection  | Roboflow                    |
| 💾 Database      | PostgreSQL                  |
| 🌍 Remote Access | DuckDNS + Port Forwarding   |
| 🔐 Auth          | JWT + OAuth2 + Bcrypt       |

---

## 📸 Screenshots

<p align="center">
  <img src="https://github.com/user-attachments/assets/334ec701-73cb-41c7-a33d-0fc870beb137" width="220"/>
  <img src="https://github.com/user-attachments/assets/5b4d1423-46a5-408e-a232-614380be0cb8" width="220"/>
  <img src="https://github.com/user-attachments/assets/c5241c26-f3a1-460a-9651-1cf08cbadc5a" width="220"/>
</p>
<p align="center">
  <img src="https://github.com/user-attachments/assets/7dbbbe0f-b694-43fb-bd6c-b5d44f9af7aa" width="220"/>
  <img src="https://github.com/user-attachments/assets/d87bc3b6-a571-413b-af90-abc374957fab" width="220"/>
  <img src="https://github.com/user-attachments/assets/d7f16290-1404-492b-870e-eb3a4bf5ba21" width="220"/>
  <img src="https://github.com/user-attachments/assets/ef82a50f-3d7a-47fe-8f53-6c7b0ff40f14" width="220"/>
</p>

---

## 🚀 Features

- Easy-to-use Android interface  
- Live object counting with AI  
- Offline-friendly edge-server setup with Raspberry Pi  
- Secure login/register system  
- Full user history tracking  

---

## 📁 Repository Structure


