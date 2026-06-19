# 🤖 Service Chat Intelligent — Gestion des Incidents

Microservice de chatbot intelligent qui aide l'utilisateur à résoudre ses problèmes
**avant** de créer un ticket, en recherchant des incidents similaires déjà résolus.

---

## 📋 Fonctionnalités

- ✅ **Conversation guidée** — Le bot pose des questions pour comprendre le problème
- ✅ **Recherche automatique** — Cherche des incidents similaires dans la base
- ✅ **Suggestions de solutions** — Propose jusqu'à 3 solutions existantes
- ✅ **Feedback utilisateur** — Marque la conversation résolue si une solution fonctionne
- ✅ **Création automatique de ticket** — Si aucune solution ne fonctionne
- ✅ **WebSocket temps réel** — Chat instantané via STOMP/SockJS
- ✅ **REST API** — Endpoints HTTP pour l'intégration
- ✅ **Mode dégradé** — Fonctionne sans incident-service (suggestions de démo)

---

## 🗄️ Modèle de données

```
chat_conversations  → une session de chat par problème
chat_messages       → historique des échanges (USER / BOT)
suggestions         → incidents similaires proposés
```

---

## 🚀 Démarrage

### Prérequis
Créer la base PostgreSQL :
```sql
CREATE DATABASE chat_db;
```

### Lancer le service
```bash
# Avec Docker Compose
docker network create incidents-network
docker compose up -d

# En local
mvn spring-boot:run
```

---

## 📡 API REST — Base URL : `http://localhost:8085`

### 1. Message d'accueil du bot
```
GET /api/chat/accueil
```

### 2. Démarrer une conversation
```
POST /api/chat/demarrer
Content-Type: application/json

{
  "utilisateurId": "550e8400-e29b-41d4-a716-446655440001",
  "utilisateurNom": "Jean Dupont",
  "messageInitial": "Mon imprimante n'imprime plus"
}
```

**Réponse :**
```json
{
  "id": "...",
  "conversationId": "abc-123",
  "expediteur": "BOT",
  "message": "✅ J'ai trouvé 2 incidents similaires...",
  "suggestions": [...],
  "actionRequise": "FEEDBACK_SOLUTION"
}
```

### 3. Envoyer un message (répondre aux suggestions)
```
POST /api/chat/message
Content-Type: application/json

{
  "conversationId": "abc-123",
  "message": "oui, la solution 1 a fonctionné !"
}
```

### 4. Voir l'historique d'une conversation
```
GET /api/chat/conversation/{id}
```

### 5. Lister les conversations d'un utilisateur
```
GET /api/chat/conversations/{utilisateurId}
```

---

## 🔌 WebSocket — `ws://localhost:8085/ws/chat`

### Connexion STOMP
```javascript
const socket = new SockJS('http://localhost:8085/ws/chat');
const stompClient = Stomp.over(socket);

stompClient.connect({}, () => {
    // S'abonner aux réponses du bot
    stompClient.subscribe('/topic/chat/' + conversationId, (message) => {
        const reponse = JSON.parse(message.body);
        afficherMessage(reponse);
    });

    // Démarrer une conversation
    stompClient.send('/app/chat/demarrer', {}, JSON.stringify({
        utilisateurNom: 'Jean Dupont',
        messageInitial: 'Mon imprimante ne fonctionne plus'
    }));
});
```

---

## 🧠 Flux de conversation

```
User: "Mon imprimante n'imprime plus"
         ↓
Bot: Recherche incidents similaires...
         ↓
Bot: "✅ 2 solutions trouvées :
      1. Redémarrer le spooler
      2. Réinstaller les pilotes
      Voulez-vous essayer ?"
         ↓
User: "oui"  → Conversation RÉSOLUE ✅
User: "non"  → Ticket créé automatiquement 📋
```

---

## ⚙️ Configuration

| Variable | Défaut | Description |
|---|---|---|
| `DB_USERNAME` | `postgres` | Utilisateur PostgreSQL |
| `DB_PASSWORD` | `postgres` | Mot de passe PostgreSQL |
| `INCIDENT_SERVICE_URL` | `http://localhost:8082` | URL du service incident |
| `EUREKA_URI` | `http://localhost:8761/eureka/` | URL Eureka |

---

*Pr. BE ELBAGHAZAOUI — ENSA BM 2026*
