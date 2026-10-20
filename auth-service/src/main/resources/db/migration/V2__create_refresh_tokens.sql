CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    token_hash VARCHAR(64) NOT NULL,
    family_id UUID NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ,
    replaced_by_token_id UUID,
    created_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_refresh_token_user
        FOREIGN KEY (user_id)
        REFERENCES users(id),

    CONSTRAINT uk_refresh_token_hash
        UNIQUE (token_hash)
);

CREATE INDEX idx_refresh_token_user
    ON refresh_tokens(user_id);

CREATE INDEX idx_refresh_token_family
    ON refresh_tokens(family_id);