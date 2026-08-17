-- =====================================================================
-- SapotaCorp SEO Dashboard - Script tạo Database & bảng dữ liệu
-- Dùng cho MySQL Server hoặc XAMPP (phpMyAdmin -> tab Import/SQL)
-- =====================================================================

CREATE DATABASE IF NOT EXISTS sapota_seo
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE sapota_seo;

-- ---------------------------------------------------------------------
-- Bảng 4.2: Backlink hiện có của sapotacorp.vn
-- (tương ứng file sapotacorp_vn-backlinks_refdomains.xlsx)
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS backlinks;
CREATE TABLE backlinks (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    domain      VARCHAR(255) NOT NULL,
    ascore      INT DEFAULT 0,
    backlinks   INT DEFAULT 0,
    country     VARCHAR(10),
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ---------------------------------------------------------------------
-- Bảng 4.3: Backlink Gap - domain tiềm năng chưa có backlink
-- (tương ứng file sapota-gap-refdomains-as30-dofollow.xlsx)
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS gap_domains;
CREATE TABLE gap_domains (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    domain              VARCHAR(255) NOT NULL,
    max_as              INT DEFAULT 0,
    competitor_count    INT DEFAULT 0,
    competitors         TEXT,
    priority_score       DECIMAL(10,2) DEFAULT 0,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ---------------------------------------------------------------------
-- Bảng 4.4: Kết quả kiểm tra Dofollow (sinh ra khi dùng module Checker)
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS check_results;
CREATE TABLE check_results (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    url         VARCHAR(1000) NOT NULL,
    status      VARCHAR(30) NOT NULL,   -- Dofollow / Nofollow / Không tìm thấy / Lỗi
    checked_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    note        VARCHAR(500)
);

-- ---------------------------------------------------------------------
-- Dữ liệu mẫu (trích từ dữ liệu thực tế trong báo cáo) để test nhanh
-- Có thể xoá phần này và import dữ liệu thật bằng chức năng
-- "Nhập dữ liệu từ Excel" trong ứng dụng.
-- ---------------------------------------------------------------------
INSERT INTO backlinks (domain, ascore, backlinks, country) VALUES
('wpnews.pro', 2, 73, 'US'),
('dev.to', 69, 71, 'US'),
('teckdeck.io', 2, 71, 'DE'),
('scour.ing', 6, 39, 'US');

INSERT INTO gap_domains (domain, max_as, competitor_count, competitors, priority_score) VALUES
('topdev.vn', 41, 11, 'arrowhitech.com, isb-vietnam.com.vn, techvify.com, vti.com.vn', 85.0),
('enterpriseleague.com', 35, 10, 'arrowhitech.com, hblabgroup.com, techvify.com', 75.0),
('superbcompanies.com', 34, 10, 'agiletech.vn, isb-vietnam.com.vn, techvify.com', 74.0),
('grokipedia.com', 65, 9, 'agiletech.vn, hblabgroup.com, kaopiz.com', 110.0);
