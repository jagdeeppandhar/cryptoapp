# Ethereum Wallet App

## Overview
This Android application demonstrates how to generate Ethereum wallet credentials (public and private keys) securely on a mobile device using Kotlin and Android's KeyStore for encryption. 
It aims to provide a basic example of integrating cryptographic operations within an Android app.It integrates with Web3j to showcase basic interactions with the Ethereum blockchain.

## Features
- Generate Ethereum wallet credentials securely.
- Encrypt and store private keys using Android KeyStore.
- Display public keys.
- Interact with Ethereum blockchain using Web3j

## Requirements
- Android SDK 21 (Lollipop) or higher.
- Kotlin 1.5.x.
- Android Studio 4.x or higher.

## Installation
1. Clone the repository:
git clone  https://github.com/jagdeeppandhar/cryptoapp.git
2. Open the project in Android Studio.
3. Build and run the app on an Android device or emulator.

## Usage
1. Open the app.
2. The public key will be displayed on the screen.
3. The private key is securely encrypted and stored using Android KeyStore.
4. Use Web3j to interact with the Ethereum blockchain:

## Security Considerations
- **Android KeyStore**: Private keys are stored securely using Android's hardware-backed KeyStore.
- **Encryption**: AES encryption is used to encrypt private keys before storing them on the device.
- **Secure Storage**: For demonstration purposes, private keys are stored in SharedPreferences as encrypted data. Use more secure storage methods in production (e.g., EncryptedSharedPreferences, File Encryption).
