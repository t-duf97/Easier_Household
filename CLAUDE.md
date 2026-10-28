# CLAUDE.md — Easier_Household (back-end)

> Instructions pour Claude Code sur CE dépôt (API Spring Boot). **Lis ce fichier en entier avant d'agir.**
> Rédigé le 2026-10-27 par lecture directe du code (pas d'hypothèses). Le front est un dépôt séparé : `frontEasierHousehold` (voir son propre CLAUDE.md).
> Règle de collaboration : **concept d'abord, puis pas-à-pas**. Explique le raisonnement avant de coder, propose des diffs petits et lisibles, je valide et j'applique souvent moi-même. Communication en français informel. Critique honnête > validation.

---

## 1. Ce qui est VRAI du projet aujourd'hui

API REST de domotique. Back-end seul dans ce dépôt. Projet **perso**, pour le plaisir (plus dans une optique de défense).

**Stack réelle (lue dans `pom.xml`)**
- Spring Boot **4.0.6**, Java **25**, Maven (wrapper `./mvnw`).
- Dépendances : `spring-boot-starter-webmvc`, `spring-boot-starter-data-jpa`, `spring-boot-starter-security`, `jjwt` 0.12.6 (JWT), `springdoc-openapi` 3.0.2 (Swagger), `postgresql`, `lombok`, `devtools`.
- **PAS de Flyway. PAS de MQTT. PAS de Zigbee/Tuya.** Si un doc ou un souvenir le prétend, c'est faux.
- Schéma DB : `ddl-auto: update` + `src/main/resources/data.sql` (jouées à chaque démarrage via `sql.init.mode: always`). Pas de migrations versionnées.
- Config dans `src/main/resources/application.yaml` (gitignoré, non suivi par git) : datasource Postgres `easierhouseholddb`, `server.port: 8080`, `jwt.secret`. Modèle versionné : `application.example.yaml` (secrets en `${DB_USERNAME}`, `${DB_PASSWORD}`, `${JWT_SECRET}`).

**Lancer**
```bash
./mvnw spring-boot:run        # nécessite un PostgreSQL local sur 5432, base easierhouseholddb
./mvnw -q -DskipTests compile # compilation seule
./mvnw test                   # tests (Docker démarré requis : Testcontainers)
```
`mvnw` n'a pas le bit exécutable : `sh ./mvnw …` ou `chmod +x mvnw`.
Swagger : `http://localhost:8080/swagger-ui`. Le simulateur de capteurs ne tourne que sous le profil `dev` (`@Profile("dev")`).

**Architecture (en cours de normalisation en 3 couches + transversal)**
- `api` — `controllers`, `dtos/request`, `dtos/response`, `security` (JwtFilter, JwtService), `exceptions` (GlobalExceptionHandler)
- `bll` — `scheduler/CronSchedule` (expressions cron des triggers TIME), `services` (dont `SensorReadingService`, entrée unique des relevés de capteurs), `mappers`, `scheduler` (AutomationScheduler, SensorSimulatorScheduler), `events` (DeviceStatusChangedEvent, DeviceValueChangedEvent), `exceptions` (ResourceNotFound → 404, UnauthorizedAccess → 403, InvalidRequest → 400, InvalidCredentials → 401, ResourceAlreadyExists → 409, DeviceUnavailable → 503)
- `dl` — `entities`, `enums`, `repositories`, `converter` (ZoneIdConverter), `condition` (JpaSensorStateAdapter), `embeddable` (HouseholdMemberId)
- `domain/condition` — **couche métier pure** (ports/adapters) : `SensorState` (record), `SensorStatePort` (port), `SensorThresholdRule` (règle, zéro Spring/JPA). Câblée dans `config/ConditionConfig`. `SensorThresholdRule.satisfies/crosses` sont aussi utilisées pour les triggers SENSOR_THRESHOLD.
- `domain/sun` — `SunCalculator` (lever/coucher du soleil, équation NOAA, Java pur).
- `config` — SecurityConfig, ConditionConfig (CorsConfig est commenté/désactivé)

**Entités (modèle réel)**
- `BaseEntity` (abstraite) : `id` (IDENTITY), `createdAt`, `updatedAt` (via @PrePersist/@PreUpdate).
- `User` (table `app_user`) : firstName, lastName, email (unique), passwordHash.
- `Household` : name, `timeZone` (ZoneId via ZoneIdConverter), address, latitude/longitude (optionnelles, ensemble, requises pour SUNRISE/SUNSET), createdBy→User.
- `HouseholdMember` : clé composite `HouseholdMemberId` (@EmbeddedId, userId+householdId), role (`Role`), joinedAt. **N'étend pas BaseEntity.**
- `Room` : name (unique **par foyer** : contrainte `uk_room_household_name`), floor, roomType (`RoomType`), household→Household.
- `Device` (table `device`) : name, type (`DeviceType`), currentValue (String), isOnline, status (`DeviceStatus`), assignedRoom→Room (@JoinColumn `roomId`).
- `DeviceState` : status, value, changeSource (`ChangeSource`), changedAt, user→User, device→Device.
- `DeviceIntegration` : provider, device→Device (@OneToOne, sans cascade), externalDeviceId, credentialsRef (référence vers les identifiants, pas le secret), extraConfig. Relie un device de l'app à un appareil réel d'un écosystème externe. **Entité seule, sans controller ni service** : sera conçue avec la tâche 6.
- `Automation` : name, description, logic (`ConditionLogic` ALL/ANY), isEnabled, lastTriggeredAt, createdBy→User, household→Household, + listes `triggers` / `conditions` / `actions` (cascade ALL, orphanRemoval).
- `AutomationTrigger` : type (`TriggerType`), timeValue, daysOfWeek (String), status, thresholdValue, thresholdOperator, cronExpression, device. Champs requis par type validés à la création (`AutomationService.validateTrigger`) : DEVICE_STATE_CHANGE → device+status ; TIME → timeValue **ou** cronExpression ; SENSOR_THRESHOLD → device+thresholdValue+thresholdOperator ; SUNRISE/SUNSET → foyer avec position.
- `AutomationCondition` : type (`ConditionType`), status, thresholdValue, thresholdOperator, timeFrom/timeTo, daysOfWeek, durationInMinutes, device.
- `AutomationAction` : executionOrder, type (`ActionType`), status, targetValue, notificationMsg, delayInSeconds, device.

**Enums** : `ActionType` (SET_DEVICE_STATUS, SET_DEVICE_VALUE, SEND_NOTIFICATION, WAIT), `ChangeSource` (MANUAL, AUTOMATION, SCHEDULED, EXTERNAL_API), `ConditionLogic` (ALL, ANY), `ConditionType` (DEVICE_STATUS, SENSOR_VALUE, TIME_RANGE, DAY_OF_WEEK, NO_MOTION_SINCE), `DeviceStatus` (ON, OFF, UNAVAILABLE, STANDBY), `DeviceType` (LIGHT, THERMOSTAT, SENSOR_TEMP, SENSOR_MOTION, SPEAKER, VACUUM, SWITCH, OTHER), `Role` (OWNER, RESIDENT, GUEST), `RoomType` (LIVING_ROOM, BEDROOM, KITCHEN, BATHROOM, GARAGE, BASEMENT, TOILET, ATTIC, OTHER), `ThresholdOperator` (GT, GTE, LT, LTE, EQ), `TriggerType` (DEVICE_STATE_CHANGE, SENSOR_THRESHOLD, SUNRISE, SUNSET, TIME).

**Endpoints réellement exposés**
- `POST /auth/register`, `POST /auth/login` → `AuthResponse` (JWT). Routes publiques.
- Households (`/households`) : `GET /{id}`, `GET` (mes foyers), `POST`, `PUT /{id}`, `DELETE /{id}`.
- Rooms : `GET /rooms/{id}`, `PUT /rooms/{id}`, `DELETE /rooms/{id}`, `POST /households/{householdId}/rooms`, `GET /households/{householdId}/rooms`.
- Devices : `GET /devices/{id}`, `GET /rooms/{roomId}/devices`, `POST /rooms/{roomId}/devices`, `PATCH /devices/{id}`, `DELETE /devices/{id}`, `PATCH /devices/{id}/state`, `GET /devices/{id}/history` (pagination **`Page<DeviceStateResponse>`** via `?page=&pageSize=`).
- Automations : `POST /households/{householdId}/automations`, `GET /households/{householdId}/automations`, `GET /automations/{id}`, `PATCH /automations/{id}/toggle`, **`PUT /automations/{id}` (existe déjà, implémenté)**, `DELETE /automations/{id}`.

**Moteur d'automatisations** (`bll/scheduler/AutomationScheduler`)
- Réactif : `DeviceService.changeState` publie `DeviceStatusChangedEvent` (seulement si le statut change) ; `onDeviceStatusChanged` l'écoute en `@TransactionalEventListener(AFTER_COMMIT)`.
- Horaire : `evaluateTimeBasedAutomations()` en `@Scheduled(cron = "0 * * * * *")`. Y passent : TIME par `timeValue` (minute courante, heure serveur), TIME par `cronExpression` (`CronSchedule`, 5 ou 6 champs, `daysOfWeek` ignoré), SUNRISE/SUNSET (`SunCalculator`, calculés **dans le fuseau du foyer**). Surcharge `evaluateTimeBasedAutomations(LocalDateTime)` pour les tests.
- Seuil : `DeviceValueChangedEvent` (relevé de température, PATCH state avec valeur) → triggers SENSOR_THRESHOLD déclenchés **au franchissement** du seuil uniquement (`SensorThresholdRule.crosses`).
- Chaque automatisation s'exécute dans **sa propre transaction** (`TransactionTemplate` REQUIRES_NEW) : un échec n'annule ni le changement déclencheur ni les autres automatisations.
- Les actions d'une automatisation ne republient pas d'événement (pas d'enchaînement, donc pas de boucle).
- Conditions évaluées : DEVICE_STATUS, SENSOR_VALUE (délègue à `SensorThresholdRule` du domaine), TIME_RANGE, DAY_OF_WEEK, NO_MOTION_SINCE.
- Actions : SET_DEVICE_STATUS, SET_DEVICE_VALUE, SEND_NOTIFICATION (log), WAIT (Thread.sleep).
- Capteurs : `SensorReadingService.recordTemperature` écrit `currentValue` au format numérique (`"21.4"`) ; `recordMotion` passe le détecteur ON/OFF et publie `DeviceStatusChangedEvent` (un mouvement déclenche les automatisations). La `ChangeSource` est un paramètre.
- `SensorSimulatorScheduler` (`@Profile("dev")`) ne fait que tirer des valeurs : marche aléatoire ±0,5 °C bornée 18–25 °C (`simulator.temperature.rate`, PT5M), détection 30 % (`simulator.motion.rate`, PT1M).
- Capteurs = **simulés**. `ChangeSource.EXTERNAL_API` n'est utilisé que par les tests : la vraie API capteurs n'aura qu'à appeler `SensorReadingService` (le port `SensorStatePort` reste le point de lecture du domaine).

**Tests existants** : `api/*ApiTest` (HTTP de bout en bout via MockMvc, vraie chaîne JWT, JSON tel que le front le reçoit : `AuthApiTest`, `HouseholdApiTest`, `DeviceApiTest`, base commune `ApiIntegrationTest`). `dl/repositories/AutomationQueriesTest` (`@DataJpaTest` des requêtes JPQL custom). `SensorThresholdRuleTest` (4 cas, pur, sans Spring — bon exemple du pattern). `AutomationEngineIntegrationTest` (moteur de bout en bout), `SensorReadingIntegrationTest`, `AutomationServiceIntegrationTest`, `HouseholdDeletionIntegrationTest`, `DeviceDeletionIntegrationTest`, `RoomNameUniquenessIntegrationTest`, `AccessControlIntegrationTest` : tous sur PostgreSQL Testcontainers (config dans `TestcontainersConfiguration`). `SensorSimulatorSchedulerTest`, `CronScheduleTest`, `SunCalculatorTest` (purs). `EasierHouseholdApplicationTests` (contexte, sur la base locale). Dépendances de test dispo : `webmvc-test`, `data-jpa-test`, `spring-security-test`.

---

## 2. Conventions & pièges (à respecter)

- **Jamais exposer une entité JPA** : toujours via DTO. Les Response enrichissent avec des champs lisibles (`roomName`, `householdName` à côté des ids) — cf. `DeviceMapper`.
- **Mappers** = conversion pure entité↔DTO, **sans injection de repository**. Le service orchestre repos + mappers (ex. `AutomationService.resolveDevice` résout les devices et vérifie qu'ils appartiennent au foyer, puis les passe aux mappers).
- **Toujours vérifier l'appartenance au foyer** avant d'accéder à une ressource : `householdMemberRepository.findByHouseholdIdAndUserId(...)` (cf. `AutomationService.checkMembership`). `checkOwnership` existe pour les actions réservées au OWNER.
- `getCurrentUserId()` vient de `BaseController` (principal = `User` dans le SecurityContext). Les controllers métier l'étendent.
- Spring Data : les noms de méthodes suivent les **champs Java**, underscore pour les relations (`findByAssignedRoom_Id`).
- **Suppressions** : supprimer un device (ou une pièce, ou un foyer) supprime **les automatisations entières** qui le référencent, jamais seulement le trigger/condition/action concerné (retirer une condition rendrait l'automatisation moins restrictive). Passer par `DeviceService.deleteWithReferences`.
- Enums persistés en **`@Enumerated(EnumType.STRING)`** partout — garder cette règle (cf. le bug `ChangeSource`).
- **Piège récurrent (noms de champs Spring ↔ TS)** : vérifier le JSON réel (onglet Network / Swagger) avant de modéliser côté Angular. Déjà vu : `status` vs `deviceStatus`, `timeZone` vs `timezone`, `type` vs `deviceType`/`triggerType`.
- `ddl-auto: update` ne renomme jamais une colonne ; après un gros refactor d'entité, un `create-drop` temporaire remet à zéro.
- Déplacements de packages : via le refactor IntelliJ (préserve les imports), pas l'explorateur de fichiers.
- CORS autorisé pour `localhost:4200` et `localhost:4300` dans `SecurityConfig`.
- **Codes HTTP** : 400 corps invalide (validation, JSON illisible, enum inconnue, règle métier) ; **401 token absent/invalide ou identifiants refusés** (même message pour email inconnu et mauvais mot de passe) ; 403 non-membre ou action réservée au OWNER ; 404 ressource absente ; 409 doublon (email, nom de pièce). Jamais de 500 pour une erreur du client : un nouveau cas → une exception `bll/exceptions` + son handler.
- **JSON côté front** : le booléen `Device.isOnline` est exposé en **`online`** ; `PATCH /devices/{id}/state` n'accepte plus de `source` (toujours `MANUAL`).

---

## 3. À réparer (rendre parfait l'existant)

### Bloquant / correction avant tout
- [x] **Stabiliser et committer le refactor en cours** : `./mvnw -q -DskipTests compile` doit passer, l'app doit démarrer, puis commit atomique. Tant que ce n'est pas fait, le dépôt n'est pas dans un état sain.
- [x] **`AutomationScheduler.executeAction` / cas `SET_DEVICE_VALUE`** : `deviceStateRepository.findById(action.getDevice().getId())` confond l'id du device avec un id de DeviceState → ne journalise pas (ou corrompt) l'état. Créer un **nouveau** `DeviceState` comme dans `SET_DEVICE_STATUS`.
- [x] **LazyInitialization sur le cron** : `evaluateTimeBasedAutomations()` (@Scheduled) n'est pas `@Transactional` mais accède aux collections LAZY `getConditions()/getActions()/getTriggers()` → risque d'exception à l'exécution. Ajouter `@Transactional` (ou fetch-join dans les queries).
- [x] **`DeviceState.changeSource` sans `@Enumerated(EnumType.STRING)`** → persisté en ORDINAL (incohérent, casse si l'ordre de l'enum change). Ajouter l'annotation. (Fait ; SQL de migration d'une base existante dans le message du commit.)
- [x] **`DELETE /households/{id}` → 500** (`TransientPropertyValueException` sur `HouseholdMember`) : les membres ne sont pas supprimés avec le foyer.

### Important
- [x] **Sécurité : aucun contrôle d'appartenance sur la plupart des routes devices.** `DeviceService.findById`, `findByRoomId`, `create`, `update`, `changeState`, `getHistory` et `RoomService.findById` ne reçoivent pas l'utilisateur : n'importe quel compte connecté peut lire, modifier ou piloter les devices de n'importe quel foyer (seul `DELETE /devices/{id}` vérifie l'appartenance). Passer le `userId` et vérifier comme dans `AutomationService.checkMembership`.
- [x] **`SensorSimulatorScheduler.simulateTemperatureSensors()`** : le nom dit « température » mais le code simule du **mouvement** (ON/OFF) sur les `SENSOR_TEMP`, et n'écrit jamais de `currentValue` numérique. Résultat : les conditions `SENSOR_VALUE` (qui lisent `Device.currentValue` comme un nombre) n'ont rien à comparer. Décider : produire une vraie valeur numérique pour `SENSOR_TEMP`, et un simulateur de mouvement distinct pour `SENSOR_MOTION`.
- [x] **`DeviceService.changeState`** appelle `evaluateDeviceStateAutomations(id, request.getStatus())` avec un `status` possiblement `null` (quand seule la value change). Garder un status non-null ou court-circuiter l'appel.
- [x] **`Room.name` `unique = true` global** : empêche deux foyers d'avoir une pièce du même nom. Rendre l'unicité relative au `household` (contrainte composite) ou la retirer.
- [x] **`DeviceState.device` en `cascade = CascadeType.ALL`** sur un `@ManyToOne` : supprimer un état cascade vers le device. Retirer la cascade.
- [ ] **`WAIT` fait un `Thread.sleep`** dans le thread appelant (requête HTTP pour le réactif, thread du scheduler pour le cron) : bloque tout le reste. À passer en exécution différée. → planifié avec les Scènes (section 6).
- [ ] **Heure serveur** : le cron et `TIME_RANGE` utilisent `LocalTime.now()` et ignorent `Household.timeZone`. (SUNRISE/SUNSET utilisent déjà le fuseau du foyer.) `TIME_RANGE` exclut aussi ses bornes (`isAfter`/`isBefore`). → planifié avec les notifications (section 6).
- [x] **`JwtFilter` logue le header `Authorization` complet** (`System.out.println`) : fuite du token dans les logs. Supprimer.
- [x] **Mappers qui injectent un repository** (`AutomationTriggerMapper`, `AutomationActionMapper`), contraire à la convention ; la réponse d'action renvoie `deviceName`/`automationId` à `null`.
- [x] **Supprimer un device ou une pièce encore référencés** : `DeviceService.deleteDevice` ne retire pas les triggers/conditions/actions qui visent le device, et `RoomService.deleteRoom` ne supprime pas les devices de la pièce → violation de clé étrangère (500). Décidé : on supprime les automatisations qui les référencent.
- [x] **Swagger** : `/v3/api-docs` (chemin par défaut de springdoc) n'est pas dans les `permitAll` de `SecurityConfig` (seul `/api-docs/**` l'est).
- [x] **`DeviceIntegrationController` vide** : supprimé avec son service et son DTO (décision du 2026-10-28, cf. tâche 6). La cascade ALL de l'intégration vers le device est retirée.
- [x] **Secrets en clair dans `application.yaml`** : le fichier n'est plus suivi par git (il l'était malgré le `.gitignore`) ; `application.example.yaml` versionné utilise des variables d'environnement. Choix assumé : valeurs en clair dans le yaml local, variables d'environnement le jour d'un déploiement.

### Finitions
- [ ] Incohérence de nommage de colonne : `Device.assignedRoom @JoinColumn(name="roomId")` en camelCase alors que le reste est snake_case (`household_id`, `user_id`).
- [ ] Renommer `ErrorReponse` → `ErrorResponse` (faute de frappe propagée au nom de classe).
- [ ] `DeviceMapper.FromRequestToEntity` → `fromRequestToEntity` (majuscule initiale non conventionnelle).
- [ ] Imports morts (ex. `@Scheduled` dans `AutomationTrigger`), `CorsConfig` entièrement commenté : nettoyer.
- [ ] Style entités : `@Getter/@Setter` dispersés champ par champ → envisageable au niveau classe pour alléger (optionnel).

---

## 4. À faire ensuite (un prompt = une tâche)

> Faire ces tâches dans l'ordre. Chacune est pensée pour une session Claude Code autonome. Toujours valider au Postman/Swagger avant de considérer une tâche terminée.

1. ✅ **Clôturer le refactor.** Vérifier compilation + démarrage, parcourir les fichiers modifiés pour cohérence des imports/packages, puis proposer un (ou quelques) commit(s) atomique(s) bien nommés. Ne rien ajouter de neuf.
2. ✅ **Fiabiliser le moteur d'automatisations.** Corriger les 3 bugs bloquants ci-dessus (SET_DEVICE_VALUE, transaction du cron, status null), puis ajouter des tests : au moins un test d'intégration qui crée une automation TIME et une DEVICE_STATE_CHANGE et vérifie l'exécution.
2bis. ✅ **Corrections rapides.** DELETE household, cascade DeviceState, JWT dans les logs, Swagger, mappers sans repository (+ contrôle d'appartenance des devices), conditions sans device.
3. ✅ **Rendre le simulateur de capteurs cohérent.** Valeurs numériques réalistes pour `SENSOR_TEMP` (ex. 18–25 °C écrites dans `currentValue`), simulateur de mouvement séparé pour `SENSOR_MOTION`, `ChangeSource.SCHEDULED`. Objectif : que les conditions `SENSOR_VALUE` deviennent testables de bout en bout.
4. ➡️ **Décider le sort de `DeviceIntegration`.** Décidé le 2026-10-28 : pas de CRUD sans consommateur ; entité conservée, controller/service supprimés, conception **fusionnée dans la tâche 6**.
5. ✅ **Tests d'intégration des endpoints.** (Fait en `@SpringBootTest` + MockMvc de bout en bout plutôt qu'en `@WebMvcTest` avec services simulés, pour tester la vraie sécurité et le vrai JSON.) Couvrir auth (register/login + accès protégé), households et devices avec `@WebMvcTest`/`spring-security-test` et un `@DataJpaTest` pour les queries custom d'`AutomationRepository`.
6. **(Plus tard) Brancher une vraie source de capteurs.** L'archi hexagonale est déjà prête : écrire un nouvel adaptateur de `SensorStatePort` (REST polling ou webhook) à la place/à côté de `JpaSensorStateAdapter`, taguer les états ingérés `ChangeSource.EXTERNAL_API`, sécuriser par clé de service (pas le JWT user), idempotence, garde-fou anti-boucle. C'est le « lien API capteurs » — à ne faire qu'une fois 1–5 au vert.
   - **Inclut `DeviceIntegration`** (ex-tâche 4), dans les deux sens : *lecture* (relevés réels → `SensorReadingService` avec `ChangeSource.EXTERNAL_API`) et *commande* (aujourd'hui une action `SET_DEVICE_STATUS` ne change que la base : il faudra un port `DeviceCommandPort` + un adaptateur par fournisseur, appelé par le moteur et par `PATCH /devices/{id}/state`).
   - **Premier point de la tâche : discuter avec l'utilisateur du choix des appareils / fournisseurs** (Hue, Shelly, Tuya, Home Assistant…). Aucun n'est choisi au 2026-10-28, et la forme de `DeviceIntegration` (appairage, credentials, config) en dépend.

---

## 5. Hors de ce dépôt
Front Angular : dépôt séparé `frontEasierHousehold` (Angular 21, PrimeNG 21, thème chaleureux `EasierHouseholdPreset`). Voir son CLAUDE.md. Base API attendue par le front : `http://localhost:8080`.

---

## 6. Évolutions voulues (plus tard, après assainissement)

> Décidées le 2026-10-27. Projet perso, pour le plaisir — pas de deadline de jury. À n'attaquer qu'une fois la section 3 (À réparer) et les points 1–5 de « À faire ensuite » au vert.

- **Notifications intelligentes.** Donner un vrai canal à `ActionType.SEND_NOTIFICATION` (aujourd'hui un simple `log.info`) : entité `Notification` (historique, lu/non-lu, gravité), livraison (in-app SSE/WebSocket, e-mail ou web push), préférences par utilisateur + heures silencieuses (s'appuyer sur `Household.timeZone` et les `timeFrom/timeTo`), anti-spam via un cooldown par automatisation (`lastTriggeredAt` existe déjà). À faire avec le fuseau horaire : cron et `TIME_RANGE` calculés dans `Household.timeZone` (et bornes de `TIME_RANGE` incluses).
- **Scènes / modes** (« Absent », « Nuit », « Cinéma »…). Nouvelle entité `Scene` + liste d'actions, qui réutilise l'exécution d'actions du moteur existant ; endpoint pour déclencher une scène d'un coup. À faire avec la reprise de `WAIT` (aujourd'hui `Thread.sleep` bloquant) : exécution différée des actions restantes via un `TaskScheduler`, que les scènes réutiliseront.
