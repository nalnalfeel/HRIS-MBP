CREATE TABLE IF NOT EXISTS employees (
                                         id INTEGER PRIMARY KEY AUTOINCREMENT,
                                         employee_id TEXT,
                                         nik TEXT UNIQUE NOT NULL,
                                         family_card_number TEXT,
                                         full_name TEXT,
                                         project_name TEXT,
                                         birth_place TEXT,
                                         birth_date TEXT,
                                         address TEXT,
                                         phone_number TEXT,
                                         gender TEXT,
                                         education TEXT,
                                         bank_account TEXT,
                                         bpjs_tk TEXT,
                                         bpjs_kesehatan TEXT,
                                         salary REAL DEFAULT 0
);

-- Tabel User Login (Hanya 1 Akun)
CREATE TABLE IF NOT EXISTS users (
                                     id INTEGER PRIMARY KEY AUTOINCREMENT,
                                     username TEXT UNIQUE NOT NULL,
                                     password TEXT NOT NULL
);

-- Insert Default Account (Hanya dimasukkan jika belum ada)
INSERT OR IGNORE INTO users (id, username, password) VALUES (1, 'admin', 'mbp26');


CREATE TABLE IF NOT EXISTS employee_leaves (
                                               id INTEGER PRIMARY KEY AUTOINCREMENT,
                                               employee_id TEXT NOT NULL,
                                               employee_name TEXT NOT NULL,
                                               leave_type TEXT NOT NULL,        -- 'SAKIT_PANJANG', 'CUTI_MELAHIRKAN', 'CUTI_BESAR', 'DILUAR_TANGGUNGAN'
                                               start_date TEXT NOT NULL,        -- Format: YYYY-MM-DD
                                               end_date TEXT NOT NULL,          -- Format: YYYY-MM-DD
                                               total_days INTEGER NOT NULL,     -- Durasi Hari (misal: 90 hari)
                                               notes TEXT,                      -- Catatan / No. Surat Dokter / Keterangan Medis
                                               status TEXT DEFAULT 'AKTIF'      -- 'AKTIF' (Masih Berlangsung) atau 'SELESAI'
);

/*
CREATE TABLE IF NOT EXISTS employees (
                                         id INTEGER PRIMARY KEY AUTOINCREMENT,
                                         nik TEXT UNIQUE NOT NULL,
                                         full_name TEXT NOT NULL,
                                         birth_date TEXT NOT NULL,
                                         address TEXT NOT NULL,
                                         phone_number TEXT NOT NULL,
                                         gender TEXT CHECK(gender IN ('Laki-laki', 'Perempuan')) NOT NULL,
                                         education TEXT NOT NULL,
                                         bank_account TEXT,
                                         bpjs_tk TEXT,
                                         bpjs_kesehatan TEXT,
                                         salary REAL DEFAULT 0
                                         );*/
