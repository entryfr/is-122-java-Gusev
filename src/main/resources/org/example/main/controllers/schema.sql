-- Создание таблиц
CREATE TABLE IF NOT EXISTS SESSIONS (
    SESSION_ID VARCHAR(255) NOT NULL,
    USER_ID INTEGER NOT NULL,
    USERNAME TEXT NOT NULL,
    DATA BLOB SUB_TYPE TEXT,
    EXPIRY TIMESTAMP,
    PRIMARY KEY (SESSION_ID)
);
CREATE TABLE IF NOT EXISTS ADS (
    AD_ID INTEGER NOT NULL,
    USER_ID INTEGER NOT NULL,
    CATEGORY_ID INTEGER NOT NULL,
    TITLE TEXT NOT NULL,
    DESCRIPTION TEXT,
    PRICE REAL,
    LOCATION TEXT,
    PUBLICATION_DATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    EXPIRATION_DATE TIMESTAMP,
    STATUS TEXT DEFAULT 'active',
    IMAGE_PATH BLOB,
    PRIMARY KEY (AD_ID),
    FOREIGN KEY (USER_ID) REFERENCES USERS (USER_ID),
    FOREIGN KEY (CATEGORY_ID) REFERENCES CATEGORIES (CATEGORY_ID)
);

CREATE TABLE IF NOT EXISTS ALLCITY (
    REGION TEXT,
    MUN_DISTRICT TEXT,
    CITY TEXT
);

CREATE TABLE IF NOT EXISTS CATEGORIES (
    CATEGORY_ID INTEGER NOT NULL,
    CATEGORY_NAME TEXT NOT NULL,
    PARENT_CATEGORY_ID INTEGER,
    PRIMARY KEY (CATEGORY_ID),
    UNIQUE (CATEGORY_NAME),
    FOREIGN KEY (PARENT_CATEGORY_ID) REFERENCES CATEGORIES (CATEGORY_ID)
);

CREATE TABLE IF NOT EXISTS CHATS (
    CHAT_ID INTEGER NOT NULL,
    USER1_ID INTEGER NOT NULL,
    USER2_ID INTEGER NOT NULL,
    LAST_MESSAGE TEXT,
    LAST_MESSAGE_TIME TIMESTAMP,
    PRIMARY KEY (CHAT_ID)
);

CREATE TABLE IF NOT EXISTS MESSAGES (
    MESSAGE_ID TEXT NOT NULL,
    CHAT_ID INTEGER NOT NULL,
    SENDER_ID INTEGER NOT NULL,
    MESSAGE_TEXT TEXT,
    SENT_TIME TIMESTAMP,
    PRIMARY KEY (MESSAGE_ID)
);

CREATE TABLE IF NOT EXISTS PURCHASES (
    PURCHASE_ID INTEGER NOT NULL,
    USER_ID INTEGER,
    AD_ID INTEGER,
    PRICE REAL,
    PURCHASE_DATE TIMESTAMP,
    PRIMARY KEY (PURCHASE_ID),
    FOREIGN KEY (USER_ID) REFERENCES USERS (USER_ID),
    FOREIGN KEY (AD_ID) REFERENCES ADS (AD_ID)
);

CREATE TABLE IF NOT EXISTS SALES (
    SALE_ID INTEGER NOT NULL,
    USER_ID INTEGER,
    AD_ID INTEGER,
    PRICE REAL,
    SALE_DATE TIMESTAMP,
    PRIMARY KEY (SALE_ID),
    FOREIGN KEY (USER_ID) REFERENCES USERS (USER_ID),
    FOREIGN KEY (AD_ID) REFERENCES ADS (AD_ID)
);


CREATE TABLE IF NOT EXISTS USERS (
    USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
    USERNAME TEXT NOT NULL UNIQUE,
    EMAIL TEXT NOT NULL UNIQUE,
    PASSWORD_HASH TEXT NOT NULL,
    FIRST_NAME TEXT,
    LAST_NAME TEXT,
    PHONE TEXT,
    IS_ADMIN BOOLEAN DEFAULT FALSE,
    REGISTRATION_DATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    LAST_LOGIN TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_users_username ON USERS(USERNAME);
CREATE INDEX IF NOT EXISTS idx_users_email ON USERS(EMAIL);

CREATE TABLE IF NOT EXISTS USER_BASKET (
    BASKET_ID INTEGER NOT NULL,
    USER_ID INTEGER NOT NULL,
    AD_ID INTEGER NOT NULL,
    PRIMARY KEY (BASKET_ID),
    FOREIGN KEY (USER_ID) REFERENCES USERS (USER_ID),
    FOREIGN KEY (AD_ID) REFERENCES ADS (AD_ID)
);