CREATE TABLE "tb_user_local" (
  "id" VARCHAR(36) NOT NULL, 
  "account" VARCHAR(30) NOT NULL, 
  "password" VARCHAR(60) NOT NULL, 
  "current_version" INTEGER NOT NULL, 
  "created_by" VARCHAR(36) NOT NULL, 
  "created_time" TIMESTAMPTZ DEFAULT NOW() NOT NULL, 
  "modified_by" VARCHAR(36) NOT NULL, 
  "modified_time" TIMESTAMPTZ DEFAULT NOW() NOT NULL
);
ALTER TABLE tb_user_local ADD CONSTRAINT pk_user_local_id PRIMARY KEY(id);
ALTER TABLE tb_user_local ADD CONSTRAINT uk_tb_user_local_account UNIQUE (account);

COMMENT ON TABLE "tb_user_local" IS 'Stores local user account information, including authentication credentials.';

COMMENT ON COLUMN "tb_user_local"."id" IS 'A unique identifier for each local user account, linking to the general user account information.';
COMMENT ON COLUMN "tb_user_local"."account" IS 'The username for the local account. This is used for identification and login purposes.';
COMMENT ON COLUMN "tb_user_local"."password" IS 'The password for the local account, stored securely using bcrypt hashing. This ensures that even if the database is compromised, the actual passwords remain protected.';
COMMENT ON COLUMN "tb_user_local"."current_version" IS 'Used for optimistic locking';