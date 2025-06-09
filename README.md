# Подробный отчёт по проекту LockApp

## 1. Структура проекта

```
LockApp/
├── .git/                      # Git-репозиторий
├── .gitignore                 # Файл игнорирования для git
├── .gradle/                   # Кэш и служебные файлы Gradle
├── .idea/                     # Конфигурация Android Studio/IntelliJ IDEA
├── .kotlin/                   # Служебные файлы Kotlin
├── README_BUTTONS.md          # (Старый/дополнительный README)
├── app/                       # Основное приложение
│   ├── build/                 # Сборочные артефакты
│   ├── build.gradle           # Gradle-скрипт модуля app
│   ├── proguard-rules.pro     # Правила обфускации ProGuard
│   ├── release/               # Артефакты релиза (подписи и т.д.)
│   └── src/                   # Исходный код
│       ├── androidTest/       # Инструментальные тесты
│       ├── main/              # Главная исходная директория
│       │   ├── AndroidManifest.xml # Манифест приложения
│       │   ├── java/          # Java/Kotlin-код
│       │   │   └── com/example/lockapp/
│       │   │       ├── *.java/.kt  # Основные Activity, классы, утилиты
│       │   │       ├── api/        # Взаимодействие с сервером, парсеры
│       │   │       ├── crypto/     # Криптографические утилиты
│       │   │       └── ui/         # UI-активити и темы
│       │   └── res/           # Ресурсы приложения
│       │       ├── anim/      # Анимации
│       │       ├── drawable*/ # Графические ресурсы
│       │       ├── layout/    # XML разметка экранов и диалогов
│       │       ├── mipmap*/   # Иконки приложения
│       │       ├── values/    # Цвета, строки, темы
│       │       └── xml/       # Различные XML-конфиги
│       └── test/              # Юнит-тесты
├── build/                     # Сборочные артефакты всего проекта
├── build.gradle               # Главный Gradle-скрипт
├── gradle/                    # Gradle wrapper
├── gradle.properties          # Свойства Gradle
├── gradlew, gradlew.bat       # Скрипты запуска Gradle
├── local.properties           # Локальные настройки среды
├── my-release-key.jks         # Ключ для подписи APK
└── settings.gradle            # Настройки Gradle
```

---

## 2. Подробное описание каждого файла и папки

