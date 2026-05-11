-- Repair invalid default admin hash from earlier seed versions.
-- Only updates the original default admin row when it still has the legacy bad hash.

UPDATE users
SET password_hash = '$argon2id$v=19$m=65536,t=3,p=1$c3luYXBzZS1hZG1pbi1zZQ$inppHcE3NSqE/UjhMycoJtpxSsTO4M4hMNSPKGA/XmU'
WHERE username = 'admin'
  AND email = 'admin@localhost'
  AND password_hash = '$argon2id$v=19$m=65536,t=3,p=1$WGFiY2RlZmdoaWprbG1ubw$7YJ8Kx8xZ3q5ZSQGXY/nHh7BZ2vGx3c8nQx8pF7DxYo';
