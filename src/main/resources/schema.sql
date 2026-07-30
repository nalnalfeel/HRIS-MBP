-- Tabel Karyawan Utama
CREATE TABLE IF NOT EXISTS employees (
                                         id INTEGER PRIMARY KEY AUTOINCREMENT,
                                         employee_id TEXT,
                                         nik TEXT UNIQUE NOT NULL,
                                         family_card_number TEXT,
                                         full_name TEXT NOT NULL,
                                         project_name TEXT,
                                         birth_place TEXT,
                                         birth_date TEXT,
                                         address TEXT,
                                         phone_number TEXT,
                                         gender TEXT CHECK(gender IN ('Laki-laki', 'Perempuan')),
                                         education TEXT,
                                         bank_account TEXT,
                                         bpjs_tk TEXT,
                                         bpjs_kesehatan TEXT,
                                         salary INTEGER DEFAULT 0
);

-- Tabel User Login
CREATE TABLE IF NOT EXISTS users (
                                     id INTEGER PRIMARY KEY AUTOINCREMENT,
                                     username TEXT UNIQUE NOT NULL,
                                     password TEXT NOT NULL
);
INSERT OR IGNORE INTO users (id, username, password) VALUES (1, 'admin', 'mbp26');

-- Tabel Cuti / Sakit
CREATE TABLE IF NOT EXISTS employee_leaves (
                                               id INTEGER PRIMARY KEY AUTOINCREMENT,
                                               employee_id TEXT NOT NULL,
                                               employee_name TEXT NOT NULL,
                                               leave_type TEXT NOT NULL,
                                               start_date TEXT NOT NULL,
                                               end_date TEXT NOT NULL,
                                               total_days INTEGER NOT NULL,
                                               notes TEXT,
                                               status TEXT DEFAULT 'AKTIF'
);

-- Tabel Pengingat (Reminders) -> HARUS DITAMBAHKAN
CREATE TABLE IF NOT EXISTS reminders (
                                         id INTEGER PRIMARY KEY AUTOINCREMENT,
                                         title TEXT NOT NULL,
                                         type TEXT NOT NULL,
                                         start_date TEXT NOT NULL,
                                         interval_days INTEGER DEFAULT 0,
                                         monthly_day INTEGER DEFAULT 0,
                                         days_before_notice INTEGER DEFAULT 3,
                                         notes TEXT,
                                         is_active INTEGER DEFAULT 1
);