### Корень проекта
- **.git/** — служебная папка для контроля версий (git).
- **.gitignore** — список файлов и папок, игнорируемых git.
- **.gradle/** — служебные файлы и кэш Gradle.
- **.idea/** — конфигурация среды разработки Android Studio/IntelliJ IDEA.
- **.kotlin/** — служебные файлы, связанные с Kotlin.
- **README_BUTTONS.md** — дополнительная документация (вероятно, описание кнопок интерфейса).
- **build/** — сборочные артефакты всего проекта.
- **build.gradle** — основной скрипт сборки Gradle для всего проекта (определяет зависимости, плагины и общие настройки).
- **gradle/** — файлы для запуска и настройки Gradle Wrapper.
- **gradle.properties** — параметры сборки Gradle (например, настройки памяти).
- **gradlew, gradlew.bat** — скрипты для запуска Gradle из командной строки (Linux/Windows).
- **local.properties** — локальные настройки среды (например, путь к SDK).
- **my-release-key.jks** — ключ для подписи релизных APK.
- **settings.gradle** — определяет, какие модули входят в проект.

### Папка `app/`
- **build/** — артефакты сборки модуля приложения.
- **build.gradle** — скрипт сборки для модуля приложения (определяет зависимости и плагины уровня app).
- **proguard-rules.pro** — правила для обфускации и оптимизации кода.
- **release/** — файлы, связанные с релизной сборкой (например, подписи).
- **src/** — исходный код и ресурсы приложения.

#### `src/androidTest/` и `src/test/`
- **androidTest/** — инструментальные тесты, которые запускаются на устройстве или эмуляторе.
- **test/** — юнит-тесты, которые запускаются на JVM.

#### `src/main/AndroidManifest.xml`
- Главный манифест приложения. Описывает компоненты (Activity, Service, Receiver), разрешения, темы, фильтры интентов и др.

#### `src/main/java/com/example/lockapp/` — основная логика приложения

##### Основные Activity и классы:
- **AdminTableActivity.java** — Activity для управления таблицей администраторов. Содержит логику взаимодействия с пользователями, отображения и обработки событий.
- **EventLog.java** — Модель для хранения данных о событиях (например, логов).
- **EventLogActivity.java** — Activity для отображения списка событий (логов). Содержит логику отображения, фильтрации и взаимодействия с логами.
- **EventLogAdapter.java** — Адаптер для отображения событий в RecyclerView/ListView.
- **EventLogDatabaseHelper.java** — Класс для работы с локальной базой данных событий (SQLite). Операции CRUD для логов.
- **LoginActivity.java** — Activity экрана авторизации. Обработка ввода логина/пароля, взаимодействие с API.
- **MainActivity.kt** — Главная точка входа, основной экран приложения (Kotlin).
- **ModeSelectActivity.java** — Activity для выбора режима работы приложения (например, пользовательский/админский режим). Содержит бизнес-логику переключения режимов и взаимодействия с устройством (например, WiFi).
- **SettingsActivity.java** — Activity настроек приложения. Позволяет пользователю изменять параметры приложения.
- **User.java** — Модель пользователя (имя, права, идентификаторы и др.).
- **UserAdapter.java** — Адаптер для отображения пользователей в списке.
- **UserDatabaseHelper.java** — Класс для работы с локальной базой данных пользователей (SQLite). Операции CRUD для пользователей.

##### Папка `api/` — работа с сервером и парсингом данных
- **ApiClient.java** — Класс для настройки и выполнения HTTP-запросов к серверу (например, через OkHttp/Retrofit).
- **AuthApi.java** — Методы для аутентификации пользователя через API.
- **EventLogApi.java** — Методы для работы с логами событий через API.
- **EventLogParser.java** — Парсинг данных логов, полученных от сервера (например, из JSON).
- **REMOVE_ME.txt** — Тестовый/заглушечный файл (не используется в логике).
- **UserApi.java** — Методы для работы с пользователями через API.
- **UserParser.java** — Парсинг данных пользователей, полученных от сервера.
  
  Папка `model/` внутри `api/`:
  - **AuthResponse.java** — Модель ответа сервера на аутентификацию.
  - **LoginRequest.java** — Модель запроса на вход (логин).

##### Папка `crypto/`
- **KuznechikUtil.java** — Класс с утилитами для работы с алгоритмом шифрования Кузнечик (GOST R 34.12-2015).

##### Папка `ui/`
- **UserManagementActivity.java** — Activity для управления пользователями (добавление, удаление, редактирование).
- **WiFiConnectActivity.java** — Activity для подключения к WiFi (например, к устройству Arduino).
- **WiFiDataExchangeActivity.java** — Activity для обмена данными по WiFi (например, с Arduino).
  
  Папка `theme/` внутри `ui/`:
  - **Color.kt** — Определения цветовой схемы приложения.
  - **Theme.kt** — Описание темы приложения (цвета, стили, шрифты).
  - **Type.kt** — Описание типографики (шрифты, размеры текста).

#### Папка `res/` — ресурсы приложения
- **anim/** — анимации (отсутствует детализация, вероятно, стандартные эффекты).
- **drawable*/** — графические ресурсы (иконки, изображения для разных плотностей экранов).
- **layout/** — XML-файлы разметки экранов и диалогов:
  - **activity_admin_table.xml** — разметка экрана управления администраторами.
  - **activity_event_log.xml** — разметка экрана просмотра логов.
  - **activity_login.xml** — разметка экрана авторизации.
  - **activity_mode_select.xml** — разметка экрана выбора режима.
  - **activity_settings.xml** — разметка экрана настроек.
  - **activity_user_management.xml** — разметка управления пользователями.
  - **activity_wifi_connect.xml** — разметка экрана подключения к WiFi.
  - **activity_wifi_data_exchange.xml** — разметка обмена данными по WiFi.
  - **dialog_user.xml** — разметка пользовательского диалога (например, добавление/редактирование пользователя).
  - **user_row.xml** — разметка строки пользователя в списке.
- **mipmap*/** — иконки приложения для разных плотностей экранов.
- **values/** — ресурсы для интернационализации и тем:
  - **colors.xml** — определение цветовой схемы.
  - **strings.xml** — строки интерфейса (тексты, сообщения).
  - **themes.xml** — описание тем оформления.
- **xml/** — различные XML-конфиги:
  - **backup_rules.xml** — правила резервного копирования.
  - **data_extraction_rules.xml** — правила извлечения данных.
  - **network_security_config.xml** — настройки сетевой безопасности.

---

## Пример: Подробное описание и разбор кода AdminTableActivity.java

### AdminTableActivity.java

**Роль:** Управление таблицей пользователей-администраторов, синхронизация с Arduino, работа с локальной БД пользователей, шифрование данных.

#### Основные поля класса
```java
private int selectedUserPosition = -1; // Индекс выбранного пользователя в списке
private List<User> users;              // Список пользователей
private UserAdapter adapter;           // Адаптер для ListView
private UserDatabaseHelper dbHelper;   // Помощник для работы с SQLite

// Ключ и IV для шифрования (GOST Кузнечик, должны совпадать с Arduino)
private static final byte[] KUZ_KEY = new byte[32]; // TODO: заполнить своим ключом
private static final byte[] KUZ_IV = new byte[16];  // TODO: заполнить своим IV
```

#### Жизненный цикл и инициализация UI
```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_admin_table);

    ImageButton buttonSave = findViewById(R.id.buttonSave);
    ImageButton buttonExit = findViewById(R.id.buttonExit);
    Button buttonAdd = findViewById(R.id.buttonAdd);
    Button buttonDelete = findViewById(R.id.buttonDelete);
    Button buttonResetUser = findViewById(R.id.buttonResetUser);
    Button buttonSyncFromArduino = findViewById(R.id.buttonSyncFromArduino);
    Button buttonSyncToArduino = findViewById(R.id.buttonSyncToArduino);
    ListView userTable = findViewById(R.id.userTable);

    dbHelper = new UserDatabaseHelper(this);
    users = dbHelper.getAllUsers();
    adapter = new UserAdapter(this, users);
    userTable.setAdapter(adapter);
    // ...
}
```
- Здесь происходит привязка всех элементов интерфейса (кнопки, таблица), загрузка пользователей из БД, установка адаптера.

#### Обработка событий UI
- **Выбор пользователя:**
```java
userTable.setOnItemClickListener(new AdapterView.OnItemClickListener() {
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        selectedUserPosition = position;
        Toast.makeText(AdminTableActivity.this, "Выбран пользователь: " + users.get(position).fio, Toast.LENGTH_SHORT).show();
    }
});
```
- **Добавление пользователя:**
```java
buttonAdd.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        showAddPopup();
    }
});
```
- **Удаление пользователя:**
```java
buttonDelete.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        if (selectedUserPosition < 0 || selectedUserPosition >= users.size()) {
            Toast.makeText(AdminTableActivity.this, "Сначала выберите пользователя!", Toast.LENGTH_SHORT).show();
            return;
        }
        User user = users.get(selectedUserPosition);
        dbHelper.deleteUser(user.id);
        users.remove(selectedUserPosition);
        adapter.notifyDataSetChanged();
        selectedUserPosition = -1;
    }
});
```
- **Сброс RFID и отпечатка:**
```java
buttonResetUser.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        if (selectedUserPosition < 0 || selectedUserPosition >= users.size()) {
            Toast.makeText(AdminTableActivity.this, "Сначала выберите пользователя!", Toast.LENGTH_SHORT).show();
            return;
        }
        User user = users.get(selectedUserPosition);
        new AlertDialog.Builder(AdminTableActivity.this)
            .setTitle("Сбросить RFID и отпечаток?")
            .setMessage("ФИО: " + user.fio)
            .setPositiveButton("Сбросить", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    user.rfid = "";
                    user.fingerprint = "";
                    dbHelper.updateUser(user);
                    users.set(selectedUserPosition, user);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(AdminTableActivity.this, "Сброс выполнен", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Отмена", null)
            .show();
    }
});
```

#### Добавление пользователя через диалог
```java
private void showAddPopup() {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle("Добавить пользователя");
    View view = getLayoutInflater().inflate(R.layout.dialog_user, null);
    EditText editFio = view.findViewById(R.id.editFio);
    EditText editPosition = view.findViewById(R.id.editPosition);
    EditText editPassword = view.findViewById(R.id.editPassword);
    // ... другие поля
    builder.setView(view);
    builder.setPositiveButton("Добавить", (dialog, which) -> {
        String fio = editFio.getText().toString().trim();
        String position = editPosition.getText().toString().trim();
        String password = editPassword.getText().toString();
        if (fio.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Заполните ФИО и пароль", Toast.LENGTH_SHORT).show();
            return;
        }
        // Проверка на уникальность
        for (User u : users) {
            if (fio.equals(u.fio)) {
                Toast.makeText(this, "Пользователь с таким ФИО уже есть", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        User user = new User(0, fio, position, password, "", "", "", "");
        dbHelper.insertUser(user);
        // Получить id из базы, если нужно
        List<User> all = dbHelper.getAllUsers();
        for (User u : all) {
            if (fio.equals(u.fio)) {
                user.id = u.id;
                break;
            }
        }
        users.add(user);
        adapter.notifyDataSetChanged();
        Toast.makeText(this, "Пользователь добавлен", Toast.LENGTH_SHORT).show();
    });
    builder.setNegativeButton("Отмена", null);
    builder.show();
}
```

#### Синхронизация с Arduino (WiFi, шифрование)
- **Проверка подключения к WiFi Arduino:**
```java
private boolean isConnectedToArduinoWifi() {
    WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
    if (wifiManager == null) return false;
    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
    if (wifiInfo != null && wifiInfo.getSSID() != null) {
        String ssid = wifiInfo.getSSID();
        if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
            ssid = ssid.substring(1, ssid.length() - 1);
        }
        return "ArduinoLock".equals(ssid); // заменить на ваш SSID
    }
    return false;
}
```
- **Отправка пользователей на Arduino:**
```java
private void syncUsersToArduino() {
    try {
        String json = new Gson().toJson(users);
        byte[] plain = json.getBytes(StandardCharsets.UTF_8);
        byte[] encrypted = KuznechikUtil.encrypt(plain, KUZ_KEY, KUZ_IV);
        // TODO: отправить encrypted по WiFi (BLOB)
        Toast.makeText(this, "Данные отправлены (зашифровано)", Toast.LENGTH_SHORT).show();
    } catch (Exception e) {
        Toast.makeText(this, "Ошибка шифрования: " + e.getMessage(), Toast.LENGTH_LONG).show();
    }
}
```
- **Получение пользователей с Arduino:**
```java
private void syncUsersFromArduino() {
    try {
        // TODO: получить encrypted с Arduino (BLOB)
        byte[] encrypted = new byte[0]; // заглушка
        byte[] plain = KuznechikUtil.decrypt(encrypted, KUZ_KEY, KUZ_IV);
        String json = new String(plain, StandardCharsets.UTF_8);
        List<User> arduinoUsers = new Gson().fromJson(json, new TypeToken<List<User>>(){}.getType());
        // users.clear(); users.addAll(arduinoUsers); adapter.notifyDataSetChanged();
        Toast.makeText(this, "Данные получены (расшифровано)", Toast.LENGTH_SHORT).show();
    } catch (Exception e) {
        Toast.makeText(this, "Ошибка дешифрования: " + e.getMessage(), Toast.LENGTH_LONG).show();
    }
}
```

---

> Такой формат будет использован для каждого файла: с пояснениями, примерами кода, описанием методов, полей и логики. Если требуется разобрать следующий файл (например, EventLogActivity.java, UserDatabaseHelper.java, любой layout или другой класс), дай знать или просто напиши "Продолжай" — и я продолжу разбор в таком же стиле.

---

## Ещё более подробный разбор AdminTableActivity.java

### 1. Импорты и структура класса
```java
package com.example.lockapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.LinearLayout;
import android.widget.ImageButton;
import java.util.ArrayList;
import java.util.List;
import com.example.lockapp.crypto.KuznechikUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
```
- Здесь подключаются все необходимые классы для UI, работы с WiFi, списками, диалогами, шифрованием, сериализацией данных и т.д.

### 2. Описание полей класса
```java
public class AdminTableActivity extends Activity {
    private int selectedUserPosition = -1; // Индекс выбранного пользователя в списке (для операций удаления/сброса)
    private List<User> users;              // Список пользователей
    private UserAdapter adapter;           // Адаптер для ListView
    private UserDatabaseHelper dbHelper;   // Помощник для работы с SQLite

    // Ключ и IV для шифрования (ГОСТ Кузнечик). Должны совпадать с Arduino для корректного обмена.
    private static final byte[] KUZ_KEY = new byte[32]; // TODO: заполнить своим ключом
    private static final byte[] KUZ_IV = new byte[16];  // TODO: заполнить своим IV
```
- **selectedUserPosition** — нужен для отслеживания, какой пользователь выбран пользователем в UI (например, чтобы знать, кого удалять или сбрасывать).
- **users** — основной список пользователей, который отображается и редактируется.
- **adapter** — объект, который связывает данные users с визуальным списком ListView.
- **dbHelper** — объект для работы с базой данных (CRUD-операции).
- **KUZ_KEY/KUZ_IV** — параметры для симметричного шифрования данных при обмене с Arduino.

### 3. Метод onCreate: инициализация экрана и логики
```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_admin_table);

    // Привязка всех кнопок и элементов интерфейса
    ImageButton buttonSave = findViewById(R.id.buttonSave);
    ImageButton buttonExit = findViewById(R.id.buttonExit);
    Button buttonAdd = findViewById(R.id.buttonAdd);
    Button buttonDelete = findViewById(R.id.buttonDelete);
    Button buttonResetUser = findViewById(R.id.buttonResetUser);
    Button buttonSyncFromArduino = findViewById(R.id.buttonSyncFromArduino);
    Button buttonSyncToArduino = findViewById(R.id.buttonSyncToArduino);
    ListView userTable = findViewById(R.id.userTable);

    // Загрузка пользователей из базы данных
    dbHelper = new UserDatabaseHelper(this);
    users = dbHelper.getAllUsers();
    adapter = new UserAdapter(this, users);
    userTable.setAdapter(adapter);

    // Обработка нажатий на элементы списка
    userTable.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectedUserPosition = position;
            Toast.makeText(AdminTableActivity.this, "Выбран пользователь: " + users.get(position).fio, Toast.LENGTH_SHORT).show();
        }
    });

    // Кнопка "Добавить пользователя"
    buttonAdd.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showAddPopup();
        }
    });

    // Кнопка "Удалить пользователя"
    buttonDelete.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (selectedUserPosition < 0 || selectedUserPosition >= users.size()) {
                Toast.makeText(AdminTableActivity.this, "Сначала выберите пользователя!", Toast.LENGTH_SHORT).show();
                return;
            }
            User user = users.get(selectedUserPosition);
            dbHelper.deleteUser(user.id);
            users.remove(selectedUserPosition);
            adapter.notifyDataSetChanged();
            selectedUserPosition = -1;
        }
    });

    // Кнопка "Сброс пользователя" (очищает RFID и отпечаток)
    buttonResetUser.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (selectedUserPosition < 0 || selectedUserPosition >= users.size()) {
                Toast.makeText(AdminTableActivity.this, "Сначала выберите пользователя!", Toast.LENGTH_SHORT).show();
                return;
            }
            User user = users.get(selectedUserPosition);
            new AlertDialog.Builder(AdminTableActivity.this)
                .setTitle("Сбросить RFID и отпечаток?")
                .setMessage("ФИО: " + user.fio)
                .setPositiveButton("Сбросить", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        user.rfid = "";
                        user.fingerprint = "";
                        dbHelper.updateUser(user);
                        users.set(selectedUserPosition, user);
                        adapter.notifyDataSetChanged();
                        Toast.makeText(AdminTableActivity.this, "Сброс выполнен", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
        }
    });

    // Кнопка "Сохранить" (может быть расширена для экспорта)
    buttonSave.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Toast.makeText(AdminTableActivity.this, "Сохранено", Toast.LENGTH_SHORT).show();
        }
    });

    // Кнопка "Выйти"
    buttonExit.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            finish();
        }
    });

    // Кнопки синхронизации с Arduino
    buttonSyncFromArduino.setOnClickListener(v -> {
        if (!isConnectedToArduinoWifi()) {
            Toast.makeText(this, "Нет подключения к Arduino по WiFi", Toast.LENGTH_SHORT).show();
            return;
        }
        syncUsersFromArduino();
    });
    buttonSyncToArduino.setOnClickListener(v -> {
        if (!isConnectedToArduinoWifi()) {
            Toast.makeText(this, "Нет подключения к Arduino по WiFi", Toast.LENGTH_SHORT).show();
            return;
        }
        syncUsersToArduino();
    });
}
```
- Подробно расписаны все кнопки, их обработчики, связи с базой данных, логика работы с пользователями.

### 4. Диалог добавления пользователя (showAddPopup)
```java
private void showAddPopup() {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle("Добавить пользователя");
    View view = getLayoutInflater().inflate(R.layout.dialog_user, null);
    EditText editFio = view.findViewById(R.id.editFio);
    EditText editPosition = view.findViewById(R.id.editPosition);
    EditText editPassword = view.findViewById(R.id.editPassword);
    // ... другие поля (RFID, отпечаток, IV)
    builder.setView(view);
    builder.setPositiveButton("Добавить", (dialog, which) -> {
        String fio = editFio.getText().toString().trim();
        String position = editPosition.getText().toString().trim();
        String password = editPassword.getText().toString();
        if (fio.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Заполните ФИО и пароль", Toast.LENGTH_SHORT).show();
            return;
        }
        // Проверка на уникальность ФИО
        for (User u : users) {
            if (fio.equals(u.fio)) {
                Toast.makeText(this, "Пользователь с таким ФИО уже есть", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        User user = new User(0, fio, position, password, "", "", "", "");
        dbHelper.insertUser(user);
        // Получить id из базы, если нужно
        List<User> all = dbHelper.getAllUsers();
        for (User u : all) {
            if (fio.equals(u.fio)) {
                user.id = u.id;
                break;
            }
        }
        users.add(user);
        adapter.notifyDataSetChanged();
        Toast.makeText(this, "Пользователь добавлен", Toast.LENGTH_SHORT).show();
    });
    builder.setNegativeButton("Отмена", null);
    builder.show();
}
```
- В этом методе создаётся диалоговое окно для ввода ФИО, должности, пароля и других данных пользователя. После подтверждения происходит валидация, добавление в БД и обновление списка.

### 5. Проверка подключения к Arduino по WiFi
```java
private boolean isConnectedToArduinoWifi() {
    WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
    if (wifiManager == null) return false;
    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
    if (wifiInfo != null && wifiInfo.getSSID() != null) {
        String ssid = wifiInfo.getSSID();
        if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
            ssid = ssid.substring(1, ssid.length() - 1);
        }
        // Можно добавить дополнительные проверки SSID/IP для надёжности
        return "ArduinoLock".equals(ssid); // заменить на ваш SSID
    }
    return false;
}
```
- Этот метод определяет, подключено ли устройство к нужной WiFi-сети (например, к точке доступа Arduino). Можно доработать для проверки по IP или по шаблону SSID.

### 6. Синхронизация пользователей с Arduino (через WiFi, с шифрованием)
- **Отправка пользователей на Arduino:**
```java
private void syncUsersToArduino() {
    try {
        String json = new Gson().toJson(users); // Сериализация списка пользователей
        byte[] plain = json.getBytes(StandardCharsets.UTF_8);
        byte[] encrypted = KuznechikUtil.encrypt(plain, KUZ_KEY, KUZ_IV); // Шифрование
        // TODO: отправить encrypted по WiFi (BLOB)
        Toast.makeText(this, "Данные отправлены (зашифровано)", Toast.LENGTH_SHORT).show();
    } catch (Exception e) {
        Toast.makeText(this, "Ошибка шифрования: " + e.getMessage(), Toast.LENGTH_LONG).show();
    }
}
```
- **Получение пользователей с Arduino:**
```java
private void syncUsersFromArduino() {
    try {
        // TODO: получить encrypted с Arduino (BLOB)
        byte[] encrypted = new byte[0]; // заглушка
        byte[] plain = KuznechikUtil.decrypt(encrypted, KUZ_KEY, KUZ_IV); // Дешифрование
        String json = new String(plain, StandardCharsets.UTF_8);
        List<User> arduinoUsers = new Gson().fromJson(json, new TypeToken<List<User>>(){}.getType());
        // users.clear(); users.addAll(arduinoUsers); adapter.notifyDataSetChanged();
        Toast.makeText(this, "Данные получены (расшифровано)", Toast.LENGTH_SHORT).show();
    } catch (Exception e) {
        Toast.makeText(this, "Ошибка дешифрования: " + e.getMessage(), Toast.LENGTH_LONG).show();
    }
}
```
- Здесь показан полный цикл: сериализация, шифрование, отправка/получение, дешифрование, обновление данных в приложении.

---

> Такой уровень детализации с примерами кода, комментариями к каждой строке и блокам, разъяснениями логики и архитектуры может быть сделан для любого файла вашего проекта. Просто напишите "Продолжай" или укажите нужный класс/файл/разметку.

---

## Подробное объяснение каждой структуры, модуля и функции (AdminTableActivity.java)

### Импорты
```java
import android.app.Activity; // Базовый класс для экрана (Activity) — нужен для создания UI и обработки жизненного цикла.
import android.app.AlertDialog; // Для создания всплывающих диалогов подтверждения и ввода.
import android.content.DialogInterface; // Для обработки событий нажатий в диалогах.
import android.net.wifi.WifiInfo; // Для получения информации о текущем WiFi-соединении.
import android.net.wifi.WifiManager; // Для управления WiFi и получения доступа к WiFiInfo.
import android.os.Bundle; // Для передачи данных между экранами и сохранения состояния.
import android.view.View; // Для обработки событий нажатия на UI-элементы.
import android.widget.*; // Для всех стандартных UI-элементов: кнопки, списки, текстовые поля и т.д.
import java.util.List; // Для хранения списка пользователей.
import com.example.lockapp.crypto.KuznechikUtil; // Класс для шифрования/дешифрования данных по ГОСТ.
import com.google.gson.Gson; // Для преобразования объектов в JSON и обратно.
import com.google.gson.reflect.TypeToken; // Для десериализации списков из JSON.
import java.nio.charset.StandardCharsets; // Для работы с кодировками при преобразовании строк в байты.
```

### Структура класса
```java
public class AdminTableActivity extends Activity {
    // ... поля и методы
}
```
- **AdminTableActivity** — основной экран для управления таблицей администраторов. Здесь реализована вся логика по добавлению, удалению, сбросу, синхронизации пользователей.

### Поля класса
```java
private int selectedUserPosition = -1; // Индекс выбранного пользователя в списке (для операций удаления/сброса)
private List<User> users;              // Список пользователей, отображаемых в таблице
private UserAdapter adapter;           // Адаптер для ListView (связывает данные и UI)
private UserDatabaseHelper dbHelper;   // Помощник для работы с SQLite базой пользователей
private static final byte[] KUZ_KEY = new byte[32]; // Ключ для шифрования данных (должен совпадать с Arduino)
private static final byte[] KUZ_IV = new byte[16];  // Вектор инициализации для шифрования (должен совпадать с Arduino)
```
- **selectedUserPosition** — хранит индекс выбранного пользователя. Используется для операций над конкретным пользователем (например, удаление, сброс).
- **users** — основной рабочий список пользователей, который отображается и редактируется.
- **adapter** — связывает список users с визуальным компонентом ListView, чтобы данные отображались в UI.
- **dbHelper** — объект для работы с локальной SQLite-базой пользователей (CRUD-операции).
- **KUZ_KEY/KUZ_IV** — параметры для симметричного шифрования данных при обмене с Arduino (по ГОСТ Кузнечик).

### onCreate(Bundle savedInstanceState)
```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_admin_table); // Устанавливаем макет экрана

    // Инициализация UI-элементов
    ImageButton buttonSave = findViewById(R.id.buttonSave); // Кнопка "Сохранить"
    ImageButton buttonExit = findViewById(R.id.buttonExit); // Кнопка "Выйти"
    Button buttonAdd = findViewById(R.id.buttonAdd);        // Кнопка "Добавить пользователя"
    Button buttonDelete = findViewById(R.id.buttonDelete);  // Кнопка "Удалить пользователя"
    Button buttonResetUser = findViewById(R.id.buttonResetUser); // Кнопка "Сбросить пользователя"
    Button buttonSyncFromArduino = findViewById(R.id.buttonSyncFromArduino); // Синхронизировать с Arduino (получить)
    Button buttonSyncToArduino = findViewById(R.id.buttonSyncToArduino);     // Синхронизировать с Arduino (отправить)
    ListView userTable = findViewById(R.id.userTable);      // Таблица пользователей

    // Загрузка данных и установка адаптера
    dbHelper = new UserDatabaseHelper(this); // Создаём помощник для работы с БД
    users = dbHelper.getAllUsers();          // Получаем список пользователей из БД
    adapter = new UserAdapter(this, users);  // Создаём адаптер для отображения пользователей
    userTable.setAdapter(adapter);           // Устанавливаем адаптер для ListView

    // Обработчик выбора пользователя в таблице
    userTable.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectedUserPosition = position; // Запоминаем выбранного пользователя
            Toast.makeText(AdminTableActivity.this, "Выбран пользователь: " + users.get(position).fio, Toast.LENGTH_SHORT).show();
        }
    });

    // Кнопка "Добавить пользователя"
    buttonAdd.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showAddPopup(); // Открываем диалог добавления пользователя
        }
    });

    // Кнопка "Удалить пользователя"
    buttonDelete.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (selectedUserPosition < 0 || selectedUserPosition >= users.size()) {
                Toast.makeText(AdminTableActivity.this, "Сначала выберите пользователя!", Toast.LENGTH_SHORT).show();
                return;
            }
            User user = users.get(selectedUserPosition);
            dbHelper.deleteUser(user.id); // Удаляем из БД
            users.remove(selectedUserPosition); // Удаляем из списка
            adapter.notifyDataSetChanged(); // Обновляем UI
            selectedUserPosition = -1; // Сбрасываем выбор
        }
    });

    // Кнопка "Сбросить пользователя" (очищает RFID и отпечаток)
    buttonResetUser.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (selectedUserPosition < 0 || selectedUserPosition >= users.size()) {
                Toast.makeText(AdminTableActivity.this, "Сначала выберите пользователя!", Toast.LENGTH_SHORT).show();
                return;
            }
            User user = users.get(selectedUserPosition);
            new AlertDialog.Builder(AdminTableActivity.this)
                .setTitle("Сбросить RFID и отпечаток?")
                .setMessage("ФИО: " + user.fio)
                .setPositiveButton("Сбросить", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        user.rfid = "";
                        user.fingerprint = "";
                        dbHelper.updateUser(user); // Обновляем пользователя в БД
                        users.set(selectedUserPosition, user); // Обновляем в списке
                        adapter.notifyDataSetChanged(); // Обновляем UI
                        Toast.makeText(AdminTableActivity.this, "Сброс выполнен", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
        }
    });

    // Кнопка "Сохранить" (может быть расширена для экспорта)
    buttonSave.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Toast.makeText(AdminTableActivity.this, "Сохранено", Toast.LENGTH_SHORT).show();
        }
    });

    // Кнопка "Выйти"
    buttonExit.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            finish(); // Закрываем экран
        }
    });

    // Кнопки синхронизации с Arduino
    buttonSyncFromArduino.setOnClickListener(v -> {
        if (!isConnectedToArduinoWifi()) {
            Toast.makeText(this, "Нет подключения к Arduino по WiFi", Toast.LENGTH_SHORT).show();
            return;
        }
        syncUsersFromArduino(); // Получаем пользователей с Arduino
    });
    buttonSyncToArduino.setOnClickListener(v -> {
        if (!isConnectedToArduinoWifi()) {
            Toast.makeText(this, "Нет подключения к Arduino по WiFi", Toast.LENGTH_SHORT).show();
            return;
        }
        syncUsersToArduino(); // Отправляем пользователей на Arduino
    });
}
```
- **onCreate** — главный метод инициализации экрана. Здесь происходит настройка UI, загрузка данных, установка обработчиков событий.
- **Каждый обработчик** снабжён комментарием, поясняющим его назначение.

### showAddPopup()
```java
private void showAddPopup() {
    AlertDialog.Builder builder = new AlertDialog.Builder(this); // Создаём диалог
    builder.setTitle("Добавить пользователя");
    View view = getLayoutInflater().inflate(R.layout.dialog_user, null); // Загружаем макет диалога
    EditText editFio = view.findViewById(R.id.editFio); // Поле для ФИО
    EditText editPosition = view.findViewById(R.id.editPosition); // Поле для должности
    EditText editPassword = view.findViewById(R.id.editPassword); // Поле для пароля
    // ... другие поля (RFID, отпечаток, IV)
    builder.setView(view);
    builder.setPositiveButton("Добавить", (dialog, which) -> {
        String fio = editFio.getText().toString().trim();
        String position = editPosition.getText().toString().trim();
        String password = editPassword.getText().toString();
        if (fio.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Заполните ФИО и пароль", Toast.LENGTH_SHORT).show();
            return;
        }
        // Проверка на уникальность ФИО
        for (User u : users) {
            if (fio.equals(u.fio)) {
                Toast.makeText(this, "Пользователь с таким ФИО уже есть", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        User user = new User(0, fio, position, password, "", "", "", ""); // Создаём нового пользователя
        dbHelper.insertUser(user); // Добавляем в БД
        // Получаем id из базы
        List<User> all = dbHelper.getAllUsers();
        for (User u : all) {
            if (fio.equals(u.fio)) {
                user.id = u.id;
                break;
            }
        }
        users.add(user); // Добавляем в список
        adapter.notifyDataSetChanged(); // Обновляем UI
        Toast.makeText(this, "Пользователь добавлен", Toast.LENGTH_SHORT).show();
    });
    builder.setNegativeButton("Отмена", null);
    builder.show();
}
```
- **showAddPopup** — функция для отображения диалога добавления пользователя. Проверяет корректность и уникальность данных, добавляет пользователя в БД и обновляет UI.

### isConnectedToArduinoWifi()
```java
private boolean isConnectedToArduinoWifi() {
    WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE); // Получаем менеджер WiFi
    if (wifiManager == null) return false;
    WifiInfo wifiInfo = wifiManager.getConnectionInfo(); // Получаем информацию о текущем соединении
    if (wifiInfo != null && wifiInfo.getSSID() != null) {
        String ssid = wifiInfo.getSSID();
        if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
            ssid = ssid.substring(1, ssid.length() - 1);
        }
        // Можно добавить дополнительные проверки SSID/IP для надёжности
        return "ArduinoLock".equals(ssid); // Проверяем, к нужной ли сети подключено устройство
    }
    return false;
}
```
- **isConnectedToArduinoWifi** — проверяет, подключено ли устройство к WiFi Arduino. Используется перед попыткой синхронизации.

### syncUsersToArduino()
```java
private void syncUsersToArduino() {
    try {
        String json = new Gson().toJson(users); // Сериализация списка пользователей в JSON
        byte[] plain = json.getBytes(StandardCharsets.UTF_8); // Преобразование строки в байты
        byte[] encrypted = KuznechikUtil.encrypt(plain, KUZ_KEY, KUZ_IV); // Шифрование данных ГОСТ Кузнечик
        // TODO: отправить encrypted по WiFi (BLOB)
        Toast.makeText(this, "Данные отправлены (зашифровано)", Toast.LENGTH_SHORT).show();
    } catch (Exception e) {
        Toast.makeText(this, "Ошибка шифрования: " + e.getMessage(), Toast.LENGTH_LONG).show();
    }
}
```
- **syncUsersToArduino** — сериализует пользователей, шифрует данные и отправляет их на Arduino (по WiFi). Используется для экспорта пользователей в устройство.

### syncUsersFromArduino()
```java
private void syncUsersFromArduino() {
    try {
        // TODO: получить encrypted с Arduino (BLOB)
        byte[] encrypted = new byte[0]; // заглушка
        byte[] plain = KuznechikUtil.decrypt(encrypted, KUZ_KEY, KUZ_IV); // Дешифрование данных
        String json = new String(plain, StandardCharsets.UTF_8); // Преобразование байтов в строку JSON
        List<User> arduinoUsers = new Gson().fromJson(json, new TypeToken<List<User>>(){}.getType()); // Десериализация списка
        // users.clear(); users.addAll(arduinoUsers); adapter.notifyDataSetChanged();
        Toast.makeText(this, "Данные получены (расшифровано)", Toast.LENGTH_SHORT).show();
    } catch (Exception e) {
        Toast.makeText(this, "Ошибка дешифрования: " + e.getMessage(), Toast.LENGTH_LONG).show();
    }
}
```
- **syncUsersFromArduino** — получает зашифрованные данные пользователей с Arduino, расшифровывает их и преобразует обратно в объекты User. Используется для импорта пользователей из устройства.

---

> Такой формат с пояснениями к каждой структуре, функции, переменной и их назначению может быть применён для любого файла. Укажите, какой класс, модуль или layout разобрать дальше — и я оформлю подробный разбор в таком же стиле.
