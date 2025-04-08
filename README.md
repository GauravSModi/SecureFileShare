# Secure File Sharing System

**End-to-End Encrypted File Storage with Dual-Server Architecture**  
A CLI-based system for secure file sharing, featuring AES-256-GCM encryption, RSA key management, and revocation support.

## Key Features
- 🔒 **Dual-Server Design**: Separates key management (`Keystore`) from file storage (`Datastore`).
- 🛡️ **End-to-End Encryption**: Files encrypted with AES-256-GCM; keys secured via RSA-2048.
- 🔑 **Passwordless Auth**: Users authenticate via public/private key pairs.
- 🔄 **Access Control**: Share/revoke files with granular permissions.

[//]: # (- 📦 **Chunked Uploads**: Supports large files via chunking.)

## Tech Stack
- **Language**: Java 17
- **Encryption**: `AES-256-GCM` (files), `RSA-OAEP` (keys), `HMAC-SHA256` (integrity)
- **Database**: SQLite (Keystore + Datastore) & file system (Datastore)
- **Networking**: Java Sockets (TCP)
