CREATE TABLE pautas (
    id UUID PRIMARY KEY,
    titulo VARCHAR(120) NOT NULL,
    descricao VARCHAR(500),
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL
);

CREATE TABLE sessoes_votacao (
    id UUID PRIMARY KEY,
    pauta_id UUID NOT NULL,
    opened_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    closes_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT fk_sessoes_votacao_pauta
        FOREIGN KEY (pauta_id) REFERENCES pautas (id),
    CONSTRAINT uk_sessoes_votacao_pauta
        UNIQUE (pauta_id)
);

CREATE TABLE votos (
    id UUID PRIMARY KEY,
    pauta_id UUID NOT NULL,
    associado_id VARCHAR(80) NOT NULL,
    opcao VARCHAR(3) NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT fk_votos_pauta
        FOREIGN KEY (pauta_id) REFERENCES pautas (id),
    CONSTRAINT ck_votos_opcao
        CHECK (opcao IN ('SIM', 'NAO')),
    CONSTRAINT uk_votos_pauta_associado
        UNIQUE (pauta_id, associado_id)
);

CREATE INDEX idx_votos_pauta_opcao ON votos (pauta_id, opcao);
CREATE INDEX idx_votos_pauta_created_at ON votos (pauta_id, created_at);
