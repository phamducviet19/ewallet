-- =========================================================
-- DROP IDEMPOTENCY AND OTP TABLES (MOVED TO REDIS)
-- =========================================================

DROP TABLE IF EXISTS idempotency_keys CASCADE;
DROP TABLE IF EXISTS otp_verifications CASCADE;

DROP TYPE IF EXISTS idempotency_status;
DROP TYPE IF EXISTS otp_purpose;
